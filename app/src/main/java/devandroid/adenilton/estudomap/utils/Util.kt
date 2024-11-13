package devandroid.adenilton.estudomap.utils

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

    }

}