package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class ReferentielDao {

    @Insert
    abstract fun insertHydrant(hydrant: Hydrant)

    @Insert
    abstract fun insertContact(contact: Contact)

    @Insert
    abstract fun insertGestionnaire(gestionnaire: Gestionnaire)

    @Query("DELETE FROM hydrant")
    abstract fun truncateHydrant()

    @Query("DELETE FROM gestionnaire")
    abstract fun truncateGestionnaire()

    @Query("DELETE FROM contact")
    abstract fun truncateContact()
}
