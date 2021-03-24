package semanticmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import graph.Graph;
import query.BPQuery;
import similarity.measures.BPSimilarityMeasures;
import similarity.results.ExtendedMatchingResult;
import supplier.BPSupplier;
import supplierdata.BPSupplierData;

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
	public static void performByProductMatching(String inputJson, int numResults, BufferedWriter writer, boolean testing, boolean isWeighted, double hard_coded_weight, double cut_threshold) throws OWLOntologyStorageException, IOException {
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

		if (query != null) {

			Set<String> materials = new HashSet<String>();

			//just to get the number of relevant by-products
			for (ByProduct p : query.getByProducts()) {
				materials.addAll(p.getMaterials());

			}

			//create graph
			MutableGraph<String> graph = null;
			graph = Graph.createGraph(ontology);

			if (!materials.isEmpty() && materials != null) {
				Graph.addMaterialsToGraph(graph, new ArrayList<>(materials));
			}


			//get supplier data from SI and re-organise
			List<BPSupplier> supplierData = BPSupplierData.createSupplierData(query, testing, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);

			TreeMap<String, Map<String, Double>> supplierByProductScoresMapping = new TreeMap<String, Map<String, Double>>();

			for (BPSupplier supplier : supplierData) {
				supplierByProductScoresMapping.putAll(BPSimilarityMeasures.computeSemanticSimilarity(query, supplier, ontology, similarityMethod, isWeighted, graph, testing, hard_coded_weight, cut_threshold));

			}

			List<ExtendedMatchingResult> results = ExtendedMatchingResult.computeExtendedMatchingResult(supplierByProductScoresMapping);

			System.err.println("results: " + results);

			writeExtendedResultToOutput(results, writer);

		} else {

			List<ExtendedMatchingResult> noResults = new ArrayList<ExtendedMatchingResult>();

			writeExtendedResultToOutput(noResults, writer);
		}

	}

	public static List<ExtendedMatchingResult> testByProductMatching(String inputJson, int numResults, BufferedWriter writer, boolean testing, boolean isWeighted, double hard_coded_weight, double cut_threshold) throws OWLOntologyStorageException, IOException {
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

		if (query != null) {

			Set<String> materials = new HashSet<String>();

			//just to get the number of relevant by-products
			for (ByProduct p : query.getByProducts()) {
				materials.addAll(p.getMaterials());

			}

			//create graph
			MutableGraph<String> graph = null;
			graph = Graph.createGraph(ontology);

			if (!materials.isEmpty() && materials != null) {
				Graph.addMaterialsToGraph(graph, new ArrayList<>(materials));
			}


			//get supplier data from SI and re-organise
			List<BPSupplier> supplierData = BPSupplierData.createSupplierData(query, testing, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);

			TreeMap<String, Map<String, Double>> supplierByProductScoresMapping = new TreeMap<String, Map<String, Double>>();

			for (BPSupplier supplier : supplierData) {
				supplierByProductScoresMapping.putAll(BPSimilarityMeasures.computeSemanticSimilarity(query, supplier, ontology, similarityMethod, isWeighted, graph, testing, hard_coded_weight, cut_threshold));

			}

			List<ExtendedMatchingResult> results = ExtendedMatchingResult.computeExtendedMatchingResult(supplierByProductScoresMapping);
			
			return results;

		} else {
			
			List<ExtendedMatchingResult> noResults = new ArrayList<ExtendedMatchingResult>();
			return noResults;
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


}
