package fr.sdis83.remocra.mobile.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeSerializer : JsonSerializer<ZonedDateTime?> {

    override fun serialize(localDate: ZonedDateTime?, srcType: Type?, context: JsonSerializationContext?): JsonElement {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return JsonPrimitive(formatter.format(localDate))
    }
}
