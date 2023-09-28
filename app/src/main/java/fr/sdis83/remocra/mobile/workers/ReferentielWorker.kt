package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Agent
import fr.sdis83.remocra.mobile.database.ContactRole
import fr.sdis83.remocra.mobile.database.HydrantAnomalie
import fr.sdis83.remocra.mobile.database.ParamConf
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.services.ReferentielService
import fr.sdis83.remocra.mobile.utils.GlobalConstants
import java.util.UUID

class ReferentielWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG: String = "ReferentielWorker"
    }

    override fun doWork(): Result {
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
        val referentielDao = RemocraDatabase.getInstance(applicationContext).referentielDao()
        val tourneesDao = RemocraDatabase.getInstance(applicationContext).tourneesDao()
        val agentDao = RemocraDatabase.getInstance(applicationContext).agentDao()

        val referentielResponse = retrofitBuilder.getReferentiel().execute()

        if (!referentielResponse.isSuccessful) {
            Log.e(TAG, "Error executing work: " + referentielResponse.errorBody().toString())
            return Result.failure()
        }

        tourneesDao.apply {
            truncateTourneesDispos()
            truncateHydrantTournee()
            truncateTournee()
        }

        referentielDao.apply {
            truncateHydrantPhoto()
            truncateHydrantAnomalie()
            truncateHydrantVisite()
            truncateHydrantTournee()
            truncateTournee()
            truncateHydrant()
            truncateContactRole()
            truncateContact()
            truncateGestionnaire()
            truncateRole()
            truncateCommune()
            truncateTypeHydrantNature()
            truncateTypeHydrantNatureDeci()
            truncateTypeHydrant()
            truncateTypeHydrantAnomalieNatureSaisie()
            truncateTypeHydrantAnomalieNature()
            truncateTypeHydrantAnomalie()
            truncateTypeHydrantCritere()
            truncateTypeHydrantSaisie()
            truncateParamConf()
            truncateTypeDroit()
        }

        referentielResponse.body()!!.apply {
            val hydrantMap = mutableListOf<Pair<UUID, Long>>()
            val anomalieMap = mutableListOf<Pair<UUID, Long>>()
            val gestionnaireMap = mutableListOf<Pair<UUID, Long>>()
            val contactMap = mutableListOf<Pair<UUID, Long>>()

            communes.forEach { commune ->
                referentielDao.insertCommune(commune.copy(idCommune = UUID.randomUUID()))
            }
            typesHydrant.forEach { typeHydrant ->
                referentielDao.insertTypeHydrant(typeHydrant.copy(idTypeHydrant = UUID.randomUUID()))
            }
            typesHydrantNature.forEach { typeHydrantNature ->
                referentielDao.insertTypeHydrantNature(typeHydrantNature.copy(idTypeHydrantNature = UUID.randomUUID()))
            }
            typesHydrantNatureDeci.forEach { typeHydrantNatureDeci ->
                referentielDao.insertTypeHydrantNatureDeci(
                    typeHydrantNatureDeci.copy(
                        idTypeHydrantNatureDeci = UUID.randomUUID(),
                    ),
                )
            }
            typesHydrantSaisie.forEach { typeHydrantSaisie ->
                referentielDao.insertTypeHydrantSaisie(
                    typeHydrantSaisie.copy(idTypeHydrantSaisie = UUID.randomUUID()),
                )
            }
            typesHydrantCritere.forEach { typeHydrantCritere ->
                referentielDao.insertTypeHydrantCritere(typeHydrantCritere.copy(idTypeHydrantCritere = UUID.randomUUID()))
            }
            typesHydrantAnomalie.forEach { typeHydrantAnomalie ->
                UUID.randomUUID().let { idTypeHydrantAnomalie ->
                    anomalieMap.add(Pair(idTypeHydrantAnomalie, typeHydrantAnomalie.idRemocra))
                    referentielDao.insertTypeHydrantAnomalie(
                        typeHydrantAnomalie.copy(
                            idTypeHydrantAnomalie = idTypeHydrantAnomalie,
                        ),
                    )
                }
            }
            typesHydrantAnomalieNature.forEach { typeHydrantAnomalieNature ->
                referentielDao.insertTypeHydrantAnomalieNature(typeHydrantAnomalieNature.copy(idTypeHydrantAnomalieNature = UUID.randomUUID()))
            }
            typesHydrantAnomalieNatureSaisie.forEach { typeHydrantAnomalieNatureSaisie ->
                referentielDao.insertTypeHydrantAnomalieNatureSaisie(typeHydrantAnomalieNatureSaisie.copy(idTypeHydrantAnomalieNatureSaisie = UUID.randomUUID()))
            }
            roles.forEach { role ->
                referentielDao.insertRole(role.copy(idRole = UUID.randomUUID()))
            }
            gestionnaires.forEach { gestionnaire ->
                UUID.randomUUID().let { idGestionnaire ->
                    gestionnaireMap.add(Pair(idGestionnaire, gestionnaire.idRemocra!!))
                    referentielDao.insertGestionnaire(gestionnaire.copy(idGestionnaire = idGestionnaire))
                }
            }
            contacts.forEach { contact ->
                UUID.randomUUID().let { idContact ->
                    contactMap.add(Pair(idContact, contact.idRemocra!!))
                    referentielDao.insertContact(
                        contact.copy(
                            idContact = idContact,
                            idGestionnaire = gestionnaireMap.find { it.second == contact.idRemocraGestionnaire }?.first,
                        ),
                    )
                }
            }
            contactsRoles.forEach { contactRole ->
                referentielDao.insertContactRole(
                    ContactRole(
                        idContact = contactMap.find { it.second == contactRole.idContact }!!.first,
                        idRole = contactRole.idRole,
                    ),
                )
            }
            hydrants.forEach { hydrant ->
                UUID.randomUUID().let { idHydrant ->
                    hydrantMap.add(Pair(idHydrant, hydrant.idRemocra!!))
                    referentielDao.insertHydrant(
                        hydrant.copy(
                            idHydrant = idHydrant,
                            idGestionnaire = gestionnaireMap.find { it.second == hydrant.idRemocraGestionnaire }?.first,
                            peiCaracteristiques = peiCaracteristiques[hydrant.idRemocra],
                        ),
                    )
                }
            }
            hydrantsAnomalies.forEach { hydrantAnomalie ->
                referentielDao.insertHydrantAnomalie(
                    HydrantAnomalie(
                        idHydrant = hydrantMap.find { it.second == hydrantAnomalie.idHydrant }!!.first,
                        idAnomalie = hydrantAnomalie.idAnomalie,
                    ),
                )
            }

            paramsConf.forEach { paramConf ->
                referentielDao.insertParamConf(paramConf.copy(idParamConf = UUID.randomUUID()))
            }

            typesDroit.forEach { typeDroit ->
                referentielDao.insertTypeDroit(typeDroit.copy(idTypeDroit = UUID.randomUUID()))
            }

            // Gestion des Agents => on récupère la méthode voulue et on stocke l'utilisateur connecté (si méthode 1 ou 2)
            referentielDao.insertParamConf(ParamConf(UUID.randomUUID(), GlobalConstants.GESTION_AGENT, gestionAgents))

            if (gestionAgents == GlobalConstants.UTILISATEUR_CONNECTE_OBLIGATOIRE || gestionAgents == GlobalConstants.UTILISATEUR_CONNECTE) {
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
