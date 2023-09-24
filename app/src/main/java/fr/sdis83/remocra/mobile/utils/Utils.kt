package fr.sdis83.remocra.mobile.utils

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
