package fr.sdis83.remocra.mobile.ui.screens.tournees

import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import org.osmdroid.util.BoundingBox

/**
 * Permet de zoomer sur la tournée que l'utilisateur sélectionne
 * @param tournee : tournée sélectionnée
 * @param mapViewModel : view model de la carte
 */
fun zoomSurTournee(tournee: Tournee, mapViewModel: MapViewModel) {
    // On zoom sur la tournée sélectionnée
    val listeHydrant = mapViewModel.tourneeList.value?.get(tournee)
    val boundingBox = listeHydrant?.toSet()?.let {
        BoundingBox(
            it.minOf { h -> h.lat },
            it.maxOf { h -> h.lon },
            it.maxOf { h -> h.lat },
            it.minOf { h -> h.lon },
        )
    }
    if (boundingBox != null) {
        mapViewModel.scaleToBox(boundingBox)
    }
}
