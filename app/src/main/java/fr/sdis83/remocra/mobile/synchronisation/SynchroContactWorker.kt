package fr.sdis83.remocra.mobile.synchronisation

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.SynchronisationService
import fr.sdis83.remocra.mobile.workers.WorkerRemocra
import java.util.UUID

class SynchroContactWorker(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {
    private val TAG = "SynchronisationContactWorker"

    companion object {
        private const val INPUT_GESTIONNAIRE_ID = "gestionnaireId"
    }

    override fun doExecute(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()
        val retrofitBuilder = SynchronisationService.getRetroFitInstance(applicationContext)

        val gestionnaireId = inputData.getString(INPUT_GESTIONNAIRE_ID)?.let(UUID::fromString)
            ?: throw IllegalArgumentException("gestionnaireId est requis")

        val contacts = synchronisationDao.getAllContactsByGestionnaire(gestionnaireId)

        contacts.forEach { contact ->
            val res = retrofitBuilder.postContact(
                contactId = contact.contactId,
                gestionnaireId = contact.gestionnaireId,
                contactNom = contact.contactNom,
                contactPrenom = contact.contactPrenom,
                contactFonctionContactId = contact.contactFonctionContactId,
                contactCivilite = contact.contactCivilite?.name?.let {
                    if (it == Contact.Civilite.M.name) {
                        "MONSIEUR"
                    } else {
                        "MADAME"
                    }
                },
                contactCodePostal = contact.contactCodePostal,
                contactVoieText = contact.contactVoieText,
                contactSuffixeVoie = contact.contactSuffixeVoie,
                contactLieuDitText = contact.contactLieuDitText,
                contactNumeroVoie = contact.contactNumeroVoie,
                contactPays = contact.contactPays,
                contactTelephone = contact.contactTelephone,
                contactCommuneText = contact.contactCommuneText,
                contactEmail = contact.contactEmail,
            ).execute()

            when (res.code()) {
                200, 201, 409 -> Unit
                else -> throw IllegalArgumentException(res.errorBody()?.string())
            }
        }
        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        failureWithError(e, "Erreur lors de la synchronisation du contact")
    }
}
