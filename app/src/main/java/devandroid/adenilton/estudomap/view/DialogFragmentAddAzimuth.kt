package devandroid.adenilton.estudomap.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.viewmodel.MapViewModel

class DialogFragmentAddAzimuth : DialogFragment() {
    private lateinit var listener: OnDataSendedListener
    private lateinit var listenerClose: OnCloseDialogListener
    private var colorToPass = Color.BLUE
    private lateinit var mapViewModel: MapViewModel
    private var markerPosition: LatLng? = null

    companion object {
        private const val ARG_MARKER_LAT = "marker_lat"
        private const val ARG_MARKER_LNG = "marker_lng"

        // Método padrão para criar uma nova instância do DialogFragment com argumentos
        fun newInstance(latitude: Double, longitude: Double): DialogFragmentAddAzimuth {
            val args = Bundle().apply {
                putDouble(ARG_MARKER_LAT, latitude)
                putDouble(ARG_MARKER_LNG, longitude)
            }
            val fragment = DialogFragmentAddAzimuth()
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupColorGrid(view: View) {
        val tableLayout =
            view.findViewById<TableLayout>(R.id.tlColorAzimuth) // Obtém a referência do TableLayout
        val tvColorView = view.findViewById<TextView>(R.id.twcorViewAzimuth)
        val numRows = tableLayout.childCount
        var selectedColor = Color.BLUE // Cor inicial selecionada

        for (i in 0 until numRows) {
            val row = tableLayout.getChildAt(i) as TableRow
            val numCells = row.childCount

            for (j in 0 until numCells) {
                val cell = row.getChildAt(j)

                cell.setOnClickListener {
                    // Remove o destaque da célula anteriormente selecionada
                    val previousSelectedCell = tableLayout.findViewWithTag<View>(selectedColor)
                    previousSelectedCell?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(100)
                        ?.start()

                    // Destaca a célula selecionada
                    selectedColor = (cell.background as ColorDrawable).color
                    cell.animate()?.scaleX(1.4f)?.scaleY(1.2f)?.setDuration(100)?.start()
                    cell.tag = selectedColor

                    tvColorView.setBackgroundColor(selectedColor)
                    colorToPass = selectedColor

                    // Lógica para usar a cor selecionada
                    // ...
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnDataSendedListener
            listenerClose = context as OnCloseDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context deve implementar OnDataSendedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DialogFragmentAddAzimuth", "onCreate")
        // Inicializar o ViewModel
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        // Obter as coordenadas dos argumentos
        arguments?.let {
            val lat = it.getDouble(DialogFragmentAddAzimuth.ARG_MARKER_LAT)
            val lng = it.getDouble(DialogFragmentAddAzimuth.ARG_MARKER_LNG)
            markerPosition = LatLng(lat, lng)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFragmentAddAzimuth", "onCreateView")
        // Inflar o layout personalizado do diálogo
        return inflater.inflate(R.layout.dialog_add_azimuth, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DialogFragmentAddAzimuth", "onViewCreated")

        val etAzimuth = view.findViewById<TextInputEditText>(R.id.etAzimuteDialog)
        val etRadiusInMeters = view.findViewById<TextInputEditText>(R.id.etRaioDialog)
        val etDescricao = view.findViewById<TextInputEditText>(R.id.etDescricaoDialog)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelDialog)
        val btnAdd = view.findViewById<Button>(R.id.btnAddDialog)

        setupColorGrid(view)

        btnAdd.setOnClickListener() {


            //Verifica se o campo do azimute está vazio
            if (etAzimuth.text!!.isEmpty()) {
                etAzimuth.setError("Preencha o Azimute")
                etAzimuth.requestFocus()

                //Verifica se o azimute e válido (entre 0 e 360)
            } else if (MapViewModel.checkAzimuth(etAzimuth.text.toString())) {

                etAzimuth.setError("O Azimute deve estar entre 0 e 360")
                etAzimuth.requestFocus()

            } else if (etRadiusInMeters.text!!.isEmpty()) {
                etRadiusInMeters.setError("Preencha o Raio")
                etRadiusInMeters.requestFocus()

            } else {

                var lat = markerPosition?.latitude
                var lng = markerPosition?.longitude
                var azimuth = etAzimuth.text.toString().toDoubleOrNull() ?: 0.0
                var radiusInMeters = etRadiusInMeters.text.toString().toDoubleOrNull() ?: 0.0
                var description = etDescricao.text.toString()
                var idetifier = "ERB"

                if (lat != null && lng != null) {
                    listener.onDataSended(
                        lat,
                        lng,
                        azimuth,
                        radiusInMeters,
                        idetifier,
                        description,
                        colorToPass
                    )
                }
                listenerClose.onCloseDialogERB()
                dialog?.dismiss()

            }

        }

        btnCancel.setOnClickListener() {
            dialog?.cancel()
        }


    }


    interface OnDataSendedListener {
        fun onDataSended(
            lat: Double,
            lng: Double,
            azimuth: Double,
            radiusInMeters: Double,
            identifier: String,
            description: String,
            colorToPass: Int
        )


    }

    interface OnCloseDialogListener {
        fun onCloseDialogERB()
    }
}

