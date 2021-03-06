package samtaylor.stravaapp.data

import android.content.Context
import android.content.SharedPreferences
import samtaylor.stravaapp.R

class Persistence(private val context: Context) {

    private val sharedPreferences: SharedPreferences
    get() = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun getString(key: String, default: String? = null) : String? = sharedPreferences.getString(key, default)
    fun putString(key: String, value: String) = sharedPreferences.edit().putString(key, value).apply()

    fun getInt(key: String, default: Int = -1) : Int = sharedPreferences.getInt(key, default)
    fun putInt(key: String, value: Int) = sharedPreferences.edit().putInt(key, value).apply()

    fun remove(key: String) = sharedPreferences.edit().remove(key).apply()

    companion object {

        val ACCESS_TOKEN = "access_token"
        val TARGET = "target"
        val ACTIVITY_CACHE = "activity_cache"
    }
}