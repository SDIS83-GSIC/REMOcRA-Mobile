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

    @Query("DELETE FROM tournee")
    abstract fun truncateTournee()

    @Query("DELETE FROM hydrantTournee")
    abstract fun truncateHydrantTournee()

    @Query("SELECT * FROM tourneeDispo where choisie = 1")
    abstract fun getTourneesAReserver(): List<TourneeDispo>

    @Update
    abstract suspend fun updateTourneeDispo(tourneeDispo: TourneeDispo)

    @Query("SELECT * FROM hydrant")
    abstract fun getHydrants(): List<Hydrant>

    @Insert
    abstract fun insertTournee(tournee: Tournee)

    @Insert
    abstract fun insertLienHydrantTournee(hydrantTournee: HydrantTournee)

    @Query(
        """
        SELECT t.*, doneCount FROM tournee t
        LEFT JOIN  (select idTournee,  COUNT(hydrantVisite.idHydrantVisite)AS doneCount
            from hydrantVisite where statut = :terminee group by  idTournee) as c on t.idTournee = c.idTournee
        GROUP BY t.idTournee
        """,
    )
    abstract fun getTourneeList(
        terminee: HydrantVisite.HydrantVisiteStatut =
            HydrantVisite.HydrantVisiteStatut.TERMINE,
    ): LiveData<List<TourneeAvancement>>

    data class TourneeAvancement(
        @Embedded val tournee: Tournee,
        val doneCount: Int,
    ) {
        val progression: Float
            get() = if (tournee.hydrantCount > 0) (doneCount.toFloat() / tournee.hydrantCount) else 0.0f
    }

    @Query(
        """
        SELECT count(*) FROM tournee t
        -- pas optimal de passer par hydrantCount mais ça passe
        WHERE t.hydrantCount >
          (SELECT count(*) FROM hydrantVisite hv WHERE hv.idTournee = t.idTournee AND hv.statut == 'TERMINE')
         """,
    )
    abstract fun getTourneeNotDoneCount(): LiveData<Int>

    @Query("SELECT count(*) FROM tournee t")
    abstract fun getTourneeCount(): LiveData<Int>

    @Query(
        """
        SELECT count(*) FROM hydrant
        WHERE hydrant.idRemocra is null
         """,
    )
    abstract fun getHydrantsCreesCount(): LiveData<Int>

    @Query("SELECT * FROM tourneeDispo t WHERE t.nom LIKE '%' || :search || '%' ")
    abstract fun getTourneeDisponibleFiltree(search: String): Flow<List<TourneeDispo>>
}
