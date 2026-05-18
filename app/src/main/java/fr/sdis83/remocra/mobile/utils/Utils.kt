package fr.sdis83.remocra.mobile.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.google.gson.GsonBuilder
import fr.sdis83.remocra.mobile.serializer.ZonedDateTimeSerializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun createImageFormData(name: String, path: String): MultipartBody.Part? =
    MultipartBody.Part.createFormData(
        name,
        path.split("/").last(),
        File(path).asRequestBody("image/jpeg".toMediaType()),
    ).takeIf { File(path).exists() }

fun getVersionName(applicationContext: Context): String =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            PackageManager.PackageInfoFlags.of(0),
        ).versionName
    } else {
        applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA,
        ).versionName
    }

fun getVersionCode(applicationContext: Context): Long =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            PackageManager.PackageInfoFlags.of(0),
        ).longVersionCode
    } else {
        applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA,
        ).longVersionCode
    }

fun deleteFile(listeFile: List<String>) {
    listeFile.forEach {
        val photo = File(it)
        if (photo.exists()) {
            photo.delete()
        }
    }
}

val Int.pxToDp: Dp @Composable get() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun dateAfterNow(dateStr: String?): Boolean {
    if (dateStr == null) return false
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val localDateTime = java.time.LocalDateTime.parse(dateStr, formatter)
        val instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
        java.time.Instant.now().isBefore(instant)
    } catch (e: Exception) {
        false
    }
}

fun <T> jsonToFile(objet: T, nameFile: String): String {
    val path = Environment.getExternalStorageDirectory().toString() + "/$nameFile.json"

    val data = File(path)
    if (!data.createNewFile()) {
        data.delete()
        data.createNewFile()
    }
    val objectOutputStream = ObjectOutputStream(FileOutputStream(data))
    objectOutputStream.writeObject(
        GsonBuilder()
            // On utilise un serializer pour que la date se génère correctement
            .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeSerializer()).create()
            .toJson(objet),
    )

    objectOutputStream.close()

    return path
}
