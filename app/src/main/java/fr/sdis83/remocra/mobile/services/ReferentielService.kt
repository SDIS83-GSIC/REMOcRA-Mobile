package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.database.Commune
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.Role
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.database.TourneeDispo
import fr.sdis83.remocra.mobile.database.TypeHydrant
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalie
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalieNature
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalieNatureSaisie
import fr.sdis83.remocra.mobile.database.TypeHydrantCritere
import fr.sdis83.remocra.mobile.database.TypeHydrantNature
import fr.sdis83.remocra.mobile.database.TypeHydrantNatureDeci
import fr.sdis83.remocra.mobile.database.TypeHydrantSaisie
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
interface ReferentielService {
    companion object {
        fun getRetroFitInstance(context: Context): ReferentielService =
            RetrofitBuilder.getInstance(context).create(ReferentielService::class.java)
    }

    @GET("referentiel")
    fun getReferentiel(): Call<ReferentielResponse>

    @GET("synchro/tourneesdispos")
    fun getTourneesDisponibles(): Call<List<TourneeDispo>>

    @FormUrlEncoded
    @POST("synchro/reservertournees")
    fun reserveTourneesDisponibles(
        @Field("listIdTournees") listIdTournees: Array<String>,
    ): Call<ReservationTourneesResponse>

    data class ReferentielResponse(
        val communes: List<Commune>,
        val hydrants: List<Hydrant>,
        val hydrantsAnomalies: List<HydrantAnomalieInput>,
        val gestionnaires: List<Gestionnaire>,
        val contacts: List<Contact>,
        val roles: List<Role>,
        val contactsRoles: List<ContactRoleInput>,
        val typesHydrant: List<TypeHydrant>,
        val typesHydrantNature: List<TypeHydrantNature>,
        val typesHydrantNatureDeci: List<TypeHydrantNatureDeci>,
        val typesHydrantAnomalie: List<TypeHydrantAnomalie>,
        val typesHydrantAnomalieNature: List<TypeHydrantAnomalieNature>,
        val typesHydrantAnomalieNatureSaisie: List<TypeHydrantAnomalieNatureSaisie>,
        val typesHydrantCritere: List<TypeHydrantCritere>,
        val typesHydrantSaisie: List<TypeHydrantSaisie>,
    )

    data class ContactRoleInput(
        val idContact: Long,
        val idRole: Long,
    )

    data class HydrantAnomalieInput(
        val idHydrant: Long,
        val idAnomalie: Long,
    )

    data class ReservationTourneesResponse(
        var tourneesReservees: List<TourneeWithHydrant>,
        var tourneesNonReservees: List<Tournee>,
    )

    data class TourneeWithHydrant(
        val idRemocra: Long,
        val nom: String,
        val listeHydrant: List<Long>,
    )
}
