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
            val res = retrofitBuilder.postContact(
                contactId = contact.contactId,
                gestionnaireId = contact.gestionnaireId,
                nom = contact.contactNom ?: "",
                prenom = contact.contactPrenom ?: "",
                fonction = contact.contactFonctionContactId,
                civilite = contact.contactCivilite?.name ?: "",
                codePostal = contact.contactCodePostal ?: "",
                voie = contact.contactVoieText ?: "",
                suffixeVoie = contact.contactSuffixeVoie,
                lieuDit = contact.contactLieuDitText,
                numeroVoie = contact.contactNumeroVoie,
                pays = contact.contactPays ?: "",
                telephone = contact.contactTelephone,
                ville = contact.contactCommuneText ?: "",
                email = contact.contactEmail ?: "",
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
