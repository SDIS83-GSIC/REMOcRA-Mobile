package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TourneeDispo
import fr.sdis83.remocra.mobile.workers.AnnuleReservationTourneeWorker
import fr.sdis83.remocra.mobile.workers.ReserveTourneesWorker
import fr.sdis83.remocra.mobile.workers.TourneesDisposWorker

class ChoixTourneeViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "TourneeViewModel"

        enum class JobStatus {
            WAITING,
            SUCCESS,
            LOADING,
            ERROR,
        }
    }
    private val tourneesDao = RemocraDatabase.getInstance(getApplication()).tourneesDao()
    val tourneesDisponibles = tourneesDao.getTourneesDisponiblesLiveData()

    suspend fun updateTourneeDispo(tourneeDispo: TourneeDispo) = tourneesDao.updateTourneeDispo(tourneeDispo)

    private var tourneesDisponiblesStatus = mutableStateOf(JobStatus.WAITING)
    private var tourneesReserveesStatus = mutableStateOf(JobStatus.WAITING)
    private var annuleReservationStatus = mutableStateOf(JobStatus.WAITING)

    var info = mutableStateOf("")
        private set

    var infoReservation = mutableStateOf("")
        private set

    var infoAnnulation = mutableStateOf("")
        private set

    fun getTourneesDisponibles() {
        val tourneesDisponiblesWorker =
            OneTimeWorkRequestBuilder<TourneesDisposWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager
                .beginWith(tourneesDisponiblesWorker)
                .enqueue()

            workManager.getWorkInfoByIdLiveData(tourneesDisponiblesWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        info.value = "Chargement en cours"
                        tourneesDisponiblesStatus.value = JobStatus.LOADING
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        info.value = "Données chargées"
                        tourneesDisponiblesStatus.value = JobStatus.SUCCESS
                    }

                    WorkInfo.State.FAILED -> {
                        info.value = "Impossible de charger les données demandées"
                        tourneesDisponiblesStatus.value = JobStatus.ERROR
                    }

                    else -> {
                        tourneesDisponiblesStatus.value = JobStatus.WAITING
                    }
                }
            }
        }
    }

    fun reserveTournees(context: Context) {
        // On vérifie que le token est valide avant de faire l'appel au serveur
        val reserveTourneesWorker = OneTimeWorkRequestBuilder<ReserveTourneesWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager
                .beginWith(reserveTourneesWorker)
                .enqueue()

            workManager.getWorkInfoByIdLiveData(reserveTourneesWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        infoReservation.value = "Réservation des tournées en cours..."
                        Toast.makeText(context, infoReservation.value, Toast.LENGTH_SHORT)
                            .show()
                        tourneesReserveesStatus.value = JobStatus.LOADING
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val tourneesNonReservees = it.outputData.getString("NON_RESERVEES")
                        infoReservation.value =
                            if (!tourneesNonReservees.isNullOrBlank()) {
                                "$tourneesNonReservees n'ont pas pu être réservées."
                            } else {
                                "Tournées réservées"
                            }

                        Toast.makeText(context, infoReservation.value, Toast.LENGTH_SHORT)
                            .show()
                        tourneesReserveesStatus.value = JobStatus.SUCCESS
                    }

                    WorkInfo.State.FAILED -> {
                        infoReservation.value = "Impossible de réserver les tournées"
                        Toast.makeText(context, infoReservation.value, Toast.LENGTH_SHORT)
                            .show()
                        tourneesReserveesStatus.value = JobStatus.ERROR
                    }

                    else -> {
                        tourneesReserveesStatus.value = JobStatus.WAITING
                    }
                }
            }
        }
    }

    fun annulerReservationTournee(context: Context, idTournee: Long) {
        val annuleReservationTourneeWorker =
            OneTimeWorkRequestBuilder<AnnuleReservationTourneeWorker>()
                .setInputData(
                    Data.Builder()
                        .putLong("idTournee", idTournee).build(),
                ).build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager
                .beginWith(annuleReservationTourneeWorker)
                .enqueue()

            workManager.getWorkInfoByIdLiveData(annuleReservationTourneeWorker.id).observeForever {
                when (it.state) {
                    WorkInfo.State.RUNNING -> {
                        infoAnnulation.value = "Annulation de la réservation en cours..."
                        Toast.makeText(context, infoReservation.value, Toast.LENGTH_SHORT)
                            .show()
                        annuleReservationStatus.value = JobStatus.LOADING
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        infoAnnulation.value =
                            "Annulation de la réservation de la tournée réussie"
                        Toast.makeText(context, infoAnnulation.value, Toast.LENGTH_SHORT)
                            .show()
                        annuleReservationStatus.value = JobStatus.SUCCESS
                    }

                    WorkInfo.State.FAILED -> {
                        infoAnnulation.value =
                            "Impossible d'annuler la réservation de la tournée"
                        Toast.makeText(context, infoAnnulation.value, Toast.LENGTH_SHORT)
                            .show()
                        annuleReservationStatus.value = JobStatus.ERROR
                    }

                    else -> {
                        annuleReservationStatus.value = JobStatus.WAITING
                    }
                }
            }
        }
    }
}
