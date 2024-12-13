package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.util.UUID

@Dao
abstract class VisiteDao {

    @Query(
        """
        SELECT v.* FROM visite v
        WHERE peiId = :peiId AND tourneeId = :tourneeId
        """,
    )
    abstract suspend fun getCurrentVisite(
        peiId: UUID,
        tourneeId: UUID,
    ): Visite?

    @Query(
        """
        SELECT a.* FROM anomalie a
        JOIN lPeiAnomalie lpa ON a.anomalieId = lpa.anomalieId
        WHERE lpa.peiId = :peiId
        """,
    )
    abstract suspend fun getExistingVisiteAnomalie(
        peiId: UUID,
    ): List<Anomalie>

    @Query(
        """
        SELECT a.* FROM anomalie a
        JOIN lVisiteAnomalie lva ON a.anomalieId = lva.anomalieId
        JOIN visite v ON v.visiteId = lva.visiteId
        WHERE v.peiId = :peiId AND v.tourneeId = :tourneeId
        """,
    )
    abstract suspend fun getCurrentVisiteAnomalie(
        peiId: UUID,
        tourneeId: UUID,
    ): List<Anomalie>

    data class VisiteWithAnomalies(
        val visite: Visite,
        val anomalies: MutableList<Anomalie> = mutableListOf(),
        val numeroPei: String?,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVisite(visite: Visite)

    @Query("DELETE FROM lVisiteAnomalie WHERE visiteId = :visiteId")
    abstract suspend fun truncateAnomalies(visiteId: UUID)

    @Insert
    abstract fun insertLVisiteAnomalie(lVisiteAnomalie: LVisiteAnomalie)

    @Transaction
    open suspend fun upsertVisite(input: VisiteWithAnomalies) {
        truncateAnomalies(input.visite.visiteId)
        insertVisite(input.visite)
        if (input.visite.hasAnomalieChanges) {
            input.anomalies.forEach {
                insertLVisiteAnomalie(
                    LVisiteAnomalie(
                        input.visite.visiteId,
                        it.anomalieId,
                    ),
                )
            }
        }
    }

    @Query("SELECT count(*) FROM visite WHERE statut = 'TERMINE'")
    abstract fun getPeiVisiteCount(): LiveData<Int>

    @Query("SELECT count(*) FROM lPeiTournee")
    abstract fun getLPeiTourneeCount(): LiveData<Int>
}
