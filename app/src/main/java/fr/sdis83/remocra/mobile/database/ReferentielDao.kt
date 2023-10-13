package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import fr.sdis83.remocra.mobile.services.ReferentielService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
abstract class ReferentielDao {

    @Insert
    abstract fun insertListTypeHydrant(listTypeHydrant: List<TypeHydrant>)

    @Insert
    abstract fun insertListTypeHydrantNature(listTypeHydrantNature: List<TypeHydrantNature>)

    @Insert
    abstract fun insertListTypeHydrantNatureDeci(listTypeHydrantNatureDeci: List<TypeHydrantNatureDeci>)

    @Insert
    abstract fun insertListTypeHydrantSaisie(listTypeHydrantSaisie: List<TypeHydrantSaisie>)

    @Insert
    abstract fun insertListTypeHydrantCritere(listTypeHydrantCritere: List<TypeHydrantCritere>)

    @Insert
    abstract fun insertListTypeHydrantAnomalie(listTypeHydrantAnomalie: List<TypeHydrantAnomalie>)

    @Insert
    abstract fun insertListHydrant(listHydrant: List<Hydrant>)

    @Insert
    abstract fun insertListHydrantAnomalie(listHydrantAnomalie: List<HydrantAnomalie>)

    @Insert
    abstract fun insertListTypeHydrantAnomalieNature(listTypeHydrantAnomalieNature: List<TypeHydrantAnomalieNature>)

    @Insert
    abstract fun insertListTypeHydrantAnomalieNatureSaisie(listTypeHydrantAnomalieNatureSaisie: List<TypeHydrantAnomalieNatureSaisie>)

    @Insert
    abstract fun insertListRole(roles: List<Role>)

    @Insert
    abstract fun insertListGestionnaire(gestionnaires: List<Gestionnaire>)

    @Insert
    abstract fun insertListContact(contacts: List<Contact>)

    @Insert
    abstract fun insertListContactRole(contactsRoles: List<ContactRole>)

    @Insert
    abstract fun insertListParamConf(listParamConf: List<ParamConf>)

    @Insert
    abstract fun insertListTypeDroit(listTypeDroit: List<TypeDroit>)

    @Query("DELETE FROM hydrantAnomalie")
    abstract fun truncateHydrantAnomalie()

    @Query("DELETE FROM hydrantPhoto")
    abstract fun truncateHydrantPhoto()

    @Query("DELETE FROM hydrantVisite")
    abstract fun truncateHydrantVisite()

    @Query("DELETE FROM hydrantTournee")
    abstract fun truncateHydrantTournee()

    @Query("DELETE FROM tournee")
    abstract fun truncateTournee()

    @Query("DELETE FROM hydrant")
    abstract fun truncateHydrant()

    @Query("DELETE FROM contactRole")
    abstract fun truncateContactRole()

    @Query("DELETE FROM contact")
    abstract fun truncateContact()

    @Query("DELETE FROM gestionnaire")
    abstract fun truncateGestionnaire()

    @Query("DELETE FROM role")
    abstract fun truncateRole()

    @Query("DELETE FROM typeHydrantNature")
    abstract fun truncateTypeHydrantNature()

    @Query("DELETE FROM typeHydrantNatureDeci")
    abstract fun truncateTypeHydrantNatureDeci()

    @Query("DELETE FROM typeHydrant")
    abstract fun truncateTypeHydrant()

    @Query("DELETE FROM typeHydrantAnomalie")
    abstract fun truncateTypeHydrantAnomalie()

    @Query("DELETE FROM typeHydrantAnomalieNatureSaisie")
    abstract fun truncateTypeHydrantAnomalieNatureSaisie()

    @Query("DELETE FROM typeHydrantAnomalieNature")
    abstract fun truncateTypeHydrantAnomalieNature()

    @Query("DELETE FROM typeHydrantCritere")
    abstract fun truncateTypeHydrantCritere()

    @Query("DELETE FROM typeHydrantSaisie")
    abstract fun truncateTypeHydrantSaisie()

    @Query("DELETE FROM paramConf")
    abstract fun truncateParamConf()

    @Query("DELETE FROM typeDroit")
    abstract fun truncateTypeDroit()

    @Query("SELECT * FROM typeHydrant")
    abstract fun getListTypeHydrant(): List<TypeHydrant>

    @Query("UPDATE typeHydrant set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra")
    abstract fun updateTypeHydrant(idRemocra: Long, nom: String, actif: Boolean, code: String?)

    @Query("DELETE FROM typeHydrant where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrant(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantNature")
    abstract fun getListTypeHydrantNature(): List<TypeHydrantNature>

    @Query("UPDATE typeHydrantNature set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra")
    abstract fun updateTypeHydrantNature(idRemocra: Long, nom: String, actif: Boolean, code: String?)

    @Query("DELETE FROM typeHydrantNature where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrantNature(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantNatureDeci")
    abstract fun getListTypeHydrantNatureDeci(): List<TypeHydrantNatureDeci>

    @Query("UPDATE typeHydrantNatureDeci set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra")
    abstract fun updateTypeHydrantNatureDeci(idRemocra: Long, nom: String, actif: Boolean, code: String?)

