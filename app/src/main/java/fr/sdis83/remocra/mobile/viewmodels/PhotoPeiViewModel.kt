package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import fr.sdis83.remocra.mobile.database.PhotoPei
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.thread

class PhotoPeiViewModel(application: Application, peiId: UUID) : AndroidViewModel(application) {

    companion object {
        private const val TAG: String = "PhotoPeiViewModel"
    }
    val peiId = peiId

    val photoPeiDao = RemocraDatabase.getInstance(application).photoPeiDao()

    val photos = photoPeiDao.getListPhotoPei(peiId)

    suspend fun deletePhotoPei(photoPei: PhotoPei) = photoPeiDao.deletePhotoPei(photoPei)

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.getExternalFilesDir(null)?.let {
            File(it, "remocra").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
    }

    fun onPictureTaken(bitmap: Bitmap) {
        val now = ZonedDateTime.now()
        val file = File(
            getOutputDirectory(getApplication<Application>().applicationContext),
            now.format(
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd-HH-mm-ss-SSS",
                    Locale.FRANCE,
                ),
            ) + ".jpg",
        )

        FileOutputStream(file).use { dest ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, dest)
        }

        // TODO refaire sans le thread => mais cercle vicieux : besoin d'un suspens
        //  mais onTakePicture ne doit pas l'être dans camera
        thread {
            photoPeiDao.insertPhotoPei(
                PhotoPei(
                    photoId = UUID.randomUUID(),
                    peiId = peiId,
                    datePhoto = now,
                    path = file.absolutePath,
                ),
            )
        }
    }
}
