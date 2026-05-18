package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.create
import retrofit2.http.PUT

interface AuthService {
    companion object {
        fun getRetroFitInstance(context: Context): AuthService =
            RetrofitBuilder.getInstance(context).create(AuthService::class.java)
        fun rebuildUrl(context: Context): AuthService =
            RetrofitBuilder.setNewUrl(context).create(AuthService::class.java)
    }

    /**
     * Retourne le mdp passe admin s'il existe
     */
    @PUT("check")
    fun checkUrl(): Call<MobileData>

    data class MobileData(
        val dureeSession: Int?,
        val keycloakConfig: KeycloakConfig,
    )

    data class KeycloakConfig(
        val url: String,
        val clientId: String,
    )
}
