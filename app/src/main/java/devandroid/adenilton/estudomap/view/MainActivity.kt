package devandroid.adenilton.estudomap.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.button.MaterialButton
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.viewmodel.MapViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import devandroid.adenilton.estudomap.utils.Util



class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    //Inicio da declaração de variáveis de botões
    private lateinit var fbtMenu: FloatingActionButton
    private lateinit var fbtAddPolygon: FloatingActionButton
    private lateinit var fbtClerPolygon: FloatingActionButton
    private lateinit var fbtSend: FloatingActionButton
    private lateinit var fbtList: FloatingActionButton
    //Fim da declaração de variáveis de botões

    // Inicio da declaração de variáveis de animação dos botões
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBotton: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_botton_anim) }
    private val toBotton: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_botton_anim) }

    private var clicked = false
    // Fim da ddeclaração de variáveis de animação dos botões

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sectorPolygon: Polygon? = null

    //--Variáveis para o controle da lista de poligonos
    private val polygonsList = mutableListOf<Polygon>()
    private var markersList = mutableListOf<Marker>()

    private lateinit var mapViewModel: MapViewModel

    // Constantes para permissões
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 15f
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapViewModel= ViewModelProvider(this).get(MapViewModel::class.java)

        //Inicializa os botões do Menu
        starMenuButtons()

        //Definição do envento do botão do menu
        fbtMenu.setOnClickListener {
            onMenuButtonClicked()
        }

        // Inicializar o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Inicialização do MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        Log.d("MapViewSequence", "[Tempo: 0ms] onCreate iniciado")

        mapView.getMapAsync(this)

        Log.d("MapViewSequence", " onCreate finalizado")



    }

    private fun starMenuButtons() {

        fbtMenu = findViewById<FloatingActionButton>(R.id.fbtMenu)
        fbtAddPolygon = findViewById<FloatingActionButton>(R.id.fbtAddPolygon)
        fbtClerPolygon = findViewById<FloatingActionButton>(R.id.fbtClerPolygon)
        fbtSend = findViewById<FloatingActionButton>(R.id.fbtSend)
        fbtList = findViewById<FloatingActionButton>(R.id.fbtList)

    }

    private fun onMenuButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked

    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            fbtAddPolygon.visibility = View.VISIBLE
            fbtClerPolygon.visibility = View.VISIBLE
            fbtSend.visibility = View.VISIBLE
            fbtList.visibility = View.VISIBLE
        }else{
            fbtAddPolygon.visibility = View.INVISIBLE
            fbtClerPolygon.visibility = View.INVISIBLE
            fbtSend.visibility = View.INVISIBLE
            fbtList.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked){
            fbtAddPolygon.startAnimation(fromBotton)
            fbtClerPolygon.startAnimation(fromBotton)
            fbtSend.startAnimation(fromBotton)
            fbtList.startAnimation(fromBotton)
            fbtMenu.startAnimation(rotateOpen)
        }else{
            fbtAddPolygon.startAnimation(toBotton)
            fbtClerPolygon.startAnimation(toBotton)
            fbtSend.startAnimation(toBotton)
            fbtList.startAnimation(toBotton)
            fbtMenu.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean){
        if (!clicked){
            fbtAddPolygon.isClickable = true
            fbtClerPolygon.isClickable = true
            fbtSend.isClickable = true
            fbtList.isClickable = true

        }else{
            fbtAddPolygon.isClickable = false
            fbtClerPolygon.isClickable = false
            fbtSend.isClickable = false
            fbtList.isClickable = false
        }
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
        //Inicializa com as coordenadas do centro do Brasil
        val initLatLng = LatLng(-15.842070765433535, -47.881240647210184)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(initLatLng, 5f)
        googleMap.animateCamera(cameraUpdate)

        try {

                fbtAddPolygon.setOnClickListener{
                showDialogAddPolygon()
            }


        } catch (e: Exception) {
            Log.e("MapDebug", "Erro no onMapReady", e)
        }

        fbtClerPolygon.setOnClickListener {
            showDialogClear()
        }

        fbtSend.setOnClickListener {
            mapViewModel.saveMapAsImage(googleMap)
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



            var lat = Util.convertCoord(etLat.text.toString())
            var lng = Util.convertCoord(etLng.text.toString())
            var azimuth = etAzimuth.text.toString().toDoubleOrNull() ?:0.0
            var radiusInMeters = etRadiusInMeters.text.toString().toDoubleOrNull() ?: 0.0
            var latLng = LatLng(lat,lng)
            onMenuButtonClicked()

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

    private fun showDialogClear(){
        val builder = MaterialAlertDialogBuilder(this)
        val view =layoutInflater.inflate(R.layout.dialog_clear_polygon, null)
        builder.setView(view)

        val btnLast = view.findViewById<MaterialButton>(R.id.btnLast)
        val btnAll = view.findViewById<MaterialButton>(R.id.btnAll)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        val dialog = builder.create()


        btnLast.setOnClickListener{
            clearLastSectorPolygon()

            dialog.dismiss()

        }

        btnAll.setOnClickListener{
            clearAllSectorPolygon()
            dialog.dismiss()

        }

        btnCancel.setOnClickListener{

            dialog.cancel()

        }

        builder.setPositiveButton(null,null)
        builder.setNeutralButton(null,null)
        builder.setNegativeButton(null,null)

        dialog.show()

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
        addMarker(mapViewModel.getCenterLocation())


        if (polygon != null) {
            sectorPolygon = polygon

            polygonsList.add(polygon)
            Log.d("MapDebug", "Poligono criado com sucesso")
        } else {
            Log.e("MapDebug", "Falha ao criar poligono")
        }
    }

    private fun clearLastSectorPolygon(){
        val lastPolygon = polygonsList.removeLastOrNull()
        lastPolygon?.remove()
        markersList.last().remove()
        markersList.removeLast()

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, "último azimute apagado!", Snackbar.LENGTH_SHORT).show()
    }

    private fun clearAllSectorPolygon(){
        for (polygon in polygonsList){
            polygon.remove()
        }
        polygonsList.clear()
        googleMap.clear()
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, "Mapa limpo!", Snackbar.LENGTH_SHORT).show()
    }

    //Cria um marcador na localização geografica
    private fun addMarker(latLng: LatLng) {
       // val icon = vectorToBitmap(R.drawable.ic_tower_48)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_tower_new)
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("ERB")
            .snippet("Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            .icon(icon)

        val marcador = googleMap.addMarker(markerOptions)
        if (marcador != null) {
            markersList.add(marcador)
        }
    }

    //Converte o icone vetor pra bitmap
    fun vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

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







