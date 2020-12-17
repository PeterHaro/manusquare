package ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class BasicMatchmaking_BP {
	

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParseException, /*JSONException,*/ OWLOntologyStorageException {
		
		long startTime = System.currentTimeMillis();

		int numMatchingResults = 10;
		// String jsonIn = "./files/rfq.json";
		String jsonOut = "./files/matchingResults.json";

		//if testing == true -> local KB + additional test data written to console, if testing == false, MANUSQUARE Semantic Infrastructure
		boolean testing = false;

		//if weighted == true, I'm trying a weight configuration of (process=0.75, materials 0.25; processAndMaterials=0.75, certifications=0.25)
		boolean weighted = true;
		
		//used for situations where a process chain has no certifications|materials|attributes and this is required by the consumer in the RFQ JSON
		double hard_coded_weight = 0.5;

		BufferedWriter writer = testing ? new BufferedWriter(new FileWriter(jsonOut)) : new BufferedWriter(new OutputStreamWriter(System.out));
		if (args.length == 1) {
			System.out.println(args[0]);
			SemanticMatching.performByProductMatching(args[0], 10, writer, testing, true, hard_coded_weight);
			return;
		} else {
			System.err.println("No arguments provided!");
			String jsonIn = "./files/TESTING_BYPRODUCT_SHARING/Radostin_17122020/Radostin_17122020_1.json";
 			SemanticMatching.performByProductMatching(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight);
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		if (testing) {
			System.out.println("The entire Matchmaking process completed in " + elapsedTime / 1000 + " seconds.");
		}

	}

}
