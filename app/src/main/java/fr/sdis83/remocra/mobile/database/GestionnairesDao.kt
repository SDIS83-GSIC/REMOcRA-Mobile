package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import java.util.UUID

@Dao
abstract class GestionnairesDao {

    @Query("SELECT * FROM gestionnaire")
    abstract fun getGestionnairesList(): LiveData<List<Gestionnaire>>

    @Query("SELECT * FROM gestionnaire WHERE idGestionnaire = :idGestionnaire")
    abstract suspend fun getGestionnaireByUUID(idGestionnaire: UUID): Gestionnaire

    @Query("SELECT * FROM gestionnaire WHERE idGestionnaire = :idGestionnaire")
    abstract fun getCurrentGestionnaireByUUID(idGestionnaire: UUID? = null): LiveData<Gestionnaire?>

    @Query("SELECT * FROM contact WHERE idGestionnaire = :idGestionnaire")
    abstract fun getContactByGestionnaireUUID(idGestionnaire: UUID? = null): LiveData<List<Contact>>

    @Upsert()
    abstract suspend fun upsertGestionnaire(gestionnaire: Gestionnaire)
}
