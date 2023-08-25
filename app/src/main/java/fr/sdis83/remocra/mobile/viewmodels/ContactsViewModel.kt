package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.ContactsDao
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ContactsViewModel(application: Application, idContact: UUID?, idGestionnaire: UUID) :
    AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "ContactsViewModel"
    }

    private val contactsDao = RemocraDatabase.getInstance(application).contactsDao()
    val contact: LiveData<Contact?> = contactsDao.getCurrentContactByUUID(idContact)
    val gestAppartenance: LiveData<Gestionnaire> =
        contactsDao.getAppartenanceInfoByGestUUID(idGestionnaire)

    val roleList: LiveData<List<Role>> = contactsDao.getRolesList()

    suspend fun upsertContactWithRoles(contactWithRoles: ContactsDao.ContactWithRoles) {
        contactsDao.saveContact(contactWithRoles)
    }

    private val _contactState = MutableStateFlow(
        ContactsDao.ContactWithRoles(
            Contact(
                idRemocra = null,
                idGestionnaire = null,
                idRemocraGestionnaire = null,
                fonction = null,
                civilite = null,
                nom = null,
                prenom = null,
                numeroVoie = null,
                suffixeVoie = null,
                voie = null,
                lieuDit = null,
                codePostal = null,
                ville = null,
                pays = null,
                telephone = null,
                email = null,
            ),
        ),
    )

    var contactState: StateFlow<ContactsDao.ContactWithRoles> = _contactState.asStateFlow()

    private suspend fun loadData(idContact: UUID?, idGestionnaire: UUID) {
        if (idContact != null) {
            _contactState.value = contactsDao.getContactByUUID(idContact)
        } else {
            _contactState.value = ContactsDao.ContactWithRoles(
                Contact(
                    idRemocra = null,
                    idGestionnaire = idGestionnaire,
                    idRemocraGestionnaire = null,
                    fonction = null,
                    civilite = null,
                    nom = null,
                    prenom = null,
                    numeroVoie = null,
                    suffixeVoie = null,
                    voie = null,
                    lieuDit = null,
                    codePostal = null,
                    ville = null,
                    pays = null,
                    telephone = null,
                    email = null,
                ),
            )
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData(idContact, idGestionnaire)
        }
    }

    fun updateForm(contactWithRoles: ContactsDao.ContactWithRoles) {
        _contactState.value = contactWithRoles
    }
}
