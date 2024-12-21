// DialogFragmentMarkerInfo.kt
package devandroid.adenilton.estudomap.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.utils.Util
import devandroid.adenilton.estudomap.viewmodel.MapViewModel
import kotlinx.coroutines.launch

class DialogFragmentMarkerInfo : DialogFragment(){

    private lateinit var mapViewModel: MapViewModel
    private var markerPosition: LatLng? = null

    companion object {
        private const val ARG_MARKER_LAT = "marker_lat"
        private const val ARG_MARKER_LNG = "marker_lng"

        // Método padrão para criar uma nova instância do DialogFragment com argumentos
        fun newInstance(latitude: Double, longitude: Double): DialogFragmentMarkerInfo {
            val args = Bundle().apply {
                putDouble(ARG_MARKER_LAT, latitude)
                putDouble(ARG_MARKER_LNG, longitude)
            }
            val fragment = DialogFragmentMarkerInfo()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("DialogFragmentMarkerInfo", "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DialogFragmentMarkerInfo", "onCreate")
        // Inicializar o ViewModel
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        // Obter as coordenadas dos argumentos
        arguments?.let {
            val lat = it.getDouble(ARG_MARKER_LAT)
            val lng = it.getDouble(ARG_MARKER_LNG)
            markerPosition = LatLng(lat, lng)
        }

        // Define o estilo do diálogo, se necessário
        setStyle(STYLE_NORMAL, R.style.MyDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFragmentMarkerInfo", "onCreateView")
        // Inflar o layout personalizado do diálogo
        return inflater.inflate(R.layout.dialog_marker_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DialogFragmentMarkerInfo", "onViewCreated")

        // Preencha os campos do layout com as informações do marcador
        val tvMarkerInfo = view.findViewById<MaterialTextView>(R.id.tvMarkerLatLng)
        val tvPolygonInfo = view.findViewById<TextView>(R.id.tvEnderecoERB)

        val btnOk = view.findViewById<Button>(R.id.btnOk)

        val lat = markerPosition?.latitude
        val lng = markerPosition?.longitude

        tvMarkerInfo.text = "Latitude: ${lat?.let { Util.formatCoord(it, "lat") }}\n" +
                "Longitude: ${lng?.let { Util.formatCoord(it, "long") }}"

        // Configurar o botão OK para fechar o diálogo
        btnOk.setOnClickListener {
            dismiss()
        }

        // Lançar uma corrotina no contexto do ciclo de vida
        viewLifecycleOwner.lifecycleScope.launch {
            if (lat != null && lng != null) {
                val endereco = Util.obterEndereco(requireContext(), lat, lng)
                tvPolygonInfo.text = "Endereço: \n${endereco ?: "Endereço não disponível."}"
            } else {
                tvPolygonInfo.text = "Coordenadas não disponíveis."
            }
        }
        //Adiciona o botão para chamar o google Mapas
        val btnCallGoogleMaps = view.findViewById<ImageButton>(R.id.btnCallGoogleMaps)
        btnCallGoogleMaps.setOnClickListener{
            if (lat != null && lng != null) {
                callGoogleMaps(lat, lng)
            } else{
                val rootView = view?.findViewById<View>(android.R.id.content)
                if (rootView != null) {
                    Snackbar.make(rootView, "Endereço não encontrado.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        val btnAddAzimuth = view.findViewById<MaterialButton>(R.id.btnAddAzimuth)
        btnAddAzimuth.setOnClickListener {
            if (lat != null && lng != null) {
                val dialogFragmentAddAzimuth = DialogFragmentAddAzimuth.newInstance(lat, lng)
                dialogFragmentAddAzimuth.show(childFragmentManager, "DialogFragmentAddAzimuth")
            }

        }
    }

    private fun callGoogleMaps(latitude: Double, longitude: Double) {
        // Cria a URI com as coordenadas
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        intent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Caso o Google Maps não esteja instalado, abra no navegador
            val navegadorUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val navegadorIntent = Intent(Intent.ACTION_VIEW, navegadorUri)
            try {
                startActivity(navegadorIntent)
            } catch (e: ActivityNotFoundException) {
                val rootView = view?.findViewById<View>(android.R.id.content)
                if (rootView != null) {
                    Snackbar.make(rootView, "Nenhum aplicativo de mapa encontrado.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("DialogFragmentMarkerInfo", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("DialogFragmentMarkerInfo", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("DialogFragmentMarkerInfo", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("DialogFragmentMarkerInfo", "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DialogFragmentMarkerInfo", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DialogFragmentMarkerInfo", "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("DialogFragmentMarkerInfo", "onDetach")
    }
}
