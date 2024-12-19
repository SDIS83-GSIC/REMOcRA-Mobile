package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel
import java.util.UUID

@Dao
abstract class PeiDao {
    @Query(
        """
        SELECT p.lat, p.lon, p.lat as mLatitude, p.lon as mLongitude, 1 as mAltitude,
            p.peiId, p.peiNumeroComplet, p.dispoTerrestre, p.adresseComplete, p.observation,
            p.peiCaracteristiques, n.natureCode
        FROM pei p 
        JOIN nature n on n.natureId = p.natureId
        WHERE p.isNew = 0
        """,
    )
    abstract fun getPeiList(): LiveData<List<MapViewModel.PeiGeoPoint>>

    @Query(
        """
        SELECT  p.lat, p.lon, p.lat as mLatitude, p.lon as mLongitude, 1 as mAltitude,
            p.peiId, p.peiNumeroComplet, p.dispoTerrestre, p.adresseComplete, p.observation,
            p.peiCaracteristiques, n.natureCode
        FROM pei p
        JOIN nature n on n.natureId = p.natureId
        WHERE p.isNew = 1
        """,
    )
    abstract fun getNewPeiList(): LiveData<List<MapViewModel.PeiGeoPoint>>

    @Query(
        """
        SELECT p.lat, p.lon, p.lat as mLatitude, p.lon as mLongitude, 1 as mAltitude,
            p.peiId, p.peiNumeroComplet, p.dispoTerrestre, p.adresseComplete, p.observation,
            p.peiCaracteristiques, v.statut as statutVisite, n.natureCode, t.*  FROM tournee t
        JOIN lPeiTournee lpt ON lpt.tourneeId = t.tourneeId
        JOIN pei p ON p.peiId = lpt.peiId
        JOIN nature n on n.natureId = p.natureId
        LEFT JOIN visite v ON v.peiId = p.peiId
        """,
    )
    abstract fun getTourneeMap(): LiveData<Map<Tournee, List<MapViewModel.PeiGeoPoint>>>

    @Query("SELECT * FROM pei WHERE peiId = :peiId ")
    abstract fun getPeiByPeiId(peiId: UUID): Pei

    @Query(
        """
        SELECT p.lat, p.lon, p.lat as mLatitude, p.lon as mLongitude, 1 as mAltitude,
            p.peiId, p.peiNumeroComplet, p.dispoTerrestre, p.adresseComplete, p.observation,
            p.peiCaracteristiques, v.statut as statutVisite, t.tourneeId, n.natureCode FROM pei p
        LEFT JOIN visite v ON v.peiId = p.peiId
        LEFT JOIN lPeiTournee lpt on lpt.peiId = p.peiId
        LEFT JOIN tournee t on t.tourneeId = lpt.tourneeId
        JOIN nature n on n.natureId = p.natureId
        WHERE p.peiId = :peiId """,
    )
    abstract fun getPeiGeoPointByIdPei(peiId: UUID): MapViewModel.PeiGeoPoint

    @Query("SELECT * FROM pei WHERE peiId = :peiId ")
    abstract fun getPeiByPeiIdLiveData(peiId: UUID): LiveData<Pei>

    @Query("SELECT IFNULL(MAX(cast(peiNumeroComplet AS int)), 0) FROM pei WHERE isNew = 0")
    abstract suspend fun getLatestCreated(): Long

    @Insert
    abstract suspend fun insertPei(pei: Pei)

    @Transaction
    @Query("SELECT * FROM pei WHERE isNew = 1")
    abstract fun getPeiCreatedList(): LiveData<List<PeiCreated>>

    data class PeiCreated(
        @Embedded
        val pei: Pei,
        @Relation(
            parentColumn = "natureId",
            entityColumn = "natureId",
        ) val nature: Nature,
        @Relation(
            parentColumn = "natureDeciId",
            entityColumn = "natureDeciId",
        ) val natureDeci: NatureDeci,
    )

    @Query("DELETE FROM pei WHERE peiId = :peiId")
    abstract suspend fun deletePei(peiId: UUID)
}
