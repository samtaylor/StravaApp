package samtaylor.stravaapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.ActivityCatalogue
import samtaylor.stravaapp.model.Athlete
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accessToken = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).getString("access_token", null)
        if (accessToken != null) {

            "https://www.strava.com/api/v3/athlete".httpGet().header("Authorization" to "Bearer $accessToken").responseString { _, _, result ->

                when (result) {

                    is Result.Success -> {

                        val athlete = Gson().fromJson<Athlete>(result.get(), Athlete::class.java)
                        update(athlete)
                    }

                    else -> {

                        getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit().remove("access_token").apply()
                        putButtonInSignInState()
                    }
                }
            }

            val goal = 1000000F // (1000km)
            var distanceToDate = 0F

            ActivityCatalogue().fetch(accessToken) {

                it.filterByType("Ride").filterByYear("2015").groupByWeek().toSortedMap(Comparator { first, second ->

                    first.compareTo(second)
                }).forEach {

//                    Log.v("XXX", "${it.value.size} rides in week ${it.key} of 2015 = ${it.value.totalDistance/1000}km")
                    distanceToDate += it.value.totalDistance

//                    val monthsRemaining = 12 - (it.key + 1)
                    val weeksRemaining = 52 - it.key
                    val adjustedPace = (goal - distanceToDate) / weeksRemaining
//
                    Log.v("XXX", "${it.value.size} rides in week ${it.key} of 2015 = ${it.value.totalDistance/1000}km => adjustedPace = ${adjustedPace/1000}km per week")
                }
            }
        } else {

            putButtonInSignInState()
        }
    }

    private fun monthToString(month: Int) = when(month) {

            Calendar.JANUARY -> "January"
            Calendar.FEBRUARY -> "February"
            Calendar.MARCH -> "March"
            Calendar.APRIL -> "April"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "June"
            Calendar.JULY -> "July"
            Calendar.AUGUST -> "August"
            Calendar.SEPTEMBER -> "September"
            Calendar.OCTOBER -> "October"
            Calendar.NOVEMBER -> "November"
            Calendar.DECEMBER -> "December"
            else -> "Unknown"
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 101) {

            if (resultCode == Activity.RESULT_OK && data != null) {

                val accessToken = data.getStringExtra("access_token")
                val athlete = data.getParcelableExtra<Athlete>("athlete")

                update(athlete)

                getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit().putString("access_token", accessToken).apply()
            }
        }
    }

    private fun update(athlete: Athlete) {

        Picasso.with(this).load(athlete.profile).into(profileImage)

        name.text = getString(R.string.name_format, athlete.firstname, athlete.lastname)
        city.text = athlete.city

        putButtonInSignOutState()
    }

    private fun putButtonInSignOutState() {

        authButton.text = getString(R.string.sign_out)
        authButton.setOnClickListener {

            getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit().remove("access_token").apply()
            putButtonInSignInState()
        }
    }

    private fun putButtonInSignInState() {

        name.text = ""
        city.text = ""

        profileImage.setImageResource(android.R.color.transparent)

        authButton.text = getString(R.string.sign_in)
        authButton.setOnClickListener {

            startActivityForResult(Intent(this, AuthActivity::class.java), 101)
        }
    }
}
