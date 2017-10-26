package samtaylor.stravaapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.ActivityCatalogue
import samtaylor.stravaapp.data.Persistence
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

                        Persistence(this).remove(Persistence.ACCESS_TOKEN)
                        startActivity(Intent(this, SignInActivity::class.java))
                        finish()
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

            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        MenuInflater(this).inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {

            R.id.menuSignOut -> {

                Persistence(this).remove(Persistence.ACCESS_TOKEN)

                startActivity(Intent(this, SignInActivity::class.java))
                finish()

                true
            }

            else -> {

                return super.onOptionsItemSelected(item)
            }
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

    private fun update(athlete: Athlete) {

        Picasso.with(this).load(athlete.profile).into(profileImage)

        name.text = getString(R.string.name_format, athlete.firstname, athlete.lastname)
        city.text = athlete.city
    }
}
