package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import java.util.UUID

@Dao
abstract class ContactsDao {

    @Query("SELECT * FROM contact")
    abstract fun getContactsList(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact WHERE gestionnaireId = :gestionnaireId")
    abstract suspend fun getContactsByGestionnaireUUID(gestionnaireId: UUID): Contact

    @Query("SELECT * FROM contact WHERE contactId = :contactId")
    @Transaction
    abstract suspend fun getContactByUUID(contactId: UUID? = null): ContactWithRoles

    @Query("SELECT * FROM contact WHERE contactId = :contactId")
    abstract fun getCurrentContactByUUID(contactId: UUID? = null): LiveData<Contact?>

    @Query("SELECT * FROM gestionnaire WHERE gestionnaireId = :gestionnaireId")
    abstract fun getAppartenanceInfoByGestUUID(gestionnaireId: UUID): LiveData<Gestionnaire>

    @Query("UPDATE gestionnaire SET edited = 1 WHERE gestionnaireId = :gestionnaireId")
    abstract suspend fun updateGestionnaireIsFlaged(gestionnaireId: UUID)

    @Upsert
    abstract suspend fun upsertContact(contact: Contact)

    @Query("SELECT * FROM role")
    abstract fun getRolesList(): LiveData<List<Role>>

    @Query("SELECT * FROM contactRole WHERE contactId = :contactId")
    abstract fun getContactRolesByUUID(contactId: UUID? = null): LiveData<List<ContactRole>>

    @Query("DELETE FROM contactRole WHERE contactId = :contactId")
    abstract suspend fun truncateContactRolesByContactUUID(contactId: UUID)

    @Query("SELECT *  FROM fonctionContact")
    abstract fun getFonctionContact(): LiveData<List<FonctionContact>>

    @Insert
    abstract suspend fun insertContactRole(contactRole: ContactRole)

    @Transaction
    open suspend fun saveContact(contactWithRoles: ContactWithRoles) {
        updateGestionnaireIsFlaged(contactWithRoles.contact.gestionnaireId)

        val contactId = contactWithRoles.contact.contactId
        upsertContact(contactWithRoles.contact.copy(edited = true))

        val listRole = contactWithRoles.roles.toList()
        truncateContactRolesByContactUUID(contactId)
        listRole.forEach { role ->
            insertContactRole(ContactRole(contactId, role.roleId))
        }
    }

    data class ContactWithRoles(
        @Embedded val contact: Contact,
        @Relation(
            parentColumn = "contactId",
            entity = Role::class,
            entityColumn = "roleId",
            associateBy = Junction(
                value = ContactRole::class,
                parentColumn = "contactId",
                entityColumn = "roleId",
            ),
        )
        val roles: MutableList<Role> = mutableListOf(),
    )
}
