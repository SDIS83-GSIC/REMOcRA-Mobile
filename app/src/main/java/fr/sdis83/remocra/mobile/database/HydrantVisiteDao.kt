package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.util.UUID

@Dao
abstract class HydrantVisiteDao {

    @Query(
        """
        SELECT hv.* FROM hydrantVisite hv
        WHERE idHydrant = :idHydrant AND idTournee = :idTournee
        """,
    )
    abstract suspend fun getCurrentVisite(
        idHydrant: UUID,
        idTournee: UUID,
    ): HydrantVisite?

    @Query(
        """
        SELECT tha.* FROM typeHydrantAnomalie tha
        JOIN hydrantAnomalie ha ON tha.idRemocra = ha.idAnomalie
        WHERE ha.idHydrant = :idHydrant
        """,
    )
    abstract suspend fun getExistingVisiteAnomalie(
        idHydrant: UUID,
    ): List<TypeHydrantAnomalie>

    @Query(
        """
        SELECT tha.* FROM typeHydrantAnomalie tha
        JOIN hydrantVisiteAnomalie hva ON tha.idRemocra = hva.idAnomalie
        JOIN hydrantVisite hv ON hv.idHydrantVisite = hva.idHydrantVisite
        WHERE hv.idHydrant = :idHydrant AND hv.idTournee = :idTournee
        """,
    )
    abstract suspend fun getCurrentVisiteAnomalie(
        idHydrant: UUID,
        idTournee: UUID,
    ): List<TypeHydrantAnomalie>

    data class HydrantVisiteWithAnomalies(
        val hydrantVisite: HydrantVisite,
        val anomalies: MutableList<TypeHydrantAnomalie> = mutableListOf(),
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHydrantVisite(hydrantVisite: HydrantVisite)

    @Query("DELETE FROM hydrantVisiteAnomalie WHERE idHydrantVisite = :idHydrantVisite")
    abstract suspend fun truncateAnomalies(idHydrantVisite: UUID)

    @Insert
    abstract fun insertHydrantVisiteAnomalie(hydrantVisiteAnomalie: HydrantVisiteAnomalie)

    @Transaction
    open suspend fun upsertHydrantVisite(input: HydrantVisiteWithAnomalies) {
        truncateAnomalies(input.hydrantVisite.idHydrantVisite)
        insertHydrantVisite(input.hydrantVisite)
        if (input.hydrantVisite.hasAnomalieChanges) {
            input.anomalies.forEach {
                insertHydrantVisiteAnomalie(
                    HydrantVisiteAnomalie(
                        input.hydrantVisite.idHydrantVisite,
                        it.idRemocra,
                    ),
                )
            }
        }
    }

    @Query("SELECT count(*) FROM hydrantVisite hv WHERE hv.statut = 'TERMINE'")
    abstract fun getHydrantVisiteCount(): LiveData<Int>

    @Query("SELECT count(*) FROM hydrantTournee ht")
    abstract fun getHydrantTourneeCount(): LiveData<Int>
}
