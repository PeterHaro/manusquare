package sparqlquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import edm.Attribute;
import edm.Certification;
import query.IMQuery;
import sparqlresult.SparqlRecord_IM;
import supplier.InnovationManager;
import utilities.StringUtilities;

public class TripleStoreConnection_IM {

	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "INNOVATIONMANAGEMENT";

	//configuration of the MANUSQUARE Semantic Infrastructure
	static String WorkshopSpaql = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0";
	static String SPARQL_ENDPOINT = WorkshopSpaql; //"http://116.203.187.118/semantic-registry-test/repository/manusquare?infer=false&limit=0&offset=0";
	static String Workshop_token = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static String AUTHORISATION_TOKEN = Workshop_token; //"c5ec0a8b494a30ed41d4d6fe3107990b";

	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://116.203.187.118/semantic-registry/repository/manusquare/ontology.owl");
	
	public static List<InnovationManager> createInnovationManagerData(IMQuery query, boolean testing, OWLOntology onto) {
		String sparql_endpoint_by_env = System.getenv("ONTOLOGY_ADDRESS");
		if (sparql_endpoint_by_env != null) {
			SPARQL_ENDPOINT = sparql_endpoint_by_env;
		}
		if (System.getenv("ONTOLOGY_KEY") != null) {
			AUTHORISATION_TOKEN = System.getenv("ONTOLOGY_KEY");
		}

		Repository repository;

		if (!testing) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", AUTHORISATION_TOKEN);
			headers.put("accept", "application/JSON");
			repository = new SPARQLRepository(SPARQL_ENDPOINT);
			repository.initialize();
			((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);
		} else {
			//connect to GraphDB
			repository = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
			HTTPRepository repo = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
			System.out.println(repo.getRepositoryURL());
			System.out.println(repo.getPreferredRDFFormat());
			repository.initialize();
			System.out.println(repository.isInitialized());
		}

		String strQuery = SparqlQuery_IM.createSparqlQuery_IM(query, onto);

		//open connection to GraphDB and run SPARQL query
		Set<SparqlRecord_IM> recordSet = new HashSet<SparqlRecord_IM>();


		try (RepositoryConnection conn = repository.getConnection()) {
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

			//if querying the local KB, we need to set setIncludeInferred to false, otherwise inference will include irrelevant results.
			//when querying the Semantic Infrastructure the non-inference is set in the http parameters.
			if (testing) {
				//do not include inferred statements from the KB
				tupleQuery.setIncludeInferred(false);
			}


			try (TupleQueryResult result = tupleQuery.evaluate()) {

				SparqlRecord_IM record = null;

				while (result.hasNext()) {

					BindingSet solution = result.next();
					
					record = new SparqlRecord_IM();

					record.setSupplierId(StringUtilities.stripIRI(solution.getValue("supplier").stringValue().replaceAll("\\s+", "")));
					record.setSupplierName(StringUtilities.stripIRI(solution.getValue("supplierName").stringValue().replaceAll("\\s+", "")));
					record.setInnovationPhase(StringUtilities.stripIRI(solution.getValue("innovationPhaseType").stringValue().replaceAll("\\s+", "")));
					record.setInnovationType(StringUtilities.stripIRI(solution.getValue("innovationTypeType").stringValue().replaceAll("\\s+", "")));
					record.setSkill(StringUtilities.stripIRI(solution.getValue("skillType").stringValue().replaceAll("\\s+", "")));
					record.setSector(StringUtilities.stripIRI(solution.getValue("innovationSectorType").stringValue().replaceAll("\\s+", "")));

					if (solution.getValue("certificationType") != null) {
						record.setCertification(StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));

					}
					

					recordSet.add(record);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//close connection to KB repository
		repository.shutDown();



		//get unique supplier ids used for constructing the supplier structure below
		Set<String> supplierIds = new HashSet<String>();

		for (SparqlRecord_IM sr : recordSet) {
			supplierIds.add(sr.getSupplierId());
		}

		Certification certification = null;
		String innovationPhase = null;
		String innovationType = null;
		String skill = null;
		String sector = null;
		InnovationManager innovationManager = null;
		List<InnovationManager> innovationManagerList = new ArrayList<InnovationManager>();

		
		for (String id : supplierIds) {


			List<Certification> certifications = new ArrayList<Certification>();
			List<String> innovationPhases = new ArrayList<String>();
			List<String> innovationTypes = new ArrayList<String>();
			List<String> skills = new ArrayList<String>();
			List<String> sectors = new ArrayList<String>();

			for (SparqlRecord_IM sr : recordSet) {

				if (sr.getSupplierId().equals(id)) {
										
					innovationManager = new InnovationManager();

					//add certifications
					certification = new Certification(sr.getCertification());
					if (certification.getId() != null && !certifications.contains(certification)) {
						certifications.add(certification);
					}
					
					//TODO: Not sure all these if clauses are needed, according to SUPSI specs innovationPhase,
					//innovationType, skill and sector are all mandatory if the innovation capability is included.
					
					//set supplier name
					
					
					//add innovationPhases
					innovationPhase = sr.getInnovationPhase();
					if (innovationPhase != null && !innovationPhases.contains(innovationPhase)) {
						innovationPhases.add(innovationPhase);
					}
					
					//add innovationTypes
					innovationType = sr.getInnovationType();
					if (innovationType != null && !innovationTypes.contains(innovationType)) {
						innovationTypes.add(innovationType);
					}
					
					//add skills
					skill = sr.getSkill();
					if (skill != null && !skills.contains(skill)) {
						skills.add(skill);
					}
					
					//add sectors
					sector = sr.getSector();
					if (sector != null && !sectors.contains(sector)) {
						sectors.add(sector);
					}
					

					innovationManager = new InnovationManager(id, sr.getSupplierName(), certifications, skills, innovationPhases,
							innovationTypes, sectors);
					
				}
			}


			innovationManagerList.add(innovationManager);
		}
		

		return innovationManagerList;

	}



}
