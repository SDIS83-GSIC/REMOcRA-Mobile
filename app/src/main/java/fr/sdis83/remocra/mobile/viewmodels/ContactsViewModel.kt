package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.ContactsDao
import fr.sdis83.remocra.mobile.database.FonctionContact
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ContactsViewModel(application: Application, contactId: UUID?, gestionnaireId: UUID) :
    AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "ContactsViewModel"
    }

    private val contactsDao = RemocraDatabase.getInstance(application).contactsDao()
    val contact: LiveData<Contact?> = contactsDao.getCurrentContactByUUID(contactId)
    val gestAppartenance: LiveData<Gestionnaire> =
        contactsDao.getAppartenanceInfoByGestUUID(gestionnaireId)

    val listFonctionContact: LiveData<List<FonctionContact>> = contactsDao.getFonctionContact()

    val roleList: LiveData<List<Role>> = contactsDao.getRolesList()

    suspend fun upsertContactWithRoles(contactWithRoles: ContactsDao.ContactWithRoles) {
        contactsDao.saveContact(contactWithRoles)
    }

    private val _contactState = MutableStateFlow(
        ContactsDao.ContactWithRoles(
            Contact(
                contactId = UUID.randomUUID(),
                gestionnaireId = gestionnaireId,
                contactCivilite = null,
                contactNom = null,
                contactPrenom = null,
                contactNumeroVoie = null,
                contactSuffixeVoie = null,
                contactLieuDitText = null,
                contactVoieText = null,
                contactCodePostal = null,
                contactCommuneText = null,
                contactPays = null,
                contactTelephone = null,
                contactEmail = null,
                contactFonctionContactId = null,
            ),
        ),
    )

    var contactState: StateFlow<ContactsDao.ContactWithRoles> = _contactState.asStateFlow()

    private suspend fun loadData(contactId: UUID?, gestionnaireId: UUID) {
        if (contactId != null) {
            _contactState.value = contactsDao.getContactByUUID(contactId)
        } else {
            _contactState.value = ContactsDao.ContactWithRoles(
                Contact(
                    contactId = UUID.randomUUID(),
                    gestionnaireId = gestionnaireId,
                    contactCivilite = null,
                    contactNom = null,
                    contactPrenom = null,
                    contactNumeroVoie = null,
                    contactSuffixeVoie = null,
                    contactLieuDitText = null,
                    contactVoieText = null,
                    contactCodePostal = null,
                    contactCommuneText = null,
                    contactPays = null,
                    contactTelephone = null,
                    contactEmail = null,
                    contactFonctionContactId = null,
                ),
            )
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadData(contactId, gestionnaireId)
        }
    }

    fun updateForm(contactWithRoles: ContactsDao.ContactWithRoles) {
        _contactState.value = contactWithRoles
    }
}
