package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.sdis83.remocra.mobile.database.RemocraDatabase

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    val agentDao = RemocraDatabase.getInstance(application).agentDao()

    val listAgent1: LiveData<List<String>> = agentDao.getListAgent(1)

    val listAgent2: LiveData<List<String>> = agentDao.getListAgent(2)

    val gestionAgents = agentDao.getMethodeGestionAgent()

    companion object {
        private const val TAG: String = "AgentViewModel"
    }
}
