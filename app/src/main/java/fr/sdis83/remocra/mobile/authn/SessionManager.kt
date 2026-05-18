package fr.sdis83.remocra.mobile.authn

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.okta.authfoundation.credential.Credential
import fr.sdis83.remocra.mobile.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val DATE_PROCHAINE_DECONNEXION = "date_prochaine_deconnexion"
        const val LOGOUT_HOURS = "logout_hours"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }

    fun saveAuthToken(credential: Credential) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, credential.token?.accessToken)
        editor.apply()
    }

    private fun saveDateDeconnexion(dateProchaineConnexion: String?) {
        prefs.edit() {
            putString(DATE_PROCHAINE_DECONNEXION, dateProchaineConnexion)
        }
    }

    fun saveLogoutHours(hours: Int) {
        prefs.edit() {
            putInt(LOGOUT_HOURS, hours)
        }
    }

    fun calculateAndSaveDateDeconnexion() {
        if (prefs.getInt(LOGOUT_HOURS, -1) != -1) {
            val nextLogoutInstant = Instant.now().plus(prefs.getInt(LOGOUT_HOURS, -1).toLong(), ChronoUnit.HOURS)
            val nextLogoutDate = DATE_FORMATTER.format(LocalDateTime.ofInstant(nextLogoutInstant, ZoneId.systemDefault()))
            saveDateDeconnexion(nextLogoutDate)
            return
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
            remove(LOGOUT_HOURS)
        }
    }
}
