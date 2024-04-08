package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.workers.AdministrationWorker

class AdministrationViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "AdministrationViewModel"
    }

    val administrationScreen = MutableLiveData(false)

    fun setAdministrationScreen(value: Boolean) = administrationScreen.postValue(value)

    fun checkUrl(context: Context) {
        val administrationWorker = OneTimeWorkRequestBuilder<AdministrationWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager.enqueue(administrationWorker)
            workManager.getWorkInfoByIdLiveData(administrationWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        Toast.makeText(context, "Connexion en cours", Toast.LENGTH_SHORT).show()
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT).show()
                    }

                    WorkInfo.State.FAILED -> {
                        Toast.makeText(
                            context,
                            "Echec de la connexion au serveur",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }
}
