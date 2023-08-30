package fr.sdis83.remocra.mobile.ui.components

import android.graphics.Color
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
    var mHydrantDisponibilite = R.id.hydrant_disponibilite
    var mHydrantAdresse = R.id.hydrant_adresse
    var mHydrantAdresseComplement = R.id.hydrant_adresse_complement
//    var mHydrantCaracteristiques = R.id.hydrant_caracteristiques

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
            (mView.findViewById<View>(mHydrantNumero) as TextView).text = hydrant.numero
            (mView.findViewById<View>(mHydrantDisponibilite) as TextView).apply {
                text = hydrant.dispoTerrestre?.name
                setTextColor(if (hydrant.dispoTerrestre == Hydrant.Disponibilite.DISPO) Color.rgb(63, 191, 63) else Color.rgb(191, 63, 63))
            }
            (mView.findViewById<View>(mHydrantAdresse) as TextView).text = hydrant.voie
            (mView.findViewById<View>(mHydrantAdresseComplement) as TextView).text =
                hydrant.observation
//            (mView.findViewById<View>(mHydrantCaracteristiques) as TextView).text = "TODO" // TODO
        } catch (e: Exception) {
        }
    }

    override fun onClose() {
        // no-op
    }
}
