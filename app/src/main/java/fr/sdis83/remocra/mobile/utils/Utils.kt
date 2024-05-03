package fr.sdis83.remocra.mobile.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
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

fun dateAfterNow(date: String) =
    ZonedDateTime.parse(
        date,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault()),
    ).isAfter(ZonedDateTime.now())
