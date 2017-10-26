package samtaylor.stravaapp.data

import android.content.Context
import android.content.SharedPreferences
import samtaylor.stravaapp.R

class Persistence(private val context: Context) {

    private val sharedPreferences: SharedPreferences
    get() = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun getString(key: String, default: String? = null) : String? = sharedPreferences.getString(key, default)
    fun setString(key: String, value: String) = sharedPreferences.edit().putString(key, value).apply()

    companion object {

        val ACCESS_TOKEN = "access_token"
    }
}