package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.database.TourneeDao.TourneePeiAvancement
import java.util.UUID

class TourneeViewModel(application: Application, tourneeId: UUID) : AndroidViewModel(application) {

    val tourneeDao = RemocraDatabase.getInstance(application).tourneeDao()
    val tourneesDao = RemocraDatabase.getInstance(application).tourneesDao()

    val peiList: LiveData<List<TourneePeiAvancement>> =
        tourneeDao.getPeiByTournee(tourneeId)

    val tourneeData: LiveData<Tournee> = tourneeDao.getTournee(tourneeId)

    val tourneesData = tourneesDao.getTourneeList()

    companion object {
        private const val TAG: String = "TourneesViewModel"
    }
}
