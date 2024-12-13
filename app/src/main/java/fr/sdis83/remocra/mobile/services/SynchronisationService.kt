package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import java.util.UUID

interface SynchronisationService {

    companion object {
        fun getRetroFitInstance(context: Context): SynchronisationService =
            RetrofitBuilder.getInstance(context).create(SynchronisationService::class.java)
    }

    @FormUrlEncoded
    @POST("synchro/gestionnaires/")
    fun postGestionnaire(
        @Field("gestionnaireId")
        gestionnaireId: UUID,
        @Field("nomGestionnaire")
        nomGestionnaire: String,
        @Field("codeGestionnaire")
        codeGestionnaire: String?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/contacts/")
    fun postContact(
        @Field("contactId")
        contactId: UUID,
        @Field("gestionnaireId")
        gestionnaireId: UUID,
        @Field("nom")
        nom: String,
        @Field("prenom")
        prenom: String,
        @Field("fonction")
        fonction: UUID?,
        @Field("civilite")
        civilite: String,
        @Field("numeroVoie")
        numeroVoie: String?,
        @Field("suffixeVoie")
        suffixeVoie: String?,
        @Field("voie")
        voie: String,
        @Field("lieuDit")
        lieuDit: String?,
        @Field("codePostal")
        codePostal: String,
        @Field("ville")
        ville: String,
        @Field("pays")
        pays: String,
        @Field("telephone")
        telephone: String?,
        @Field("email")
        email: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/contactsrole/")
    fun postContactsRole(
        @Field("contactId")
        contactId: UUID,
        @Field("idRoleRemocra")
        idRoleRemocra: UUID,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/createhydrant/")
    fun postHydrants(
        @Field("peiId")
        peiId: UUID,
        @Field("lat")
        lat: Double,
        @Field("lon")
        lon: Double,
        @Field("code")
        code: String,
        @Field("gestionnaireId")
        gestionnaireId: UUID?,
        @Field("idGestionnaireRemocra")
        idGestionnaireRemocra: UUID?,
        @Field("idNatureDeci")
        idNatureDeci: UUID,
        @Field("idNature")
        idNature: UUID,
        @Field("observations")
        observations: String?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/synchrohydrantvisite/")
    fun postHydrantsVisites(
        @Field("visiteId")
        visiteId: UUID,
        @Field("peiId")
        peiId: UUID,
        @Field("date")
        date: String,
        @Field("idTypeVisite")
        idTypeVisite: UUID,
        @Field("ctrDebitPression")
        ctrDebitPression: Boolean,
        @Field("agent1")
        agent1: String?,
        @Field("agent2")
        agent2: String?,
        @Field("debit")
        debit: Int?,
        @Field("pression")
        pression: Double?,
        @Field("pressionDyn")
        pressionDyn: Double?,
        @Field("observations")
        observations: String?,
        @Field("hasAnomalieChanges")
        hasAnomalieChange: Boolean,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/synchrohydrantvisiteanomalie/")
    fun postHydrantVisiteAnomalie(
        @Field("visiteId")
        visiteId: UUID,
        @Field("idAnomalie")
        idAnomalie: UUID,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/synchrotournee/")
    fun postTournee(
        @Field("idTourneeRemocra")
        idTourneeRemocra: UUID,
        @Field("nom")
        nom: String,
    ): Call<ResponseBody>

    @PUT("synchro/incomingtoremocra/")
    fun incomingToRemocra(): Call<ResponseBody>

    @POST("synchro/synchrohydrantphoto")
    @Multipart
    fun postHydrantPhoto(
        @Part("peiId")
        peiId: UUID,
        @Part("datePhoto")
        datePhoto: String,
        @Part photo: MultipartBody.Part,
    ): Call<ResponseBody>
}
