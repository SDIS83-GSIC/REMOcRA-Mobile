package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class DroitDao {

    @Query("SELECT typeDroit.* from typeDroit")
    abstract fun getTypeDroitList(): LiveData<List<TypeDroit>>
}
