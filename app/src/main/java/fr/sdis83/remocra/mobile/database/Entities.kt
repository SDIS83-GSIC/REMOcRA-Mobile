package fr.sdis83.remocra.mobile.database

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(
    tableName = "hydrant",
    indices = [
        Index("idHydrant"), Index(
            "idRemocra",
            unique = true,
        ), Index("idCommune"), Index("idNature"),
        Index("idNatureDeci"), Index("idGestionnaire"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Commune::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idCommune"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TypeHydrantNature::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idNature"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TypeHydrantNatureDeci::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idNatureDeci"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Gestionnaire::class,
            parentColumns = ["idGestionnaire"],
            childColumns = ["idGestionnaire"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class Hydrant(
    @PrimaryKey val idHydrant: UUID = UUID.randomUUID(),
    val idRemocra: Long?,
    val idNature: Long?,
    val idNatureDeci: Long?,
    val dispoHbe: DisponibiliteHbe?,
    val dispoTerrestre: Disponibilite?,
    val x: Double,
    val y: Double,
    val lon: Double,
    val lat: Double,
    val numero: String?,
    val code: String?,
    val idCommune: Long?,
    val complement: String?,
    val voie: String?,
    val voie2: String?,
    val suffixeVoie: String?,
    val lieuDit: String?,
    val observation: String?,
    val idGestionnaire: UUID?,
    val idRemocraGestionnaire: Long?,
) {
    enum class Disponibilite {
        DISPO,
        INDISPO,
        NON_CONFORME,
    }

    enum class DisponibiliteHbe {
        DISPO,
        INDISPO,
    }
}

@Entity(
    tableName = "tournee",
    indices = [Index("idTournee"), Index("idRemocra", unique = true)],
)
data class Tournee(
    @PrimaryKey val idTournee: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val hydrantCount: Int,
    val nom: String,
) {
    fun getColor(): Color =
        when (idRemocra % 4) {
            0L -> Color(191, 63, 63)
            1L -> Color(63, 191, 63)
            2L -> Color(63, 63, 191)
            3L -> Color(191, 191, 63)
            4L -> Color(63, 191, 191)
            5L -> Color(191, 63, 191)
            else -> Color(127, 127, 127)
        }
}

@Entity(
    tableName = "hydrantTournee",
    indices = [Index("idHydrantTournee"), Index("idRemocraHydrant"), Index("idRemocraTournee")],
    foreignKeys = [
        ForeignKey(
            entity = Hydrant::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idRemocraHydrant"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Tournee::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idRemocraTournee"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HydrantTournee(
    @PrimaryKey val idHydrantTournee: UUID = UUID.randomUUID(),
    val idRemocraHydrant: Long,
    val idRemocraTournee: Long,
)

@Entity(
    tableName = "hydrantVisite",
    indices = [
        Index(
            "idHydrantVisite",
            unique = true,
        ), Index("idHydrant"), Index("idTournee"), Index("idTypeHydrantSaisie"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Hydrant::class,
            parentColumns = ["idHydrant"],
            childColumns = ["idHydrant"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Tournee::class,
            parentColumns = ["idTournee"],
            childColumns = ["idTournee"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeHydrantSaisie::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idTypeHydrantSaisie"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HydrantVisite(
    @PrimaryKey val idHydrantVisite: UUID = UUID.randomUUID(),
    val idHydrant: UUID,
    val idTournee: UUID,
    val dateVisite: ZonedDateTime = ZonedDateTime.now(),
    val statut: HydrantVisiteStatut = HydrantVisiteStatut.EN_COURS,
    val idTypeHydrantSaisie: Long? = null,
    var agent1: String? = null,
    var agent2: String? = null,
    var ctrlDebitPression: Boolean = false,
    @ColumnInfo("Débit à 1 bar (㎥/h)")
    var debit: Int? = null,
    @ColumnInfo("Pression dynamique à 60 ㎥ (bar)")
    var pressionDyn: Double? = null,
    @ColumnInfo("Pression statique (bar)")
    var pression: Double? = null,
    var hasAnomalieChanges: Boolean = false,
    var observations: String? = null,
) {
    val isValid: Boolean
        get() =
            idTypeHydrantSaisie != null &&
                (
                    ctrlDebitPression &&
                        debit != null && debit!! > 0 &&
                        pressionDyn != null && pressionDyn!! > 0 &&
                        pression != null && pression!! > 0.0 ||
                        !ctrlDebitPression &&
                        debit == null &&
                        pressionDyn == null &&
                        pression == null
                    )

    enum class HydrantVisiteStatut {
        A_FAIRE,
        EN_COURS,
        TERMINE,
        SYNCHRONISE,
    }
}

@Entity(
    tableName = "hydrantVisiteAnomalie",
    indices = [Index("idHydrantVisite"), Index("idAnomalie")],
    primaryKeys = ["idHydrantVisite", "idAnomalie"],
    foreignKeys = [
        ForeignKey(
            entity = HydrantVisite::class,
            parentColumns = ["idHydrantVisite"],
            childColumns = ["idHydrantVisite"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeHydrantAnomalie::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idAnomalie"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HydrantVisiteAnomalie(
    val idHydrantVisite: UUID,
    val idAnomalie: Long,
)

@Entity(
    tableName = "hydrantAnomalie",
    indices = [Index("idHydrant"), Index("idAnomalie")],
    primaryKeys = ["idHydrant", "idAnomalie"],
    foreignKeys = [
        ForeignKey(
            entity = Hydrant::class,
            parentColumns = ["idHydrant"],
            childColumns = ["idHydrant"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeHydrantAnomalie::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idAnomalie"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HydrantAnomalie(
    val idHydrant: UUID,
    val idAnomalie: Long,
)

@Entity(
    tableName = "typeHydrantCritere",
    indices = [Index("idTypeHydrantCritere"), Index("idRemocra", unique = true)],
)
data class TypeHydrantCritere(
    @PrimaryKey val idTypeHydrantCritere: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val code: String,
    val nom: String,
    val actif: Boolean,
)

@Entity(
    tableName = "typeHydrantAnomalie",
    indices = [
        Index("idTypeHydrantAnomalie"), Index(
            "idRemocra",
            unique = true,
        ), Index("idCritere"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TypeHydrantCritere::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idCritere"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TypeHydrantAnomalie(
    @PrimaryKey val idTypeHydrantAnomalie: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val idCritere: Long,
    val code: String,
    val nom: String,
    val actif: Boolean,
)

@Entity(
    tableName = "typeHydrantAnomalieNature",
    indices = [
        Index("idTypeHydrantAnomalieNature"), Index(
            "idRemocra",
            unique = true,
        ), Index("idTypeHydrantAnomalie"), Index("idTypeHydrantNature"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TypeHydrantAnomalie::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idTypeHydrantAnomalie"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeHydrantNature::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idTypeHydrantNature"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TypeHydrantAnomalieNature(
    @PrimaryKey val idTypeHydrantAnomalieNature: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val idTypeHydrantAnomalie: Long,
    val idTypeHydrantNature: Long,
    val valIndispoTerrestre: Int,
    val valIndispoHbe: Int,
    val valIndispoAdmin: Int,
)

@Entity(
    tableName = "typeHydrantAnomalieNatureSaisie",
    indices = [
        Index("idTypeHydrantAnomalieNatureSaisie"), Index("idTypeHydrantAnomalieNature"), Index(
            "idTypeHydrantSaisie",
        ),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TypeHydrantAnomalieNature::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idTypeHydrantAnomalieNature"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeHydrantSaisie::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idTypeHydrantSaisie"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TypeHydrantAnomalieNatureSaisie(
    @PrimaryKey val idTypeHydrantAnomalieNatureSaisie: UUID = UUID.randomUUID(),
    val idTypeHydrantAnomalieNature: Long,
    val idTypeHydrantSaisie: Long,
)

@Entity(
    tableName = "commune",
    indices = [Index("idCommune"), Index("idRemocra", unique = true)],
)
data class Commune(
    @PrimaryKey val idCommune: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val code: String?,
    val nom: String,
    val insee: String,
)

@Entity(
    tableName = "typeHydrant",
    indices = [Index("idTypeHydrant"), Index("idRemocra", unique = true)],
)
data class TypeHydrant(
    @PrimaryKey val idTypeHydrant: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val code: String,
    val nom: String,
    val actif: Boolean,
)

@Entity(
    tableName = "typeHydrantNature",
    indices = [
        Index("idTypeHydrantNature"), Index(
            "idRemocra",
            unique = true,
        ), Index("idTypeHydrant"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TypeHydrant::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idTypeHydrant"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class TypeHydrantNature(
    @PrimaryKey val idTypeHydrantNature: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val code: String,
    val nom: String,
    val idTypeHydrant: Long?,
    val actif: Boolean,
)

@Entity(
    tableName = "typeHydrantNatureDeci",
    indices = [Index("idTypeHydrantNatureDeci"), Index("idRemocra", unique = true)],
)
data class TypeHydrantNatureDeci(
    @PrimaryKey val idTypeHydrantNatureDeci: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val code: String,
    val nom: String,
    val actif: Boolean,
)

@Entity(
    tableName = "typeHydrantSaisie",
    indices = [Index("idTypeHydrantSaisie"), Index("idRemocra", unique = true)],
)
data class TypeHydrantSaisie(
    @PrimaryKey val idTypeHydrantSaisie: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val code: String,
    val nom: String,
    val actif: Boolean,
)

@Entity(
    tableName = "gestionnaire",
    indices = [Index("idGestionnaire")],
)
data class Gestionnaire(
    @PrimaryKey val idGestionnaire: UUID = UUID.randomUUID(),
    val idRemocra: Long?,
    val nom: String,
    val code: String?,
    val actif: Boolean,
    val edited: Boolean = false,
)

@Entity(
    tableName = "contact",
    indices = [Index("idContact"), Index("idGestionnaire")],
    foreignKeys = [
        ForeignKey(
            entity = Gestionnaire::class,
            parentColumns = ["idGestionnaire"],
            childColumns = ["idGestionnaire"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class Contact(
    @PrimaryKey val idContact: UUID = UUID.randomUUID(),
    val idRemocra: Long?,
    val idGestionnaire: UUID?,
    val idRemocraGestionnaire: Long?,
    val fonction: String?,
    val civilite: Civilite?,
    val nom: String?,
    val prenom: String?,
    val numeroVoie: String?,
    val voie: String?,
    val suffixeVoie: String?,
    val lieuDit: String?,
    val codePostal: String?,
    val ville: String?,
    val pays: String?,
    val telephone: String?,
    val email: String?,
    val edited: Boolean = false,
) {
    enum class Civilite {
        M,
        MME,
    }
}

@Entity(
    tableName = "contactRole",
    indices = [Index("idContact"), Index("idRole")],
    primaryKeys = ["idContact", "idRole"],
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["idContact"],
            childColumns = ["idContact"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Role::class,
            parentColumns = ["idRemocra"],
            childColumns = ["idRole"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ContactRole(
    val idContact: UUID,
    val idRole: Long,
)

@Entity(
    tableName = "role",
    indices = [Index("idRole"), Index("idRemocra", unique = true)],
)
data class Role(
    @PrimaryKey val idRole: UUID = UUID.randomUUID(),
    val idRemocra: Long,
    val nom: String?,
    val code: String,
    val actif: Boolean,
)

@Entity(
    tableName = "tourneeDispo",
    indices = [Index("idTourneeDispo")],
)
data class TourneeDispo(
    @PrimaryKey val idTourneeDispo: UUID = UUID.randomUUID(),
    val idRemocra: Long?,
    val nom: String?,
    var choisie: Boolean = false,
)

@Entity(
    tableName = "paramConf",
    indices = [Index("idParamConf")],
)
data class ParamConf(
    @PrimaryKey val idParamConf: UUID = UUID.randomUUID(),
    val cle: String,
    val valeur: String,
)

@Entity(
    tableName = "typeDroit",
    indices = [Index("idTypeDroit")],
)
data class TypeDroit(
    @PrimaryKey val idTypeDroit: UUID = UUID.randomUUID(),
    val code: String,
)
