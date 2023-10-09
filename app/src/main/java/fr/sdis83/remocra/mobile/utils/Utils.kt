package fr.sdis83.remocra.mobile.utils

import android.content.Context
import android.content.pm.PackageManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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
