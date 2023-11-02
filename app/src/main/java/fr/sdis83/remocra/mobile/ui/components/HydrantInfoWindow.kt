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
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.HydrantVisite
import fr.sdis83.remocra.mobile.navigation.Screens
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.infowindow.InfoWindow

class HydrantInfoWindow(mapView: MapView, val navController: NavController) : InfoWindow(R.layout.map_bubble, mapView) {

    val mHydrantNumero = R.id.hydrant_numero
    val mOpenGps = R.id.open_gps
    val mGoToVisite = R.id.go_to_visite
    var mHydrantDisponibilite = R.id.hydrant_disponibilite
    var mHydrantAdresse = R.id.hydrant_adresse
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

            getTextView(mHydrantAdresse).text = if (hydrant.adresseComplete != null) Html.fromHtml(hydrant.adresseComplete, Html.FROM_HTML_MODE_COMPACT) else ""

            getTextView(mHydrantCaracteristiques).text = Html.fromHtml(hydrant.peiCaracteristiques, Html.FROM_HTML_MODE_COMPACT)

            val goToVisite = mView.findViewById<Button>(mGoToVisite)
            if (hydrant.idTournee != null) {
                goToVisite.visibility = View.VISIBLE
                goToVisite.text = if (hydrant.statutVisite == HydrantVisite.HydrantVisiteStatut.A_FAIRE || hydrant.statutVisite == null) {
                    "Démarrer la visite"
                } else {
                    "Editer la visite"
                }
                goToVisite.setOnClickListener {
                    navController.navigate(
                        Screens.Hydrant.route
                            .replace(
                                oldValue = "{idHydrant}",
                                newValue = hydrant.idHydrant.toString(),
                            )
                            .replace(
                                oldValue = "{idTournee}",
                                newValue = hydrant.idTournee.toString(),
                            ),
                    ) {
                        popUpTo(Screens.TourneeHydrants.route)
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
