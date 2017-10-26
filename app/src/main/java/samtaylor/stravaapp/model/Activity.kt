package samtaylor.stravaapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Activity(val name: String, val distance: Float, val start_date_local: String, val timezone: String, val type: String) : Parcelable