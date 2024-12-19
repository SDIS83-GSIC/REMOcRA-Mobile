package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TourneesDao {

    @Insert
    abstract fun insertTourneeDispo(tourneeDispo: TourneeDispo)

    @Query("DELETE FROM tourneeDispo")
    abstract fun truncateTourneesDispos()

    @Query("SELECT * FROM tourneeDispo where choisie = 1")
    abstract fun getTourneesAReserver(): List<TourneeDispo>

    @Update
    abstract suspend fun updateTourneeDispo(tourneeDispo: TourneeDispo)

    @Insert
    abstract fun insertTournee(tournee: Tournee)

    @Insert
    abstract fun insertLPeiTournee(LPeiTournee: LPeiTournee)

    @Query(
        """
        SELECT t.*, doneCount FROM tournee t
        LEFT JOIN  (select tourneeId,  COUNT(visiteId) AS doneCount
            from visite where statut = :terminee group by  tourneeId) as c on t.tourneeId = c.tourneeId
        GROUP BY t.tourneeId
        """,
    )
    abstract fun getTourneeList(
        terminee: Visite.VisiteStatut =
            Visite.VisiteStatut.TERMINE,
    ): LiveData<List<TourneeAvancement>>

    data class TourneeAvancement(
        @Embedded val tournee: Tournee,
        val doneCount: Int,
    ) {
        val progression: Float
            get() = if (tournee.peiCount > 0) (doneCount.toFloat() / tournee.peiCount) else 0.0f
    }

    @Query(
        """
        SELECT count(*) FROM tournee t
        -- pas optimal de passer par peiCount mais ça passe
        WHERE t.peiCount >
          (SELECT count(*) FROM visite hv WHERE hv.tourneeId = t.tourneeId AND hv.statut == 'TERMINE')
         """,
    )
    abstract fun getTourneeNotDoneCount(): LiveData<Int>

    @Query("SELECT count(*) FROM tournee t")
    abstract fun getTourneeCount(): LiveData<Int>

    @Query(
        """
        SELECT count(*) FROM pei
        WHERE isNew = 1
         """,
    )
    abstract fun getPeiCreesCount(): LiveData<Int>

    @Query("SELECT * FROM tourneeDispo t WHERE t.nom LIKE '%' || :search || '%' ")
    abstract fun getTourneeDisponibleFiltree(search: String): Flow<List<TourneeDispo>>
}
