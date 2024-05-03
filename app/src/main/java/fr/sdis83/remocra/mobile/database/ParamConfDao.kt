package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.sdis83.remocra.mobile.utils.GlobalConstants

@Dao
abstract class ParamConfDao {

    @Query("SELECT paramConf.* from paramConf")
    abstract fun getParamConfList(): LiveData<List<ParamConf>>

    @Query("SELECT paramConf.valeur from paramConf where cle = :mdpAdmin")
    abstract fun getMdpAdministrateur(mdpAdmin: String = GlobalConstants.MDP_ADMINISTRATEUR): LiveData<String?>

    @Query("UPDATE paramConf set valeur = :valeur where cle = :cle")
    abstract fun updateParamConf(cle: String, valeur: String)

    @Insert
    abstract fun insertParamConf(paramConf: ParamConf)

    @Query("DELETE FROM paramConf where cle =:cle")
    abstract fun deleteParamConf(cle: String)

    @Query("SELECT paramConf.valeur from paramConf where cle = :constantCle")
    abstract fun getAffichageIndispo(constantCle: String = GlobalConstants.AFFICHAGE_INDISPO): LiveData<String?>

    @Query("SELECT paramConf.valeur from paramConf where cle = :constantCle")
    abstract fun getAffichageSymbolesNormalises(constantCle: String = GlobalConstants.AFFICHAGE_SYMBOLES_NORMALISES): LiveData<String?>
}
