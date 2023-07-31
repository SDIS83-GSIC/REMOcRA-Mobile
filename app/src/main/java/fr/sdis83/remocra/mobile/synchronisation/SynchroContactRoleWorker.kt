package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService

class SynchroContactRoleWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val TAG = "SynchroContactRoleWorker"

    override fun doWork(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val contactsRoles = synchronisationDao.getAllContactsRole()

        contactsRoles.forEach { role ->
            val res = retrofitBuilder.postContactsRole(
                idContact = role.idContact,
                idRoleRemocra = role.idRole,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.message())
            }
        }
        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }
}
