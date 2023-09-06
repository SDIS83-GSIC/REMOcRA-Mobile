package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

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

    @Query("SELECT * FROM tourneeDispo")
    abstract fun getTourneesDisponiblesLiveData(): LiveData<List<TourneeDispo>>

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
        SELECT t.*, COUNT(hv.idHydrantVisite) AS doneCount FROM tournee t
        LEFT JOIN hydrantVisite hv ON hv.idTournee = t.idTournee
        GROUP BY t.idTournee
        """,
    )
    abstract fun getTourneeList(): LiveData<List<TourneeAvancement>>

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
        GROUP BY t.idRemocra
        -- pas optimal de passer par hydrantCount mais ça passe
        HAVING t.hydrantCount !=
          (SELECT count(*) FROM hydrantVisite hv WHERE hv.idTournee = t.idTournee AND hv.statut == 'TERMINE')
         """,
    )
    abstract fun getTourneeDoneCount(): LiveData<Int>

    @Query("SELECT count(*) FROM tournee t")
    abstract fun getTourneeCount(): LiveData<Int>
}
