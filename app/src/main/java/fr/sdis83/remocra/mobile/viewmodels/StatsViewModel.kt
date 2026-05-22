package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val visiteDao = RemocraDatabase.getInstance(getApplication()).visiteDao()
    private val tourneesDao = RemocraDatabase.getInstance(getApplication()).tourneesDao()

    val visiteCount = visiteDao.getPeiVisiteCount()
    val lPeiTourneeCount = visiteDao.getLPeiTourneeCount()
    val tourneeNotDoneCount = tourneesDao.getTourneeNotDoneCount()
    val tourneeCount = tourneesDao.getTourneeCount()
    val PeiCreesCount = tourneesDao.getPeiCreesCount()
    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()
}
