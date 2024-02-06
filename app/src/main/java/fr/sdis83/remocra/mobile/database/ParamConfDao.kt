package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
abstract class ParamConfDao {

    @Query("SELECT paramConf.* from paramConf")
    abstract fun getParamConfList(): LiveData<List<ParamConf>>

    @Upsert()
    abstract fun upsertParamConf(paramConf: ParamConf)

    @Query("DELETE FROM paramConf where cle =:cle")
    abstract fun deleteParamConf(cle: String)
}
