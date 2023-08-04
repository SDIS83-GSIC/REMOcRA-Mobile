package fr.sdis83.remocra.mobile.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import java.util.UUID

@Dao
abstract class TourneeDao {

    @Query(
        """
        SELECT h.*, hv.statut FROM hydrant h
        JOIN hydrantTournee ht ON ht.idRemocraHydrant = h.idRemocra
        JOIN tournee t ON t.idRemocra = ht.idRemocraTournee
        LEFT JOIN hydrantVisite hv ON hv.idHydrant = h.idHydrant AND hv.idTournee = :idTournee
        WHERE t.idTournee = :idTournee
        """
    )
    abstract fun getHydrantByTournee(idTournee: UUID): LiveData<List<TourneeHydrantAvancement>>

    data class TourneeHydrantAvancement(
        @Embedded val hydrant: Hydrant,
        @Relation(
            parentColumn = "idNature",
            entityColumn = "idRemocra"
        ) val hydrantNature: TypeHydrantNature,
        var statut: String? = "",
    )

    @Query(
        """
        SELECT t.* FROM tournee t
        WHERE t.idTournee = :idTournee
        """
    )
    abstract fun getTournee(idTournee: UUID): LiveData<Tournee>
}
