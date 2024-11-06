package devandroid.adenilton.estudomap.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import devandroid.adenilton.estudomap.model.SectorPolygon

class MapViewModel : ViewModel() {

    private var centerLocation: LatLng = LatLng(-25.505476,-49.308400)
    private var  azimuth: Double = 30.0
    private var radiusInMeters: Double = 3000.0
    private val angleInDegrees = 30.0
    private val numberOfPoints = 60


    private var sectorPolygon: SectorPolygon? = null

    private fun updateSectorPolygon(){
       sectorPolygon = SectorPolygon(
            centerLocation,
            radiusInMeters,
            azimuth,
            angleInDegrees,
            numberOfPoints
        )

    }

    fun getSectorPolygonPoints(): List<LatLng> {
        return sectorPolygon?.getPolygonPoints() ?: emptyList()
    }

    fun setCenterLocation(latLng: LatLng){
        this.centerLocation = latLng
        updateSectorPolygon()
    }

    fun getCenterLocation(): LatLng {
        return centerLocation
    }

    fun setAzimuth(azimuth:Double){
        this.azimuth = azimuth
        updateSectorPolygon()
    }

    fun setRadiusInMeters (radiusInMeters: Double){
        this.radiusInMeters = radiusInMeters
        updateSectorPolygon()
    }

}