package devandroid.adenilton.estudomap.model

import com.google.android.gms.maps.model.LatLng

data class PolygonData(val points: List<LatLng>,
                       val strokeColor: Int,
                       val strokeWidth: Float,
                       val fillColor: Int)
