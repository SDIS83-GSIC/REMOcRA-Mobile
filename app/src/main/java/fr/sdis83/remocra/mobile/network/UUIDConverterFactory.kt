package fr.sdis83.remocra.mobile.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.UUID

class UUIDConverterFactory : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<*, String>? {
        if (type != (UUID::class.java)) {
            return super.stringConverter(type, annotations, retrofit)
        }
        return Converter<UUID, String>(function = { value -> value.toString() })
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? {
        if (type != (UUID::class.java)) {
            return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }
        return Converter<UUID, RequestBody>(
            function = { value ->
                value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            },
        )
    }
}
