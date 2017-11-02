package samtaylor.stravaapp.data

import org.json.JSONObject

class PersistentCache(private val persistence: Persistence) {

    fun getCachedResponse(url: String) : String? {

        val entryJson = persistence.getString(url)
        return if (entryJson != null) {

            val entry = JSONObject(entryJson)
            val timestamp = entry.getLong("timestamp")
            val liveForInMillis = entry.getLong("liveForInMillis")

            if (timestamp + liveForInMillis > System.currentTimeMillis()) {

                entry.getString("response")
            }
            else {

                null
            }
        }
        else {

            null
        }
    }

    fun setCachedResponse(url: String, response: String, liveForInMillis: Long = 60 * 5 * 1000) {

        val entry = JSONObject()
        entry.put("response", response)
        entry.put("timestamp", System.currentTimeMillis())
        entry.put("liveForInMillis", liveForInMillis)

        persistence.putString(url, entry.toString())
    }

}