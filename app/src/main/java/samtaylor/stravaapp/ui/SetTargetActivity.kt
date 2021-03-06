package samtaylor.stravaapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_set_target.*
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.Persistence
import samtaylor.stravaapp.model.Athlete

class SetTargetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_target)
        title = getString(R.string.title_set_target)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val accessToken = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).getString("access_token", null)
        if (accessToken != null) {

            "https://www.strava.com/api/v3/athlete".httpGet().header("Authorization" to "Bearer $accessToken").responseString { _, _, result ->

                when (result) {

                    is Result.Success -> {

                        val athlete = Gson().fromJson<Athlete>(result.get(), Athlete::class.java)

                        Picasso.with(this).load(athlete.profile).into(profileImage)

                        name.text = getString(R.string.name_format, athlete.firstname, athlete.lastname)
                        city.text = athlete.city
                    }

                    else -> {

                        Persistence(this).remove(Persistence.ACCESS_TOKEN)
                        startActivity(Intent(this, SignInActivity::class.java))
                        finish()
                    }
                }
            }

            val currentTarget = Persistence(this).getInt(Persistence.TARGET, 0)
            if (currentTarget != 0) {

                newTarget.setText("$currentTarget")
            }

//            val goal = 1000000F // (1000km)
//            var distanceToDate = 0F
//
//            ActivityCatalogue().fetch(accessToken) {
//
//                it.ridesOnly("Ride").currentYearOnly("2015").groupByWeek().toSortedMap(Comparator { first, second ->
//
//                    first.compareTo(second)
//                }).forEach {
//
////                    Log.v("XXX", "${it.value.size} rides in week ${it.key} of 2015 = ${it.value.totalDistance/1000}km")
//                    distanceToDate += it.value.totalDistance
//
////                    val monthsRemaining = 12 - (it.key + 1)
//                    val weeksRemaining = 52 - it.key
//                    val adjustedPace = (goal - distanceToDate) / weeksRemaining
////
//                    Log.v("XXX", "${it.value.size} rides in week ${it.key} of 2015 = ${it.value.totalDistance/1000}km => adjustedPace = ${adjustedPace/1000}km per week")
//                }
//            }
        } else {

            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        saveButton.setOnClickListener {

            if (newTarget.text.isNullOrEmpty()) {

            } else {

                Persistence(this).putInt(Persistence.TARGET, Integer.parseInt(newTarget.text.toString()))

                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {

            android.R.id.home -> {

                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


//    private fun monthToString(month: Int) = when(month) {
//
//            Calendar.JANUARY -> "January"
//            Calendar.FEBRUARY -> "February"
//            Calendar.MARCH -> "March"
//            Calendar.APRIL -> "April"
//            Calendar.MAY -> "May"
//            Calendar.JUNE -> "June"
//            Calendar.JULY -> "July"
//            Calendar.AUGUST -> "August"
//            Calendar.SEPTEMBER -> "September"
//            Calendar.OCTOBER -> "October"
//            Calendar.NOVEMBER -> "November"
//            Calendar.DECEMBER -> "December"
//            else -> "Unknown"
//        }
}
