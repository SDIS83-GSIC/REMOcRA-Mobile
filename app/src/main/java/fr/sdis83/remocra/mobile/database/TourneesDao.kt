package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

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
    abstract fun getTourneesDisponiblesLiveData() : LiveData<List<TourneeDispo>>

    @Query("SELECT * FROM tourneeDispo where choisie = 1")
    abstract fun getTourneesAReserver() : List<TourneeDispo>

    @Update
    abstract suspend fun updateTourneeDispo(tourneeDispo: TourneeDispo)

    @Query("SELECT * FROM hydrant")
    abstract fun getHydrants() : List<Hydrant>

    @Insert
    abstract fun insertTournee(tournee: Tournee)
    @Insert
    abstract fun insertLienHydrantTournee(hydrantTournee: HydrantTournee)
}