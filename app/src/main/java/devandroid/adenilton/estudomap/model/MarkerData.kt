package devandroid.adenilton.estudomap.model

import com.google.android.gms.maps.model.LatLng

data class MarkerData(
    val latLng: LatLng,
    val title: String,
    val snippet: String,
    val iconId: Int,
    val description: String// Armazena o ID do recurso do ícone
)
