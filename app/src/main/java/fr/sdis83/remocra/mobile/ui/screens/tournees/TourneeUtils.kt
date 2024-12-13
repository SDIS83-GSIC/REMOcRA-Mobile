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
    val listePei = mapViewModel.tourneeList.value?.get(tournee)
    val boundingBox = getBoundingBox(listePei)
    if (boundingBox != null) {
        mapViewModel.scaleToBox(boundingBox)
    }
}

/**
 * Permet de zoomer sur les tournées réservées
 * @param mapViewModel : view model de la carte
 */
fun zoomSurTournees(mapViewModel: MapViewModel) {
    // On zoom sur la tournée sélectionnée
    val listePei = mapViewModel.tourneeList.value?.values?.flatten()
    val boundingBox = getBoundingBox(listePei)
    if (boundingBox != null) {
        mapViewModel.scaleToBox(boundingBox)
    }
}

private fun getBoundingBox(listePei: List<MapViewModel.PeiGeoPoint>?) =
    listePei?.toSet()?.let {
        BoundingBox(
            it.minOf { h -> h.lat },
            it.maxOf { h -> h.lon },
            it.maxOf { h -> h.lat },
            it.minOf { h -> h.lon },
        )
    }