    @Query("DELETE FROM typeHydrantNatureDeci where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrantNatureDeci(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantSaisie")
    abstract fun getListTypeHydrantSaisie(): List<TypeHydrantSaisie>

    @Query("UPDATE typeHydrantSaisie set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra")
    abstract fun updateTypeHydrantSaisie(idRemocra: Long, nom: String, actif: Boolean, code: String?)

    @Query("DELETE FROM typeHydrantSaisie where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrantSaisie(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantCritere")
    abstract fun getListTypeHydrantCritere(): List<TypeHydrantCritere>

    @Query("UPDATE typeHydrantCritere set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra")
    abstract fun updateTypeHydrantCritere(idRemocra: Long, nom: String, actif: Boolean, code: String?)

    @Query("DELETE FROM typeHydrantCritere where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrantCritere(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantAnomalie")
    abstract fun getListTypeHydrantAnomalie(): List<TypeHydrantAnomalie>

    @Query("UPDATE typeHydrantAnomalie set nom = :nom, actif = :actif, code = :code, idCritere = :idCritere where idRemocra = :idRemocra")
    abstract fun updateTypeHydrantAnomalie(idRemocra: Long, nom: String, actif: Boolean, idCritere: Long, code: String?)

    @Query("DELETE FROM typeHydrantAnomalie where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrantAnomalie(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantAnomalieNature")
    abstract fun getListTypeHydrantAnomalieNature(): List<TypeHydrantAnomalieNature>

    @Query(
        "UPDATE typeHydrantAnomalieNature set idTypeHydrantNature = :idTypeHydrantNature, idTypeHydrantAnomalie = :idTypeHydrantAnomalie," +
            "valIndispoTerrestre = :valIndispoTerrestre, valIndispoHbe =:valIndispoHbe, valIndispoAdmin = :valIndispoAdmin   where idRemocra = :idRemocra",
    )
    abstract fun updateTypeHydrantAnomalieNature(
        idRemocra: Long,
        idTypeHydrantNature: Long,
        idTypeHydrantAnomalie: Long,
        valIndispoTerrestre: Int,
        valIndispoHbe: Int,
        valIndispoAdmin: Int,
    )

    @Query("DELETE FROM typeHydrantAnomalieNature where idRemocra in (:listIdRemocra)")
    abstract fun deleteTypeHydrantAnomalieNature(listIdRemocra: List<Long>)

    @Query("SELECT * FROM typeHydrantAnomalieNatureSaisie")
    abstract fun getListTypeHydrantAnomalieNatureSaisie(): List<TypeHydrantAnomalieNatureSaisie>

    @Query("DELETE FROM typeHydrantAnomalieNatureSaisie where idTypeHydrantAnomalieNature = :idTypeHydrantAnomalieNature and  idTypeHydrantSaisie = :idTypeHydrantSaisie")
    abstract fun deleteTypeHydrantAnomalieNatureSaisie(idTypeHydrantAnomalieNature: Long, idTypeHydrantSaisie: Long)

    @Query("SELECT * FROM role")
    abstract fun getListRole(): List<Role>

    @Query("UPDATE role set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra")
    abstract fun updateRole(idRemocra: Long, nom: String?, actif: Boolean, code: String?)

    @Query("DELETE FROM role where idRemocra in (:listIdRemocra)")
    abstract fun deleteRole(listIdRemocra: List<Long>)

    @Query("SELECT * FROM gestionnaire")
    abstract fun getListGestionnaire(): List<Gestionnaire>

    @Query("UPDATE gestionnaire set nom = :nom, actif = :actif, code = :code  where idRemocra = :idRemocra and edited = 0")
    abstract fun updateGestionnaire(idRemocra: Long, nom: String?, actif: Boolean, code: String?)

    @Query("DELETE FROM gestionnaire where idRemocra in (:listIdRemocra)")
    abstract fun deleteGestionnaire(listIdRemocra: List<Long>)

    @Query("SELECT * FROM contact")
    abstract fun getListContact(): List<Contact>

    @Query(
        "UPDATE contact set idGestionnaire = :idGestionnaire, idRemocraGestionnaire = :idRemocraGestionnaire, " +
            "fonction =:fonction, civilite = :civilite, nom = :nom, prenom = :prenom, " +
            "numeroVoie = :numeroVoie, suffixeVoie = :suffixeVoie, voie = :voie, " +
            "lieuDit = :lieuDit, codePostal = :codePostal, ville = :ville, pays = :pays, " +
            "telephone = :telephone, email = :email where idRemocra = :idRemocra and edited = 0",
    )
    abstract fun updateContact(
        idRemocra: Long,
        idRemocraGestionnaire: Long,
        idGestionnaire: UUID,
        fonction: String?,
        civilite: Contact.Civilite?,
        nom: String?,
        prenom: String?,
        numeroVoie: String?,
        suffixeVoie: String?,
        voie: String?,
        lieuDit: String?,
        codePostal: String?,
        ville: String?,
        pays: String?,
        telephone: String?,
        email: String?,
    )

