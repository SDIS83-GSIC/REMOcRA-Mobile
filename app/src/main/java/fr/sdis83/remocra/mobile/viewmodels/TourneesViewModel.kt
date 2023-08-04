package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Tournee
import fr.sdis83.remocra.mobile.workers.TokenWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TourneesViewModel(application: Application) : AndroidViewModel(application) {

    val tourneeDao = RemocraDatabase.getInstance(application).tourneesDao()

    val tourneeList = tourneeDao.getTourneeList()

    companion object {
        private const val TAG: String = "TourneesViewModel"
    }
}
