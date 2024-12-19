package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import java.util.UUID

@Dao
abstract class TourneeDao {

    @Query(
        """
        SELECT p.*, v.statut FROM pei p
        JOIN lPeiTournee lpt ON  lpt.peiId = p.peiId
        JOIN tournee t ON t.tourneeId =  lpt.tourneeId
        LEFT JOIN visite v ON v.peiId = p.peiId AND v.tourneeId = :tourneeId
        WHERE t.tourneeId = :tourneeId
        ORDER BY lpt.ordre
        """,
    )
    @Transaction
    abstract fun getPeiByTournee(tourneeId: UUID): LiveData<List<TourneePeiAvancement>>

    data class TourneePeiAvancement(
        @Embedded val pei: Pei,
        @Relation(
            parentColumn = "natureId",
            entityColumn = "natureId",
        )
        val nature: Nature,
        var statut: String? = "",
    )

    @Query(
        """
        SELECT t.* FROM tournee t
        WHERE t.tourneeId = :tourneeId
        """,
    )
    abstract fun getTournee(tourneeId: UUID): LiveData<Tournee>

    @Query(
        """
        SELECT v.visiteId FROM visite v
        WHERE v.tourneeId = :tourneeId
        """,
    )
    abstract fun getListVisiteIdByTournee(tourneeId: UUID): List<UUID>

    @Query(
        """
        DELETE FROM lPeiTournee
        WHERE tourneeId = :tourneeId
        """,
    )
    abstract fun deletePeiTournee(tourneeId: UUID)

    @Query(
        """
        DELETE FROM lVisiteAnomalie
        WHERE visiteId in (:listVisiteId)
        """,
    )
    abstract fun deleteLVisiteAnomalie(listVisiteId: List<UUID>)

    @Query(
        """
        DELETE FROM visite
        WHERE tourneeId = :tourneeId
        """,
    )
    abstract fun deleteVisite(tourneeId: UUID)

    @Query(
        """
        DELETE FROM tournee
        WHERE tourneeId = :tourneeId
        """,
    )
    abstract fun deleteTournee(tourneeId: UUID)
}
