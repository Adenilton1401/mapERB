package devandroid.adenilton.estudomap.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import devandroid.adenilton.estudomap.model.SectorPolygon

class MapViewModel : ViewModel() {

    private val centerLocation = LatLng(-25.505476, -49.308461)
    private val azimuth = 30.0
    private val radiusInMeters = 3000.0
    private val angleInDegrees = 30.0
    private val numberOfPoints = 60

    private val sectorPolygon = SectorPolygon(
        centerLocation,
        radiusInMeters,
        azimuth,
        angleInDegrees,
        numberOfPoints
    )

    fun getSectorPolygonPoints(): List<LatLng> {
        return sectorPolygon.getPolygonPoints()
    }

    fun getCenterLocation(): LatLng {
        return centerLocation
    }
}