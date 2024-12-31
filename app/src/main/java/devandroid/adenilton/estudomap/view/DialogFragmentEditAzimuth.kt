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
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.material.textfield.TextInputEditText
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.model.PolygonData
import devandroid.adenilton.estudomap.viewmodel.MapViewModel

class DialogFragmentEditAzimuth : DialogFragment() {
    private lateinit var listener: OnDataSendedListener
    private var colorToPass = Color.BLUE

    private lateinit var mapViewModel: MapViewModel
    private lateinit var polygonData: PolygonData
    private lateinit var polygon: Polygon
    private lateinit var polygonDataID: String
    private var markerPosition: LatLng? = null

    companion object {
        private const val ARG_MARKER_ID = "marker_lat"


        // Método padrão para criar uma nova instância do DialogFragment com argumentos
        fun newInstance(polygonDataID: String): DialogFragmentEditAzimuth {
            val args = Bundle().apply {
                putString(ARG_MARKER_ID, polygonDataID)

            }
            val fragment = DialogFragmentEditAzimuth()
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupColorGrid(view: View) {
        val tableLayout =
            view.findViewById<TableLayout>(R.id.tlColorAzimuth) // Obtém a referência do TableLayout
        val tvColorView = view.findViewById<TextView>(R.id.twcorViewAzimuth)
        val numRows = tableLayout.childCount
        var selectedColor = colorToPass // Cor inicial selecionada

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

        } catch (e: ClassCastException) {
            throw ClassCastException("$context deve implementar OnDataSendedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DialogFragmentEditAzimuth", "onCreate")
        // Inicializar o ViewModel
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        var polygonDataID_ : String? = null

        // Obter as coordenadas dos argumentos
        arguments?.let {
            polygonDataID_ = it.getString(DialogFragmentEditAzimuth.ARG_MARKER_ID)

        }
        if(polygonDataID_ != null){

            polygonDataID = polygonDataID_ as String
        }

        polygonData = mapViewModel.getPolygonData(polygonDataID)!!


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

        Log.d("DialogFragmentEditAzimuth", "onViewCreated")

        val twTitulo = view.findViewById<TextView>(R.id.twTitulo)
        val etAzimuth = view.findViewById<TextInputEditText>(R.id.etAzimuteDialog)
        val etRadiusInMeters = view.findViewById<TextInputEditText>(R.id.etRaioDialog)
        val etDescricao = view.findViewById<TextInputEditText>(R.id.etDescricaoDialog)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelDialog)
        val btnAdd = view.findViewById<Button>(R.id.btnAddDialog)
        val twcorViewAzimuth = view.findViewById<TextView>(R.id.twcorViewAzimuth)



        etAzimuth.setText(polygonData.azimuth.toInt().toString())
        etRadiusInMeters.setText(polygonData.radiusInMeters.toInt().toString())
        etDescricao.setText(polygonData.description)
        btnAdd.setText("Atualizar")
        twTitulo.setText("Editar Azimute")
        var color = polygonData.fillColor

        twcorViewAzimuth.setBackgroundColor(Color.argb(255,color.red,color.green,color.blue))
        colorToPass = polygonData.fillColor

        setupColorGrid(view)



        btnAdd.setOnClickListener() {


            //Verifica se o campo do azimute está vazio
            if (etAzimuth.text!!.isEmpty()) {
                etAzimuth.setError("Preencha o Azimute")
                etAzimuth.requestFocus()

                //Verifica se o azimute e válido (entre 0 e 360)
            } else if (MapViewModel.checkAzimuth(etAzimuth.text.toString().format().replace(",","."))) {

                etAzimuth.setError("O Azimute deve estar entre 0 e 360")
                etAzimuth.requestFocus()

            } else if (etRadiusInMeters.text!!.isEmpty()) {
                etRadiusInMeters.setError("Preencha o Raio")
                etRadiusInMeters.requestFocus()

            } else {

                var lat = polygonData.centerPoint.latitude
                var lng = polygonData.centerPoint.longitude
                var azimuth = etAzimuth.text.toString().toDoubleOrNull() ?: 0.0
                var radiusInMeters = etRadiusInMeters.text.toString().toDoubleOrNull() ?: 0.0
                var description = etDescricao.text.toString()
                var idetifier = "ERB"
                var polygonDataID = polygonData.polygonDataID

                if (lat != null && lng != null) {
                    listener.onDataEditAzimuth(
                        polygonDataID,
                        lat,
                        lng,
                        azimuth,
                        radiusInMeters,
                        idetifier,
                        description,
                        colorToPass


                    )
                }

                dialog?.dismiss()

            }

        }

        btnCancel.setOnClickListener() {
            dialog?.cancel()
        }


    }


    interface OnDataSendedListener {
        fun onDataEditAzimuth(
            polygonDataID: String,
            lat: Double,
            lng: Double,
            azimuth: Double,
            radiusInMeters: Double,
            identifier: String,
            description: String,
            colorToPass: Int
        )


    }


}