    @Query("DELETE FROM contact where idRemocra in (:listIdRemocra)")
    abstract fun deleteContact(listIdRemocra: List<Long>)

    @Query("SELECT contact.idRemocra as idContact, contactRole.idRole FROM contactRole JOIN contact ON contactRole.idContact = contact.idContact")
    abstract fun getListContactRole(): List<ReferentielService.ContactRoleInput>

    @Query("DELETE FROM contactRole where idRole = :idRole and idContact = :idContact")
    abstract fun deleteContactRole(idRole: Long, idContact: UUID)

    @Query("SELECT * FROM hydrant where idRemocra is not null")
    abstract fun getListHydrant(): List<Hydrant>

    @Query(
        "DELETE FROM hydrant where idRemocra is not null and (idRemocra not in (select idRemocraHydrant from hydrantTournee) " +
            "and idHydrant not in (select idHydrant from hydrantVisite))",
    )
    abstract fun deleteHydrantsNonUtilises()

    @Query("DELETE FROM typeHydrantAnomalieNature where idRemocra in (:listIdRemocra)")
    abstract fun deleteHydrant(listIdRemocra: List<Long>)

    @Query("SELECT * FROM agent where isUserConnecte = 1 LIMIT 1")
    abstract fun getAgentConnecte(): Agent?

    @Query(
        "UPDATE hydrant set idNatureDeci = :idNatureDeci, " +
            "idNature =:idNature, dispoHbe = :dispoHbe, dispoTerrestre = :dispoTerrestre, x = :x, " +
            "y = :y, lon = :lon, lat = :lat, " +
            "numero = :numero, code = :code, " +
            "adresseComplete = :adresseComplete, idGestionnaire = :idGestionnaire, " +
            "observation = :observation, idRemocraGestionnaire = :idRemocraGestionnaire, peiCaracteristiques = :peiCaracteristiques " +
            "where idRemocra = :idRemocra",
    )
    abstract fun updateHydrant(
        idRemocra: Long,
        idNatureDeci: Long?,
        idNature: Long?,
        dispoHbe: Hydrant.DisponibiliteHbe?,
        dispoTerrestre: Hydrant.Disponibilite?,
        x: Double,
        y: Double,
        lon: Double,
        lat: Double,
        numero: String?,
        code: String?,
        adresseComplete: String?,
        observation: String?,
        idRemocraGestionnaire: Long?,
        idGestionnaire: UUID?,
        peiCaracteristiques: String?,
    )

    @Query("SELECT idAnomalie, hydrant.idRemocra as idHydrant FROM hydrantAnomalie join hydrant on hydrant.idHydrant = hydrantAnomalie.idHydrant")
    abstract fun getListHydrantAnomalie(): List<ReferentielService.HydrantAnomalieInput>

    @Query("SELECT * FROM paramConf")
    abstract fun getListParamConf(): List<ParamConf>

    @Query("UPDATE paramConf set valeur = :valeur  where cle = :cle")
    abstract fun updateParamConf(cle: String, valeur: String)

    @Query("DELETE FROM paramConf where cle in (:listeCle)")
    abstract fun deleteParamConf(listeCle: List<String>)

    @Query("SELECT * FROM typeDroit")
    abstract fun getListTypeDroit(): List<TypeDroit>

    @Query("SELECT ths.* FROM typeHydrantSaisie ths")
    abstract fun getTypeSaisieList(): LiveData<List<TypeHydrantSaisie>>

    @Query(
        """
        SELECT tha.*, than.valIndispoTerrestre FROM typeHydrantAnomalie tha
        JOIN typeHydrantAnomalieNature than ON than.idTypeHydrantAnomalie = tha.idRemocra
        WHERE than.idRemocra IN
          (SELECT thans.idTypeHydrantAnomalieNature FROM typeHydrantAnomalieNatureSaisie thans
          WHERE thans.idTypeHydrantSaisie = :idTypeHydrantSaisie
          )
        AND than.idTypeHydrantNature = :idNature
        """,
    )
    abstract fun getAnomalieItemList(idTypeHydrantSaisie: Long?, idNature: Long?): Flow<List<AnomalieItem>>

    @Query("SELECT g.* FROM gestionnaire g WHERE g.actif = 1")
    abstract fun getGestionnaireList(): LiveData<List<Gestionnaire>>

    @Query("SELECT thn.* FROM typeHydrantNature thn WHERE thn.actif = 1")
    abstract fun getTypeHydrantNatureList(): LiveData<List<TypeHydrantNature>>

    @Query("SELECT thnd.* FROM typeHydrantNatureDeci thnd WHERE thnd.actif = 1")
    abstract fun getTypeHydrantNatureDeciList(): LiveData<List<TypeHydrantNatureDeci>>

    @Query("SELECT th.code FROM typeHydrant th  WHERE idRemocra = :idTypeHydrant")
    abstract suspend fun getTypeHydrant(idTypeHydrant: Long): String

    data class AnomalieItem(
        @Embedded val anomalie: TypeHydrantAnomalie,
        val valIndispoTerrestre: Int,
        @Relation(parentColumn = "idCritere", entityColumn = "idRemocra") val critere: TypeHydrantCritere,
    )
}
