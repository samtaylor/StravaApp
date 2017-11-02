package samtaylor.stravaapp.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_pace.view.*
import samtaylor.stravaapp.R
import samtaylor.stravaapp.data.ActivityCatalogue
import samtaylor.stravaapp.data.Persistence
import samtaylor.stravaapp.data.PersistentCache
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.title_main)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
    }

    override fun onResume() {

        super.onResume()

        recyclerView.visibility = View.GONE
        chartContainer.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        val accessToken = Persistence(this).getString(Persistence.ACCESS_TOKEN)
        val targetInMetres = Persistence(this).getInt(Persistence.TARGET) * 1000F

        if (accessToken != null) {

            if (targetInMetres > -1) {

                ActivityCatalogue(PersistentCache(Persistence(this))).fetch(accessToken) {

                    val data = it.ridesOnly().currentYearOnly().groupByWeek(true).toSortedMap(kotlin.Comparator { first, second ->

                        second.compareTo(first)
                    }).values.toList().asReversed()

                    recyclerView.adapter = ActivityAdapter(data, targetInMetres)

                    var totalDistance = 0.0
                    val distanceArray = Array(data.size) { index ->

                        totalDistance += data[index].totalDistance / 1000.0
                        DataPoint(index.toDouble(), totalDistance)
                    }

                    var maxPace = 0.0
                    totalDistance = 0.0
                    val adjustedPaceArray = Array(data.size) { index ->

                        totalDistance += data[index].totalDistance
                        val adjustedPace = (targetInMetres - totalDistance) / (52 - (index + 1)) / 1000.0

                        if (maxPace < adjustedPace) {

                            maxPace = adjustedPace
                        }

                        DataPoint(index.toDouble(), adjustedPace)
                    }

                    val distanceSeries = LineGraphSeries<DataPoint>(distanceArray)
                    distanceSeries.color = resources.getColor(R.color.primary_dark)

                    val adjustedPaceSeries = LineGraphSeries<DataPoint>(adjustedPaceArray)
                    adjustedPaceSeries.color = resources.getColor(R.color.accent)

                    lineChart.removeAllSeries()
                    lineChart.addSeries(distanceSeries)

                    lineChart.secondScale.removeAllSeries()
                    lineChart.secondScale.addSeries(adjustedPaceSeries)
                    lineChart.secondScale.setMinY(0.0)
                    lineChart.secondScale.setMaxY(maxPace)
                    lineChart.secondScale.calcCompleteRange()

                    lineChart.viewport.isXAxisBoundsManual = true
                    lineChart.viewport.setMinX(0.0)
                    lineChart.viewport.setMaxX(55.0)

                    recyclerView.visibility = View.VISIBLE
                    chartContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            } else {

                startActivity(Intent(this, SetTargetActivity::class.java))
            }
        }
        else {

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

                AlertDialog.Builder(this).setMessage(R.string.sign_out_dialog).setNegativeButton(R.string.cancel) { dialog, _ ->

                    dialog.dismiss()
                }.setPositiveButton(R.string.sign_out) { dialog, _ ->

                    Persistence(this).remove(Persistence.ACCESS_TOKEN)

                    startActivity(Intent(this, SignInActivity::class.java))
                    dialog.dismiss()
                    finish()
                }.create().show()

                true
            }

            R.id.menuChangeTarget -> {

                startActivity(Intent(this, SetTargetActivity::class.java))

                true
            }

            else -> {

                return super.onOptionsItemSelected(item)
            }
        }
    }

    private class ActivityAdapter(private val data: List<ActivityCatalogue>, private val target: Float) : RecyclerView.Adapter<ActivityViewHolder>() {

        override fun onBindViewHolder(holder: ActivityViewHolder?, position: Int) {

            var totalDistance = 0F
            val week = data.size - 1 - position
            (0 .. week).forEach {

                totalDistance += data[it].totalDistance
            }
            holder?.bind(week, data[week].totalDistance, totalDistance, target)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ActivityViewHolder {

            val view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_pace, parent, false)

            return ActivityViewHolder(view)
        }

        override fun getItemCount(): Int = data.size
    }

    private class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(week: Int, distance: Float, totalDistance: Float, target: Float) {

            val date = Calendar.getInstance(Locale.UK)
            date[Calendar.WEEK_OF_YEAR] = week + 1
            date[Calendar.DAY_OF_WEEK] = date.firstDayOfWeek

            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.UK)

            itemView.findViewById<TextView>(R.id.date).text = itemView.context.getString(R.string.week_format, week + 1, simpleDateFormat.format(date.time))

            itemView.distance.text = itemView.context.getString(R.string.km_format, distance/1000F)
            itemView.totalDistanceToDate.text = itemView.context.getString(R.string.km_format, totalDistance/1000F)

            val weeksRemaining = 52 - (week + 1)
            val adjustedPace = (target - totalDistance) / weeksRemaining

            if (adjustedPace < 0 || adjustedPace == Float.POSITIVE_INFINITY || adjustedPace == Float.POSITIVE_INFINITY) {

                itemView.adjustedPace.text = itemView.context.getString(R.string.invalid_adjusted_pace)
            } else {

                itemView.adjustedPace.text = itemView.context.getString(R.string.pace_per_week_format, adjustedPace / 1000F)
            }
        }
    }
}