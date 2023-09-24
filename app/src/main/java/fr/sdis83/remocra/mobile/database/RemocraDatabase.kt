package fr.sdis83.remocra.mobile.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fr.sdis83.remocra.mobile.utils.SingletonHolder
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Database(
    version = 1,
    entities = [
        Hydrant::class,
        Commune::class,
        Gestionnaire::class,
        Role::class,
        ContactRole::class,
        Contact::class,
        Tournee::class,
        HydrantTournee::class,
        HydrantVisite::class,
        TypeHydrant::class,
        TypeHydrantAnomalie::class,
        TypeHydrantAnomalieNature::class,
        TypeHydrantAnomalieNatureSaisie::class,
        TypeHydrantCritere::class,
        TypeHydrantNature::class,
        TypeHydrantNatureDeci::class,
        TypeHydrantSaisie::class,
        HydrantPhoto::class,
        HydrantVisiteAnomalie::class,
        HydrantAnomalie::class,
        TourneeDispo::class,
        ParamConf::class,
        TypeDroit::class,
    ],
)
@TypeConverters(Converters::class)
abstract class RemocraDatabase : RoomDatabase() {
    companion object : SingletonHolder<RemocraDatabase>() {
        private const val DATABASE_NAME = "remocra.db"

        override fun newInstance(context: Context): RemocraDatabase =
            Room.databaseBuilder(context, RemocraDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    abstract fun referentielDao(): ReferentielDao
    abstract fun hydrantDao(): HydrantDao

    abstract fun tourneeDao(): TourneeDao

    abstract fun tourneesDao(): TourneesDao

    abstract fun hydrantVisiteDao(): HydrantVisiteDao

    abstract fun gestionnairesDao(): GestionnairesDao

    abstract fun contactsDao(): ContactsDao

    abstract fun synchronisationDao(): SynchronisationDao

    abstract fun paramConfDao(): DroitDao
    abstract fun hydrantPhotoDao(): HydrantPhotoDao
}

class Converters {
    @TypeConverter
    fun toTimestamp(d: ZonedDateTime?) = d?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun dateFromTimestamp(t: Long?) =
        t?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault()) }

    @TypeConverter
    fun toBinary(u: UUID?) = u?.let {
        ByteArrayOutputStream(16).apply {
            DataOutputStream(this).use {
                it.writeLong(u.mostSignificantBits)
                it.writeLong(u.leastSignificantBits)
            }
        }.toByteArray()
    }

    @TypeConverter
    fun uuidFromBinary(b: ByteArray?): UUID? = b?.let {
        DataInputStream(ByteArrayInputStream(b)).use {
            UUID(it.readLong(), it.readLong())
        }
    }

    @TypeConverter
    fun stringListToJson(list: List<String>): String {
        return JSONArray(list).toString()
    }

    @TypeConverter
    fun jsonToStringList(str: String): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until JSONArray(str).length()) {
            list.add(JSONArray(str).getString(i))
        }
        return list
    }
}
