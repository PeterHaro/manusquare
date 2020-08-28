package sparql;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import edm.Attribute;
import edm.Certification;
import edm.Material;
import edm.Process;
import edm.SparqlRecord;
import edm.SparqlRecord_IM;

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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import query.ConsumerQuery;
import query.InnovationManagementQuery;
import supplierdata.InnovationManager;
import supplierdata.Supplier;

import java.util.*;
import java.util.Map.Entry;

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
	
	public static List<InnovationManager> createInnovationManagerData(InnovationManagementQuery query, boolean testing, OWLOntology onto) {
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

					record.setSupplierId(stripIRI(solution.getValue("supplier").stringValue().replaceAll("\\s+", "")));
					record.setInnovationPhase(stripIRI(solution.getValue("innovationPhaseType").stringValue().replaceAll("\\s+", "")));
					record.setInnovationType(stripIRI(solution.getValue("innovationTypeType").stringValue().replaceAll("\\s+", "")));
					record.setSkill(stripIRI(solution.getValue("skillType").stringValue().replaceAll("\\s+", "")));
					record.setSector(stripIRI(solution.getValue("innovationSectorType").stringValue().replaceAll("\\s+", "")));

					if (solution.getValue("certificationType") != null) {
						record.setCertification(stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));

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
					

					innovationManager = new InnovationManager(id, certifications, skills, innovationPhases,
							innovationTypes, sectors);
					
				}
			}


			innovationManagerList.add(innovationManager);
		}
		
		System.out.println("Testing results from SPARQL Query");
		//List<InnovationManager>
		for (InnovationManager im : innovationManagerList) {
			System.out.println("\nInnovation Manager ID: " + im.getId());
			System.out.println("InnovationPhases: " + im.getInnovationPhases());
			System.out.println("InnovationTypes: " + im.getInnovationTypes());
			System.out.println("Skills: " + im.getSkills());
			System.out.println("Sectors: " + im.getSectors());
			
			
		}

		return innovationManagerList;

	}

	

	/**
	 * Removes the IRIs in front of processes etc. retrieved from the Semantic Infrastructure
	 *
	 * @param inputConcept an input ontology concept (with full IRI)
	 * @return ontology concept with the IRI removed
	 * Nov 5, 2019
	 */
	private static String stripIRI(String inputConcept) {
		String returnedConceptName = null;
		if (inputConcept.contains("http://manusquare.project.eu/industrial-manusquare#")) {
			returnedConceptName = inputConcept.replaceAll("http://manusquare.project.eu/industrial-manusquare#", "");
		} else if (inputConcept.contains("http://manusquare.project.eu/core-manusquare#")) {
			returnedConceptName = inputConcept.replaceAll("http://manusquare.project.eu/core-manusquare#", "");
		} else {
			returnedConceptName = inputConcept;
		}
		return returnedConceptName;

	}

	private static Attribute alignValues (Attribute supplierAttribute, Attribute consumerAttribute) {

		double newValue = 0;

		if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			}


		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("µm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("µm")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			}
		}

		return supplierAttribute;

	}

}
