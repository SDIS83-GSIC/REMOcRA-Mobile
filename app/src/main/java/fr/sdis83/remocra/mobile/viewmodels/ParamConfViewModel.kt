package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.ParamConf
import fr.sdis83.remocra.mobile.database.RemocraDatabase

class ParamConfViewModel(application: Application) : AndroidViewModel(application) {

    val paramConfDao = RemocraDatabase.getInstance(application).paramConfDao()

    val paramsConf: LiveData<List<ParamConf>> = paramConfDao.getParamConfList()
    val mdpAdmin: LiveData<String?> = paramConfDao.getMdpAdministrateur()

    val paramAffichageIndispo: LiveData<String?> = paramConfDao.getAffichageIndispo()
    val paramAffichageSymbolesNormalises: LiveData<String?> = paramConfDao.getAffichageSymbolesNormalises()

    companion object {
        private const val TAG: String = "ParamConfViewModel"
    }
}
