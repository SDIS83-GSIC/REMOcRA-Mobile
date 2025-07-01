package fr.sdis83.remocra.mobile.ui.components

import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.Visite
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.infowindow.InfoWindow

class PeiInfoWindow(mapView: MapView, val navController: NavController) : InfoWindow(R.layout.map_bubble, mapView) {

    val mPeiNumero = R.id.pei_numero
    val mOpenGps = R.id.open_gps
    val mGoToVisite = R.id.go_to_visite
    var mPeiDisponibilite = R.id.pei_disponibilite
    var mPeiAdresse = R.id.pei_adresse
    var mPeiCaracteristiques = R.id.pei_caracteristiques

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
            val pei = overlay.relatedObject as MapViewModel.PeiGeoPoint
            getTextView(mPeiNumero).text = "Point d'eau N°${pei.peiNumeroComplet}"

            getTextView(mPeiDisponibilite).apply {
                text = pei.dispoTerrestre?.name
                setTextColor(if (pei.dispoTerrestre == Pei.Disponibilite.DISPONIBLE) Color.rgb(63, 191, 63) else Color.rgb(191, 63, 63))
            }

            val linkGpsTextView = getTextView(mOpenGps)
            linkGpsTextView.movementMethod = LinkMovementMethod.getInstance()
            linkGpsTextView.text = Html.fromHtml(
                "<a href=\"https://maps.google.com/?q=" +
                    "${pei.lat},${pei.lon}\">Ouvrir le GPS</a>",
                Html.FROM_HTML_MODE_COMPACT,
            )

            getTextView(mPeiAdresse).text = if (pei.adresseComplete != null) Html.fromHtml(pei.adresseComplete, Html.FROM_HTML_MODE_COMPACT) else ""

            getTextView(mPeiCaracteristiques).text = Html.fromHtml(pei.peiCaracteristiques ?: "", Html.FROM_HTML_MODE_COMPACT)

            val goToVisite = mView.findViewById<Button>(mGoToVisite)
            if (pei.tourneeId != null) {
                goToVisite.visibility = View.VISIBLE
                goToVisite.text = if (pei.statutVisite == Visite.VisiteStatut.A_FAIRE || pei.statutVisite == null) {
                    "Démarrer la visite"
                } else {
                    "Editer la visite"
                }
                goToVisite.setOnClickListener {
                    navController.navigate(
                        Screens.Pei.route
                            .replace(
                                oldValue = "{peiId}",
                                newValue = pei.peiId.toString(),
                            )
                            .replace(
                                oldValue = "{tourneeId}",
                                newValue = pei.tourneeId.toString(),
                            ),
                    ) {
                        popUpTo(Screens.TourneePei.route)
                    }
                }
            } else {
                goToVisite.visibility = View.GONE
            }
        } catch (e: Exception) {
        }
    }

    private fun getTextView(rId: Int): TextView =
        mView.findViewById<View>(rId) as TextView

    override fun onClose() {
        // no-op
    }
}
