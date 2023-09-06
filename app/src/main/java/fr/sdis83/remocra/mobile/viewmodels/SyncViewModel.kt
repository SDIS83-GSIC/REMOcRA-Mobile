package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.RemocraDatabase

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val hydrantVisiteDao = RemocraDatabase.getInstance(getApplication()).hydrantVisiteDao()
    private val tourneesDao = RemocraDatabase.getInstance(getApplication()).tourneesDao()

    val hydrantVisiteCount = hydrantVisiteDao.getHydrantVisiteCount()
    val hydrantTourneeCount = hydrantVisiteDao.getHydrantTourneeCount()
    val tourneeNotDoneCount = tourneesDao.getTourneeNotDoneCount()
    val tourneeCount = tourneesDao.getTourneeCount()
}
