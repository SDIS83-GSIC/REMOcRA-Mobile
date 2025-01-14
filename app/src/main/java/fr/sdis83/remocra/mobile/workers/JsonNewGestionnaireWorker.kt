package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.ContactRole
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.utils.jsonToFile

/**
 * Permet de générer un fichier json par tournée. A chaque fois qu'un opération CRUD est faite, on
 * stocke les informations dans un fichier. si le fichier existe on l'écrase sinon on le crée.
 */
class JsonNewGestionnaireWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "JsonNewGestionnaireWorker"
    }

    override fun doWork(): Result = try {
        val synchronisationDao = RemocraDatabase.getInstance(applicationContext).synchronisationDao()

        val gestionnaires = synchronisationDao.getAllGestionnaire()

        val contacts = synchronisationDao.getAllContacts()

        val contactsRole = synchronisationDao.getAllContactsRole()

        jsonToFile(
            gestionnaires.map {
                GestionnaireWithContact(
                    gestionnaire = it,
                    listeContactWithRole = contacts.map {
                        ContactWithRole(
                            it,
                            contactsRole.filter { cr -> cr.contactId == it.contactId },
                        )
                    },
                )
            },
            "new_gestionnaire",
        )

        Result.success()
    } catch (e: Throwable) {
        Log.e(TAG, "Error executing work: " + e.message, e)
        Result.failure()
    }

    data class GestionnaireWithContact(
        val gestionnaire: Gestionnaire,
        val listeContactWithRole: List<ContactWithRole>,
    )

    data class ContactWithRole(
        val contact: Contact,
        val listeContactRole: List<ContactRole>,
    )
}
