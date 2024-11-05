package devandroid.adenilton.estudomap.model

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SectorPolygon(
    private val center : LatLng,
    private val radiusInMeters : Double,
    private val azimuth : Double,
    private val angleInDegrees : Double,
    private val numberOfPoints : Int

) {
    fun getPolygonPoints(): List<LatLng> {
        // Validações
        if (radiusInMeters <= 0 || angleInDegrees <= 0 || numberOfPoints <= 0) {
            return emptyList()
        }

        val points = mutableListOf<LatLng>()
        points.add(center)

        // Calcula ângulos
        val halfAngle = angleInDegrees / 2.0
        val startAngle = (azimuth - halfAngle).toRadians()
        val endAngle = (azimuth + halfAngle).toRadians()

        // Calcula pontos do arco
        val angleDelta = (endAngle - startAngle) / (numberOfPoints - 1)

        for (i in 0 until numberOfPoints) {
            val angle = startAngle + (i * angleDelta)
            val point = calculateDestinationPoint(
                center,
                radiusInMeters,
                angle.toDegrees()
            )
            points.add(point)
        }

        // Fecha o polígono
        points.add(center)

        return points
    }

    private fun calculateDestinationPoint(
        start: LatLng,
        distance: Double,
        bearing: Double
    ): LatLng {
        val R = 6371000.0 // Raio da Terra em metros

        val lat1 = start.latitude.toRadians()
        val lon1 = start.longitude.toRadians()
        val bearingRad = bearing.toRadians()

        val angularDistance = distance / R

        val lat2 = asin(
            sin(lat1) * cos(angularDistance) +
                    cos(lat1) * sin(angularDistance) * cos(bearingRad)
        )

        val lon2 = lon1 + atan2(
            sin(bearingRad) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )

        return LatLng(lat2.toDegrees(), lon2.toDegrees())
    }

    // Funções de extensão úteis
    private fun Double.toRadians() = this * PI / 180.0
    private fun Double.toDegrees() = this * 180.0 / PI

}