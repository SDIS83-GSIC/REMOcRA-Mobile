package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import java.util.UUID

class PeiListViewModel(application: Application) : AndroidViewModel(application) {

    private val peiDao = RemocraDatabase.getInstance(application).peiDao()

    val peiList = peiDao.getPeiCreatedList()

    suspend fun deletePei(peiId: UUID) {
        peiDao.deletePei(peiId)
    }

    companion object {
        private const val TAG: String = "PeiListViewModel"
    }
}
