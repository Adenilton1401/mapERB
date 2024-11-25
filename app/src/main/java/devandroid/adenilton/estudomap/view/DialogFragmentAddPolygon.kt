package devandroid.adenilton.estudomap.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColor
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.utils.Util
import devandroid.adenilton.estudomap.viewmodel.MapViewModel

class DialogFragmentAddPolygon: DialogFragment() {
    private lateinit var listener: OnDataSendedListener
    private var colorToPass = Color.BLUE

    private fun setupColorGrid(view: View) {
        val tableLayout = view.findViewById<TableLayout>(R.id.tlColor) // Obtém a referência do TableLayout
        val tvColorView = view.findViewById<TextView>(R.id.twcorView)
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
                    previousSelectedCell?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(100)?.start()

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
        }catch (e: ClassCastException){
            throw ClassCastException("$context deve implementar OnDataSendedListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_polygon,null)

            builder.setView(view)

            val etLat = view.findViewById<TextInputEditText>(R.id.etLatitude)
            val etLng = view.findViewById<TextInputEditText>(R.id.etLongitude)
            val etAzimuth = view.findViewById<TextInputEditText>(R.id.etAzimute)
            val etRadiusInMeters = view.findViewById<TextInputEditText>(R.id.etRaio)
            val etIdentificador = view.findViewById<TextInputEditText>(R.id.etIdentificador)
            val etDescricao = view.findViewById<TextInputEditText>(R.id.etDescricao)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)
            val btnAdd = view.findViewById<Button>(R.id.btnAdd)

            setupColorGrid(view)

            btnAdd.setOnClickListener(){
                // Processar as entradas substituindo ',' por '.' antes da conversão
                val latitudeStr = etLat.text.toString().replace(",", ".")
                val longitudeStr = etLng.text.toString().replace(",", ".")

                    //Verifeca se o campo Latitude foi preenchido
                if (etLat.text!!.isEmpty()  ){
                    etLat.setError("Preencha a Latitude")
                    etLat.requestFocus()

                    //Verifica se a latitude é válida (entre -90 e 90)
                }else if (MapViewModel.checkLat(latitudeStr)){
                    etLat.setError("A Latitude deve estar entre -90 e 90")
                    etLat.requestFocus()


                    //Verifica se o campo do Longitude foi preenchida
                }else if (etLng.text!!.isEmpty()){
                    etLng.setError("Preencha a Longitude")
                    etLng.requestFocus()


                    //Verifica se a Longitude é válida (entre -180 e 180)
                }else if (MapViewModel.checkLng(longitudeStr)){
                    etLng.setError("A Longitude deve estar entre -180 e 180")
                    etLng.requestFocus()


                    //Verifica se o campo do azimute está vazio
                } else if (etAzimuth.text!!.isEmpty()){
                    etAzimuth.setError("Preencha o Azimute")
                    etAzimuth.requestFocus()

                    //Verifica se o azimute e válido (entre 0 e 360)
                } else if (MapViewModel.checkAzimuth(etAzimuth.text.toString())){

                    etAzimuth.setError("O Azimute deve estar entre 0 e 360")
                    etAzimuth.requestFocus()

                }else if (etRadiusInMeters.text!!.isEmpty()){
                    etRadiusInMeters.setError("Preencha o Raio")
                    etRadiusInMeters.requestFocus()

                } else{

                    var lat = Util.convertCoord(latitudeStr)
                    var lng = Util.convertCoord(longitudeStr)
                    var azimuth = etAzimuth.text.toString().toDoubleOrNull() ?: 0.0
                    var radiusInMeters = etRadiusInMeters.text.toString().toDoubleOrNull() ?: 0.0
                    var idetifier = etIdentificador.text.toString()
                    var description = etDescricao.text.toString()

                    listener.onDataSended(lat, lng, azimuth, radiusInMeters, idetifier, description,colorToPass)
                    dialog?.dismiss()
                }

            }

            btnCancel.setOnClickListener(){
                dialog?.cancel()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity não pode se nula")
    }




    interface OnDataSendedListener{
        fun onDataSended(lat: Double,
                         lng: Double,
                         azimuth: Double,
                         radiusInMeters:Double,
                         identifier: String,
                         description: String,
                         colorToPass: Int)


    }
}