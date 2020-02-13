package ui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.codehaus.jettison.json.JSONException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BasicMatchmaking_MVP {
	

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParseException, JSONException, OWLOntologyStorageException {
		long startTime = System.currentTimeMillis();
		logging(false);
		int numMatchingResults = 10;
		// String jsonIn = "./files/rfq.json";
		String jsonOut = "./files/matchingResults.json";


		//if test == true -> local KB + additional test data written to console, if test == false, MANUSQUARE Semantic Infrastructure
		boolean testing = true;

		//if weighted == true, I'm trying a weight configuration of (process=0.75, materials 0.25; processAndMaterials=0.75, certifications=0.25)
		boolean weighted = true;
		
		//used for situations where a process chain has no certifications|materials|attributes and this is required by the consumer in the RFQ JSON
		double hard_coded_weight = 0.9;


		BufferedWriter writer = testing ? new BufferedWriter(new FileWriter(jsonOut)) : new BufferedWriter(new OutputStreamWriter(System.out));
		if (args.length == 1) {
			System.out.println(args[0]);
			SemanticMatching_MVP.performSemanticMatching(args[0], 10, writer, testing, true, hard_coded_weight);
			return;
		} else {
			System.err.println("No arguments provided!");
			String jsonIn = "./MANUSQUARE/files/rfq-attributes.json";
 			SemanticMatching_MVP.performSemanticMatching(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight);
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		if (testing) {
			System.out.println("The entire Matchmaking process completed in " + elapsedTime / 1000 + " seconds.");
		}

	}


	private static void logging(boolean logging) {
		Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "org.eclipse.rdf4j"));

		if (!logging) {
			for (String log : loggers) {
				Logger logger = (Logger) LoggerFactory.getLogger(log);
				logger.setLevel(Level.ERROR);
				logger.setAdditive(false);
			}
		} else {

			System.out.println("Logging:");

		}

	}

}
