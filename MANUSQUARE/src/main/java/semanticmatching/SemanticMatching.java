package semanticmatching;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import com.google.gson.Gson;

import similarity.SimilarityMethods;

public class SemanticMatching {

	static SimilarityMethods similarityMethod = SimilarityMethods.WU_PALMER;

	//configuration of the MANUSQUARE Semantic Infrastructure
	static String SPARQL_ENDPOINT = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0"; 
	static String AUTHORISATION_TOKEN = "7777e8ed0d5eb1b63ab1815a56e31ff1";

	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare/ontology.owl");
	
	/**
	 * Prints an empty list of results
	 *
	 * @param writer Output writer
	 * @throws IOException Jan 15, 2021
	 */
	protected static void writeEmptyResultToOutput(BufferedWriter writer) throws IOException {
		
		Map<Integer, String> empty = new HashMap<>();
	    Gson gson = new Gson();
	        
	    String output = gson.toJson(empty);
		
		writer.write(output);
		writer.flush();
		writer.close();
	}
	
}
