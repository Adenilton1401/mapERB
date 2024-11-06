package devandroid.adenilton.estudomap.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import android.graphics.Color
import androidx.activity.viewModels
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.viewmodel.MapViewModel
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sectorPolygon: Polygon? = null

    private val mapViewModel : MapViewModel by viewModels()

    // Constantes para permissões
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 15f
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Inicialização do MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        Log.d("MapViewSequence", "[Tempo: 0ms] onCreate iniciado")

        mapView.getMapAsync(this)

        Log.d("MapViewSequence", " onCreate finalizado")



    }

    override fun onMapReady(map: GoogleMap) {

        Log.d("MapViewSequence", "onMapReady chamado")

        googleMap = map

        // Habilitar controles básicos
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true

            }

        val initLatLng = LatLng(-15.842070765433535, -47.881240647210184)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(initLatLng, 5f)
        googleMap.animateCamera(cameraUpdate)

        try {

            val fab = findViewById<FloatingActionButton>(R.id.fbtnAddAPolygon)
            fab.setOnClickListener{
                showDialogAddPolygon()
            }


        } catch (e: Exception) {
            Log.e("MapDebug", "Erro no onMapReady", e)
        }
    }

    private fun showDialogAddPolygon(){
        val builder = MaterialAlertDialogBuilder(this)
        val view =layoutInflater.inflate(R.layout.dialog_add_polygon, null)
        builder.setView(view)

        val etLat = view.findViewById<TextInputEditText>(R.id.etLatitude)
        val etLng = view.findViewById<TextInputEditText>(R.id.etLongitude)
        val etAzimuth = view.findViewById<TextInputEditText>(R.id.etAzimute)
        val etRadiusInMeters = view.findViewById<TextInputEditText>(R.id.etRaio)

        builder.setPositiveButton("Adicionar"){ dialog, _ ->
            var lat = etLat.text.toString().toDoubleOrNull() ?:0.0
            var lng = etLng.text.toString().toDoubleOrNull() ?:0.0
            var azimuth = etAzimuth.text.toString().toDoubleOrNull() ?:0.0
            var radiusInMeters = etRadiusInMeters.text.toString().toDoubleOrNull() ?: 0.0
            var latLng = LatLng(lat,lng)

            mapViewModel.setCenterLocation(latLng)
            Log.e("MapDebug", "Carregou o CenterLocation com"+latLng)
            mapViewModel.setAzimuth(azimuth)
            mapViewModel.setRadiusInMeters(radiusInMeters)

            try {

                var polygonPoints = mapViewModel.getSectorPolygonPoints()
                drawSectorPolygon(polygonPoints)

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapViewModel.getCenterLocation(), 13.5f)
                googleMap.animateCamera(cameraUpdate)

            } catch (e: Exception) {
                Log.e("MapDebug", "Erro no onMapReady", e)
            }

        }

        builder.setNegativeButton("Cancelar",null)
        builder.show()

    }


    private fun drawSectorPolygon(sectorPoints: List<LatLng>) {
        // Verificar se o mapa está inicializado
        if (!::googleMap.isInitialized) {
            Log.e("MapDebug", "Google Map nao esta inicializado")
            return
        }

        // Remover polígono anterior se existir
       // sectorPolygon?.remove()

        // Criar as options do polígono
        val polygonOptions = PolygonOptions().apply {
            addAll(sectorPoints)
            strokeColor(Color.BLUE)
            strokeWidth(2f)
            fillColor(Color.argb(70, 0, 0, 255))
        }

        // Adicionar o polígono e verificar
        val polygon = googleMap.addPolygon(polygonOptions)

        if (polygon != null) {
            sectorPolygon = polygon
            Log.d("MapDebug", "Poligono criado com sucesso")
        } else {
            Log.e("MapDebug", "Falha ao criar poligono")
        }
    }



    override fun onStart() {
        super.onStart()
        mapView.onStart()
        Log.d("MapViewLifecycle", "onStart chamado")
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        Log.d("MapViewLifecycle", "onResume chamado")
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
        Log.d("MapViewLifecycle", "onPause chamado")
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
        Log.d("MapViewLifecycle", "onStop chamado")
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
        Log.d("MapViewLifecycle", "onDestroy chamado")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        Log.d("MapViewLifecycle", "onSaveInstanceState chamado")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
        Log.d("MapViewLifecycle", "onLowMemory chamado")
    }


}







