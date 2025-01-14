package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Anomalie
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.database.Visite
import fr.sdis83.remocra.mobile.utils.jsonToFile
import java.util.UUID

/**
 * Permet de générer un fichier json par tournée. A chaque fois qu'un opération CRUD est faite, on
 * stocke les informations dans un fichier. si le fichier existe on l'écrase sinon on le crée.
 */
class JsonTourneeWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "JsonTourneeWorker"
    }

    override fun doWork(): Result = try {
        // Récupérer l'id de la tournée, puis tous les PEI avec les anomalies et ctrl ctp
        val idTournee = UUID.fromString(inputData.getString("idTournee"))

        // On va chercher dans la base de données la tournée avec les PEI et leurs visites
        val tourneeDao = RemocraDatabase.getInstance(applicationContext).tourneeDao()
        val visiteDao = RemocraDatabase.getInstance(applicationContext).visiteDao()
        val peiDao = RemocraDatabase.getInstance(applicationContext).peiDao()
        val photoDao = RemocraDatabase.getInstance(applicationContext).photoPeiDao()

        val tournee = tourneeDao.getTourneeEnCours(idTournee)

        val visites = visiteDao.getVisites(idTournee)

        jsonToFile(
            TourneeWithData(
                tournee = tournee,
                infosVisites = visites.map {
                    VisiteWithAnomaliePhoto(
                        visite = it,
                        anomalies = if (it.hasAnomalieChanges) {
                            visiteDao.getCurrentVisiteAnomalieByIdPei(it.peiId, idTournee).toMutableList()
                        } else {
                            visiteDao.getExistingVisiteAnomalieByIdPei(it.peiId).toMutableList()
                        },
                        numeroPei = peiDao.getNumeroPei(it.peiId),
                        photos = photoDao.getListCheminPhoto(it.peiId),

                    )
                },
            ),
            tournee.nom,
        )

        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }

    data class TourneeWithData(
        val tournee: Tournee,
        val infosVisites: List<VisiteWithAnomaliePhoto>,
    )

    data class VisiteWithAnomaliePhoto(
        val anomalies: List<Anomalie>,
        val numeroPei: String,
        val visite: Visite,
        val photos: List<String>,
    )
}
