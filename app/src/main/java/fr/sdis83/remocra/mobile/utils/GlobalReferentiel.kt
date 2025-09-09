package fr.sdis83.remocra.mobile.utils

import fr.sdis83.remocra.mobile.database.Pei

object GlobalReferentiel {
    var mapDisponibiliteByLibelle: Map<Pei.Disponibilite, String> = emptyMap()
}
