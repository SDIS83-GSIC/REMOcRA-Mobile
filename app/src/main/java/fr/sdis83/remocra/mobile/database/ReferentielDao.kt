package fr.sdis83.remocra.mobile.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class ReferentielDao {

    @Insert
    abstract fun insertCommune(commune: Commune)
    @Insert
    abstract fun insertTypeHydrant(typeHydrant: TypeHydrant)
    @Insert
    abstract fun insertTypeHydrantNature(typeHydrantNature: TypeHydrantNature)
    @Insert
    abstract fun insertTypeHydrantNatureDeci(typeHydrantNatureDeci: TypeHydrantNatureDeci)
    @Insert
    abstract fun insertTypeHydrantSaisie(typeHydrantSaisie: TypeHydrantSaisie)
    @Insert
    abstract fun insertTypeHydrantCritere(typeHydrantCritere: TypeHydrantCritere)
    @Insert
    abstract fun insertTypeHydrantAnomalie(typeHydrantAnomalie: TypeHydrantAnomalie)
    @Insert
    abstract fun insertHydrant(hydrant: Hydrant)
    @Insert
    abstract fun insertHydrantAnomalie(hydrantAnomalie: HydrantAnomalie)
    @Insert
    abstract fun insertRole(role: Role)
    @Insert
    abstract fun insertGestionnaire(gestionnaire: Gestionnaire)
    @Insert
    abstract fun insertContact(contact: Contact)
    @Insert
    abstract fun insertContactRole(contactRole: ContactRole)

    @Query("DELETE FROM hydrantAnomalie")
    abstract fun truncateHydrantAnomalie()
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
    @Query("DELETE FROM commune")
    abstract fun truncateCommune()
    @Query("DELETE FROM typeHydrantNature")
    abstract fun truncateTypeHydrantNature()
    @Query("DELETE FROM typeHydrantNatureDeci")
    abstract fun truncateTypeHydrantNatureDeci()
    @Query("DELETE FROM typeHydrant")
    abstract fun truncateTypeHydrant()
    @Query("DELETE FROM typeHydrantAnomalie")
    abstract fun truncateTypeHydrantAnomalie()
    @Query("DELETE FROM typeHydrantCritere")
    abstract fun truncateTypeHydrantCritere()
    @Query("DELETE FROM typeHydrantSaisie")
    abstract fun truncateTypeHydrantSaisie()
}
