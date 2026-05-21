package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Query
import fr.sdis83.remocra.mobile.synchronisation.SynchroDeplacementPeiWorker
import java.util.UUID

@Dao
abstract class SynchronisationDao {

    @Query("SELECT * FROM gestionnaire where edited = 1")
    abstract fun getAllGestionnaire(): List<Gestionnaire>

    @Query("SELECT * FROM contact where edited = 1")
    abstract fun getAllContacts(): List<Contact>

    @Query("SELECT contactRole.* FROM contactRole join contact on contactRole.contactId = contact.contactId where contact.edited = 1")
    abstract fun getAllContactsRole(): List<ContactRole>

    @Query("SELECT * FROM pei where isNew = 1")
    abstract fun getAllNewPei(): List<Pei>

    @Query("SELECT * FROM typePei")
    abstract fun getAllTypePei(): List<TypePei>

    @Query("SELECT * FROM visite where statut = :statutFini")
    abstract fun getAllVisite(statutFini: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE): List<Visite>

    @Query("SELECT * FROM pei")
    abstract fun getAllPei(): List<Pei>

    @Query(
        """
            SELECT lva.*, visite.tourneeId FROM lVisiteAnomalie lva
                JOIN visite ON visite.visiteId = lva.visiteId
                WHERE visite.statut = :statutFini
        """,
    )
    abstract fun getAllVisiteAnomalie(statutFini: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE): List<VisiteAnomalieWithTournee>

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
        GROUP BY t.tourneeId
        """,
    )
    abstract fun getAllTournee(terminee: Visite.VisiteStatut = Visite.VisiteStatut.TERMINE): List<TourneesDao.TourneeAvancement>

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
        SELECT * FROM lPeiTournee
        """,
    )
    abstract fun getAllLPeiTournee(): List<LPeiTournee>

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

    @Query("DELETE FROM pei where isNew = 1")
    abstract fun deleteNewPeiSynchronises()

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
    abstract fun getPeiDeplacesByTournee(): List<SynchroDeplacementPeiWorker.PeiDeplace>
}
