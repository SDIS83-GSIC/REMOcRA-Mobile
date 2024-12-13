package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.sdis83.remocra.mobile.utils.GlobalConstants

@Dao
abstract class ParametreDao {

    @Query("SELECT * from parametre")
    abstract fun getParametreList(): LiveData<List<Parametre>>

    @Query("SELECT parametreValeur from parametre where parametreCode = :mdpAdmin")
    abstract fun getMdpAdministrateur(mdpAdmin: String = GlobalConstants.MDP_ADMINISTRATEUR): LiveData<String?>

    @Query("UPDATE parametre set parametreValeur = :valeur where parametreCode = :cle")
    abstract fun updateParametre(cle: String, valeur: String)

    @Insert
    abstract fun insertParamConf(parametre: Parametre)

    @Query("DELETE FROM parametre where parametreCode =:cle")
    abstract fun deleteParametre(cle: String)

    @Query("SELECT parametreValeur from parametre where parametreCode = :constantCle")
    abstract fun getAffichageIndispo(constantCle: String = GlobalConstants.AFFICHAGE_INDISPO): LiveData<String?>

    @Query("SELECT parametreValeur from parametre where parametreCode = :constantCle")
    abstract fun getAffichageSymbolesNormalises(constantCle: String = GlobalConstants.AFFICHAGE_SYMBOLES_NORMALISES): LiveData<String?>
}
