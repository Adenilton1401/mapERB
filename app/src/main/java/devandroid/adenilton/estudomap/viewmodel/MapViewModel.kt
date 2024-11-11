package devandroid.adenilton.estudomap.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import devandroid.adenilton.estudomap.model.SectorPolygon
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapViewModel(application: Application) : AndroidViewModel(application) {

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

    fun saveMapAsImage (googleMap: GoogleMap){
        val dataTime = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val fileName = "$dataTime.png"

        googleMap.snapshot { bitmap ->
            if(bitmap != null){
                val outputStream = FileOutputStream(File(
                    getApplication<Application>().getExternalFilesDir(null),fileName))
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Toast.makeText(getApplication(), "Mapa salvo como imagem!", Toast.LENGTH_SHORT).show()
            }else{

            }
        }
    }



}