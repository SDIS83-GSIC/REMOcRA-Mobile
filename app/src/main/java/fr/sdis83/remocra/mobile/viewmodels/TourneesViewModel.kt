package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.RemocraDatabase

class TourneesViewModel(application: Application) : AndroidViewModel(application) {

    val tourneeDao = RemocraDatabase.getInstance(application).tourneesDao()

    val tourneeList = tourneeDao.getTourneeList()

    companion object {
        private const val TAG: String = "TourneesViewModel"
    }
}
