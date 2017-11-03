package samtaylor.stravaapp.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import samtaylor.stravaapp.model.Activity

class ActivityCache(private val persistence: Persistence) {

    private val activities = ArrayList<Activity>()

    init {

        val json = persistence.getString(Persistence.ACTIVITY_CACHE)
        if (json != null) {

            val type = object : TypeToken<List<Activity>>() {}.type
            val fetchedActivities = Gson().fromJson<List<Activity>>(json, type)

            activities.addAll(fetchedActivities)
        }
    }

    fun contain(activity: Activity) : Boolean = activities.filter { it.id == activity.id }.size == 1

    fun add(activity: Activity) {

        if (!contain(activity)) {

            activities.add(activity)
            persistence.putString(Persistence.ACTIVITY_CACHE, Gson().toJson(activities))
        }
    }

    fun getAll() : List<Activity> = activities
}