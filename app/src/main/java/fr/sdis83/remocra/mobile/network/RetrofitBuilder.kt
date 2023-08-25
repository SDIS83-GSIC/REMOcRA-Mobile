package fr.sdis83.remocra.mobile.network

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import foodintech.collecte.synchronization.LocalDateConverterFactory
import foodintech.collecte.synchronization.OffsetDateTimeConverterFactory
import fr.sdis83.remocra.mobile.R
import fr.sdis83.remocra.mobile.authn.AuthInterceptor
import fr.sdis83.remocra.mobile.utils.SingletonHolder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitBuilder : SingletonHolder<Retrofit>() {

    override fun newInstance(context: Context): Retrofit {
        val prefs: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val url = context.resources.getString(R.string.url_api)

        val client = OkHttpClient.Builder().eventListenerFactory(
            LoggingEventListener.Factory(),
        )
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .cache(Cache(File(context.cacheDir, "OkHttpCache"), 1024 * 1024))
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(
                buildApiUrl(
                    prefs.getString(url, "")!!,
                ),
            )
            .addConverterFactory(LocalDateConverterFactory())
            .addConverterFactory(OffsetDateTimeConverterFactory())
            .addConverterFactory(UUIDConverterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Permert de recharger le retrofit si on change l'url du serveur
     */
    fun setNewUrl(context: Context): Retrofit {
        return newInstance(context)
    }
}

private fun buildApiUrl(uri: String): String {
    // On reconstruit l'url de l'api afin de s'assurer qu'elle est conforme
    val parsedUri = Uri.parse(uri)
    return Uri.decode(
        Uri.Builder()
            .scheme(parsedUri.scheme)
            .authority(parsedUri.authority)
            .appendPath("mobile")
            .appendPath("")
            .build().toString(),
    )
}
