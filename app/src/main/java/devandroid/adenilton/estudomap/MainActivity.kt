package devandroid.adenilton.estudomap

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import android.graphics.Color
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sectorPolygon: Polygon? = null

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

        //val minhaLocalizacao = LatLng(-25.505476, -49.308461)
       // val azimute = 0.0 // direção em graus
       // drawSectorPolygon(minhaLocalizacao, azimute)
        try {
            val centerLocation = LatLng(-25.505476, -49.308461)
            val azimuth = 45.0

            Log.d("MapDebug", "Iniciando desenho do poligono")
            drawSectorPolygon(centerLocation, azimuth)

        } catch (e: Exception) {
            Log.e("MapDebug", "Erro no onMapReady", e)
        }

      //  checkLocationPermissionAndGetLocation()

        Log.d("MapViewLifecycle", "onMapReady chamado")
    }

    // Métodos do ciclo de vida


    private fun drawSectorPolygon(center: LatLng, azimuth: Double) {
        try {
            // Verificar se o mapa está inicializado
            if (!::googleMap.isInitialized) {
                Log.e("MapDebug", "Google Map nao esta inicializado")
                return
            }

            // Parâmetros do setor
            val radiusInMeters = 3000.0
            val angleInDegrees = 30.0
            val numberOfPoints = 60

            // Calcular pontos e verificar
            val sectorPoints = calculateSectorPoints(
                center,
                radiusInMeters,
                azimuth,
                angleInDegrees,
                numberOfPoints
            )

            // Verificar se a lista de pontos é válida
            if (sectorPoints.isEmpty()) {
                Log.e("MapDebug", "Lista de pontos está vazia")
                return
            }

            // Log dos pontos para debug
            Log.d("MapDebug", "Numero de pontos: ${sectorPoints.size}")
            sectorPoints.forEachIndexed { index, point ->
                Log.d("MapDebug", "Ponto $index: Lat=${point.latitude}, Lng=${point.longitude}")
            }

            // Remover polígono anterior se existir
            sectorPolygon?.remove()

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

        } catch (e: Exception) {
            Log.e("MapDebug", "Erro ao desenhar poligono", e)
        }
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, 13.5f) // Ajuste o zoom conforme necessário
        googleMap.animateCamera(cameraUpdate)
    }

    private fun calculateSectorPoints(
        center: LatLng,
        radiusInMeters: Double,
        azimuth: Double,
        angleInDegrees: Double,
        numberOfPoints: Int
    ): List<LatLng> {
        try {
            val points = mutableListOf<LatLng>()

            // Validações
            if (radiusInMeters <= 0 || angleInDegrees <= 0 || numberOfPoints <= 0) {
                Log.e("MapDebug", "Parametros invalidos: radius=$radiusInMeters, angle=$angleInDegrees, points=$numberOfPoints")
                return emptyList()
            }

            // Adiciona o ponto central
            points.add(center)
            Log.d("MapDebug", "Ponto central adicionado: Lat=${center.latitude}, Lng=${center.longitude}")

            // Calcula ângulos
            val halfAngle = angleInDegrees / 2.0
            val startAngle = (azimuth - halfAngle).toRadians()
            val endAngle = (azimuth + halfAngle).toRadians()

            // Calcula pontos do arco
            val angleDelta = (endAngle - startAngle) / (numberOfPoints - 1)

            for (i in 0 until numberOfPoints) {
                val angle = startAngle + (i * angleDelta)
                val point = calculateDestinationPoint(
                    center,
                    radiusInMeters,
                    angle.toDegrees()
                )
                points.add(point)
                Log.d("MapDebug", "Ponto $i calculado: Lat=${point.latitude}, Lng=${point.longitude}")
            }

            // Fecha o polígono
            points.add(center)

            return points

        } catch (e: Exception) {
            Log.e("MapDebug", "Erro ao calcular pontos do setor", e)
            return emptyList()
        }
    }

    private fun calculateDestinationPoint(
        start: LatLng,
        distance: Double,
        bearing: Double
    ): LatLng {
        val R = 6371000.0 // Raio da Terra em metros

        val lat1 = start.latitude.toRadians()
        val lon1 = start.longitude.toRadians()
        val bearingRad = bearing.toRadians()

        val angularDistance = distance / R

        val lat2 = asin(
            sin(lat1) * cos(angularDistance) +
                    cos(lat1) * sin(angularDistance) * cos(bearingRad)
        )

        val lon2 = lon1 + atan2(
            sin(bearingRad) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )

        return LatLng(lat2.toDegrees(), lon2.toDegrees())
    }

    // Funções de extensão úteis
    private fun Double.toRadians() = this * PI / 180.0
    private fun Double.toDegrees() = this * 180.0 / PI

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

  /*  private fun checkLocationPermissionAndGetLocation() {
        when {
            // Verifica se já temos permissão
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            // Verifica se devemos mostrar explicação
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionExplanationDialog()
            }
            else -> {
                // Solicita a permissão
                requestLocationPermission()
            }
        }
    }
    private fun enableMyLocation() {
        try {
            // Habilita o botão "Minha Localização" no mapa
            googleMap.isMyLocationEnabled = true

            // Obtém a última localização conhecida
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            DEFAULT_ZOOM
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Trata erro de permissão
            Log.e("MapView", "Erro ao acessar localização", e)
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissão Necessária")
            .setMessage("Precisamos da sua localização para mostrar onde você está no mapa.")
            .setPositiveButton("OK") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    // Permissão negada
                    showLocationDeniedMessage()
                }
            }
        }
    }

    private fun showLocationDeniedMessage() {
        Snackbar.make(
            mapView,
            "Funcionalidade de localização não está disponível",
            Snackbar.LENGTH_LONG
        ).show()
    }*/






