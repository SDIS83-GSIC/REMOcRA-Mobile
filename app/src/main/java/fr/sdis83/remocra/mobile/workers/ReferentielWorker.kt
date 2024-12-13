package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Agent
import fr.sdis83.remocra.mobile.database.LPoidsAnomalieTypeVisite
import fr.sdis83.remocra.mobile.database.Nature
import fr.sdis83.remocra.mobile.database.Pei
import fr.sdis83.remocra.mobile.database.PoidsAnomalie
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Role
import fr.sdis83.remocra.mobile.database.TypeDroit
import fr.sdis83.remocra.mobile.database.TypePei
import fr.sdis83.remocra.mobile.database.TypeVisite
import fr.sdis83.remocra.mobile.services.ReferentielService
import java.util.UUID

class ReferentielWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : WorkerRemocra(context, workerParams) {

    companion object {
        private const val TAG: String = "ReferentielWorker"
    }

    override fun doExecute(): Result {
        val retrofitBuilder = ReferentielService.rebuildUrl(applicationContext)
        val referentielDao = RemocraDatabase.getInstance(applicationContext).referentielDao()
        val agentDao = RemocraDatabase.getInstance(applicationContext).agentDao()

        val referentielResponse = retrofitBuilder.getReferentiel().execute()

        if (!referentielResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + referentielResponse.errorBody().toString())
            return Result.failure()
        }

