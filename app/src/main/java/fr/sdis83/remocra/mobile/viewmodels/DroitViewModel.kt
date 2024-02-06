package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.TypeDroit

class DroitViewModel(application: Application) : AndroidViewModel(application) {

    val droitDao = RemocraDatabase.getInstance(application).droitDao()

    val typesDroit: LiveData<List<TypeDroit>> = droitDao.getTypeDroitList()

    companion object {
        private const val TAG: String = "DroitViewModel"
    }
}
