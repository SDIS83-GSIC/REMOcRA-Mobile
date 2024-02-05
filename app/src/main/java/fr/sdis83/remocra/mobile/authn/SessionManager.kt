package fr.sdis83.remocra.mobile.authn

import android.content.Context
import android.content.SharedPreferences
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.services.AuthService

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val DATE_PROCHAINE_DECONNEXION = "date_prochaine_deconnexion"
    }

    fun saveAuthToken(loginResponse: AuthService.LoginResponse) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, loginResponse.token)
        editor.putString(DATE_PROCHAINE_DECONNEXION, loginResponse.dateProchaineDeconnexion)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun getDateDeconnexion(): String? {
        return prefs.getString(DATE_PROCHAINE_DECONNEXION, null)
    }

    fun invalidateAuthToken() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.remove(DATE_PROCHAINE_DECONNEXION)
        editor.apply()
    }
}
