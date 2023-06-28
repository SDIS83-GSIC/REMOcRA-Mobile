package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.http.GET

interface ReferentielService {
    companion object {
        fun getRetroFitInstance(context: Context): ReferentielService =
            RetrofitBuilder.getInstance(context).create(ReferentielService::class.java)
    }

    @GET("referentiel")
    fun getReferentiel(): Call<ReferentielResponse>

    data class ReferentielResponse(
        var hydrants: List<Hydrant>,
        var gestionnaires: List<Gestionnaire>,
        var contacts: List<Contact>
    )
}
