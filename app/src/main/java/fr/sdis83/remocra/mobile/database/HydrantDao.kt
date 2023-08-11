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
        SELECT h.* FROM hydrant h
        WHERE h.lat <= :north AND h.lat >= :south AND h.lon >= :west AND h.lon <= :east
        """
    )
    abstract fun getHydrantInBoundingBox(
        north: Double,
        south: Double,
        west: Double,
        east: Double
    ): List<Hydrant>

    @Query("SELECT h.* FROM hydrant h WHERE h.idHydrant = :idHydrant ")
    abstract fun getHydrantByIdHydrant(idHydrant: UUID): Hydrant

    @Insert
    abstract suspend fun insertHydrant(hydrant: Hydrant)

    @Query("SELECT h.* FROM hydrant h WHERE h.idRemocra IS NULL")
    abstract fun getHydrantCreatedList(): LiveData<List<HydrantCreated>>

    data class HydrantCreated(
        @Embedded
        val hydrant: Hydrant,
        @Relation(
            parentColumn = "idNature",
            entityColumn = "idRemocra"
        ) val hydrantNature: TypeHydrantNature,
        @Relation(
            parentColumn = "idNatureDeci",
            entityColumn = "idRemocra"
        ) val hydrantNatureDeci: TypeHydrantNatureDeci,
    )

    @Query("DELETE FROM hydrant WHERE idHydrant = :idHydrant")
    abstract suspend fun deleteHydrant(idHydrant: UUID)
}
