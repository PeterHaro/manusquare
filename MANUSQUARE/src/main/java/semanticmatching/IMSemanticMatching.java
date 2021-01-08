package semanticmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.common.collect.Iterables;
import com.google.common.graph.MutableGraph;
import com.google.gson.GsonBuilder;

import edm.Certification;
import graph.Graph;
import query.IMQuery;
import similarity.MatchingResult;
import similarity.IMSimilarityMeasures;
import similarity.SimilarityMethods;
import supplier.IMSupplier;
import supplierdata.IMSupplierData;
import utilities.MathUtilities;
import utilities.StringUtilities;

/**
 * Contains functionality for performing the semantic matching in the Matchmaking service.
 *
 * @author audunvennesland
 */
public class IMSemanticMatching extends SemanticMatching {

	static SimilarityMethods similarityMethod = SimilarityMethods.WU_PALMER;


	
	public static void performSemanticMatching_IM (String inputJson, int numResults, BufferedWriter writer, boolean testing, boolean isWeighted, double hard_coded_weight) throws OWLOntologyStorageException, IOException {
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
		File localOntoFile = new File("files/ONTOLOGIES/updatedOntology.owl");

		manager.saveOntology(Objects.requireNonNull(ontology), IRI.create(localOntoFile.toURI()));
		
		IMQuery imq = IMQuery.createQuery(inputJson, ontology);

		//create graph using Guava´s graph library instead of using Neo4j
		MutableGraph<String> graph = null;

		graph = Graph.createGraph(ontology);
		
		//System.out.println("Printing graph nodes");
		//SimpleGraph.printGraphNodes(graph);

		List<IMSupplier> innovationManagerData = IMSupplierData.createInnovationManagerData(imq, testing, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);

		Map<IMSupplier, Double> innovationManagerScores = new HashMap<IMSupplier, Double>();
		//for each supplier get the list of best matching processes (and certifications)
		List<Double> innovationManagerSim = new LinkedList<Double>();

		for (IMSupplier innovationManager : innovationManagerData) {

			innovationManagerSim = IMSimilarityMeasures.computeSemanticSimilarity_IM(imq, innovationManager, ontology, similarityMethod, isWeighted, graph, testing, hard_coded_weight);
			//get the highest score for the process chains offered by supplier n
			innovationManagerScores.put(innovationManager, getHighestScore(innovationManagerSim));	
			
		}

		//extract the n innovation managers with the highest similarity scores
		Map<String, Double> bestSuppliers = extractBestInnovationManagers(innovationManagerScores, numResults);

		//prints the n best innovation managers in ranked order to JSON
		writeResultToOutput(bestSuppliers, writer);		
		
		//prints additional data to console for testing/validation
		if (testing == true) {			
			printResultsToConsoleIM(innovationManagerData, imq, innovationManagerScores, numResults);			
		}

	}

	/**
	 * Prints the query and the ranked list of suppliers along with the similarity score as well as processes offered by each supplier (for validation of the algorithms).
	 *
	 * @param query          The query from which a ranked list of suppliers is computed.
	 * @param supplierScores Map holding suppliers (key) and their similarity scores (value)
	 * @param numResults     number of results to include in the ranked list.
	 *                       Nov 4, 2019
	 */
	private static void printResultsToConsoleIM(List<IMSupplier> innovationManagerData, IMQuery query, Map<IMSupplier, Double> supplierScores, int numResults) {

		Map<IMSupplier, Double> rankedResults = sortDescending(supplierScores);

		Iterable<Entry<IMSupplier, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//below code is used for testing purposes
		System.out.println("Consumer query:");
		
		//check if the query includes certifications
		if (query.getCertifications() != null && !query.getCertifications().isEmpty()) {
			System.out.println("Certifications: ");
			for (Certification c : query.getCertifications()) {
				System.out.println(c.getId());
			}
		}

		//get all processes for the suppliers included in the ranked list
		List<String> rankedSuppliers = new ArrayList<String>();
		for (Entry<IMSupplier, Double> e : firstEntries) {
			rankedSuppliers.add(e.getKey().getId());
		}


		System.out.println("\nRanked results from semantic matching");
		int ranking = 0;


		for (Entry<IMSupplier, Double> e : firstEntries) {
			ranking++;
			System.out.println("\n" + ranking + "; Innovation Manager ID: " + e.getKey().getId() + "; Sim score: " + "(" + MathUtilities.round(e.getValue(), 4) + ")");

			for (IMSupplier innovationManager : innovationManagerData) {
				if (e.getKey().getId().equals(innovationManager.getId())) {


					System.out.println("\nCertifications:");
					Set<String> certificationNames = new HashSet<String>();
					for (Certification cert : innovationManager.getCertifications()) {
						certificationNames.add(cert.getId());
					}

					System.out.println(StringUtilities.printSetItems(certificationNames));
					

				}
			}
			System.out.println("\n");

		}
	}


	private static Map<String, Double> extractBestInnovationManagers(Map<IMSupplier, Double> supplierScores, int numResults) {
		//sort the results from highest to lowest score and return the [numResults] highest scores
		Map<IMSupplier, Double> rankedResults = sortDescending(supplierScores);
		Iterable<Entry<IMSupplier, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//return the [numResults] best suppliers according to highest scores
		Map<String, Double> finalSupplierMap = new LinkedHashMap<String, Double>();
		for (Entry<IMSupplier, Double> e : firstEntries) {
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
	 * Get the average score relative to number of consumer processes (sum supplier scores / num consumer processes in query)
	 *
	 * @param inputScores a list of scores for each supplier resource assigned by the semantic matching
	 * @return the n highest scores from a list of input scores
	 * Oct 12, 2019
	 */
	private static double getAverageSupplierScore(List<Double> inputScores, int numConsumerProcesses) {
		double sum = 0;

		for (double d : inputScores) {
			sum += d;
		}

		return sum / (double)numConsumerProcesses;

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
