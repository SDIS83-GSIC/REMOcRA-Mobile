package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
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
        @Field("gestionnaireLibelle")
        gestionnaireLibelle: String,
        @Field("gestionnaireCode")
        gestionnaireCode: String?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/contacts/")
    fun postContact(
        @Field("contactId")
        contactId: UUID,
        @Field("gestionnaireId")
        gestionnaireId: UUID,
        @Field("contactNom")
        contactNom: String?,
        @Field("contactPrenom")
        contactPrenom: String?,
        @Field("contactFonctionContactId")
        contactFonctionContactId: UUID?,
        @Field("contactCivilite")
        contactCivilite: String?,
        @Field("contactNumeroVoie")
        contactNumeroVoie: String?,
        @Field("contactSuffixeVoie")
        contactSuffixeVoie: String?,
        @Field("contactVoieText")
        contactVoieText: String?,
        @Field("contactLieuDitText")
        contactLieuDitText: String?,
        @Field("contactCodePostal")
        contactCodePostal: String?,
        @Field("contactCommuneText")
        contactCommuneText: String?,
        @Field("contactPays")
        contactPays: String?,
        @Field("contactTelephone")
        contactTelephone: String?,
        @Field("contactEmail")
        contactEmail: String?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/contacts-roles/")
    fun postContactsRole(
        @Field("contactId")
        contactId: UUID,
        @Field("roleId")
        roleId: UUID,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/create-pei/")
    fun postPei(
        @Field("peiId")
        peiId: UUID,
        @Field("lat")
        lat: Double,
        @Field("lon")
        lon: Double,
        @Field("peiTypePei")
        peiTypePei: String,
        @Field("gestionnaireId")
        gestionnaireId: UUID?,
        @Field("natureDeciId")
        natureDeciId: UUID,
        @Field("natureId")
        natureId: UUID,
        @Field("peiObservation")
        peiObservation: String?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/synchro-visite/")
    fun postVisites(
        @Field("visiteId")
        visiteId: UUID,
        @Field("tourneeId")
        tourneeId: UUID,
        @Field("peiId")
        peiId: UUID,
        @Field("visiteDate")
        visiteDate: String,
        @Field("visiteTypeVisite")
        visiteTypeVisite: String,
        @Field("ctrDebitPression")
        ctrDebitPression: Boolean,
        @Field("visiteAgent1")
        visiteAgent1: String?,
        @Field("visiteAgent2")
        visiteAgent2: String?,
        @Field("visiteCtrlDebitPressionDebit")
        visiteCtrlDebitPressionDebit: Int?,
        @Field("visiteCtrlDebitPressionPression")
        visiteCtrlDebitPressionPression: Double?,
        @Field("visiteCtrlDebitPressionPressionDyn")
        visiteCtrlDebitPressionPressionDyn: Double?,
        @Field("visiteObservations")
        visiteObservations: String?,
        @Field("hasAnomalieChanges")
        hasAnomalieChange: Boolean,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/synchro-visite-anomalie/")
    fun postVisiteAnomalie(
        @Field("visiteId")
        visiteId: UUID,
        @Field("anomalieId")
        anomalieId: UUID,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/synchro-tournee/")
    fun postTournee(
        @Field("tourneeId")
        tourneeId: UUID,
        @Field("tourneeLibelle")
        tourneeLibelle: String,
    ): Call<ResponseBody>

    @POST("synchro/incoming-to-remocra/{tourneeId}")
    fun incomingToRemocra(
        @Path("tourneeId")
        tourneeId: UUID,
    ): Call<ResponseBody>

    @POST("synchro/synchro-photo")
    @Multipart
    fun postPhotoPei(
        @Part("photoId") photoId: RequestBody,
        @Part("peiId") peiId: RequestBody,
        @Part("photoDate") photoDate: RequestBody,
        @Part photo: MultipartBody.Part,
    ): Call<ResponseBody>
}
