package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra

class SynchroContactWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchronisationContactWorker"

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val contacts = synchronisationDao.getAllContacts()

        contacts.forEach { contact ->
            if (contact.idGestionnaire == null) {
                throw IllegalArgumentException("Le gestionnaire ne peut pas être nul.")
            }
            val res = retrofitBuilder.postContact(
                idContact = contact.idContact,
                idRemocra = contact.idRemocra,
                idGestionnaire = contact.idGestionnaire,
                nom = contact.nom ?: "",
                prenom = contact.prenom ?: "",
                fonction = contact.fonction,
                civilite = contact.civilite?.name ?: "",
                codePostal = contact.codePostal ?: "",
                voie = contact.voie ?: "",
                suffixeVoie = contact.suffixeVoie,
                lieuDit = contact.lieuDit,
                numeroVoie = contact.numeroVoie,
                pays = contact.pays ?: "",
                telephone = contact.telephone,
                ville = contact.ville ?: "",
                email = contact.email ?: "",
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
