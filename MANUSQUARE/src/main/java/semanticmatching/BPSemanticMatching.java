package semanticmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.common.graph.MutableGraph;
import com.google.gson.GsonBuilder;

import edm.ByProduct;
import edm.Certification;
import graph.Graph;
import query.BPQuery;
import similarity.measures.BPSimilarityMeasures;
import similarity.results.ExtendedMatchingResult;
import similarity.results.MatchingResult;
import supplier.BPSupplier;
import supplierdata.BPSupplierData;
import utilities.MathUtilities;
import validation.ByProductValidator;

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
		
		if (ByProductValidator.validQuery(inputJson, ontology)) {
			
			BPQuery query = BPQuery.createByProductQuery(inputJson, ontology); // get process(s) from the query and use them to subset the supplier records in the SPARQL query
			
			List<String> byProducts = new ArrayList<>();

			//just to get the number of relevant by-products
			for (ByProduct p : query.getByProducts()) {
				byProducts.add(p.getId());

			}
			
			int numByProducts = byProducts.size();
			

			//create graph
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
				supplierScores.put(supplier, MathUtilities.getAverage(supplierSim, numByProducts));	
				
			}
							
			List<ExtendedMatchingResult> results = ExtendedMatchingResult.computeExtendedMatchingResult(supplierByProductScoresMapping);
			
			writeExtendedResultToOutput(results, writer);

			
		} else {
			
			List<ExtendedMatchingResult> results = ExtendedMatchingResult.returnEmptyResults();
			
			writeExtendedResultToOutput(results, writer);
			
		}

		

	}
	
	public static List<ExtendedMatchingResult> testByProductMatching(String inputJson, int numResults, BufferedWriter writer, boolean testing, boolean isWeighted, double hard_coded_weight) throws OWLOntologyStorageException, IOException {
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
		
		if (ByProductValidator.validQuery(inputJson, ontology)) {
			
			BPQuery query = BPQuery.createByProductQuery(inputJson, ontology); // get process(s) from the query and use them to subset the supplier records in the SPARQL query
			
			List<String> byProducts = new ArrayList<>();

			//just to get the number of relevant by-products
			for (ByProduct p : query.getByProducts()) {
				byProducts.add(p.getId());

			}
			
			int numByProducts = byProducts.size();
			

			//create graph
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
				supplierScores.put(supplier, MathUtilities.getAverage(supplierSim, numByProducts));	
				
			}
							
			List<ExtendedMatchingResult> results = ExtendedMatchingResult.computeExtendedMatchingResult(supplierByProductScoresMapping);
			
			return results;

			
		} else {
			
			return null;

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
	private static void printResultsToConsole(BPQuery query, List<ExtendedMatchingResult> results) {

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

		System.out.println("\nRanked results from semantic matching");
		int ranking = 0;
		
		for (ExtendedMatchingResult result : results) {
			ranking++;
			System.out.println("\n" + ranking + "; Supplier ID: " + result.getSupplierId() + "; Sim score: " + result.getByProductScores().values() + "; Rank: " + result.getRank());
		}
	
	}

	
	/**
	 * Prints a ranked list of suppliers along with similarity scores to a JSON file
	 *
	 * @param writer Output writer
	 * @throws IOException Nov 4, 2019
	 */
	private static void writeExtendedResultToOutput(List<ExtendedMatchingResult> results, BufferedWriter writer) throws IOException {
		String output = new GsonBuilder().create().toJson(results);
		writer.write(output);
		writer.flush();
		writer.close();
	}
	
	/**
	 * Prints an empty list of results
	 *
	 * @param writer Output writer
	 * @throws IOException Jan 15, 2021
	 */
	private static void writeEmptyResultToOutput(BufferedWriter writer) throws IOException {
		List<MatchingResult> scores = new LinkedList<>();
		scores.add(new MatchingResult(0, "Not a valid consumer query due to non-valid byProductName and/or material attributes", 0));
		String output = new GsonBuilder().create().toJson(scores);
		writer.write(output);
		writer.flush();
		writer.close();
	}

}
