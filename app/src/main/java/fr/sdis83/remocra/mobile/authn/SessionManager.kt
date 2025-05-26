package fr.sdis83.remocra.mobile.authn

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.okta.authfoundation.credential.Credential
import fr.sdis83.remocra.mobile.R

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val DATE_PROCHAINE_DECONNEXION = "date_prochaine_deconnexion"
    }

    fun saveAuthToken(credential: Credential) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, credential.token?.accessToken)
        editor.apply()
    }

    fun saveDateDeconnexion(dateProchaineConnexion: String?) {
        prefs.edit() {
            putString(DATE_PROCHAINE_DECONNEXION, dateProchaineConnexion)
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun getDateDeconnexion(): String? {
        return prefs.getString(DATE_PROCHAINE_DECONNEXION, null)
    }

    fun invalidateAuthToken() {
        prefs.edit() {
            remove(USER_TOKEN)
            remove(DATE_PROCHAINE_DECONNEXION)
        }
    }
}
