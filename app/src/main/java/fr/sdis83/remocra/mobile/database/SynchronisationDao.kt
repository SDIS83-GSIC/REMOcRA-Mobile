package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class SynchronisationDao {

    @Query("SELECT * FROM gestionnaire where edited = 1")
    abstract fun getAllGestionnaire(): List<Gestionnaire>

    @Query("SELECT * FROM contact where edited = 1")
    abstract fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM contactRole join contact on contactRole.idContact = contact.idContact where contact.edited = 1")
    abstract fun getAllContactsRole(): List<ContactRole>

    @Query("SELECT * FROM hydrant where idRemocra is null")
    abstract fun getAllNewHydrants(): List<Hydrant>

    @Query("SELECT * FROM hydrantVisite where statut = :statutFini")
    abstract fun getAllHydrantVisite(statutFini: HydrantVisite.HydrantVisiteStatut = HydrantVisite.HydrantVisiteStatut.TERMINE): List<HydrantVisite>

    @Query("SELECT * FROM hydrant")
    abstract fun getAllHydrant(): List<Hydrant>
}
