package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import java.util.UUID

@Dao
abstract class SynchronisationDao {

    @Query("SELECT * FROM gestionnaire where edited = 1")
    abstract fun getAllGestionnaire(): List<Gestionnaire>

    @Query(
        """
        SELECT
            g.gestionnaireId AS gestionnaireId,
            g.gestionnaireLibelle AS gestionnaireLibelle,
            g.gestionnaireCode AS gestionnaireCode,
            g.edited AS gestionnaireEdited,
            c.contactId AS contactId,
            c.contactCivilite AS contactCivilite,
            c.contactNom AS contactNom,
            c.contactPrenom AS contactPrenom,
            c.contactTelephone AS contactTelephone,
            c.contactEmail AS contactEmail,
            c.contactCommuneText AS contactCommuneText,
            c.edited AS contactEdited
        FROM gestionnaire g
        LEFT JOIN contact c ON c.gestionnaireId = g.gestionnaireId
        WHERE g.edited = 1 OR c.edited = 1
        ORDER BY g.gestionnaireLibelle, c.contactNom, c.contactPrenom
        """,
    )
    abstract fun getAllGestionnaireWithContact(): List<GestionnaireWithContactRow>

    data class GestionnaireWithContactRow(
        val gestionnaireId: UUID,
        val gestionnaireLibelle: String,
        val gestionnaireCode: String,
        val gestionnaireEdited: Boolean,
        val contactId: UUID?,
        val contactCivilite: Contact.Civilite?,
        val contactNom: String?,
        val contactPrenom: String?,
        val contactTelephone: String?,
        val contactEmail: String?,
        val contactCommuneText: String?,
        val contactEdited: Boolean?,
    )

    @Query("SELECT * FROM contact where edited = 1")
    abstract fun getAllContacts(): List<Contact>

    @Query("SELECT contactRole.* FROM contactRole join contact on contactRole.contactId = contact.contactId where contact.edited = 1")
    abstract fun getAllContactsRole(): List<ContactRole>

    @Query("SELECT * FROM pei where isNew = 1")
    abstract fun getAllNewPei(): List<Pei>

    @Query("SELECT * FROM pei where isNew = 1 and peiId = :peiId")
    abstract fun getNewPeiById(peiId: UUID): Pei?

    @Query(
        """
        SELECT p.*, tp.typePeiCode, n.natureLibelle, nd.natureDeciLibelle, d.domaineLibelle 
        FROM pei p
        LEFT JOIN typePei tp ON tp.typePeiId = p.typePeiId
        LEFT JOIN nature n ON n.natureId = p.natureId
        LEFT JOIN natureDeci nd ON nd.natureDeciId = p.natureDeciId
        LEFT JOIN domaine d ON d.domaineId = p.domaineId
        WHERE p.isNew = 1
        """,
    )
    abstract fun getAllNewPeiWithDetails(): List<NewPeiWithDetails>

    data class NewPeiWithDetails(
        @Embedded val pei: Pei,
        val typePeiCode: String?,
        val natureLibelle: String?,
        val natureDeciLibelle: String?,
        val domaineLibelle: String?,
    )

    @Query("SELECT * FROM typePei")
    abstract fun getAllTypePei(): List<TypePei>

    @Query("SELECT * FROM visite where statut = :statutFini and (:tourneeId IS NULL OR tourneeId = :tourneeId)")
    abstract fun getAllVisite(statutFini: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE, tourneeId: UUID? = null): List<Visite>

    @Query("SELECT * FROM pei")
    abstract fun getAllPei(): List<Pei>

    @Query(
        """
            SELECT lva.*, visite.tourneeId FROM lVisiteAnomalie lva
                JOIN visite ON visite.visiteId = lva.visiteId
                WHERE visite.statut = :statutFini
                and (:tourneeId IS NULL OR visite.tourneeId = :tourneeId)
        """,
    )
    abstract fun getAllVisiteAnomalie(statutFini: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE, tourneeId: UUID? = null): List<VisiteAnomalieWithTournee>

    data class VisiteAnomalieWithTournee(
        val visiteId: UUID,
        val anomalieId: UUID,
        val tourneeId: UUID,
    )

    @Query("SELECT * FROM lPeiAnomalie")
    abstract fun getAllAnomalie(): List<LPeiAnomalie>

