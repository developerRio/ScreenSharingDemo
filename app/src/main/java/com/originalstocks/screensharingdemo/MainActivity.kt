package com.originalstocks.screensharingdemo

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.originalstocks.screensharingdemo.databinding.ActivityMainBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val IMAGE_URI_KEY = "image_uri"
    private val ABS_PATH_KEY = "abs_path"
    private lateinit var mSharedPref: MySharedPreferences
    private var path: String? = null


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mSharedPref = MySharedPreferences.getInstance(this)!!

        binding.takeScreenshotButton.setOnClickListener {
            //captureAndShareImage()
            openWhatsAppDeepLink(
                "android.resource://data/user/0/com.originalstocks.screensharingdemo/files/ScreenSharingDemo/Images/aboutMe_screenshot_capture_26082020_1730.jpg",
                "How're the mountains ?"
            )
        }


    }

    private fun captureAndShareImage() {
        getBitmapFromView(binding.screenshotContentLayout, this) { bitmap ->
            path = saveToInternalStorage(bitmap, this)
            Log.i(TAG, "takeScreenshotButton: fetch this path $path")
            if (path != null) {
                // share the URI
                binding.takeScreenshotButton.text = getString(R.string.share_screenshot)

                val imageName = mSharedPref.getKey(IMAGE_URI_KEY, "") ?: ""
                val absolutePath = mSharedPref.getKey(ABS_PATH_KEY, "") ?: ""
                Log.i(TAG, "captureAndShareImage: imageName = $imageName")
                fetchURIAndSharing(absolutePath, imageName)

            } else {
                // nothing to share
                binding.takeScreenshotButton.text = getString(R.string.take_screenshot)
            }
        }

    }

    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                // By using PixelCopy a new API which supports API 26 or above
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            }
                            // possible to handle other result codes ...
                        },
                        Handler()
                    )
                } else {
                    // By using Canvas
                    var canvas = Canvas(bitmap)
                    view.draw(canvas)
                    callback(bitmap)
                }
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
                Log.e(TAG, "getBitmapFromView: IllegalArgumentException ", e)
            }
        }
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap, context: Context): String? {
        val directory = File(context.filesDir, "ScreenSharingDemo" + File.separator + "Images")
        if (!directory.exists()) {
            directory.mkdirs()
            Log.i(TAG, "saveToInternalStorage: directory doesn't exists, making One")
        } else {
            Log.i(TAG, "saveToInternalStorage: directory exists")
        }
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
        val mImageName = "aboutMe_screenshot_capture_$timeStamp.jpg"
        Log.i(TAG, "saveToInternalStorage: actual image path going to be : $mImageName")
        val myPath = File(directory, mImageName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myPath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "saveToInternalStorage: IOException ", e)
            }
        } // Path Looks like this :  /data/data/com.originalstocks.screensharingdemo/files/ScreenSharingDemo/Images/aboutMe_screenshot_capture_26082020_1253.jpg
        /** save it sharedPref to fetch image later [mImageName]*/
        mSharedPref.setKey(IMAGE_URI_KEY, mImageName)
        mSharedPref.setKey(ABS_PATH_KEY, directory.absolutePath)

        Log.i(TAG, "saveToInternalStorage: File Path : ${directory.absolutePath}/$mImageName")
        return directory.absolutePath
    }

    private fun fetchURIAndSharing(absolutePath: String, childName: String) {
        var bitmap: Bitmap? = null
        try {
            val file = File(absolutePath, childName)
            Log.i(TAG, "fetchURIFromStorage: file_path = $file")
            //bitmap = BitmapFactory.decodeStream(FileInputStream(file))

            openWhatsAppDeepLink(file.toString(), "How's the Mountain View ?")

        } catch (e: FileNotFoundException) {
            Log.e(TAG, "loadImageFromStorage: IOException ", e)
        }
    }

    private fun openWhatsAppDeepLink(
        imagePath: String,
        textToShare: String
    ) {
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "image/jpeg"

        //val imageUri = Uri.parse("android.resource:/$imagePath")
        val imageUri = Uri.parse(imagePath)
        Log.i(TAG, "openWhatsAppDeepLink: imageURI = $imageUri")
        whatsAppIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
        var appFound = false
        val matches2 = packageManager.queryIntentActivities(whatsAppIntent, 0)

        for (info in matches2) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(
                    "com.whatsapp"
                )
            ) {
                whatsAppIntent.setPackage(info.activityInfo.packageName)
                appFound = true
                break
            }
        }
        if (appFound) {
            startActivity(whatsAppIntent)
        } else {
            // app not present
            //
            val url = "https://wa.me/?text=$textToShare"
            val implicitFacebookIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            implicitFacebookIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            implicitFacebookIntent.setPackage("com.android.chrome")
            try {
                startActivity(implicitFacebookIntent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                implicitFacebookIntent.setPackage(null)
                startActivity(implicitFacebookIntent)
            }

            /*exitDialog.setMessage("Seems like you don't have WhatsApp.")
            exitDialog.show()*/
        }
    }

}

