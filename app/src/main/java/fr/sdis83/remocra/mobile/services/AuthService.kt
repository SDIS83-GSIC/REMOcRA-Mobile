package fr.sdis83.remocra.mobile.services

import android.content.Context
import com.google.gson.annotations.SerializedName
import fr.sdis83.remocra.mobile.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface AuthService {
    companion object {
        fun getRetroFitInstance(context: Context): AuthService =
            RetrofitBuilder.getInstance(context).create(AuthService::class.java)
        fun rebuildUrl(context: Context): AuthService =
            RetrofitBuilder.setNewUrl(context).create(AuthService::class.java)
    }

    @PUT("authentication/token")
    fun checkToken(): Call<ResponseBody>

    @PUT("authentication/check")
    fun checkUrl(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("authentication/login")
    fun doLogin(
        @Field("username")
        username: String,
        @Field("password")
        password: String,
        @Field("versionName")
        versionName: String,
    ): Call<LoginResponse>

    data class LoginResponse (
        @SerializedName("token")
        var token: String,

        @SerializedName("username")
        var username: String
    )
}
