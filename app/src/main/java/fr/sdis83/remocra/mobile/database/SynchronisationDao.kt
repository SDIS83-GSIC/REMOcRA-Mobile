package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Query
import java.util.UUID

@Dao
abstract class SynchronisationDao {

    @Query("SELECT * FROM gestionnaire where edited = 1")
    abstract fun getAllGestionnaire(): List<Gestionnaire>

    @Query("SELECT * FROM contact where edited = 1")
    abstract fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM contactRole join contact on contactRole.idContact = contact.idContact where contact.edited = 1")
    abstract fun getAllContactsRole(): List<ContactRole>

    @Query("SELECT * FROM hydrant where idRemocra is null")
    abstract fun getAllNewHydrants(): List<Hydrant>

    @Query("SELECT * FROM hydrantVisite where statut = :statutFini")
    abstract fun getAllHydrantVisite(statutFini: HydrantVisite.HydrantVisiteStatut = HydrantVisite.HydrantVisiteStatut.TERMINE): List<HydrantVisite>

    @Query("SELECT * FROM hydrant")
    abstract fun getAllHydrant(): List<Hydrant>

    @Query(
        """
            SELECT * FROM hydrantVisiteAnomalie
                JOIN hydrantVisite ON hydrantVisite.idHydrantVisite = hydrantVisiteAnomalie.idHydrantVisite
                WHERE hydrantVisite.statut = :statutFini
        """,
    )
    abstract fun getAllHydrantVisiteAnomalie(statutFini: HydrantVisite.HydrantVisiteStatut = HydrantVisite.HydrantVisiteStatut.TERMINE): List<HydrantVisiteAnomalie>

    @Query("SELECT * FROM hydrantAnomalie")
    abstract fun getAllAnomalie(): List<HydrantAnomalie>

    @Query(
        """
        SELECT t.*, COUNT(hv.idHydrantVisite) AS doneCount FROM tournee t
        LEFT JOIN hydrantVisite hv ON hv.idTournee = t.idTournee
        GROUP BY t.idTournee
        """,
    )
    abstract fun getAllTournee(): List<TourneesDao.TourneeAvancement>

    @Query(
        """
        SELECT * FROM hydrantPhoto
        """,
    )
    abstract fun getHydrantPhoto(): List<HydrantPhoto>

    @Query("DELETE FROM gestionnaire where edited = 1")
    abstract fun deleteGestionnaireSynchronises()

    @Query("DELETE FROM contact where edited = 1")
    abstract fun deleteContactsSynchronises()

    @Query(
        "DELETE FROM contactRole where idContact in " +
            "(SELECT  idContact from  contact where contact.edited = 1)",
    )
    abstract fun deleteContactsRoleSynchronises()

    @Query("DELETE FROM hydrant where idRemocra is null")
    abstract fun deleteNewHydrantsSynchronises()

    @Query(
        """
            DELETE FROM hydrantVisiteAnomalie where idHydrantVisite in (
                SELECT idHydrantVisite FROM hydrantVisite
                join tournee on tournee.idTournee = hydrantVisite.idTournee 
                WHERE hydrantVisite.statut = :statutFini and tournee.idRemocra in (:idsTournee))
        """,
    )
    abstract fun deleteHydrantVisiteAnomalie(idsTournee: List<UUID>, statutFini: HydrantVisite.HydrantVisiteStatut = HydrantVisite.HydrantVisiteStatut.TERMINE)

    @Query(
        """
            DELETE FROM hydrantVisite where idHydrantVisite in (
                SELECT idHydrantVisite FROM hydrantVisite  
                join tournee on tournee.idTournee = hydrantVisite.idTournee
                WHERE hydrantVisite.statut = :statutFini and tournee.idRemocra in (:idsTournee))
        """,
    )
    abstract fun deleteHydrantVisite(idsTournee: List<UUID>, statutFini: HydrantVisite.HydrantVisiteStatut = HydrantVisite.HydrantVisiteStatut.TERMINE)

    @Query(
        """
        DELETE FROM tournee where idTournee in (:idsTournee)       
        """,
    )
    abstract fun deleteTourneesSynchronisees(idsTournee: List<UUID>)
}
