package fr.sdis83.remocra.mobile.authn

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.okta.authfoundation.AuthFoundationDefaults
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.client.OidcConfiguration
import com.okta.authfoundation.client.SharedPreferencesCache
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundationbootstrap.CredentialBootstrap
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.services.AuthService
import okhttp3.HttpUrl.Companion.toHttpUrl

class KeycloakManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val KEYCLOAK_URL = "keycloak_url"
        const val KEYCLOAK_CLIENT_ID = "keycloak_client_id"
        private var isCredentialBootstrapInitialized = false
        private var isCacheInitialized = false
    }

    fun getKeycloakUrl(): String? {
        return prefs.getString(KEYCLOAK_URL, null)
    }

    fun getKeycloakClientId(): String? {
        return prefs.getString(KEYCLOAK_CLIENT_ID, null)
    }

    fun invalidateKeycloakConfig() {
        prefs.edit() {
            remove(KEYCLOAK_URL)
            remove(KEYCLOAK_CLIENT_ID)
        }
    }

    fun initKeycloakConf(keycloakConfig: AuthService.KeycloakConfig, context: Context) {
        prefs.edit() {
            putString(KEYCLOAK_URL, keycloakConfig.url)
            putString(KEYCLOAK_CLIENT_ID, keycloakConfig.clientId)
        }

        // Initialise la connexion à Keycloak
        if (!isCacheInitialized) {
            AuthFoundationDefaults.cache = SharedPreferencesCache.create(context)
            isCacheInitialized = true
        }
        val oidcConfiguration = OidcConfiguration(
            clientId = getKeycloakClientId()!!,
            defaultScope = "openid email profile offline_access",
        )
        val client = OidcClient.createFromDiscoveryUrl(
            oidcConfiguration,
            getKeycloakUrl()!!.toHttpUrl(),
        )

        if (!isCredentialBootstrapInitialized) {
            CredentialBootstrap.initialize(client.createCredentialDataSource(context))
            isCredentialBootstrapInitialized = true
        }
    }
}
