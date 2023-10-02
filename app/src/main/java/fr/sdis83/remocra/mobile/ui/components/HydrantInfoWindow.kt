package fr.sdis83.remocra.mobile.ui.components

import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.infowindow.InfoWindow

class HydrantInfoWindow(mapView: MapView) : InfoWindow(R.layout.map_bubble, mapView) {

    val mHydrantNumero = R.id.hydrant_numero
    val mOpenGps = R.id.open_gps
    var mHydrantDisponibilite = R.id.hydrant_disponibilite
    var mHydrantAdresse = R.id.hydrant_adresse
    var mHydrantAdresseComplement = R.id.hydrant_adresse_complement
    var mHydrantCaracteristiques = R.id.hydrant_caracteristiques

    init {
        mView.setOnTouchListener { _, motionEvent: MotionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                close()
            }
            true
        }
    }

    override fun onOpen(item: Any?) {
        try {
            val overlay = item as OverlayWithIW
            val hydrant = overlay.relatedObject as MapViewModel.HydrantGeoPoint
            getTextView(mHydrantNumero).text = "Point d'eau N°${hydrant.numero}"

            getTextView(mHydrantDisponibilite).apply {
                text = hydrant.dispoTerrestre?.name
                setTextColor(if (hydrant.dispoTerrestre == Hydrant.Disponibilite.DISPO) Color.rgb(63, 191, 63) else Color.rgb(191, 63, 63))
            }

            val linkGpsTextView = getTextView(mOpenGps)
            linkGpsTextView.movementMethod = LinkMovementMethod.getInstance()
            linkGpsTextView.text = Html.fromHtml(
                "<a href=\"https://maps.google.com/?q=" +
                    "${hydrant.lat},${hydrant.lon}\">Ouvrir le GPS</a>",
                Html.FROM_HTML_MODE_COMPACT,
            )

            getTextView(mHydrantAdresse).text = hydrant.voie
            getTextView(mHydrantAdresseComplement).text =
                hydrant.observation

            getTextView(mHydrantCaracteristiques).text = Html.fromHtml(hydrant.peiCaracteristiques, Html.FROM_HTML_MODE_COMPACT)
        } catch (e: Exception) {
        }
    }

    private fun getTextView(rId: Int): TextView =
        mView.findViewById<View>(rId) as TextView

    override fun onClose() {
        // no-op
    }
}
