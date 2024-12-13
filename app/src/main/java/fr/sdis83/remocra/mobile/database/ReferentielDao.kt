package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
abstract class ReferentielDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListTypePei(listTypePei: Collection<TypePei>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListNature(listNature: Collection<Nature>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListNatureDeci(listNatureDeci: Collection<NatureDeci>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListTypeVisite(listTypeVisite: Collection<TypeVisite>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListAnomalieCategorie(listAnomalieCategorie: Collection<AnomalieCategorie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListAnomalie(listAnomalie: Collection<Anomalie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListPei(listPei: Collection<Pei>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListLPeiAnomalie(listLPeiAnomalie: Collection<LPeiAnomalie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListPoidsAnomalie(listPoidsAnomalie: Collection<PoidsAnomalie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListLPoidsAnomalieTypeVisite(listLPoidsAnomalieTypeVisite: Collection<LPoidsAnomalieTypeVisite>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListRole(roles: Collection<Role>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListGestionnaire(gestionnaires: Collection<Gestionnaire>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListFonctionContact(fonctionsContacts: Collection<FonctionContact>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListContact(contacts: Collection<Contact>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListContactRole(contactsRoles: Collection<ContactRole>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListParamConf(listParametre: Collection<Parametre>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertListTypeDroit(listTypeDroit: Collection<TypeDroit>)

    @Query("DELETE FROM lPeiAnomalie where peiId not in (SELECT visite.peiId FROM visite where statut!= 'A_FAIRE')")
    abstract fun deleteLPeiAnomalie()

    @Query("DELETE FROM photoPei")
    abstract fun truncatePhotoPei()

    @Query("DELETE FROM visite")
    abstract fun truncateVisite()

    @Query("DELETE FROM lPeiTournee")
    abstract fun truncateLPeiTournee()

    @Query("DELETE FROM tournee")
    abstract fun truncateTournee()

    @Query("DELETE FROM pei")
    abstract fun truncatePei()

    @Query("DELETE FROM contactRole")
    abstract fun truncateContactRole()

    @Query("DELETE FROM contact")
    abstract fun truncateContact()

    @Query("DELETE FROM gestionnaire")
    abstract fun truncateGestionnaire()

    @Query("DELETE FROM role")
    abstract fun truncateRole()

    @Query("DELETE FROM nature")
    abstract fun truncateNature()

    @Query("DELETE FROM natureDeci")
    abstract fun truncateNatureDeci()

    @Query("DELETE FROM typePei")
    abstract fun truncateTypePei()

    @Query("DELETE FROM anomalie")
    abstract fun truncateAnomalie()

    @Query("DELETE FROM poidsAnomalie")
    abstract fun truncatePoidsAnomalie()

    @Query("DELETE FROM anomalieCategorie")
    abstract fun truncateAnomalieCategorie()

    @Query("DELETE FROM typeVisite")
    abstract fun truncateTypeVisite()

    @Query("DELETE FROM parametre")
    abstract fun truncateParametre()

    @Query("DELETE FROM typeDroit")
    abstract fun truncateTypeDroit()

    @Query("SELECT * FROM typePei")
    abstract fun getListTypePei(): List<TypePei>

    @Query("DELETE FROM typePei where typePeiId in (:listTypePeiId)")
    abstract fun deleteTypePei(listTypePeiId: List<UUID>)

    @Query("SELECT * FROM nature")
    abstract fun getListNature(): List<Nature>

    @Query("DELETE FROM nature where natureId in (:listNatureId)")
    abstract fun deleteNature(listNatureId: List<UUID>)

    @Query("SELECT * FROM natureDeci")
    abstract fun getListNatureDeci(): List<NatureDeci>

    @Query("DELETE FROM natureDeci where natureDeciId in (:listNatureDeciId)")
    abstract fun deleteNatureDeci(listNatureDeciId: List<UUID>)

    @Query("SELECT * FROM typeVisite")
    abstract fun getListTypeVisite(): List<TypeVisite>

    @Query("DELETE FROM typeVisite where typeVisiteId in (:listTypeVisiteId)")
    abstract fun deleteTypeVisite(listTypeVisiteId: List<UUID>)

    @Query("DELETE FROM fonctionContact where fonctionContactId in (:listFonctionContactId)")
    abstract fun deleteFonctionContact(listFonctionContactId: List<UUID>)

    @Query("SELECT * FROM anomalieCategorie")
    abstract fun getListAnomalieCategorie(): List<AnomalieCategorie>

    @Query("DELETE FROM anomalieCategorie where anomalieCategorieId in (:listAnomalieCategorieId)")
    abstract fun deleteAnomalieCategorie(listAnomalieCategorieId: List<UUID>)

    @Query("SELECT * FROM anomalie")
    abstract fun getListAnomalie(): List<Anomalie>

    @Query("DELETE FROM anomalie where anomalieId in (:listAnomalieId)")
    abstract fun deleteAnomalie(listAnomalieId: List<UUID>)

    @Query("SELECT * FROM poidsAnomalie")
    abstract fun getListPoidsAnomalie(): List<PoidsAnomalie>

    @Query("SELECT * FROM lpoidsanomalietypevisite")
    abstract fun getListLPoidsAnomalieTypeVisite(): List<LPoidsAnomalieTypeVisite>

    @Query("DELETE FROM lpoidsanomalietypevisite where poidsAnomalieId in (:listePoidsAnomalieId)")
    abstract fun deleteLPoidsAnomalieTypeVisite(listePoidsAnomalieId: List<UUID>)

    @Query("DELETE FROM poidsAnomalie where poidsAnomalieId in (:listPoidsAnomalieId)")
    abstract fun deletePoidsAnomalie(listPoidsAnomalieId: List<UUID>)

    @Query("SELECT * FROM role")
    abstract fun getListRole(): List<Role>

    @Query("DELETE FROM role where roleId in (:listRoleId)")
    abstract fun deleteRole(listRoleId: List<UUID>)

    @Query("SELECT * FROM gestionnaire")
    abstract fun getListGestionnaire(): List<Gestionnaire>

    @Query("SELECT * FROM fonctionContact")
    abstract fun getListFonctionContact(): List<FonctionContact>

    @Query("DELETE FROM gestionnaire where gestionnaireId in (:listGestionnaireId)")
    abstract fun deleteGestionnaire(listGestionnaireId: List<UUID>)

    @Query("SELECT * FROM contact")
    abstract fun getListContact(): List<Contact>

    @Query("DELETE FROM contact where contactId in (:listContactId)")
    abstract fun deleteContact(listContactId: List<UUID>)

    @Query("DELETE FROM contactRole where roleId = :roleId and contactId = :contactId")
    abstract fun deleteContactRole(roleId: UUID, contactId: UUID)

    @Query("SELECT * FROM pei")
    abstract fun getListPei(): List<Pei>

    @Query("SELECT * FROM pei where peiId in (:listPeiId) and isNew = 0")
    abstract fun getListPeiToRemove(listPeiId: List<UUID>): List<Pei>

    @Query("DELETE FROM pei where peiId in (:listPeiId)")
    abstract fun deletePei(listPeiId: List<UUID>)

    @Query("SELECT * FROM agent where isUserConnecte = 1 LIMIT 1")
    abstract fun getAgentConnecte(): Agent?

    @Query("SELECT * FROM typeDroit")
    abstract fun getListTypeDroit(): List<TypeDroit>

    @Transaction
    @Query(
        """
        SELECT tha.*, than.valIndispoTerrestre FROM anomalie tha
        JOIN poidsAnomalie than ON than.poidsAnomalieAnomalieId = tha.anomalieId
        WHERE than.poidsAnomalieId IN
          (SELECT thans.poidsAnomalieId FROM lPoidsAnomalieTypeVisite thans
          WHERE thans.typeVisiteId = :typeVisiteId
          )
        AND than.poidsAnomalieNatureId = :natureId
        """,
    )
    abstract fun getAnomalieItemList(typeVisiteId: UUID?, natureId: UUID?): Flow<List<AnomalieItem>>

    @Query("SELECT * FROM gestionnaire")
    abstract fun getGestionnaireList(): LiveData<List<Gestionnaire>>

    data class AnomalieItem(
        @Embedded val anomalie: Anomalie,
        val valIndispoTerrestre: Int,
        @Relation(parentColumn = "anomalieCategorieId", entityColumn = "anomalieCategorieId") val categorie: AnomalieCategorie,
    )

    @Query("SELECT * FROM nature")
    abstract fun getNatureList(): LiveData<List<Nature>>

    @Query("SELECT * FROM natureDeci")
    abstract fun getNatureDeciList(): LiveData<List<NatureDeci>>

    @Query("SELECT * FROM typeVisite")
    abstract fun getTypeVisiteList(): LiveData<List<TypeVisite>>
}
