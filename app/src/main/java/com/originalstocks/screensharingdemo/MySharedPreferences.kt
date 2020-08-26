package com.originalstocks.screensharingdemo

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton class for sharedPreferences
 */
class MySharedPreferences {
    //Shared Preference key
    private val KEY_PREFERENCE_NAME = "tripster"

    //private keyS
    private val KEY_DEFAULT: String? = null

    //Method to set boolean for (AppIntro)
    fun setBooleanKey(keyname: String?) {
        mSharedPreference.edit().putBoolean(keyname, true).apply()
    }

    fun setBooleanKey(keyname: String?, state: Boolean) {
        mSharedPreference.edit().putBoolean(keyname, state).apply()
    }

    /*
     * Method to get boolan key
     * true = means set
     * false = not set (show app intro)
     * */
    fun getBooleanKey(keyname: String?): Boolean {
        return mSharedPreference.getBoolean(keyname, false)
    }

    //Method to store user Mobile number
    fun setKey(key: String?, value: String?) {
        mSharedPreference.edit().putString(key, value).apply()
    }

    //Method to get User mobile number
    fun getKey(key: String?, defValue: String): String? {
        return mSharedPreference.getString(key, defValue)
    }

    companion object {
        //user details keys
        const val API_KEY = "apiToken"
        const val ID_KEY = "id"
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val MOBILE_KEY = "mobile"
        const val DOB_KEY = "dob"
        const val COUNTRY_CODE_KEY = "ccp"
        const val PROFILE_IMAGE_LINK_KEY = "profile_img"
        const val GENDER_KEY = "gender"

        //instance field
        private lateinit var mSharedPreference: SharedPreferences
        private var mInstance: MySharedPreferences? = null
        private var mContext: Context? = null
        fun getInstance(context: Context?): MySharedPreferences? {
            mContext = context
            if (mInstance == null) {
                mInstance = MySharedPreferences()
            }
            return mInstance
        }

        fun removeValueFromKey(keyname: String?) {
            mSharedPreference.edit().remove(keyname).apply()
        }

        fun removeAllData() {
            mSharedPreference.edit().clear().apply()
        }
    }

    init {
        mSharedPreference =
            mContext!!.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
}