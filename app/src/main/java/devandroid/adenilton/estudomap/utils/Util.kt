package devandroid.adenilton.estudomap.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.DecimalFormat
import java.util.Locale

class Util {

    companion object {
        @JvmStatic
        fun convertCoord(coordenada: String): Double {
            // Tenta reconhecer o formato graus, minutos e segundos (DMS)
            val padraoDMS = Regex("""(-?\d{2})(\d{2})(\d+.\d+)""")
            val resultadoDMS = padraoDMS.find(coordenada)


            if (resultadoDMS != null) {
                // Extrai graus, minutos e segundos
                val graus = resultadoDMS.groupValues[1].toInt()
                val minutos = resultadoDMS.groupValues[2].toInt()
                val segundos = resultadoDMS.groupValues[3].toDouble()

                // Converte para graus decimais
                if(graus == 0){
                    return (graus + (-minutos / 60.0) + (-segundos / 3600.0))*(-1)
                }else{
                    return (graus + (-minutos / 60.0) + (-segundos / 3600.0))
                }


            } else {
                // Tenta converter diretamente para Double (graus decimais)

                return coordenada.replace(",", ".").toDoubleOrNull() ?: 0.0
            }
        }

        fun formatCoord(coordenadaStr: Double, tipoCoordenada: String): String? {
            val numero = coordenadaStr

            // Validação dos limites da coordenada
            val valido = when (tipoCoordenada) {
                "lat" -> numero in -90.0..90.0
                "long" -> numero in -180.0..180.0
                else -> false
            }

            if (!valido) {
                return null
            }

            // Definir o padrão de formatação
            val df = DecimalFormat("00.000000")
            df.isDecimalSeparatorAlwaysShown = true

            // Formatar o número
            val coordenadaFormatada = df.format(numero)

            return coordenadaFormatada
        }


        /**
         * Função para obter o endereço a partir de coordenadas de latitude e longitude.
         *
         * @param context Contexto da aplicação.
         * @param latitude Latitude da coordenada.
         * @param longitude Longitude da coordenada.
         * @return String com o endereço formatado ou mensagem de erro.
         */
        suspend fun obterEndereco(
            context: Context,
            latitude: Double,
            longitude: Double
        ): String? {
            return withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val enderecos: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                    if (enderecos != null && enderecos.isNotEmpty()) {
                        val endereco: Address = enderecos[0]
                        val enderecoCompleto = StringBuilder()

                        for (i in 0..endereco.maxAddressLineIndex) {
                            enderecoCompleto.append(endereco.getAddressLine(i)).append("\n")
                        }

                        enderecoCompleto.toString().trim()
                    } else {
                        "Endereço não encontrado para as coordenadas fornecidas."
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    "Erro ao obter o endereço. Verifique sua conexão de internet."
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    "Coordenadas inválidas fornecidas."
                }
            }
        }

    }



}