        Log.i(TAG, "Téléchargement du référentiel")
        referentielResponse.body()!!.apply {
            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT
            val dataInMobileTypePei = referentielDao.getListTypePei()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListTypePei(
                listTypePei.filter {
                    !dataInMobileTypePei.map { it.typePeiCode }.contains(it)
                }.map {
                    TypePei(
                        typePeiCode = it,
                    )
                },
            )

            // On stocke les data à supprimer
            val typePeiToRemove =
                dataInMobileTypePei.filterNot { data -> listTypePei.contains(data.typePeiCode) }

            // /////////////////////////////////////////////////////////////////////////////////////////// NATURE
            val dataInMobileNature = referentielDao.getListNature()
            val typePei = referentielDao.getListTypePei()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListNature(
                listNature.map {
                    Nature(
                        natureId = it.natureId,
                        natureCode = it.natureCode,
                        natureLibelle = it.natureLibelle,
                        typePeiId = typePei.find { t -> t.typePeiCode == it.natureTypePei }!!.typePeiId,
                    )
                },
            )

            val natureToRemove = dataInMobileNature.filterNot { data ->
                listNature.map { it.natureId }.contains(data.natureId)
            }

            // /////////////////////////////////////////////////////////////////////////////////////////// NATURE DECI
            val dataInMobileNatureDeci = referentielDao.getListNatureDeci()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListNatureDeci(listNatureDeci)

            val natureDeciToRemove = dataInMobileNatureDeci.filterNot { data ->
                listNatureDeci.map { it.natureDeciId }.contains(data.natureDeciId)
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE VISITE
            val dataInMobileTypeVisite = referentielDao.getListTypeVisite()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListTypeVisite(
                listTypeVisite
                    .filter { !dataInMobileTypeVisite.map { it.typeVisiteCode }.contains(it) }.map {
                        TypeVisite(
                            typeVisiteCode = it,
                        )
                    },
            )

            val typeVisiteToRemove = dataInMobileTypeVisite
                .filterNot { data -> listTypeVisite.contains(data.typeVisiteCode) }

            // /////////////////////////////////////////////////////////////////////////////////////////// ANOMALIE CATEGORIE
            val dataInMobileAnomalieCategorie = referentielDao.getListAnomalieCategorie()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListAnomalieCategorie(listAnomalieCategorie)

            val anomalieCategorieToRemove = dataInMobileAnomalieCategorie
                .filterNot { data ->
                    listAnomalieCategorie.map { it.anomalieCategorieId }
                        .contains(data.anomalieCategorieId)
                }

            // /////////////////////////////////////////////////////////////////////////////////////////// ANOMALIE
            val dataInMobileAnomalie = referentielDao.getListAnomalie()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListAnomalie(listAnomalie)

            val anomalieToRemove = dataInMobileAnomalie
                .filterNot { data -> listAnomalie.map { it.anomalieId }.contains(data.anomalieId) }

            // /////////////////////////////////////////////////////////////////////////////////////////// POIDS ANOMALIE
            val dataInMobilePoidsAnomalie = referentielDao.getListPoidsAnomalie()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListPoidsAnomalie(
                listPoidsAnomalie.map {
                    PoidsAnomalie(
                        poidsAnomalieId = it.poidsAnomalieId,
                        poidsAnomalieAnomalieId = it.poidsAnomalieAnomalieId,
                        poidsAnomalieNatureId = it.poidsAnomalieNatureId,
                        valIndispoHbe = it.poidsAnomalieValIndispoHbe,
                        valIndispoTerrestre = it.poidsAnomalieValIndispoTerrestre,
                    )
                },
            )

            val mapTypeVisiteCode =
                referentielDao.getListTypeVisite().map { it.typeVisiteCode to it.typeVisiteId }
                    .toMap()

            // On delete le lien entre les visites et les poids pour les remettre
            referentielDao.deleteLPoidsAnomalieTypeVisite(listPoidsAnomalie.map { it.poidsAnomalieId })

            // Puis le lien avec les visites
            val listLPoidsAnomalieTypeVisite = mutableListOf<LPoidsAnomalieTypeVisite>()
            listPoidsAnomalie.forEach { p ->
                p.poidsAnomalieTypeVisite.map {
                    listLPoidsAnomalieTypeVisite.add(
                        LPoidsAnomalieTypeVisite(
                            poidsAnomalieId = p.poidsAnomalieId,
                            typeVisiteId = mapTypeVisiteCode[it]!!,
                        ),
                    )
                }
            }

            referentielDao.insertListLPoidsAnomalieTypeVisite(listLPoidsAnomalieTypeVisite)

            val poidsAnomalieToRemove = dataInMobilePoidsAnomalie
                .filterNot { data ->
                    listLPoidsAnomalieTypeVisite
                        .map { it.poidsAnomalieId }.contains(data.poidsAnomalieId)
                }

            val lPoidsAnomalieTypeVisiteToRemove = referentielDao.getListLPoidsAnomalieTypeVisite()
                .filter {
                    poidsAnomalieToRemove.map { it.poidsAnomalieId }.contains(it.poidsAnomalieId)
                }

            // /////////////////////////////////////////////////////////////////////////////////////////// ROLE
            val dataInMobileRole = referentielDao.getListRole()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListRole(
                listRole.map {
                    Role(
                        roleId = it.id,
                        roleLibelle = it.libelle,
                        roleCode = it.code,
                    )
                },
            )

            val roleToRemove = dataInMobileRole
                .filterNot { data -> listRole.map { it.id }.contains(data.roleId) }

            // ///////////////////////////////////////////////////////////////////////////////////////////GESTIONNAIRE
            val dataInMobileGestionnaire = referentielDao.getListGestionnaire()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListGestionnaire(listGestionnaire)

            val gestionnaireToRemove = dataInMobileGestionnaire
                .filterNot { data ->
                    listGestionnaire.map { it.gestionnaireId }.contains(data.gestionnaireId)
                }

            // /////////////////////////////////////////////////////////////////////////////////////////// FONCTION CONTACT
            val dataInMobileFonctionContact = referentielDao.getListFonctionContact()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListFonctionContact(listFonctionContact ?: listOf())

            val fonctionContactToRemove = dataInMobileFonctionContact
                .filterNot { data ->
                    listFonctionContact?.map { it.fonctionContactId }
                        ?.contains(data.fonctionContactId) == true
                }

            // ///////////////////////////////////////////////////////////////////////////////////////////CONTACT
            val dataInMobileContact = referentielDao.getListContact()

            // On insère les données qui ne sont pas déjà dans la tablette
            referentielDao.insertListContact(listContact)

            val contactToRemove = dataInMobileContact
                .filterNot { data ->
                    listContact.map { it.contactId }.contains(data.contactId)
                }

            // ///////////////////////////////////////////////////////////////////////////////////////////CONTACT ROLE
            referentielDao.truncateContactRole()
            referentielDao.insertListContactRole(listContactRole)

            // ///////////////////////////////////////////////////////////////////////////////////////////PEI
            referentielDao.insertListPei(
                listPei.map {
                    Pei(
                        peiId = it.peiId,
                        natureId = it.natureId,
                        natureDeciId = it.natureDeciId,
                        dispoHbe = it.dispoHbe,
                        dispoTerrestre = it.dispoTerrestre,
                        x = it.x,
                        y = it.y,
                        lon = it.lon,
                        lat = it.lat,
                        peiNumeroComplet = it.peiNumeroComplet,
                        typePeiId = typePei.find { t -> t.typePeiCode == it.peiTypePei }!!.typePeiId,
                        adresseComplete = it.peiComplementAdresse,
                        observation = it.peiObservation,
                        gestionnaireId = it.gestionnaireId,
                        peiCaracteristiques = peiCaracteristiques[it.peiId],
                    )
                },
            )

            val list = referentielDao.getListPei()
            val peiToRemove = if (list.isNotEmpty()) {
                referentielDao.getListPeiToRemove(
                    list.map { it.peiId }.minus(
                        listPei.map { it.peiId }
                            .toSet(),
                    ),
                )
            } else {
                emptyList()
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////L_PEI_ANOMALIE
            referentielDao.deleteLPeiAnomalie()
            referentielDao.insertListLPeiAnomalie(listPeiAnomalies)

            // ///////////////////////////////////////////////////////////////////////////////////////////PARAMETRE
            // On n'a pas beaucoup de param conf donc pour réduire le temps, on supprime et on réintègre
            referentielDao.truncateParametre()
            referentielDao.insertListParamConf(listParametre)

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE DROIT
            referentielDao.truncateTypeDroit()
            referentielDao.insertListTypeDroit(
                listDroit.map {
                    TypeDroit(
                        UUID.randomUUID(),
                        it,
                    )
                },
            )

            // //////////////////////////////////////////////////////////////////////////////////////// Gestion des suppressions

            referentielDao.apply {
                deletePei(peiToRemove.map { it.peiId })
                deleteContact(contactToRemove.map { it.contactId })
                deleteGestionnaire(gestionnaireToRemove.map { it.gestionnaireId })
                deleteRole(roleToRemove.map { it.roleId })
                deleteNature(natureToRemove.map { it.natureId })
                deleteNatureDeci(natureDeciToRemove.map { it.natureDeciId })
                deleteTypePei(typePeiToRemove.map { it.typePeiId })
                deletePoidsAnomalie(poidsAnomalieToRemove.map { it.poidsAnomalieId })
                deleteAnomalie(anomalieToRemove.map { it.anomalieId })
                deleteAnomalieCategorie(anomalieCategorieToRemove.map { it.anomalieCategorieId })
                deleteTypeVisite(typeVisiteToRemove.map { it.typeVisiteId })
                deleteFonctionContact(fonctionContactToRemove.map { it.fonctionContactId })
                deleteLPoidsAnomalieTypeVisite(lPoidsAnomalieTypeVisiteToRemove.map { it.poidsAnomalieId })
            }

            if (utilisateurConnecte != referentielDao.getAgentConnecte()?.nomAgent) {
                agentDao.removeUserConnecte()
                agentDao.insertComposantAgent(
                    Agent(
                        idAgent = UUID.randomUUID(),
                        nomAgent = utilisateurConnecte,
                        numeroAgent = 1,
                        isLastValue = false,
                        isUserConnecte = true,
                    ),
                )
            }
        }

        return Result.success()
    }
}
