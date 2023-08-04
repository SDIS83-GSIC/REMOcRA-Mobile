package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Query
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
}
