package foodintech.collecte.synchronization

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.OffsetDateTime

class OffsetDateTimeConverterFactory : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        if (type != (OffsetDateTime::class.java)) {
            return super.stringConverter(type, annotations, retrofit)
        }
        return Converter<OffsetDateTime, String>(function = { value -> value.toString() })
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        if (type != (OffsetDateTime::class.java)) {
            return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }
        return Converter<OffsetDateTime, RequestBody>(
            function = { value ->
                value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            }
        )
    }
}
