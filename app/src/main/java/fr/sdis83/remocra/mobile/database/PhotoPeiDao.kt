package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
abstract class PhotoPeiDao {

    @Query("SELECT * from photoPei where peiId = :peiId")
    abstract fun getListPhotoPei(peiId: UUID): LiveData<List<PhotoPei>>

    @Insert
    abstract fun insertPhotoPei(photoPei: PhotoPei)

    @Delete
    abstract suspend fun deletePhotoPei(photoPei: PhotoPei)
}
