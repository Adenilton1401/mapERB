package devandroid.adenilton.estudomap.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.model.PolygonData
import devandroid.adenilton.estudomap.viewmodel.MapViewModel


class DialogFragmentPolygonInfo: DialogFragment() {
    private lateinit var mapViewModel: MapViewModel
    private var polygonData: PolygonData? = null
    private lateinit var polygonId: String
    private lateinit var listener: OnDataSendedListener

    companion object{
        private const val ARG_POLYGON_ID = "polygon_id"

        fun newInstance(polygonId : String): DialogFragmentPolygonInfo{
            val arg = Bundle().apply {
                putString(ARG_POLYGON_ID, polygonId)
            }

            val fragment = DialogFragmentPolygonInfo()
            fragment.arguments = arg
            return fragment

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnDataSendedListener
        }catch (e: ClassCastException){
            throw ClassCastException("$context deve implementar OnDataSendedListener")
        }
        Log.d("DialogFragmentPolygonInfo", "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DialogFragmentPolygonInfo", "onCreate")
        // Inicializar o ViewModel
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        // Obter as coordenadas dos argumentos de forma segura
        val polygonId = arguments?.getString(ARG_POLYGON_ID)

        if (!polygonId.isNullOrEmpty()) {
            // Obtém polygonData de forma segura
            polygonData = mapViewModel.getPolygonData(polygonId.toString())

            if (polygonData != null) {
                // Continue com o processamento usando polygonId e polygonData
            } else {
                // Trate o caso em que polygonData é nulo
                Log.e("DialogFragmentPolygonInfo", "polygonData não encontrado para polygonId: $polygonId")
                //Snackbar.make(view.findViewById(android.R.id.content), "Erro ao tentar recuperar as informações do azimute!", Snackbar.LENGTH_SHORT).show()
                //dismiss() // Fecha o fragmento ou tome outra ação apropriada
            }
        } else {
            Snackbar.make(requireView().findViewById(android.R.id.content), "Erro ao tentar recuperar as informações do azimute!", Snackbar.LENGTH_SHORT).show()
            // Trate o caso em que polygonId é nulo ou vazio
            Log.e("DialogFragmentPolygonInfo", "ARG_POLYGON_ID está ausente ou nulo")
            dismiss() // Fecha o fragmento ou tome outra ação apropriada
        }


        // Define o estilo do diálogo, se necessário
        setStyle(STYLE_NORMAL, R.style.MyDialog)



    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DialogFragmentPolygonInfo", "onCreateView")
        // Inflar o layout personalizado do diálogo
        return inflater.inflate(R.layout.dialog_polygon_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvAzimuthInfo = view.findViewById<MaterialTextView>(R.id.tvAzimuthInfo)
        val tvAzimuthDesciption = view.findViewById<MaterialTextView>(R.id.tvAzimuthDesciption)
        val btnEditAzimuth = view.findViewById<FloatingActionButton>(R.id.fabEditAzimuth)
        val btnTargetAdress = view.findViewById<FloatingActionButton>(R.id.fabAddTarget)
        val btnApagarAzimuth = view.findViewById<FloatingActionButton>(R.id.fabApagarAzimuth)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnOk)

        val azimuth = polygonData?.azimuth
        tvAzimuthInfo.text = "Azimute: ${azimuth.toString().format().replace(".",",")}"

        val description = polygonData?.description
        tvAzimuthDesciption.text = "Descrição:\n${description}"

        btnApagarAzimuth.setOnClickListener {

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmação")
            builder.setMessage("Você tem certeza que deseja apagar esse azimute?")
            builder.setPositiveButton("Sim"){ dialog, which ->
                val polygonDataID = polygonData?.polygonDataID
                if (polygonDataID != null) {
                    listener.onDataToRemovePolygon(polygonDataID)

                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Não") { dialog, which ->
                // Ação a ser executada quando o usuário clicar em "Não"
                dialog.dismiss()
            }
            val dialogo = builder.create()
            dialogo.show()



            //val rootview = requireView().findViewById(android.R.id.content)
            //Snackbar.make(view, "Essa funcionalidade ainda será implementada!", Snackbar.LENGTH_SHORT).show()
        }

        btnEditAzimuth.setOnClickListener {

        }

        // Configurar o botão OK para fechar o diálogo
        btnOk.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("DialogFragmentPolygonInfo", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("DialogFragmentPolygonInfo", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("DialogFragmentPolygonInfo", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("DialogFragmentPolygonInfo", "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DialogFragmentPolygonInfo", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DialogFragmentPolygonInfo", "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("DialogFragmentPolygonInfo", "onDetach")
    }

    interface OnDataSendedListener{
        fun onDataToRemovePolygon(polygonDataID: String

        )
    }


}