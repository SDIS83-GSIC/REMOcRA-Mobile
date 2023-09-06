package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.database.TourneeDao.TourneeHydrantAvancement
import org.osmdroid.util.BoundingBox
import java.util.UUID

class TourneeViewModel(application: Application, idTournee: UUID) : AndroidViewModel(application) {

    val tourneeDao = RemocraDatabase.getInstance(application).tourneeDao()
    val tourneesDao = RemocraDatabase.getInstance(application).tourneesDao()

    val hydrantList: LiveData<List<TourneeHydrantAvancement>> =
        tourneeDao.getHydrantByTournee(idTournee)

    val tourneeData: LiveData<Tournee> = tourneeDao.getTournee(idTournee)

    val tourneesData = tourneesDao.getTourneeList()

    val tourneeBoundingBox: BoundingBox?
        get() = hydrantList.value?.let {
            BoundingBox(
                it.minOf { h -> h.hydrant.lat },
                it.maxOf { h -> h.hydrant.lon },
                it.maxOf { h -> h.hydrant.lat },
                it.minOf { h -> h.hydrant.lon },
            )
        }

    companion object {
        private const val TAG: String = "TourneesViewModel"
    }
}
