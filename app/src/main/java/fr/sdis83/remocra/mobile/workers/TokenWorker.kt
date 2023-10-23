package fr.sdis83.remocra.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters

class TokenWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    companion object {
        private const val TAG: String = "TokenWorker"
    }

    override fun doExecute(): Result {
        // On retourne un success puisque c'est le workerRemocra qui s'occupe de vérifier la bonne connexion
        return Result.success()
    }
}
