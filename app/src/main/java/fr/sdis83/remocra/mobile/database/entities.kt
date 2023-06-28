package fr.sdis83.remocra.mobile.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime
import java.util.UUID

@Entity(
    tableName = "hydrant",
    indices = [Index("idHydrant")],
)
data class Hydrant(
    @PrimaryKey val idHydrant: UUID = UUID.randomUUID(),
    val idRemocra: Int?,
    val x: Double,
    val y: Double,
    val lon: Double,
    val lat: Double,
    val numero: String?,
    val code: String?,
    val commune: String?,
    val voie: String?,
    val voie2: String?,
    val lieuDit: String?,
) {
    val adresse:String
        get() = "$voie $voie2"
}

@Entity(
    tableName = "organisme",
    indices = [Index("idOrganisme")],
)
data class Organisme(
    @PrimaryKey val idOrganisme: UUID = UUID.randomUUID(),
    val idRemocra: Int?,
) {

}

@Entity(
    tableName = "gestionnaire",
    indices = [Index("idGestionnaire")],
)
data class Gestionnaire(
    @PrimaryKey val idGestionnaire: UUID = UUID.randomUUID(),
    val idRemocra: Int?,
    val nom: String?,
    val code: String?,
)

@Entity(
    tableName = "contact",
    indices = [Index("idContact"), Index("idGestionnaire")],
    foreignKeys = [
        ForeignKey(
            entity = Gestionnaire::class,
            parentColumns = ["idGestionnaire"],
            childColumns = ["idGestionnaire"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
)
data class Contact(
    @PrimaryKey val idContact: UUID = UUID.randomUUID(),
    val idRemocra: Int?,
    val idGestionnaire: UUID?,
    val idRemocraGestionnaire: Int?,
    val fonction: String?,
    val civilite: Civilite?,
    val nom: String?,
    val prenom: String?,
    val numero_voie: String?,
    val suffixe_voie: String?,
    val voie: String?,
    val lieu_dit: String?,
    val code_postal: String?,
    val ville: String?,
    val pays: String?,
    val telephone: String?,
    val email: String?
) {
    enum class Civilite {
        M,
        MME,
    }

}
