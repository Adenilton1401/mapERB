package devandroid.adenilton.estudomap.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import devandroid.adenilton.estudomap.R
import devandroid.adenilton.estudomap.utils.Util
import devandroid.adenilton.estudomap.viewmodel.MapViewModel

class DialogFragmentAddPolygon: DialogFragment() {
    private lateinit var listener: OnDataSendedListener

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
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)
            val btnAdd = view.findViewById<Button>(R.id.btnAdd)

            btnAdd.setOnClickListener(){
                    //Verifeca se o campo Latitude foi preenchido
                if (etLat.text!!.isEmpty()  ){
                    etLat.setError("Preencha a Latitude")
                    etLat.requestFocus()

                    //Verifica se a latitude é válida (entre -90 e 90)
                }else if (MapViewModel.checkLat(etLat.text.toString())){
                    etLat.setError("A Latitude deve estar entre -90 e 90")
                    etLat.requestFocus()


                    //Verifica se o campo do Longitude foi preenchida
                }else if (etLng.text!!.isEmpty()){
                    etLng.setError("Preencha a Longitude")
                    etLng.requestFocus()


                    //Verifica se a Longitude é válida (entre -180 e 180)
                }else if (MapViewModel.checkLng(etLng.text.toString())){
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

                    var lat = Util.convertCoord(etLat.text.toString())
                    var lng = Util.convertCoord(etLng.text.toString())
                    var azimuth = etAzimuth.text.toString().toDoubleOrNull() ?: 0.0
                    var radiusInMeters = etRadiusInMeters.text.toString().toDoubleOrNull() ?: 0.0

                    listener.onDataSended(lat, lng, azimuth, radiusInMeters)
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
        fun onDataSended(lat: Double, lng: Double, azimuth: Double, radiusInMeters: Double)


    }
}