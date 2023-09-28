package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import fr.sdis83.remocra.mobile.utils.GlobalConstants

@Dao
abstract class AgentDao {

    @Insert
    abstract suspend fun insertAgent(composantAgent: Agent)

    @Query("SELECT valeur from paramConf where cle = :gestionAgent")
    abstract fun getMethodeGestionAgent(gestionAgent: String = GlobalConstants.GESTION_AGENT): LiveData<String>

    @Transaction
    @Insert
    abstract fun insertComposantAgent(composantAgent: Agent)

    @Query("SELECT nomAgent from agent where isUserConnecte = 1")
    abstract fun getUtilisateurConnecte(): String

    @Query("SELECT nomAgent from agent where isLastValue = 1 and numeroAgent = 1")
    abstract fun getAgent1ValeurPrecedente(): String

    @Query("SELECT nomAgent from agent where isLastValue = 1 and numeroAgent = 2")
    abstract fun getAgent2ValeurPrecedente(): String

    @Query("SELECT agent.* from agent where numeroAgent = :numeroAgent and nomAgent = :nomAgent")
    abstract suspend fun checkIfExist(nomAgent: String, numeroAgent: Int): Agent?

    @Query("UPDATE agent set isLastValue = 0 where numeroAgent = :numeroAgent")
    abstract suspend fun updateLastValue(numeroAgent: Int)

    @Update
    abstract suspend fun updateAgent(agent: Agent)

    @Query("SELECT agent.nomAgent from agent where numeroAgent = :numeroAgent")
    abstract fun getListAgent(numeroAgent: Int): LiveData<List<String>>
}
