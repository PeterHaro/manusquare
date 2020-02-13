package ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.MutableGraph;
import com.google.gson.GsonBuilder;

import edm.Certification;
import edm.Material;
import edm.Process;
import edm.SparqlRecord;
import graph.SimpleGraph;
import query.ConsumerQuery;
import similarity.MatchingResult;
import similarity.SimilarityMeasures;
import similarity.SimilarityMethods;
import sparql.SparqlQuery;
import supplierdata.Supplier;
import utilities.MathUtils;
import utilities.StringUtilities;

/**
 * Contains functionality for performing the semantic matching in the Matchmaking service.
 *
 * @author audunvennesland
 */
public class SemanticMatching_MVP {


	static SimilarityMethods similarityMethod = SimilarityMethods.WU_PALMER;

	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "AttributeTest2";

	//configuration of the MANUSQUARE Semantic Infrastructure
	//OLD SPARQL ENDPOINT static String SPARQL_ENDPOINT = "http://116.203.187.118/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0";
	static String WorkshopSpaql = "http://manusquare.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0";
	static String SPARQL_ENDPOINT = WorkshopSpaql; //"http://116.203.187.118/semantic-registry-test/repository/manusquare?infer=false&limit=0&offset=0";
	static String Workshop_token = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static String AUTHORISATION_TOKEN = Workshop_token; //"c5ec0a8b494a30ed41d4d6fe3107990b";

	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://116.203.187.118/semantic-registry/repository/manusquare/ontology.owl");

