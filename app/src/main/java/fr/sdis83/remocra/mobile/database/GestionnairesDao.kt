package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
abstract class GestionnairesDao {

    @Query("SELECT * FROM gestionnaire g WHERE g.gestionnaireCode LIKE '%' || :search || '%' OR g.gestionnaireLibelle LIKE '%' ||:search || '%'")
    abstract fun getGestionnairesList(search: String): Flow<List<Gestionnaire>>

    @Query("SELECT * FROM gestionnaire WHERE gestionnaireId = :gestionnaireId")
    abstract suspend fun getGestionnaireByUUID(gestionnaireId: UUID): Gestionnaire

    @Query("SELECT * FROM gestionnaire WHERE gestionnaireId = :gestionnaireId")
    abstract fun getCurrentGestionnaireByUUID(gestionnaireId: UUID? = null): LiveData<Gestionnaire?>

    @Query("SELECT * FROM contact WHERE gestionnaireId = :gestionnaireId")
    abstract fun getContactByGestionnaireUUID(gestionnaireId: UUID?): LiveData<List<Contact>>

    @Upsert()
    abstract suspend fun upsertGestionnaire(gestionnaire: Gestionnaire)
}
