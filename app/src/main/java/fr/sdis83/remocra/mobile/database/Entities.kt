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
    tableName = "typePei",
    indices = [Index("typePeiId")],
)
data class TypePei(
    @PrimaryKey val typePeiId: UUID = UUID.randomUUID(),
    val typePeiCode: String,
)

@Entity(
    tableName = "typeVisite",
    indices = [Index("typeVisiteId")],
)
data class TypeVisite(
    @PrimaryKey val typeVisiteId: UUID = UUID.randomUUID(),
    val typeVisiteCode: String,
    val typeVisiteLibelle: String,
)

@Entity(
    tableName = "domaine",
    indices = [Index("domaineId")],
)
data class Domaine(
    @PrimaryKey val domaineId: UUID,
    val domaineActif: Boolean,
    val domaineCode: String,
    val domaineLibelle: String,
)

@Entity(
    tableName = "pei",
    indices = [
        Index("peiId"),
        Index("natureId"),
        Index("natureDeciId"),
        Index("gestionnaireId"),
        Index("domaineId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Nature::class,
            parentColumns = ["natureId"],
            childColumns = ["natureId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = NatureDeci::class,
            parentColumns = ["natureDeciId"],
            childColumns = ["natureDeciId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Gestionnaire::class,
            parentColumns = ["gestionnaireId"],
            childColumns = ["gestionnaireId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Domaine::class,
            parentColumns = ["domaineId"],
            childColumns = ["domaineId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class Pei(
    @PrimaryKey val peiId: UUID,
    val natureId: UUID,
    val natureDeciId: UUID,
    val domaineId: UUID,
    val dispoHbe: DisponibiliteHbe?,
    val dispoTerrestre: Disponibilite?,
    val x: Double,
    val y: Double,
    val lon: Double,
    val lat: Double,
    val peiNumeroComplet: String?,
    val typePeiId: UUID,
    val adresseComplete: String?,
    val observation: String?,
    val gestionnaireId: UUID?,
    var peiCaracteristiques: String?,
    var isNew: Boolean = false,
    var isDeplace: Boolean = false,
) {
    enum class Disponibilite {
        DISPONIBLE,
        INDISPONIBLE,
        NON_CONFORME,
    }

    enum class DisponibiliteHbe {
        DISPONIBLE,
        INDISPONIBLE,
    }
}

@Entity(
    tableName = "tournee",
    indices = [Index("tourneeId")],
)
data class Tournee(
    @PrimaryKey val tourneeId: UUID,
    val peiCount: Int,
    val nom: String,
) {
    fun getColor(): Color =
        when (nom.hashCode() % 4) {
            0 -> Color(191, 63, 63)
            1 -> Color(63, 191, 63)
            2 -> Color(63, 63, 191)
            3 -> Color(191, 191, 63)
            4 -> Color(63, 191, 191)
            5 -> Color(191, 63, 191)
            else -> Color(127, 127, 127)
        }
}

@Entity(
    tableName = "lPeiTournee",
    primaryKeys = ["peiId", "tourneeId"],
    indices = [Index("peiId"), Index("tourneeId")],
    foreignKeys = [
        ForeignKey(
            entity = Pei::class,
            parentColumns = ["peiId"],
            childColumns = ["peiId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Tournee::class,
            parentColumns = ["tourneeId"],
            childColumns = ["tourneeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LPeiTournee(
    val peiId: UUID,
    val tourneeId: UUID,
    val ordre: Int,
)

@Entity(
    tableName = "visite",
    indices = [
        Index(
            "visiteId",
            unique = true,
        ),
        Index("peiId"),
        Index("tourneeId"),
        Index("typeVisiteId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Pei::class,
            parentColumns = ["peiId"],
            childColumns = ["peiId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Tournee::class,
            parentColumns = ["tourneeId"],
            childColumns = ["tourneeId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeVisite::class,
            parentColumns = ["typeVisiteId"],
            childColumns = ["typeVisiteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Visite(
    @PrimaryKey val visiteId: UUID,
    val peiId: UUID,
    val tourneeId: UUID,
    val dateVisite: ZonedDateTime = ZonedDateTime.now(),
    val statut: VisiteStatut = VisiteStatut.EN_COURS,
    val typeVisiteId: UUID? = null,
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
            typeVisiteId != null &&
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

    enum class VisiteStatut {
        A_FAIRE,
        EN_COURS,
        TERMINE,
        SYNCHRONISE,
    }
}

@Entity(
    tableName = "lVisiteAnomalie",
    indices = [Index("visiteId"), Index("anomalieId")],
    primaryKeys = ["visiteId", "anomalieId"],
    foreignKeys = [
        ForeignKey(
            entity = Visite::class,
            parentColumns = ["visiteId"],
            childColumns = ["visiteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Anomalie::class,
            parentColumns = ["anomalieId"],
            childColumns = ["anomalieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LVisiteAnomalie(
    val visiteId: UUID,
    val anomalieId: UUID,
)

@Entity(
    tableName = "photoPei",
    indices = [Index("photoId"), Index("peiId")],
    foreignKeys = [
        ForeignKey(
            entity = Pei::class,
            parentColumns = ["peiId"],
            childColumns = ["peiId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PhotoPei(
    @PrimaryKey val photoId: UUID = UUID.randomUUID(),
    val peiId: UUID,
    val datePhoto: ZonedDateTime,
    val path: String,
)

@Entity(
    tableName = "lPeiAnomalie",
    indices = [Index("peiId"), Index("anomalieId")],
    primaryKeys = ["peiId", "anomalieId"],
    foreignKeys = [
        ForeignKey(
            entity = Pei::class,
            parentColumns = ["peiId"],
            childColumns = ["peiId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Anomalie::class,
            parentColumns = ["anomalieId"],
            childColumns = ["anomalieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LPeiAnomalie(
    val peiId: UUID,
    val anomalieId: UUID,
)

@Entity(
    tableName = "anomalieCategorie",
    indices = [Index("anomalieCategorieId")],
)
data class AnomalieCategorie(
    @PrimaryKey val anomalieCategorieId: UUID,
    val anomalieCategorieCode: String,
    val anomalieCategorieLibelle: String,
    val anomalieCategorieActif: Boolean,
    val anomalieCategorieOrdre: Int,
)

@Entity(
    tableName = "anomalie",
    indices = [
        Index("anomalieId"), Index("anomalieAnomalieCategorieId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = AnomalieCategorie::class,
            parentColumns = ["anomalieCategorieId"],
            childColumns = ["anomalieAnomalieCategorieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Anomalie(
    @PrimaryKey val anomalieId: UUID,
    val anomalieAnomalieCategorieId: UUID,
    val anomalieCode: String,
    val anomalieLibelle: String,
    val anomalieOrdre: Int,
)

@Entity(
    tableName = "poidsAnomalie",
    indices = [
        Index("poidsAnomalieId"),
        Index("poidsAnomalieAnomalieId"),
        Index("poidsAnomalieNatureId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Anomalie::class,
            parentColumns = ["anomalieId"],
            childColumns = ["poidsAnomalieAnomalieId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Nature::class,
            parentColumns = ["natureId"],
            childColumns = ["poidsAnomalieNatureId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PoidsAnomalie(
    @PrimaryKey val poidsAnomalieId: UUID,
    val poidsAnomalieAnomalieId: UUID,
    val poidsAnomalieNatureId: UUID,
    val valIndispoTerrestre: Int?,
    val valIndispoHbe: Int?,
)

@Entity(
    tableName = "lPoidsAnomalieTypeVisite",
    indices = [Index("poidsAnomalieId"), Index("typeVisiteId")],
    primaryKeys = ["poidsAnomalieId", "typeVisiteId"],
    foreignKeys = [
        ForeignKey(
            entity = PoidsAnomalie::class,
            parentColumns = ["poidsAnomalieId"],
            childColumns = ["poidsAnomalieId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TypeVisite::class,
            parentColumns = ["typeVisiteId"],
            childColumns = ["typeVisiteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LPoidsAnomalieTypeVisite(
    val poidsAnomalieId: UUID,
    val typeVisiteId: UUID,
)

@Entity(
    tableName = "nature",
    indices = [
        Index("natureId"),
        Index("typePeiId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TypePei::class,
            parentColumns = ["typePeiId"],
            childColumns = ["typePeiId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class Nature(
    @PrimaryKey val natureId: UUID,
    val natureCode: String,
    val natureActif: Boolean,
    val natureLibelle: String,
    val typePeiId: UUID,
)

@Entity(
    tableName = "natureDeci",
    indices = [Index("natureDeciId")],
)
data class NatureDeci(
    @PrimaryKey val natureDeciId: UUID,
    val natureDeciActif: Boolean,
    val natureDeciCode: String,
    val natureDeciLibelle: String,
)

@Entity(
    tableName = "gestionnaire",
    indices = [Index("gestionnaireId")],
)
data class Gestionnaire(
    @PrimaryKey val gestionnaireId: UUID,
    val gestionnaireActif: Boolean,
    val gestionnaireLibelle: String,
    val gestionnaireCode: String,
    val edited: Boolean = false,
)

@Entity(
    tableName = "fonctionContact",
    indices = [Index("fonctionContactId")],
)
data class FonctionContact(
    @PrimaryKey val fonctionContactId: UUID,
    val fonctionContactActif: Boolean,
    val fonctionContactCode: String,
    val fonctionContactLibelle: String,
)

@Entity(
    tableName = "contact",
    indices = [Index("contactId"), Index("gestionnaireId"), Index("contactFonctionContactId")],
    foreignKeys = [
        ForeignKey(
            entity = Gestionnaire::class,
            parentColumns = ["gestionnaireId"],
            childColumns = ["gestionnaireId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = FonctionContact::class,
            parentColumns = ["fonctionContactId"],
            childColumns = ["contactFonctionContactId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class Contact(
    @PrimaryKey val contactId: UUID,
    val contactActif: Boolean,
    val gestionnaireId: UUID,
    val contactCivilite: Civilite?,
    val contactNom: String?,
    val contactPrenom: String?,
    val contactNumeroVoie: String?,
    val contactSuffixeVoie: String?,
    val contactLieuDitText: String?,
    val contactVoieText: String?,
    val contactCodePostal: String?,
    val contactCommuneText: String?,
    val contactPays: String?,
    val contactTelephone: String?,
    val contactEmail: String?,
    val contactFonctionContactId: UUID?,
    val edited: Boolean = false,
) {
    enum class Civilite {
        M,
        MME,
    }
}

@Entity(
    tableName = "contactRole",
    indices = [Index("contactId"), Index("roleContactId")],
    primaryKeys = ["contactId", "roleContactId"],
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["contactId"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Role::class,
            parentColumns = ["roleContactId"],
            childColumns = ["roleContactId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ContactRole(
    val contactId: UUID,
    val roleContactId: UUID,
)

@Entity(
    tableName = "role",
    indices = [Index("roleContactId")],
)
data class Role(
    @PrimaryKey val roleContactId: UUID,
    val roleContactActif: Boolean,
    val roleContactLibelle: String?,
    val roleContactCode: String,
)

@Entity(
    tableName = "tourneeDispo",
    indices = [Index("tourneeDispoId")],
)
data class TourneeDispo(
    @PrimaryKey val tourneeDispoId: UUID,
    val nom: String?,
    var choisie: Boolean = false,
)

@Entity(
    tableName = "parametre",
    indices = [Index("parametreId")],
)
data class Parametre(
    @PrimaryKey val parametreId: UUID,
    val parametreCode: String,
    val parametreValeur: String?,
)

@Entity(
    tableName = "typeDroit",
    indices = [Index("typeDroitId")],
)
data class TypeDroit(
    @PrimaryKey val typeDroitId: UUID = UUID.randomUUID(),
    val code: String,
)

@Entity(
    tableName = "agent",
    indices = [Index("idAgent")],
)
data class Agent(
    @PrimaryKey val idAgent: UUID = UUID.randomUUID(),
    val nomAgent: String,
    val numeroAgent: Int,
    val isLastValue: Boolean = false,
    val isUserConnecte: Boolean = false,
)