	/**
	 * Matches a consumer query against a set of resources offered by suppliers and returns a ranked list of the [numResult] suppliers having the highest semantic similarity as a JSON file.
	 *
	 * @param inputJson  an input json file (or json string) holding process(es) and certifications from the RFQ creation process.
	 * @param numResults number of relevant suppliers to be returned from the matching
	 * @param isWeighted true if the facets (process, material, certifications) should be weighted, false if not.
	 * @throws IOException
	 * @throws OWLOntologyStorageException Oct 31, 2019
	 */
	public static void performSemanticMatching(String inputJson, int numResults, BufferedWriter writer, boolean testing, boolean isWeighted, double hard_coded_weight) throws OWLOntologyStorageException, IOException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		String sparql_endpoint_by_env = System.getenv("ONTOLOGY_ADDRESS");
		if (sparql_endpoint_by_env != null) {
			SPARQL_ENDPOINT = sparql_endpoint_by_env;
		}
		if (System.getenv("ONTOLOGY_KEY") != null) {
			AUTHORISATION_TOKEN = System.getenv("ONTOLOGY_KEY");
		}


		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntology(MANUSQUARE_ONTOLOGY_IRI);
		} catch (OWLOntologyCreationException e) {
			System.err.println("It seems the MANUSQUARE ontology is not available from " + MANUSQUARE_ONTOLOGY_IRI.toString() + "\n");
			e.printStackTrace();
		}

		//save a local copy of the ontology for constructing an (up-to-date) (guava) graph used for the wu-palmer computation. 
		File localOntoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");

		manager.saveOntology(Objects.requireNonNull(ontology), IRI.create(localOntoFile.toURI())); 

		ConsumerQuery query = ConsumerQuery.createConsumerQuery(inputJson, ontology); // get process(s) from the query and use them to subset the supplier records in the SPARQL query
		List<String> processes = new ArrayList<>();
		for (Process p : query.getProcesses()) {
			processes.add(p.getName());
		}

		//create graph using Guava´s graph library instead of using Neo4j
		MutableGraph<String> graph = null;
		try {
			graph = SimpleGraph.createGraph(ontology);
		} catch (OWLOntologyCreationException e) {
			System.err.println("It seems the MANUSQUARE ontology is not available from " + MANUSQUARE_ONTOLOGY_IRI.toString() + "\n");
			e.printStackTrace();
		}

		//re-organise the SupplierResourceRecords so that we have ( Supplier (1) -> Resource (*) )
		List<Supplier> supplierData = createSupplierData(query, testing);

		Map<Supplier, Double> supplierScores = new HashMap<Supplier, Double>();
		//for each supplier get the list of best matching processes (and certifications)
		List<Double> supplierSim = new LinkedList<Double>();
		for (Supplier supplier : supplierData) {
			supplierSim = SimilarityMeasures.computeSemanticSimilarity(query, supplier, ontology, similarityMethod, isWeighted, graph, testing, hard_coded_weight);
			//get the highest score for the process chains offered by supplier n
			supplierScores.put(supplier, getHighestScore(supplierSim));
		}

		//extract the n suppliers with the highest similarity scores
		Map<String, Double> bestSuppliers = extractBestSuppliers(supplierScores, numResults);

		//prints the n best suppliers in ranked order to JSON
		writeResultToOutput(bestSuppliers, writer);

		//prints additional data to console for testing/validation
		if (testing == true) {
			printResultsToConsole(supplierData, query, supplierScores, numResults);
		}

	}


	/**
	 * Retrieves (relevant) data / concepts from the Semantic Infrastructure using the content of a consumer query as input.
	 *
	 * @param query content of a consumer query
	 * @return list of suppliers along with the processes (including relevant materials) and certifications registered in the Semantic Infrastructure.
	 * Nov 9, 2019
	 */
	private static List<Supplier> createSupplierData(ConsumerQuery query, boolean testing) {
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
			//repo.setPreferredRDFFormat();
			System.out.println(repo.getRepositoryURL());
			System.out.println(repo.getPreferredRDFFormat());
			repository.initialize();
			//repository.getConnection().add();
			System.out.println(repository.isInitialized());
		}

		//creates a SPARQL query that is run against the Semantic Infrastructure
		//String strQuery = SparqlQuery.querySI_MVP(processNames);

		//creates a SPARQL query that is run against the Semantic Infrastructure
		//09.02.2020: Added dynamicAttributeQuery
		String strQuery = SparqlQuery.dynamicAttributeQuery(query);

		//System.out.println(strQuery);

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
					
					//testing method for dynamic attribute weighting
					
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
							
							
							//added 11.02.2020
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
							
							//added 11.02.2020
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

		// TODO: Replace the use of Map with Set<process, Set<Material, Set<Attribute>> and establish Supplier objects without the use of Map.
		//create a map of processes and materials relevant for each supplier
		Map<String, SetMultimap<Object, Object>> multimap = new HashMap<String, SetMultimap<Object, Object>>();

		for (String id : supplierIds) {
			SetMultimap<Object, Object> map = HashMultimap.create();

			String supplierID = null;

			for (SparqlRecord sr : recordSet) {

				if (sr.getSupplierId().equals(id)) {

					map.put(sr.getProcess(), sr.getMaterial());

					supplierID = sr.getSupplierId();

				}

			}
			multimap.put(supplierID, map);
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
					processAndMaterialMap = multimap.get(sr.getSupplierId());

					String processName = null;

					Set<Object> list = new HashSet<Object>();

					//iterate processAndMaterialMap and extract process and relevant materials for that process
					for (Entry<Object, Collection<Object>> e : processAndMaterialMap.asMap().entrySet()) {

						Set<Material> materialsSet = new HashSet<Material>();

						//get list/set of materials
						list = new HashSet<>(e.getValue());


						//transform to Set<Material>
						for (Object o : list) {
							if (o != null) { //Audun: if there are no suppliers materials retrieved from SPARQL, don´t add null-valued Material objects to the set of materials (should be handled properly with !isEmpty check in SimilarityMeasures.java)
								materialsSet.add(new Material((String) o));
							}
						}

						processName = (String) e.getKey();

						//add relevant set of materials together with process name
						//11.02.2020: Adding attributeWeight to process
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


	/**
	 * Prints the query and the ranked list of suppliers along with the similarity score as well as processes offered by each supplier (for validation of the algorithms).
	 *
	 * @param query          The query from which a ranked list of suppliers is computed.
	 * @param supplierScores Map holding suppliers (key) and their similarity scores (value)
	 * @param numResults     number of results to include in the ranked list.
	 *                       Nov 4, 2019
	 */
	private static void printResultsToConsole(List<Supplier> supplierData, ConsumerQuery query, Map<Supplier, Double> supplierScores, int numResults) {

		Map<Supplier, Double> rankedResults = sortDescending(supplierScores);

		Iterable<Entry<Supplier, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//below code is used for testing purposes
		System.out.println("Consumer query:");
		int n = 1;
		for (Process p : query.getProcesses()) {
			System.out.println("Process " + n + ": " + p.getName());

			//check if the query includes materials
			if (p.getMaterials() == null || p.getMaterials().isEmpty()) {
				//System.err.println("Note: No materials specified in the query!");
			} else {
				for (Material m : p.getMaterials()) {
					System.out.println(" - Material: " + m.getName());
				}
			}
			n++;
		}

		//check if the query includes certifications
		if (query.getCertifications() != null && !query.getCertifications().isEmpty()) {
			System.out.println("Certifications: ");
			for (Certification c : query.getCertifications()) {
				System.out.println(c.getId());
			}
		}

		//get all processes for the suppliers included in the ranked list
		List<String> rankedSuppliers = new ArrayList<String>();
		for (Entry<Supplier, Double> e : firstEntries) {
			rankedSuppliers.add(e.getKey().getId());
		}


		System.out.println("\nRanked results from semantic matching");
		int ranking = 0;


		for (Entry<Supplier, Double> e : firstEntries) {
			ranking++;
			System.out.println("\n" + ranking + "; Supplier ID: " + e.getKey().getId() + "; Sim score: " + "(" + MathUtils.round(e.getValue(), 4) + ")");

			for (Supplier sup : supplierData) {
				if (e.getKey().getId().equals(sup.getId())) {

					System.out.println("Processes:");
					for (Process pro : sup.getProcesses()) {
						System.out.println(pro.toString());
					}

					System.out.println("\nCertifications:");
					Set<String> certificationNames = new HashSet<String>();
					for (Certification cert : sup.getCertifications()) {
						certificationNames.add(cert.getId());
					}

					System.out.println(StringUtilities.printSetItems(certificationNames));

				}
			}
			System.out.println("\n");

		}

	}

	private static Map<String, Double> extractBestSuppliers(Map<Supplier, Double> supplierScores, int numResults) {
		//sort the results from highest to lowest score and return the [numResults] highest scores
		Map<Supplier, Double> rankedResults = sortDescending(supplierScores);
		Iterable<Entry<Supplier, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//return the [numResults] best suppliers according to highest scores
		Map<String, Double> finalSupplierMap = new LinkedHashMap<String, Double>();
		for (Entry<Supplier, Double> e : firstEntries) {
			finalSupplierMap.put(e.getKey().getId(), e.getValue());
		}

		return finalSupplierMap;

	}

	/**
	 * Prints a ranked list of suppliers along with similarity scores to a JSON file
	 *
	 * @param writer Output writer
	 * @throws IOException Nov 4, 2019
	 */
	private static void writeResultToOutput(Map<String, Double> bestSuppliers, BufferedWriter writer) throws IOException {
		int rank = 0;
		List<MatchingResult> scores = new LinkedList<>();
		for (Entry<String, Double> e : bestSuppliers.entrySet()) {
			scores.add(new MatchingResult(++rank, e.getKey(), e.getValue()));
		}

		String output = new GsonBuilder().create().toJson(scores);
		writer.write(output);
		writer.flush();
		writer.close();
	}

	/**
	 * Sorts the scores for each resource offered by a supplier (from highest to lowest)
	 *
	 * @param inputScores a list of scores for each supplier resource assigned by the semantic matching
	 * @return the n highest scores from a list of input scores
	 * Oct 12, 2019
	 */
	private static double getHighestScore(List<Double> inputScores) {
		inputScores.sort(Collections.reverseOrder());
		return inputScores.get(0);

	}

	/**
	 * Computes an attribute weight by computing the average score from "Y" (attribute included by supplier and satisfies customer req), 
	 * "N" (attribute included by supplier and does not satisfy customer req), "O" (attribute not included by supplier).
	 * @param attVars list of relevant attributes checked for whether the supplier has included those in the process description or not.
	 * @return average score of attributes used to weight the overall similarity score.
	   Feb 9, 2020
	 */
	public static double computeAttributeWeight (List<String> attVars) {

		double attributeWeight = 0;

		for (String s : attVars) {
			if (s.equals("Y")) {
				attributeWeight += 1;
			} else if (s.equals("O")) {
				attributeWeight += 0.75;
			} else if (s.equals("N")) {
				attributeWeight += 0.5;
			}

		}

		return attributeWeight / (double) attVars.size();
	}

	/**
	 * Returns the average score of all scores for each resource offered by a supplier
	 *
	 * @param inputScores a list of scores for each supplier resource assigned by the semantic matching
	 * @return the average score of all scores for each supplier resource
	 * Oct 30, 2019
	 */
	private static double getAverageScore(List<Double> inputScores) {
		double sum = 0;

		for (double d : inputScores) {
			sum += d;
		}

		return sum / inputScores.size();
	}

	/**
	 * Sorts a map based on similarity scores (values in the map)
	 *
	 * @param map the input map to be sorted
	 * @return map with sorted values
	 * May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				if (compare == 0) return 1;
				else return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);

		sortedByValues.putAll(map);

		return sortedByValues;
	}



}
