package devandroid.adenilton.estudomap.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.model.MarkerData
import devandroid.adenilton.estudomap.model.PolygonData
import devandroid.adenilton.estudomap.viewmodel.MapViewModel
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    DialogFragmentAddPolygon.OnDataSendedListener {

    //Inicio da declaração de variáveis de botões
    private lateinit var fbtMenu: FloatingActionButton
    private lateinit var fbtAddPolygon: FloatingActionButton
    private lateinit var fbtClerPolygon: FloatingActionButton
    private lateinit var fbtSend: FloatingActionButton
    private lateinit var fbtList: FloatingActionButton
    //Fim da declaração de variáveis de botões

    // Inicio da declaração de variáveis de animação dos botões
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBotton: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_botton_anim
        )
    }
    private val toBotton: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_botton_anim
        )
    }

    private var clicked = false
    // Fim da ddeclaração de variáveis de animação dos botões

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sectorPolygon: Polygon? = null

    //--Variáveis para o controle da lista de poligonos
    private var markersOnMap = mutableListOf<Marker>()
    private  val polygonsOnMap = mutableListOf<Polygon>()

    private lateinit var mapViewModel: MapViewModel

    // Constantes para permissões
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 15f
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        //Inicializa os botões do Menu
        starMenuButtons()


        //Definição do envento do botão do menu
        fbtMenu.setOnClickListener {
            onMenuButtonClicked()
        }
        if (savedInstanceState != null) {

            clicked = savedInstanceState.getBoolean("menu_state", false)
            clicked = !clicked
            setVisibility(clicked)
            setAnimation(clicked)
            setClickable(clicked)
           clicked = !clicked
        }

        // Inicializar o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Inicialização do MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        Log.d("MapViewSequence", "[Tempo: 0ms] onCreate iniciado")

        mapView.getMapAsync(this)

        Log.d("MapViewSequence", " onCreate finalizado")

        // Obtém o objeto onBackPressedDispatcher da Activity atual.
        // Esse objeto gerencia os eventos do botão "voltar".
        val onBackPressedDispatcher = this.onBackPressedDispatcher

        // Cria um callback que será chamado quando o botão "voltar" for pressionado.
        val callback = object : OnBackPressedCallback(true) { // true indica que o callback está habilitado
            override fun handleOnBackPressed() {
                if(clicked){ // Verifica se o menu flutuante está aberto
                    onMenuButtonClicked() // Fecha o menu flutuante
                }else {// Se o menu flutuante estiver fechado
                    // Cria um AlertDialog para confirmar o fechamento do app
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Fechar o app?")
                        .setMessage("Tem certeza que deseja sair?")
                        .setPositiveButton("Sim") { _, _ -> // Define o botão positivo ("Sim")
                            finish() // Fecha a Activity (encerra o app)
                        }
                        .setNegativeButton("Não", null) // Define o botão negativo ("Não")
                        .show() // Exibe o AlertDialog
                }
            }
        }
        // Adiciona o callback ao onBackPressedDispatcher.
        // Agora, o callback será chamado quando o botão "voltar" for pressionado.
        onBackPressedDispatcher.addCallback(this, callback)


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
        if (!clicked) {
            fbtAddPolygon.visibility = View.VISIBLE
            fbtClerPolygon.visibility = View.VISIBLE
            fbtSend.visibility = View.VISIBLE
            fbtList.visibility = View.VISIBLE
        } else {
            fbtAddPolygon.visibility = View.INVISIBLE
            fbtClerPolygon.visibility = View.INVISIBLE
            fbtSend.visibility = View.INVISIBLE
            fbtList.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fbtAddPolygon.startAnimation(fromBotton.apply { setAnimationListener(animationListener) })
            fbtClerPolygon.startAnimation(fromBotton.apply { setAnimationListener(animationListener) })
            fbtSend.startAnimation(fromBotton.apply { setAnimationListener(animationListener) })
            fbtList.startAnimation(fromBotton.apply { setAnimationListener(animationListener) })
            fbtMenu.startAnimation(rotateOpen.apply { setAnimationListener(animationListener) })

        } else {
            fbtAddPolygon.startAnimation(toBotton.apply { setAnimationListener(animationListener) })
            fbtClerPolygon.startAnimation(toBotton.apply { setAnimationListener(animationListener) })
            fbtSend.startAnimation(toBotton.apply { setAnimationListener(animationListener) })
            fbtList.startAnimation(toBotton.apply { setAnimationListener(animationListener) })
            fbtMenu.startAnimation(rotateClose.apply { setAnimationListener(animationListener) })
        }

    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fbtAddPolygon.isClickable = true
            fbtClerPolygon.isClickable = true
            fbtSend.isClickable = true
            fbtList.isClickable = true

        } else {
            fbtAddPolygon.isClickable = false
            fbtClerPolygon.isClickable = false
            fbtSend.isClickable = false
            fbtList.isClickable = false
        }
    }

    val animationListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            // Desabilita os botões no início da animação
            setClickable(false)
           // setVisibility(false)
        }

        override fun onAnimationEnd(animation: Animation?) {
            // Habilita ou desabilita os botões no final da animação,
            // de acordo com o estado do menu
            setClickable(!clicked)
          // setVisibility(clicked)
        }

        override fun onAnimationRepeat(animation: Animation?) {
            // Não precisa fazer nada aqui
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

            fbtAddPolygon.setOnClickListener {
                // showDialogAddPolygon()
                val dialogFragmentAddPolygon = DialogFragmentAddPolygon()
                dialogFragmentAddPolygon.show(supportFragmentManager, "DialogFragmentAddPolygon")
            }


        } catch (e: Exception) {
            Log.e("MapDebug", "Erro no onMapReady", e)
        }

        fbtClerPolygon.setOnClickListener {
            if (polygonsOnMap.isEmpty()) { // Verifica se a lista está vazia
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView, "O mapa já está limpo!", Snackbar.LENGTH_SHORT).show()

            }else
            showDialogClear()
        }

        fbtSend.setOnClickListener {
            if (polygonsOnMap.isEmpty()) { // Verifica se a lista está vazia
                val rootView = findViewById<View>(android.R.id.content)
                val snackbar = Snackbar.make(rootView, "Nenhuma ERB e azimute no mapa!", Snackbar.LENGTH_SHORT)
                snackbar.view.textAlignment = View.TEXT_ALIGNMENT_CENTER
                snackbar.show()

            }else mapViewModel.saveMapAsImage(googleMap)
        }
        // Restaurar os polígonos
        for (polygonData in mapViewModel.polygonsList) {
            val polygonOptions = PolygonOptions().apply {
                addAll(polygonData.points)
                strokeColor(polygonData.strokeColor)
                strokeWidth(polygonData.strokeWidth)
                fillColor(polygonData.fillColor)

            }
            googleMap.addPolygon(polygonOptions)?.let{
                polygonsOnMap.add(it)
            }

            val cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(mapViewModel.getCenterLocation(), 13.5f)
            googleMap.animateCamera(cameraUpdate)
        }

        // Recriar os marcadores
        for (markerData in mapViewModel.markersList) {
            val icon = vectorToBitmap(markerData.iconId) // Usa o ID do ícone armazenado
            val markerOptions = MarkerOptions()
                .position(markerData.latLng)
                .title(markerData.title)
                .snippet(markerData.snippet)
                .icon(icon)
            googleMap.addMarker(markerOptions)?.let { markersOnMap.add(it) }

        }

    }


    private fun showDialogClear() {
        val builder = MaterialAlertDialogBuilder(this)
        val view = layoutInflater.inflate(R.layout.dialog_clear_polygon, null)
        builder.setView(view)

        val btnLast = view.findViewById<MaterialButton>(R.id.btnLast)
        val btnAll = view.findViewById<MaterialButton>(R.id.btnAll)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        val dialog = builder.create()


        btnLast.setOnClickListener {
            clearLastSectorPolygon()

            dialog.dismiss()

        }

        btnAll.setOnClickListener {
            clearAllSectorPolygon()
            dialog.dismiss()

        }

        btnCancel.setOnClickListener {

            dialog.cancel()

        }

        builder.setPositiveButton(null, null)
        builder.setNeutralButton(null, null)
        builder.setNegativeButton(null, null)

        dialog.show()

    }


    private fun drawSectorPolygon(sectorPoints: List<LatLng>) {
        // Verificar se o mapa está inicializado
        if (!::googleMap.isInitialized) {
            Log.e("MapDebug", "Google Map nao esta inicializado")
            return
        }

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

            polygonsOnMap.add(polygon)
            Log.d("MapDebug", "Poligono criado com sucesso")

            val polygonData = PolygonData(
                sectorPoints,
                Color.BLUE,
                2f,
                Color.argb(70, 0, 0, 255)
            )
            mapViewModel.addPolygon(polygonData)
        } else {
            Log.e("MapDebug", "Falha ao criar poligono")
        }
    }

    @SuppressLint("NewApi")
    private fun clearLastSectorPolygon() {
        polygonsOnMap.removeLastOrNull()?.remove() // Remove o polígono do mapa e da lista na MainActivity
        markersOnMap.removeLastOrNull()?.remove() // Remove o marcador do mapa e da lista na MainActivity

        mapViewModel.removeLastPolygon() // Remove o polígono do ViewModel
        mapViewModel.removeLastMarker() // Remove o marcador do ViewModel

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, "Útima ERB e azimute apagado!", Snackbar.LENGTH_SHORT).show()
    }

    private fun clearAllSectorPolygon() {
        googleMap.clear() // Limpa todos os elementos do mapa (incluindo polígonos e marcadores)

        polygonsOnMap.clear() // Limpa a lista de polígonos na MainActivity
        mapViewModel.removeAllPolygon()// Remove todos os polígonos do ViewModel


        markersOnMap.clear() // Limpa a lista de marcadores na MainActivity
        mapViewModel.removeAllMarkers() // Remove todos os marcadores do ViewModel

        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, "Mapa limpo!", Snackbar.LENGTH_SHORT).show()
    }

    //Cria um marcador na localização geografica
    private fun addMarker(latLng: LatLng) {
        val icon = vectorToBitmap(R.drawable.ic_tower_48)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("ERB")
            .snippet("Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            .icon(icon)

        val marcador = googleMap.addMarker(markerOptions)
        if (marcador != null) {
            markersOnMap.add(marcador)
        }
        // Salvar as informações do marcador no ViewModel
        val markerData = MarkerData(
            latLng,
            "ERB",
            "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}",
            R.drawable.ic_tower_48 // Passa o ID do ícone
        )
        mapViewModel.addMarker(markerData)
    }

    //Converte o icone vetor pra bitmap
    fun vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

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

        //Salvar o estado do menu
        outState.putBoolean("menu_state", clicked)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
        Log.d("MapViewLifecycle", "onLowMemory chamado")
    }



    override fun onDataSended(
        lat: Double,
        lng: Double,
        azimuth: Double,
        radiusInMeters: Double
    ) {
        var latLng = LatLng(lat, lng)
        onMenuButtonClicked()

        mapViewModel.setCenterLocation(latLng)
        Log.e("MapDebug", "Carregou o CenterLocation com" + latLng)
        mapViewModel.setAzimuth(azimuth)
        mapViewModel.setRadiusInMeters(radiusInMeters)

        try {

            var polygonPoints = mapViewModel.getSectorPolygonPoints()
            drawSectorPolygon(polygonPoints)

            val cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(mapViewModel.getCenterLocation(), 13.5f)
            googleMap.animateCamera(cameraUpdate)

        } catch (e: Exception) {
            Log.e("MapDebug", "Erro no onMapReady", e)
        }

    }




}







