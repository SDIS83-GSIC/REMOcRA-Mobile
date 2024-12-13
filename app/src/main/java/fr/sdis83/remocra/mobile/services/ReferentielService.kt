package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.database.Anomalie
import fr.sdis83.remocra.mobile.database.AnomalieCategorie
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.ContactRole
import fr.sdis83.remocra.mobile.database.FonctionContact
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.LPeiAnomalie
import fr.sdis83.remocra.mobile.database.NatureDeci
import fr.sdis83.remocra.mobile.database.Parametre
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.database.TourneeDispo
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.UUID
interface ReferentielService {
    companion object {
        fun getRetroFitInstance(context: Context): ReferentielService =
            RetrofitBuilder.getInstance(context).create(ReferentielService::class.java)
        fun rebuildUrl(context: Context): ReferentielService =
            RetrofitBuilder.setNewUrl(context).create(ReferentielService::class.java)
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

    @FormUrlEncoded
    @POST("synchro/annulereservation")
    fun annuleReservation(
        @Field("tourneeId") tourneeId: UUID,
    ): Call<String>

    data class ReferentielResponse(
        val listPei: List<PeiInput>,
        val listPeiAnomalies: List<LPeiAnomalie>,
        val listGestionnaire: List<Gestionnaire>,
        val listFonctionContact: List<FonctionContact>? = listOf(),
        val listContact: List<Contact>,
        val listRole: List<IdCodeLibelleInput>,
        val listContactRole: List<ContactRole>,
        val listTypePei: List<String>,
        val listNature: List<NatureInput>,
        val listNatureDeci: List<NatureDeci>,
        val listAnomalie: List<Anomalie>,
        val listPoidsAnomalie: List<PoidsAnomalieInput>,
        val listAnomalieCategorie: List<AnomalieCategorie>,
        val listTypeVisite: List<String>,
        val listParametre: List<Parametre>,
        val listDroit: List<String>,
        val peiCaracteristiques: Map<UUID, String>,
        val utilisateurConnecte: String,
    )

    data class NatureInput(
        val natureId: UUID,
        val natureCode: String,
        val natureLibelle: String,
        val natureTypePei: String,
    )

    data class PeiInput(
        val peiId: UUID,
        val natureId: UUID,
        val natureDeciId: UUID,
        val dispoHbe: Pei.DisponibiliteHbe?,
        val dispoTerrestre: Pei.Disponibilite,
        val x: Double,
        val y: Double,
        val lon: Double,
        val lat: Double,
        val peiNumeroComplet: String?,
        val peiTypePei: String,
        val peiEnFace: Boolean?,
        val peiNumeroVoie: String?,
        val peiSuffixeVoie: String?,
        val peiVoieId: UUID?,
        val peiVoieLibelle: String?,
        val peiVoieTexte: String?,
        val peiComplementAdresse: String?,
        val communeCodePostal: String?,
        val communeLibelle: String,
        val lieuDitId: UUID?,
        val lieuDitLibelle: String?,
        val peiObservation: String?,
        val gestionnaireId: UUID?,
    )

    data class IdCodeLibelleInput(
        val id: UUID,
        val code: String,
        val libelle: String,
    )

    data class PoidsAnomalieInput(
        val poidsAnomalieId: UUID,
        val poidsAnomalieAnomalieId: UUID,
        val poidsAnomalieNatureId: UUID,
        val poidsAnomalieTypeVisite: Collection<String>,
        val poidsAnomalieValIndispoHbe: Int?,
        val poidsAnomalieValIndispoTerrestre: Int?,
    )

    data class ReservationTourneesResponse(
        var tourneesReservees: List<TourneeWithPei>,
        var tourneesNonReservees: List<Tournee>,
    )

    data class TourneeWithPei(
        val tourneeId: UUID,
        val tourneeLibelle: String,
        val listePei: List<UUID>,
    )
}
