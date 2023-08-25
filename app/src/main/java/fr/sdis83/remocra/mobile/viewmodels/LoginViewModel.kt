package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.authn.SessionManager
import fr.sdis83.remocra.mobile.workers.LoginWorker
import fr.sdis83.remocra.mobile.workers.ReferentielWorker

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LoginViewModel"

        enum class JobStatus {
            WAITING,
            SUCCESS,
            LOADING,
            ERROR_VERSION,
            ERROR,
        }
    }
    val sessionManager = SessionManager(application)

    private var loginStatus = mutableStateOf(JobStatus.WAITING)
    private var referentielStatus = mutableStateOf(JobStatus.WAITING)

    var info = mutableStateOf("")
        private set

    val isBusy: Boolean
        get() = loginStatus.value == JobStatus.LOADING || referentielStatus.value == JobStatus.LOADING

    val goToMainActivity = MutableLiveData(false)

    fun login(username: String, password: String) {
        val loginWorker = OneTimeWorkRequestBuilder<LoginWorker>().setInputData(
            Data.Builder()
                .putString("username", username)
                .putString("password", password)
                .build(),
        ).build()
        val referentielWorker = OneTimeWorkRequestBuilder<ReferentielWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager
                .beginWith(loginWorker)
                .then(referentielWorker)
                .enqueue()
            workManager.getWorkInfoByIdLiveData(loginWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        info.value = "Connexion en cours"
                        loginStatus.value = JobStatus.LOADING
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        info.value = "Connexion réussie"
                        loginStatus.value = JobStatus.SUCCESS
                    }

                    WorkInfo.State.FAILED -> {
                        info.value = if (!it.outputData.getString("VERSION_INCOMPATIBLE").isNullOrEmpty()) {
                            loginStatus.value = JobStatus.ERROR_VERSION
                            "La version de la tablette n'est pas à jour, veuillez contacter votre SDIS."
                        } else {
                            loginStatus.value = JobStatus.ERROR
                            "Erreur de connexion"
                        }
                    }

                    else -> loginStatus.value = JobStatus.WAITING
                }
            }
            workManager.getWorkInfoByIdLiveData(referentielWorker.id).observeForever {
                if (loginStatus.value != JobStatus.ERROR_VERSION) {
                    when (it.state) {
                        WorkInfo.State.RUNNING -> {
                            info.value = "Récupération du référentiel"
                            referentielStatus.value = JobStatus.LOADING
                        }

                        WorkInfo.State.SUCCEEDED -> goToMainActivity.postValue(true)
                        WorkInfo.State.FAILED -> {
                            info.value = "Erreur lors de la récupération du référentiel"
                            referentielStatus.value = JobStatus.ERROR
                        }

                        else -> referentielStatus.value = JobStatus.WAITING
                    }
                }
            }
        }
    }
}
