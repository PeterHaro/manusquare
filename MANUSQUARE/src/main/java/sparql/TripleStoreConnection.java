package sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import edm.Certification;
import edm.Material;
import edm.Process;
import edm.SparqlRecord;
import query.ConsumerQuery;
import supplierdata.Supplier;

public class TripleStoreConnection {
	
	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "MANUSQUARE-INFERENCE-TESTDATA";

	//configuration of the MANUSQUARE Semantic Infrastructure
	static String WorkshopSpaql = "http://manusquare.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0";
	static String SPARQL_ENDPOINT = WorkshopSpaql; //"http://116.203.187.118/semantic-registry-test/repository/manusquare?infer=false&limit=0&offset=0";
	static String Workshop_token = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static String AUTHORISATION_TOKEN = Workshop_token; //"c5ec0a8b494a30ed41d4d6fe3107990b";

	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://116.203.187.118/semantic-registry/repository/manusquare/ontology.owl");
	
	/**
	 * Retrieves (relevant) data / concepts from the Semantic Infrastructure using the content of a consumer query as input.
	 *
	 * @param query content of a consumer query
	 * @return list of suppliers along with the processes (including relevant materials) and certifications registered in the Semantic Infrastructure.
	 * Nov 9, 2019
	 * @throws OWLOntologyCreationException 
	 */
	public static List<Supplier> createSupplierData(ConsumerQuery query, boolean testing, OWLOntology onto) throws OWLOntologyCreationException {
		Repository repository;

		//use name of processes in query to retrieve subset of relevant supplier data from semantic infrastructure
		List<String> processNames = new ArrayList<String>();

		if (query.getProcesses() == null || query.getProcesses().isEmpty()) {			
			System.err.println("There are no processes specified!");			
		} else {			
			for (Process process : query.getProcesses()) {				
				processNames.add(process.getName());			
			}
		}

		long startTime = System.currentTimeMillis();

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

		String strQuery = SparqlQuery.createSparqlQuery(query, onto);

		//open connection to GraphDB and run SPARQL query
		Set<SparqlRecord> recordSet = new HashSet<SparqlRecord>();
		SparqlRecord record;
		int counter = 0;

		try (RepositoryConnection conn = repository.getConnection()) {
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

			//if querying the local KB, we need to set setIncludeInferred to false, otherwise inference will include irrelevant results.
			//when querying the Semantic Infrastructure the non-inference is set in the http parameters.
			if (testing) {
				//do not include inferred statements from the KB
				tupleQuery.setIncludeInferred(false);
			}

			try (TupleQueryResult result = tupleQuery.evaluate()) {
				while (result.hasNext()) {
					Map<String, String> attributeMap = new HashMap<String, String>();
					counter++;
					BindingSet solution = result.next();  

					Set<String> bindings = solution.getBindingNames();

					//if there are attributes in the query results
					if (containsAttributes(bindings) == true) {

						for (String s : bindings) {
														
							if (s.endsWith("Attr")) {					
								attributeMap.put(s.replace("Attr", ""), solution.getValue(s).stringValue());
							}
						}
					} 

					//omit the NamedIndividual types from the query result
					if (solution.hasBinding("materialType") ) {
						if (!solution.getValue("processType").stringValue().equals("http://www.w3.org/2002/07/owl#NamedIndividual")
								&& !solution.getValue("certificationType").stringValue().equals("http://www.w3.org/2002/07/owl#NamedIndividual")
								&& !solution.getValue("materialType").stringValue().equals("http://www.w3.org/2002/07/owl#NamedIndividual")) {

							record = new SparqlRecord();
							record.setSupplierId(solution.getValue("supplier").stringValue().replaceAll("\\s+", ""));
							record.setProcess(stripIRI(solution.getValue("processType").stringValue().replaceAll("\\s+", "")));
							record.setMaterial(stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", "")));                           
							record.setCertification(stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));

							if (containsAttributes(bindings) == true) {
								record.setAttributeWeightMap(attributeMap);
							}
							recordSet.add(record);
						}
					} else {
						if (!solution.getValue("processType").stringValue().equals("http://www.w3.org/2002/07/owl#NamedIndividual")
								&& !solution.getValue("certificationType").stringValue().equals("http://www.w3.org/2002/07/owl#NamedIndividual")) {

							record = new SparqlRecord();
							record.setSupplierId(solution.getValue("supplier").stringValue().replaceAll("\\s+", ""));
							record.setProcess(stripIRI(solution.getValue("processType").stringValue().replaceAll("\\s+", "")));
							record.setCertification(stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));

							if (containsAttributes(bindings) == true) {
								record.setAttributeWeightMap(attributeMap);
							}

							recordSet.add(record);
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (testing == true) {
			System.out.println("Number of results returned from Semantic Infrastructure: " + counter + "\n");
		}
		//close connection to KB repository
		repository.shutDown();

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		if (testing == true) {
			System.out.println("The SPARQL querying process took " + elapsedTime / 1000 + " seconds.");
		}

		//get unique supplier ids used for constructing the supplier structure below
		Set<String> supplierIds = new HashSet<String>();
		for (SparqlRecord sr : recordSet) {
			supplierIds.add(sr.getSupplierId());
		}

		Certification certification = null;
		Supplier supplier = null;
		List<Supplier> suppliersList = new ArrayList<Supplier>();

		Map<String, SetMultimap<Object, Object>> supplierToProcessMap = new HashMap<String, SetMultimap<Object, Object>>();

		for (String id : supplierIds) {
			SetMultimap<Object, Object> map = HashMultimap.create();
			String supplierID = null;

			for (SparqlRecord sr : recordSet) {
				if (sr.getSupplierId().equals(id)) {
					map.put(sr.getProcess(), sr.getMaterial());
					supplierID = sr.getSupplierId();
				}
			}
			supplierToProcessMap.put(supplierID, map);
		}

		Process process = null;

		//create supplier objects (supplier id, processes (including materials) and certifications) based on the multimap created in the previous step
		for (String id : supplierIds) {
			SetMultimap<Object, Object> processAndMaterialMap = null;

			List<Certification> certifications = new ArrayList<Certification>();
			List<Process> processes = new ArrayList<Process>();

			for (SparqlRecord sr : recordSet) {
				if (sr.getSupplierId().equals(id)) {
					//add certifications
					certification = new Certification(sr.getCertification());
					if (!certifications.contains(certification)) {
						certifications.add(certification);
					}

					//add processes and associated materials
					processAndMaterialMap = supplierToProcessMap.get(sr.getSupplierId());
					String processName = null;
					Set<Object> list = new HashSet<Object>();

					//iterate processAndMaterialMap and extract process and relevant materials for that process
					for (Entry<Object, Collection<Object>> e : processAndMaterialMap.asMap().entrySet()) {
						Set<Material> materialsSet = new HashSet<Material>();
						//get list/set of materials
						list = new HashSet<>(e.getValue());

						//transform to Set<Material>
						//FIXME: Is this transformation really necessary? Why not stick to *either* list or set?
						for (Object o : list) {
							if (o != null) { //Audun: if there are no suppliers materials retrieved from SPARQL, don´t add null-valued Material objects to the set of materials (should be handled properly with !isEmpty check in SimilarityMeasures.java)
								materialsSet.add(new Material((String) o));
							}
						}

						processName = (String) e.getKey();

						//add relevant set of materials together with process name
						process = new Process(processName, materialsSet, sr.getAttributeWeightMap());

						//add processes
						if (!processes.contains(process)) {
							processes.add(process);
						}

					}
					supplier = new Supplier(id, processes, certifications);
				}
			}
			suppliersList.add(supplier);
		}

		return suppliersList;

	}

	/**
	 * Checks if the bindings associated with the SPARQL results contains attributes. Requires that the binding variable names end with "Attr". 
	 * @param bindings
	 * @return
       Feb 9, 2020
	 */
	private static boolean containsAttributes (Set<String> bindings) {

		boolean containsAttribute = false;

		for (String s : bindings) {
			if (s.endsWith("Attr")) {
				containsAttribute = true;
			}
		}

		return containsAttribute;

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

}