    @Query(
        """
        SELECT t.*, doneCount FROM tournee t
        LEFT JOIN  (select tourneeId,  COUNT(visite.visiteId)AS doneCount
            from visite where statut = :terminee group by tourneeId) as c on t.tourneeId = c.tourneeId
            where (:tourneeId IS NULL OR t.tourneeId = :tourneeId)
        GROUP BY t.tourneeId
        """,
    )
    abstract fun getAllTournee(terminee: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE, tourneeId: UUID? = null): List<TourneesDao.TourneeAvancement>

    @Query(
        """
        SELECT t.*, doneCount FROM tournee t
        LEFT JOIN (select tourneeId,  COUNT(visite.visiteId) AS doneCount
            from visite group by tourneeId) as c on t.tourneeId = c.tourneeId
        GROUP BY t.tourneeId
        """,
    )
    abstract fun getAllTourneeReservees(): List<TourneesDao.TourneeAvancement>

    @Query(
        """
        SELECT * FROM photoPei
        """,
    )
    abstract fun getPhotoPei(): List<PhotoPei>

    @Query(
        """
        SELECT * FROM photoPei
        where peiId in (:peiIds)
        """,
    )
    abstract fun getPhotoPei(peiIds: List<UUID>): List<PhotoPei>

    @Query(
        """
        SELECT * FROM lPeiTournee
        """,
    )
    abstract fun getAllLPeiTournee(): List<LPeiTournee>

    @Query(
        """
        SELECT * FROM lPeiTournee
        where tourneeId = :tourneeId
        """,
    )
    abstract fun getAllLPeiTournee(tourneeId: UUID): List<LPeiTournee>

    @Query(
        """
        SELECT * FROM typeVisite
        """,
    )
    abstract fun getAllTypeVisite(): List<TypeVisite>

    @Query("DELETE FROM gestionnaire where edited = 1")
    abstract fun deleteGestionnaireSynchronises()

    @Query("DELETE FROM contact where edited = 1")
    abstract fun deleteContactsSynchronises()

    @Query(
        "DELETE FROM contactRole where contactId in " +
            "(SELECT  contactId from  contact where contact.edited = 1)",
    )
    abstract fun deleteContactsRoleSynchronises()

    @Query("DELETE FROM pei where isNew = 1 and peiId = :peiId")
    abstract fun deleteNewPeiSynchronises(peiId: UUID)

    @Query(
        """
            DELETE FROM lVisiteAnomalie where visiteId in (
                SELECT visiteId FROM visite
                join tournee on tournee.tourneeId = visite.tourneeId 
                WHERE visite.statut = :statutFini and tournee.tourneeId = :tourneeId)
        """,
    )
    abstract fun deleteVisiteAnomalie(tourneeId: UUID, statutFini: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE)

    @Query(
        """
            SELECT path FROM photoPei where peiId in (:listePeiId)
        """,
    )
    abstract fun getPhotoPeiFini(listePeiId: List<UUID>): List<String>

    @Query(
        """
            DELETE FROM photoPei where path in (:listPath)
        """,
    )
    abstract fun deletePhotoPei(listPath: List<String>)

    @Query(
        """
            DELETE FROM visite where visiteId in (
                SELECT visiteId FROM visite  
                join tournee on tournee.tourneeId = visite.tourneeId
                WHERE visite.statut = :statutFini and tournee.tourneeId = :tourneeId)
        """,
    )
    abstract fun deleteVisite(tourneeId: UUID, statutFini: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE)

    @Query(
        """
        DELETE FROM tournee where tourneeId = :tourneeId      
        """,
    )
    abstract fun deleteTourneeSynchronisee(tourneeId: UUID)

    @Query(
        """
            SELECT p.lat, p.lon, p.peiId, tourneeId from pei p 
                JOIN lPeiTournee lpt on lpt.peiId = p.peiId
                where p.isDeplace = 1
        """,
    )
    abstract fun getPeiDeplacesByTournee(): List<PeiDeplace>

    @Query(
        """
            SELECT p.lat, p.lon, p.peiId, tourneeId from pei p 
                JOIN lPeiTournee lpt on lpt.peiId = p.peiId
                where p.isDeplace = 1 and lpt.tourneeId = :tourneeId
        """,
    )
    abstract fun getPeiDeplaces(tourneeId: UUID): List<PeiDeplace>

    @Query(
        """
            SELECT * FROM tournee where tourneeId = :tourneeId
        """,
    )
    abstract fun getTourneeById(tourneeId: UUID): Tournee?

    data class PeiDeplace(
        val peiId: UUID,
        val tourneeId: UUID,
        val lat: Double,
        val lon: Double,
    )
}
