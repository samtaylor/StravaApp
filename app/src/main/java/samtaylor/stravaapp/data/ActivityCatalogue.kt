package samtaylor.stravaapp.data

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import samtaylor.stravaapp.model.Activity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ActivityCatalogue(private val catalogue: ArrayList<Activity> = ArrayList()) {

    val size: Int
    get() = catalogue.size

    val totalDistance: Float
    get() {

        var total = 0F
        catalogue.forEach {

            total += it.distance
        }

        return total
    }

    fun fetch(accessToken: String, finished: (ActivityCatalogue) -> Unit) {

        fetch(accessToken, 1, catalogue, finished)
    }

    fun ridesOnly(): ActivityCatalogue = ActivityCatalogue(catalogue.filter { it.type == "Ride" } as ArrayList<Activity>)

    fun currentYearOnly(): ActivityCatalogue = ActivityCatalogue(catalogue.filter {

        val year = Calendar.getInstance(Locale.UK)[Calendar.YEAR]
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
        val startDate = simpleDateFormat.parse("$year-01-01T00:00:00Z")
        val endDate = simpleDateFormat.parse("$year-12-31T23:59:59Z")

        simpleDateFormat.timeZone = TimeZone.getTimeZone(it.timezone)
        val date = simpleDateFormat.parse(it.start_date_local)

        date.after(startDate) && date.before(endDate)

    } as ArrayList<Activity>)

    fun groupByMonth(addMissing: Boolean = false): Map<Int, ActivityCatalogue> {

        val newCatalogue = groupBy(Calendar.MONTH)

        if (addMissing) {

            val currentMonth = Calendar.getInstance(Locale.UK)[Calendar.MONTH]

            (Calendar.JANUARY .. currentMonth).forEach {

                if (!newCatalogue.containsKey(it)) {

                    newCatalogue.put(it, ActivityCatalogue())
                }
            }
        }

        return newCatalogue
    }

    fun groupByDay(): Map<Int, ActivityCatalogue> = groupBy(Calendar.DAY_OF_YEAR)

    fun groupByWeek(addMissing: Boolean = false): Map<Int, ActivityCatalogue> {

        val newCatalogue = groupBy(Calendar.WEEK_OF_YEAR)

        if (addMissing) {

            val currentWeek = Calendar.getInstance(Locale.UK)[Calendar.WEEK_OF_YEAR]

            (1 .. currentWeek).forEach {

                if (!newCatalogue.containsKey(it)) {

                    newCatalogue.put(it, ActivityCatalogue())
                }
            }
        }

        return newCatalogue
    }

    private fun groupBy(grouping: Int) : HashMap<Int, ActivityCatalogue> {

        val groupings = HashMap<Int, ActivityCatalogue>()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)

        catalogue.forEach {

            simpleDateFormat.timeZone = TimeZone.getTimeZone(it.timezone)
            val date = Calendar.getInstance(Locale.UK)
            date.firstDayOfWeek = Calendar.MONDAY
            date.time = simpleDateFormat.parse(it.start_date_local)

            val groupKey = date[grouping]
            val yearList = groupings[groupKey]?: ActivityCatalogue()
            yearList.catalogue.add(it)
            groupings.put(groupKey, yearList)
        }

        return groupings
    }

    private fun fetch(accessToken: String, page: Int, activities: ArrayList<Activity>, finished: (ActivityCatalogue) -> Unit) {

        "https://www.strava.com/api/v3/athlete/activities?page=$page".httpGet().header("Authorization" to "Bearer $accessToken").response { _, _, result ->

            when (result) {

                is Result.Success -> {

                    val type = object: TypeToken<List<Activity>>() {}.type

                    val fetchedActivities = Gson().fromJson<List<Activity>>(String(result.get()), type)
                    if (fetchedActivities.isNotEmpty()) {

                        activities.addAll(fetchedActivities)
                        fetch(accessToken, page + 1, activities, finished)
                    } else {

                        finished(this)
                    }
                }
            }
        }
    }
}