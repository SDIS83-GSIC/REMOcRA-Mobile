package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.Parametre
import fr.sdis83.remocra.mobile.database.RemocraDatabase

class ParametreViewModel(application: Application) : AndroidViewModel(application) {

    val parametreDao = RemocraDatabase.getInstance(application).parametreDao()

    val parametres: LiveData<List<Parametre>> = parametreDao.getParametreList()
    val mdpAdmin: LiveData<String?> = parametreDao.getMdpAdministrateur()

    val paramAffichageIndispo: LiveData<String?> = parametreDao.getAffichageIndispo()
    val paramAffichageSymbolesNormalises: LiveData<String?> = parametreDao.getAffichageSymbolesNormalises()

    companion object {
        private const val TAG: String = "ParametreViewModel"
    }
}
