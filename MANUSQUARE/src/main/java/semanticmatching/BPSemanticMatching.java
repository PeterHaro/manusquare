package semanticmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import edm.ByProduct;
import edm.Certification;
import graph.Graph;
import query.BPQuery;
import similarity.ExtendedMatchingResult;
import similarity.MatchingResult;
import similarity.BPSimilarityMeasures;
import supplier.BPSupplier;
import supplierdata.BPSupplierData;
import utilities.MathUtilities;
import utilities.StringUtilities;

/**
 * Contains functionality for performing the semantic matching in the Matchmaking service.
 *
 * @author audunvennesland
 */
public class BPSemanticMatching extends SemanticMatching {

	/**
	 * Matches a consumer query against a set of resources offered by suppliers and returns a ranked list of the [numResult] suppliers having the highest semantic similarity as a JSON file.
	 * @param inputJson an input json file (or json string) holding process(es), materials, attributes and certifications from the RFQ creation process.
	 * @param numResults number of relevant suppliers to be returned from the matching
	 * @param writer writes the results to a buffer
	 * @param testing whether or not the matching is performed "live" or in testing mode
	 * @param isWeighted true if the facets (process, material, certifications) should be weighted, false if not.
	 * @param hard_coded_weight used for reducing the overall similarity score if a facet (e.g. material) requested by the consumer is not present in a process chain.
	 * @throws OWLOntologyStorageException
	 * @throws IOException
	 * @throws OWLOntologyCreationException
	   Mar 5, 2020
	 */
	public static void performByProductMatching(String inputJson, int numResults, BufferedWriter writer, boolean testing, boolean isWeighted, double hard_coded_weight) throws OWLOntologyStorageException, IOException {
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

		BPQuery query = BPQuery.createByProductQuery(inputJson, ontology); // get process(s) from the query and use them to subset the supplier records in the SPARQL query
		
		List<String> byProducts = new ArrayList<>();

		//just to get the number of relevant by-products
		for (ByProduct p : query.getByProducts()) {
			byProducts.add(p.getId());

		}
		
		int numByProducts = byProducts.size();
		

		//create graph using Guava´s graph library instead of using Neo4j
		MutableGraph<String> graph = null;

		graph = Graph.createGraph(ontology);

		//re-organise the SupplierResourceRecords so that we have ( Supplier (1) -> Resource (*) )
		List<BPSupplier> supplierData = BPSupplierData.createSupplierData(query, testing, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);

		Map<BPSupplier, Double> supplierScores = new HashMap<BPSupplier, Double>();
		//for each supplier get the list of best matching processes (and certifications)
		List<Double> supplierSim = new LinkedList<Double>();
		
		TreeMap<String, Map<String, Double>> supplierByProductScoresMapping = new TreeMap<String, Map<String, Double>>();

		for (BPSupplier supplier : supplierData) {
			supplierByProductScoresMapping.putAll(BPSimilarityMeasures.computeSemanticSimilarity(query, supplier, ontology, similarityMethod, isWeighted, graph, testing, hard_coded_weight));
			supplierScores.put(supplier, getAverageSupplierScore(supplierSim, numByProducts));	
			
		}
		
		System.err.println("supplierByProductScoresMapping: " + supplierByProductScoresMapping);
				
		List<ExtendedMatchingResult> results = ExtendedMatchingResult.computeExtendedMatchingResult(supplierByProductScoresMapping);
		
		//extract the n suppliers with the highest similarity scores
		//Map<String, Double> bestSuppliers = extractBestSuppliers(supplierScores, numResults);

		//prints the n best suppliers in ranked order to JSON
//		writeResultToOutput(bestSuppliers, writer);
		
		writeExtendedResultToOutput(results, writer);

		//prints additional data to console for testing/validation
		if (testing == true) {			
			printResultsToConsole(supplierData, query, supplierScores, numResults);			
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
	private static void printResultsToConsole(List<BPSupplier> supplierData, BPQuery query, Map<BPSupplier, Double> supplierScores, int numResults) {

		Map<BPSupplier, Double> rankedResults = sortDescending(supplierScores);

		Iterable<Entry<BPSupplier, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//below code is used for testing purposes
		System.out.println("Consumer query:");
		int n = 1;
		for (ByProduct p : query.getByProducts()) {
			System.out.println("ByProduct " + n + ": " + p.getName());

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
		for (Entry<BPSupplier, Double> e : firstEntries) {
			rankedSuppliers.add(e.getKey().getId());
		}


		System.out.println("\nRanked results from semantic matching");
		int ranking = 0;


		for (Entry<BPSupplier, Double> e : firstEntries) {
			ranking++;
			System.out.println("\n" + ranking + "; Supplier ID: " + e.getKey().getId() + "; Sim score: " + "(" + MathUtilities.round(e.getValue(), 4) + ")");

			for (BPSupplier sup : supplierData) {
				if (e.getKey().getId().equals(sup.getId())) {

					System.out.println("By-products:");
					for (ByProduct pro : sup.getByProducts()) {
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

	private static Map<String, Double> extractBestSuppliers(Map<BPSupplier, Double> supplierScores, int numResults) {
		//sort the results from highest to lowest score and return the [numResults] highest scores
		Map<BPSupplier, Double> rankedResults = sortDescending(supplierScores);
		Iterable<Entry<BPSupplier, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//return the [numResults] best suppliers according to highest scores
		Map<String, Double> finalSupplierMap = new LinkedHashMap<String, Double>();
		for (Entry<BPSupplier, Double> e : firstEntries) {
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
	 * Prints a ranked list of suppliers along with similarity scores to a JSON file
	 *
	 * @param writer Output writer
	 * @throws IOException Nov 4, 2019
	 */
	private static void writeExtendedResultToOutput(List<ExtendedMatchingResult> results, BufferedWriter writer) throws IOException {
		int rank = 0;
		String output = new GsonBuilder().create().toJson(results);
		writer.write(output);
		writer.flush();
		writer.close();
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
