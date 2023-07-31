package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.UUID

interface SynchronisationService {

    companion object {
        fun getRetroFitInstance(context: Context): SynchronisationService =
            RetrofitBuilder.getInstance(context).create(SynchronisationService::class.java)
    }

    @FormUrlEncoded
    @POST("synchro/gestionnaires/")
    fun postGestionnaire(
        @Field("idGestionnaire")
        idGestionnaire: UUID,
        @Field("idRemocra")
        idRemocra: Long?,
        @Field("nomGestionnaire")
        nomGestionnaire: String,
        @Field("codeGestionnaire")
        codeGestionnaire: String?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("synchro/contacts/")
    fun postContact(
        @Field("idContact")
        idContact: UUID,
        @Field("idGestionnaire")
        idGestionnaire: UUID,
        @Field("idRemocra")
        idRemocra: Long?,
        @Field("nom")
        nom: String,
        @Field("prenom")
        prenom: String,
        @Field("fonction")
        fonction: String?,
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
        @Field("idContact")
        idContact: UUID,
        @Field("idRoleRemocra")
        idRoleRemocra: Long,
    ): Call<ResponseBody>
}
