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
    abstract fun getContactsList() : LiveData<List<Contact>>

    @Query("SELECT * FROM contact WHERE idGestionnaire = :idGestionnaire")
    abstract suspend fun getContactsByGestionnaireUUID(idGestionnaire: UUID) : Contact

    @Query("SELECT * FROM contact WHERE idContact = :idContact")
    abstract suspend fun getContactByUUID(idContact: UUID? = null) : ContactWithRoles

    @Query("SELECT * FROM contact WHERE idContact = :idContact")
    abstract fun getCurrentContactByUUID(idContact: UUID? = null) : LiveData<Contact?>

    @Query("SELECT * FROM gestionnaire WHERE idGestionnaire = :idGestionnaire")
    abstract fun getAppartenanceInfoByGestUUID(idGestionnaire: UUID) : LiveData<Gestionnaire>

    @Query("UPDATE gestionnaire SET edited = 1 WHERE idGestionnaire = :idGestionnaire")
    abstract suspend fun updateGestionnaireIsFlaged(idGestionnaire: UUID)

    @Upsert
    abstract suspend fun upsertContact(contact: Contact)

    @Query("SELECT * FROM role")
    abstract fun getRolesList(): LiveData<List<Role>>

    @Query("SELECT * FROM contactRole WHERE idContact = :idContact")
    abstract fun getContactRolesByUUID(idContact: UUID? = null) : LiveData<List<ContactRole>>

    @Query("DELETE FROM contactRole WHERE idContact = :idContact")
    abstract suspend fun truncateContactRolesByContactUUID(idContact: UUID)

    @Insert
    abstract suspend fun insertContactRole(contactRole: ContactRole)
    
    @Transaction
    open suspend fun saveContact(contactWithRoles: ContactWithRoles) {
        updateGestionnaireIsFlaged(contactWithRoles.contact.idGestionnaire!!)

        val idContact = contactWithRoles.contact.idContact
        upsertContact(contactWithRoles.contact.copy(edited = true))

        val listRole = contactWithRoles.roles.toList()
        truncateContactRolesByContactUUID(idContact)
        listRole.forEach { role ->
            insertContactRole(ContactRole(idContact, role.idRemocra))
        }
    }

    data class ContactWithRoles(
        @Embedded val contact: Contact,
        @Relation(
            parentColumn = "idContact",
            entity = Role::class,
            entityColumn = "idRemocra",
            associateBy = Junction(
                value = ContactRole::class,
                parentColumn = "idContact",
                entityColumn = "idRole"
            )
        )
        val roles: MutableList<Role> = mutableListOf()
    )
}