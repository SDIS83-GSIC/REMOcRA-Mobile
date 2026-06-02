package fr.sdis83.remocra.mobile.services

import android.content.Context
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LogService {

    companion object {
        fun getRetroFitInstance(context: Context): LogService =
            RetrofitBuilder.getInstance(context).create(LogService::class.java)
    }

    @POST("log/export")
    @Multipart
    fun exportLog(
        @Part("tabletteId") tabletteId: RequestBody,
        @Part file: MultipartBody.Part,
    ): Call<ResponseBody>
}
