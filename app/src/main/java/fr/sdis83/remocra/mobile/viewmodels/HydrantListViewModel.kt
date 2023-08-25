package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import java.util.UUID

class HydrantListViewModel(application: Application) : AndroidViewModel(application) {

    private val hydrantDao = RemocraDatabase.getInstance(application).hydrantDao()

    val hydrantList = hydrantDao.getHydrantCreatedList()

    suspend fun deleteHydrant(idHydrant: UUID) {
        hydrantDao.deleteHydrant(idHydrant)
    }

    companion object {
        private const val TAG: String = "HydrantListViewModel"
    }
}
