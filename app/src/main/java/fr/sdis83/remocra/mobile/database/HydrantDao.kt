package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import java.util.UUID

@Dao
abstract class HydrantDao {

    @Query(
        """
        SELECT h.* FROM hydrant h WHERE h.idRemocra IS NOT NULL
        """,
    )
    abstract fun getHydrantList(): LiveData<List<Hydrant>>

    @Query(
        """
        SELECT h.* FROM hydrant h WHERE h.idRemocra IS NULL
        """,
    )
    abstract fun getNewHydrantList(): LiveData<List<Hydrant>>

    @Query(
        """
        SELECT h.*, t.* FROM tournee t
        JOIN hydrantTournee ht ON ht.idRemocraTournee = t.idRemocra
        JOIN hydrant h ON h.idRemocra = ht.idRemocraHydrant
        """,
    )
    abstract fun getTourneeMap(): LiveData<Map<Tournee, List<Hydrant>>>

    @Query("SELECT h.* FROM hydrant h WHERE h.idHydrant = :idHydrant ")
    abstract fun getHydrantByIdHydrant(idHydrant: UUID): Hydrant

    @Query("SELECT h.* FROM hydrant h WHERE h.idHydrant = :idHydrant ")
    abstract fun getHydrantByIdHydrantLiveData(idHydrant: UUID): LiveData<Hydrant>

    @Query("SELECT IFNULL(MAX(cast(h.numero AS int)), 0) FROM hydrant h WHERE h.idRemocra IS NULL")
    abstract suspend fun getLatestCreated(): Long

    @Insert
    abstract suspend fun insertHydrant(hydrant: Hydrant)

    @Query("SELECT h.* FROM hydrant h WHERE h.idRemocra IS NULL")
    abstract fun getHydrantCreatedList(): LiveData<List<HydrantCreated>>

    data class HydrantCreated(
        @Embedded
        val hydrant: Hydrant,
        @Relation(
            parentColumn = "idNature",
            entityColumn = "idRemocra",
        ) val hydrantNature: TypeHydrantNature,
        @Relation(
            parentColumn = "idNatureDeci",
            entityColumn = "idRemocra",
        ) val hydrantNatureDeci: TypeHydrantNatureDeci,
    )

    @Query("DELETE FROM hydrant WHERE idHydrant = :idHydrant")
    abstract suspend fun deleteHydrant(idHydrant: UUID)
}
