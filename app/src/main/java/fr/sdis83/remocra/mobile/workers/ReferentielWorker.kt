package fr.sdis83.remocra.mobile.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import fr.sdis83.remocra.mobile.database.Agent
import fr.sdis83.remocra.mobile.database.Contact
import fr.sdis83.remocra.mobile.database.ContactRole
import fr.sdis83.remocra.mobile.database.Gestionnaire
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.HydrantAnomalie
import fr.sdis83.remocra.mobile.database.ParamConf
import fr.sdis83.remocra.mobile.database.RemocraDatabase
import fr.sdis83.remocra.mobile.database.Role
import fr.sdis83.remocra.mobile.database.TypeDroit
import fr.sdis83.remocra.mobile.database.TypeHydrant
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalie
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalieNature
import fr.sdis83.remocra.mobile.database.TypeHydrantAnomalieNatureSaisie
import fr.sdis83.remocra.mobile.database.TypeHydrantCritere
import fr.sdis83.remocra.mobile.database.TypeHydrantNature
import fr.sdis83.remocra.mobile.database.TypeHydrantNatureDeci
import fr.sdis83.remocra.mobile.database.TypeHydrantSaisie
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
        val retrofitBuilder = ReferentielService.getRetroFitInstance(applicationContext)
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
            val listeNewUpdateDeleteTypeHydrant: ListeNewUpdateDelete<TypeHydrant> =
                gestionReferentiel(
                    dataInRemocra = typesHydrant,
                    idPrimaryRemocra = TypeHydrant::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrant(),
                    arguments = arrayOf(
                        TypeHydrant::code,
                        TypeHydrant::code,
                        TypeHydrant::actif,
                    ),
                )
            val typeHydrantToInsert = mutableListOf<TypeHydrant>()
            listeNewUpdateDeleteTypeHydrant.nouveauxElements.forEach {
                typeHydrantToInsert.add(it.copy(UUID.randomUUID()))
            }

            referentielDao.insertListTypeHydrant(typeHydrantToInsert)

            if (listeNewUpdateDeleteTypeHydrant.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrant.elementsModifies.forEach {
                    referentielDao.updateTypeHydrant(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT NATURE
            val listeNewUpdateDeleteTypeHydrantNature: ListeNewUpdateDelete<TypeHydrantNature> =
                gestionReferentiel(
                    dataInRemocra = typesHydrantNature,
                    idPrimaryRemocra = TypeHydrantNature::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrantNature(),
                    arguments = arrayOf(
                        TypeHydrantNature::idTypeHydrant,
                        TypeHydrantNature::actif,
                        TypeHydrantNature::nom,
                        TypeHydrantNature::code,
                    ),
                )

            val typeHydrantNatureToInsert = mutableListOf<TypeHydrantNature>()
            listeNewUpdateDeleteTypeHydrantNature.nouveauxElements.forEach {
                typeHydrantNatureToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantNature(typeHydrantNatureToInsert)

            if (listeNewUpdateDeleteTypeHydrantNature.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrantNature.elementsModifies.forEach {
                    referentielDao.updateTypeHydrantNature(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT NATURE DECI
            val listeNewUpdateDeleteTypeHydrantNatureDeci: ListeNewUpdateDelete<TypeHydrantNatureDeci> =
                gestionReferentiel(
                    dataInRemocra = typesHydrantNatureDeci,
                    idPrimaryRemocra = TypeHydrantNatureDeci::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrantNatureDeci(),
                    arguments = arrayOf(
                        TypeHydrantNatureDeci::actif,
                        TypeHydrantNatureDeci::code,
                        TypeHydrantNatureDeci::nom,
                    ),
                )
            val typeHydrantNatureDeciToInsert = mutableListOf<TypeHydrantNatureDeci>()
            listeNewUpdateDeleteTypeHydrantNatureDeci.nouveauxElements.forEach {
                typeHydrantNatureDeciToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantNatureDeci(typeHydrantNatureDeciToInsert)

            if (listeNewUpdateDeleteTypeHydrantNatureDeci.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrantNatureDeci.elementsModifies.forEach {
                    referentielDao.updateTypeHydrantNatureDeci(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT SAISIE
            val listeNewUpdateDeleteTypeHydrantSaisie: ListeNewUpdateDelete<TypeHydrantSaisie> =
                gestionReferentiel(
                    dataInRemocra = typesHydrantSaisie,
                    idPrimaryRemocra = TypeHydrantSaisie::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrantSaisie(),
                    arguments = arrayOf(
                        TypeHydrantSaisie::actif,
                        TypeHydrantSaisie::code,
                        TypeHydrantSaisie::nom,
                    ),
                )
            val typeHydrantSaisieToInsert = mutableListOf<TypeHydrantSaisie>()
            listeNewUpdateDeleteTypeHydrantSaisie.nouveauxElements.forEach {
                typeHydrantSaisieToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantSaisie(typeHydrantSaisieToInsert)

            if (listeNewUpdateDeleteTypeHydrantSaisie.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrantSaisie.elementsModifies.forEach {
                    referentielDao.updateTypeHydrantSaisie(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT CRITERE
            val listeNewUpdateDeleteTypeHydrantCritere: ListeNewUpdateDelete<TypeHydrantCritere> =
                gestionReferentiel(
                    dataInRemocra = typesHydrantCritere,
                    idPrimaryRemocra = TypeHydrantCritere::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrantCritere(),
                    arguments = arrayOf(
                        TypeHydrantCritere::actif,
                        TypeHydrantCritere::code,
                        TypeHydrantCritere::nom,
                    ),
                )

            val typeHydrantCritereToInsert = mutableListOf<TypeHydrantCritere>()
            listeNewUpdateDeleteTypeHydrantCritere.nouveauxElements.forEach {
                typeHydrantCritereToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantCritere(typeHydrantCritereToInsert)

            if (listeNewUpdateDeleteTypeHydrantCritere.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrantCritere.elementsModifies.forEach {
                    referentielDao.updateTypeHydrantCritere(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT ANOMALIE
            val listeNewUpdateDeleteTypeHydrantAnomalie: ListeNewUpdateDelete<TypeHydrantAnomalie> =
                gestionReferentiel(
                    dataInRemocra = typesHydrantAnomalie,
                    idPrimaryRemocra = TypeHydrantAnomalie::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrantAnomalie(),
                    arguments = arrayOf(
                        TypeHydrantAnomalie::actif,
                        TypeHydrantAnomalie::code,
                        TypeHydrantAnomalie::nom,
                        TypeHydrantAnomalie::idCritere,
                    ),
                )
            val typeHydrantAnomalieToInsert = mutableListOf<TypeHydrantAnomalie>()
            listeNewUpdateDeleteTypeHydrantAnomalie.nouveauxElements.forEach {
                typeHydrantAnomalieToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantAnomalie(typeHydrantAnomalieToInsert)

            if (listeNewUpdateDeleteTypeHydrantAnomalie.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrantAnomalie.elementsModifies.forEach {
                    referentielDao.updateTypeHydrantAnomalie(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.idCritere,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE HYDRANT ANOMALIE NATURE
            val listeNewUpdateDeleteTypeHydrantAnomalieNature: ListeNewUpdateDelete<TypeHydrantAnomalieNature> =
                gestionReferentiel(
                    dataInRemocra = typesHydrantAnomalieNature,
                    idPrimaryRemocra = TypeHydrantAnomalieNature::idRemocra,
                    dataInMobile = referentielDao.getListTypeHydrantAnomalieNature(),
                    arguments = arrayOf(
                        TypeHydrantAnomalieNature::valIndispoHbe,
                        TypeHydrantAnomalieNature::valIndispoTerrestre,
                        TypeHydrantAnomalieNature::valIndispoAdmin,
                        TypeHydrantAnomalieNature::idTypeHydrantAnomalie,
                        TypeHydrantAnomalieNature::idTypeHydrantNature,
                    ),
                )

            val typeHydrantAnomalieNatureToInsert = mutableListOf<TypeHydrantAnomalieNature>()
            listeNewUpdateDeleteTypeHydrantAnomalieNature.nouveauxElements.forEach {
                typeHydrantAnomalieNatureToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantAnomalieNature(typeHydrantAnomalieNatureToInsert)

            if (listeNewUpdateDeleteTypeHydrantAnomalieNature.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteTypeHydrantAnomalieNature.elementsModifies.forEach {
                    referentielDao.updateTypeHydrantAnomalieNature(
                        it.idRemocra,
                        it.idTypeHydrantNature,
                        it.idTypeHydrantAnomalie,
                        it.valIndispoTerrestre,
                        it.valIndispoHbe,
                        it.valIndispoAdmin,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE ANOMALIE NATURE SAISIE

            referentielDao.truncateTypeHydrantAnomalieNatureSaisie()

            val typeHydrantAnomalieNatureSaisieToInsert = mutableListOf<TypeHydrantAnomalieNatureSaisie>()
            typesHydrantAnomalieNatureSaisie.forEach {
                typeHydrantAnomalieNatureSaisieToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeHydrantAnomalieNatureSaisie(typeHydrantAnomalieNatureSaisieToInsert)

            // ///////////////////////////////////////////////////////////////////////////////////////////ROLE
            val listeNewUpdateDeleteRole: ListeNewUpdateDelete<Role> = gestionReferentiel(
                dataInRemocra = roles,
                idPrimaryRemocra = Role::idRemocra,
                dataInMobile = referentielDao.getListRole(),
                arguments = arrayOf(Role::actif, Role::code, Role::nom),
            )

            val roleToInsert = mutableListOf<Role>()
            listeNewUpdateDeleteRole.nouveauxElements.forEach {
                roleToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListRole(roleToInsert)

            if (listeNewUpdateDeleteRole.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteRole.elementsModifies.forEach {
                    referentielDao.updateRole(
                        it.idRemocra,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////GESTIONNAIRE
            val listeNewUpdateDeleteGestionnaire: ListeNewUpdateDelete<Gestionnaire> =
                gestionReferentiel(
                    dataInRemocra = gestionnaires,
                    idPrimaryRemocra = Gestionnaire::idRemocra,
                    dataInMobile = referentielDao.getListGestionnaire(),
                    arguments = arrayOf(
                        Gestionnaire::actif,
                        Gestionnaire::code,
                        Gestionnaire::nom,
                    ),
                )
            val gestionnaireToInsert = mutableListOf<Gestionnaire>()
            listeNewUpdateDeleteGestionnaire.nouveauxElements.forEach {
                gestionnaireToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListGestionnaire(gestionnaireToInsert)

            if (listeNewUpdateDeleteGestionnaire.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteGestionnaire.elementsModifies.forEach {
                    referentielDao.updateGestionnaire(
                        it.idRemocra!!,
                        it.nom,
                        it.actif,
                        it.code,
                    )
                }
            }

            val listeGestionnaire = referentielDao.getListGestionnaire()

            // ///////////////////////////////////////////////////////////////////////////////////////////CONTACT
            val listeNewUpdateDeleteContact: ListeNewUpdateDelete<Contact> = gestionReferentiel(
                dataInRemocra = contacts,
                idPrimaryRemocra = Contact::idRemocra,
                dataInMobile = referentielDao.getListContact(),
                arguments = arrayOf(
                    Contact::idRemocraGestionnaire,
                    Contact::civilite,
                    Contact::nom,
                    Contact::prenom,
                    Contact::fonction,
                    Contact::email,
                    Contact::numeroVoie,
                    Contact::suffixeVoie,
                    Contact::voie,
                    Contact::codePostal,
                    Contact::ville,
                    Contact::lieuDit,
                    Contact::pays,
                    Contact::telephone,
                ),
            )
            val contactToInsert = mutableListOf<Contact>()
            listeNewUpdateDeleteContact.nouveauxElements.forEach {
                contactToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListContact(contactToInsert)

            if (listeNewUpdateDeleteContact.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteContact.elementsModifies.forEach {
                    referentielDao.updateContact(
                        idRemocra = it.idRemocra!!,
                        idRemocraGestionnaire = it.idRemocraGestionnaire!!,
                        idGestionnaire = listeGestionnaire.first { gestionnaire -> gestionnaire.idRemocra == it.idRemocraGestionnaire }.idGestionnaire,
                        fonction = it.fonction,
                        civilite = it.civilite,
                        nom = it.nom,
                        prenom = it.prenom,
                        numeroVoie = it.numeroVoie,
                        suffixeVoie = it.suffixeVoie,
                        voie = it.voie,
                        lieuDit = it.lieuDit,
                        codePostal = it.codePostal,
                        ville = it.ville,
                        pays = it.pays,
                        telephone = it.telephone,
                        email = it.email,
                    )
                }
            }

            val listeContact = referentielDao.getListContact()

            // ///////////////////////////////////////////////////////////////////////////////////////////CONTACT ROLE
            referentielDao.truncateContactRole()
            val contactRoleToInsert = mutableListOf<ContactRole>()
            contactsRoles.forEach {
                contactRoleToInsert.add(
                    ContactRole(
                        listeContact.first { c -> c.idRemocra == it.idContact }.idContact,
                        it.idRole,
                    ),
                )
            }
            referentielDao.insertListContactRole(contactRoleToInsert.toSet().toList())

            // ///////////////////////////////////////////////////////////////////////////////////////////HYDRANT
            referentielDao.deleteHydrantsNonUtilises()
            hydrants.forEach {
                it.peiCaracteristiques = peiCaracteristiques[it.idRemocra]
            }
            val listeNewUpdateDeleteHydrant: ListeNewUpdateDelete<Hydrant> = gestionReferentiel(
                dataInRemocra = hydrants,
                idPrimaryRemocra = Hydrant::idRemocra,
                dataInMobile = referentielDao.getListHydrant(),
                arguments = arrayOf(
                    Hydrant::idNature,
                    Hydrant::idRemocraGestionnaire,
                    Hydrant::idNatureDeci,
                    Hydrant::adresseComplete,
                    Hydrant::lat,
                    Hydrant::lon,
                    Hydrant::x,
                    Hydrant::y,
                    Hydrant::numero,
                    Hydrant::dispoHbe,
                    Hydrant::dispoTerrestre,
                    Hydrant::peiCaracteristiques,
                ),
            )

            val hydrantToInsert = mutableListOf<Hydrant>()
            listeNewUpdateDeleteHydrant.nouveauxElements.forEach {
                hydrantToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListHydrant(hydrantToInsert)
            if (listeNewUpdateDeleteHydrant.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteHydrant.elementsModifies.forEach {
                    referentielDao.updateHydrant(
                        idRemocra = it.idRemocra!!,
                        idNatureDeci = it.idNatureDeci,
                        idNature = it.idNature,
                        dispoHbe = it.dispoHbe,
                        dispoTerrestre = it.dispoTerrestre,
                        x = it.x,
                        y = it.y,
                        lon = it.lon,
                        lat = it.lat,
                        numero = it.numero,
                        code = it.code,
                        adresseComplete = it.adresseComplete,
                        observation = it.observation,
                        idRemocraGestionnaire = it.idRemocraGestionnaire,
                        idGestionnaire = if (it.idRemocraGestionnaire != null) {
                            listeGestionnaire.first { gestionnaire -> gestionnaire.idRemocra == it.idRemocraGestionnaire }.idGestionnaire
                        } else {
                            null
                        },
                        peiCaracteristiques = peiCaracteristiques[it.idRemocra],
                    )
                }
            }

            val listHydrant = referentielDao.getListHydrant()

            // ///////////////////////////////////////////////////////////////////////////////////////////HYDRANT ANOMALIE
            referentielDao.truncateHydrantAnomalie()
            val hydrantAnomalieToInsert = mutableListOf<HydrantAnomalie>()
            hydrantsAnomalies.forEach {
                hydrantAnomalieToInsert.add(
                    HydrantAnomalie(
                        listHydrant.first { h -> h.idRemocra == it.idHydrant }.idHydrant,
                        it.idAnomalie,
                    ),
                )
            }
            referentielDao.insertListHydrantAnomalie(hydrantAnomalieToInsert)

            // ///////////////////////////////////////////////////////////////////////////////////////////PARAM CONF
            val listeNewUpdateDeleteParamConf: ListeNewUpdateDelete<ParamConf> =
                gestionReferentiel(
                    dataInRemocra = paramsConf,
                    idPrimaryRemocra = null,
                    cle = ParamConf::cle,
                    dataInMobile = referentielDao.getListParamConf(),
                    arguments = arrayOf(ParamConf::cle, ParamConf::valeur),
                )
            val paramConfToInsert = mutableListOf<ParamConf>()
            listeNewUpdateDeleteParamConf.nouveauxElements.forEach {
                paramConfToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListParamConf(paramConfToInsert)

            if (listeNewUpdateDeleteParamConf.elementsModifies.isNotEmpty()) {
                listeNewUpdateDeleteParamConf.elementsModifies.forEach {
                    referentielDao.updateParamConf(
                        it.cle,
                        it.valeur,
                    )
                }
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////TYPE DROIT
            val listeNewUpdateDeleteTypeDroit: ListeNewUpdateDelete<TypeDroit> =
                gestionReferentiel(
                    dataInRemocra = typesDroit,
                    cle = TypeDroit::code,
                    idPrimaryRemocra = null,
                    dataInMobile = referentielDao.getListTypeDroit(),
                    arguments = arrayOf(TypeDroit::code),
                )

            val typeDroitToInsert = mutableListOf<TypeDroit>()
            listeNewUpdateDeleteTypeDroit.nouveauxElements.forEach {
                typeDroitToInsert.add(it.copy(UUID.randomUUID()))
            }
            referentielDao.insertListTypeDroit(typeDroitToInsert)

            // //////////////////////////////////////////////////////////////////////////////////////// Gestion des suppressions

            referentielDao.apply {
                deleteHydrant(listeNewUpdateDeleteHydrant.elementsSupprimes.map { it.idRemocra!! })
                deleteContact(listeNewUpdateDeleteContact.elementsSupprimes.map { it.idRemocra!! })
                deleteGestionnaire(listeNewUpdateDeleteGestionnaire.elementsSupprimes.map { it.idRemocra!! })
                deleteRole(listeNewUpdateDeleteRole.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrantNature(listeNewUpdateDeleteTypeHydrantNature.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrantNatureDeci(listeNewUpdateDeleteTypeHydrantNatureDeci.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrant(listeNewUpdateDeleteTypeHydrant.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrantAnomalieNature(listeNewUpdateDeleteTypeHydrantAnomalieNature.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrantAnomalie(listeNewUpdateDeleteTypeHydrantAnomalie.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrantCritere(listeNewUpdateDeleteTypeHydrantCritere.elementsSupprimes.map { it.idRemocra })
                deleteTypeHydrantSaisie(listeNewUpdateDeleteTypeHydrantSaisie.elementsSupprimes.map { it.idRemocra })
                deleteParamConf(listeNewUpdateDeleteParamConf.elementsSupprimes.map { it.cle })
            }

            // On regarde les agents
            if (referentielDao.getAgentConnecte() == null) {
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

    private fun <T> gestionReferentiel(
        dataInRemocra: List<T>,
        idPrimaryRemocra: (T.() -> Long?)?,
        cle: (T.() -> String)? = null,
        dataInMobile: List<T>,
        vararg arguments: T.() -> Any?,
    ): ListeNewUpdateDelete<T> {
        val property = idPrimaryRemocra ?: cle!!

        val nouveauxElements = dataInRemocra.filterNot { data -> dataInMobile.map { it.property() }.contains(data.property()) }

        // Get suprimées
        val elementsSupprimes = dataInMobile.filterNot { data -> dataInRemocra.map { it.property() }.contains(data.property()) }

        // Get modifiées
        val elementsModifies = dataInRemocra.minus(nouveauxElements).filter { data ->
            dataInMobile.none { t -> t.property() == data.property() && equalIn(data, t, *arguments) }
        }

        return ListeNewUpdateDelete(
            nouveauxElements = nouveauxElements,
            elementsModifies = elementsModifies,
            elementsSupprimes = elementsSupprimes,
        )
    }

    class ListeNewUpdateDelete<T>(
        val nouveauxElements: List<T>,
        val elementsModifies: List<T>,
        val elementsSupprimes: List<T>,
    )

    fun <T> equalIn(t: T, t2: T, vararg arguments: T.() -> Any?): Boolean {
        val argumentsList = arguments.toList()
        return argumentsList.all {
            it(t) == it(t2)
        }
    }
}
