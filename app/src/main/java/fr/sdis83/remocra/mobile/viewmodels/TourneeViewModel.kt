package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.database.TourneeDao.TourneeHydrantAvancement
import java.util.UUID

class TourneeViewModel(application: Application, idTournee: UUID) : AndroidViewModel(application) {

    val tourneeDao = RemocraDatabase.getInstance(application).tourneeDao()

    val hydrantList: LiveData<List<TourneeHydrantAvancement>> =
        tourneeDao.getHydrantByTournee(idTournee)

    val tourneeData: LiveData<Tournee> = tourneeDao.getTournee(idTournee)

    companion object {
        private const val TAG: String = "TourneesViewModel"
    }
}
