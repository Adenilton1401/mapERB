package devandroid.adenilton.estudomap.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import devandroid.adenilton.estudomap.model.MarkerData
import devandroid.adenilton.estudomap.model.PolygonData
import devandroid.adenilton.estudomap.model.SectorPolygon
import devandroid.adenilton.estudomap.utils.Util
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

    var newStateClicked = false


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
                saveToGallery(bitmap,fileName)
                Toast.makeText(getApplication(), "Mapa salvo como imagem!", Toast.LENGTH_SHORT).show()
            }else{

            }
        }
    }

    private fun saveToGallery(bitmap: Bitmap,fileName: String){
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val contentResolver = getApplication<Application>().contentResolver
        val imageUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)


        imageUri?.let { uri: Uri ->
            contentResolver.openOutputStream(uri)?.use {
                outputStream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if(imageUri != null){
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null,null)
            }
        }
    }

    companion object{

        fun checkAzimuth (azimuthStg: String): Boolean {
            if (azimuthStg.toDouble() >= 0.0 && azimuthStg.toDouble()<=360){
                return false
            }else
                return true


        }

        fun checkLat (latStg: String): Boolean {
            if (Util.convertCoord(latStg) >= -90.0 && Util.convertCoord(latStg)<=90){
                return false
            }else
                return true


        }

        fun checkLng (lngStg: String): Boolean {
            if (Util.convertCoord(lngStg) >= -180.0 && Util.convertCoord(lngStg)<=180){
                return false
            }else
                return true


        }


    }

    private val _polygonsList = mutableListOf<PolygonData>()
    val polygonsList : List<PolygonData> = _polygonsList

    fun addPolygon(polygonData: PolygonData) {
        _polygonsList.add(polygonData)
    }

    fun removePolygon(polygon: Polygon){
        val iterator =  _polygonsList.iterator()
        while (iterator.hasNext()){
            val dataPolygon = iterator.next()
            if (dataPolygon.points == polygon.points){
                iterator.remove()
                break

            }
        }

    }

    fun removeAllPolygon(){
        _polygonsList.clear()
    }

    private val _markersList = mutableListOf<MarkerData>()
    val markersList: List<MarkerData> = _markersList

    fun addMarker(markerData: MarkerData) {
        _markersList.add(markerData)
    }

    fun removeLastMarker(){
        _markersList.removeLastOrNull()
    }

    fun removeAllMarkers(){
        _markersList.clear()
    }


}