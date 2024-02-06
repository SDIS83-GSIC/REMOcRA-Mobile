package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import fr.sdis83.remocra.mobile.workers.AdministrationWorker
import fr.sdis83.remocra.mobile.workers.MdpAdministrateurWorker

class MdpAdministeurViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "MdpAdministeurViewModel"
    }

    fun getMdpAdmin(administrationViewModel: AdministrationViewModel) {
        val mdpAdministrationWorker = OneTimeWorkRequestBuilder<MdpAdministrateurWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager.enqueue(mdpAdministrationWorker)
            workManager.getWorkInfoByIdLiveData(mdpAdministrationWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val mdp = it.outputData.getString(GlobalConstants.MDP_ADMINISTRATEUR)
                        if (mdp.isNullOrBlank()) {
                            administrationViewModel.setAdministrationScreen(true)
                        } else {
                            administrationViewModel.setMdpAdmin(true)
                        }
                    }

                    WorkInfo.State.FAILED -> {
                        // On redirige vers l'écran d'administration
                        administrationViewModel.setAdministrationScreen(true)
                    }

                    else -> {}
                }
            }
        }

        fun getMdpAdministrateur(context: Context) {
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
}
