package samtaylor.stravaapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Athlete(val firstname: String, val lastname: String, val city: String, val profile: String) : Parcelable