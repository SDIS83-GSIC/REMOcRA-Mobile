package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
abstract class HydrantPhotoDao {

    @Query("SELECT hydrantPhoto.* from hydrantPhoto where idHydrant = :idHydrant")
    abstract fun getListHydrantPhoto(idHydrant: UUID): LiveData<List<HydrantPhoto>>

    @Insert
    abstract fun insertHydrantPhoto(hydrantPhoto: HydrantPhoto)

    @Delete
    abstract suspend fun deleteHydrantPhoto(hydrantPhoto: HydrantPhoto)
}
