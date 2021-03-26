package ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import semanticmatching.BPSemanticMatching;
import semanticmatching.CSSemanticMatching;
import semanticmatching.IMSemanticMatching;

public class Main {
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParseException, /*JSONException,*/ OWLOntologyStorageException {

		int numMatchingResults = 10;

		String jsonOut = "./files/matchingResults.json";

		//if testing == true -> local KB + additional test data written to console, if testing == false, MANUSQUARE Semantic Infrastructure
		boolean testing = false;
		//if weighted == true, we're trying a weight configuration of (process=0.75, materials 0.25; processAndMaterials=0.75, certifications=0.25)
		boolean weighted = true;		
		//used for situations where a process chain has no certifications|materials|attributes and this is required by the consumer in the RFQ JSON
		double hard_coded_weight = 0.5;
		//used for filtering out all scores below this cut threshold
		double cut_threshold = 0.75;

		BufferedWriter writer = testing ? new BufferedWriter(new FileWriter(jsonOut)) : new BufferedWriter(new OutputStreamWriter(System.out));

		//either "CS", "IM" or "BP"
		String functionality = "CS";

		switch (functionality) {

		case "CS":
			String jsonIn = "./files/ORIGINAL_TEST_JSONS/TESTING_CAPACITY_SHARING/Test_CS_4.json";
			CSSemanticMatching.performSemanticMatching(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight, cut_threshold);
			return;

		case "IM":
//			jsonIn = "./files/ORIGINAL_TEST_JSONS/TESTING_INNOVATION_MANAGEMENT/Test_IM_1.json";
			jsonIn = "./files/10032021/elias.json";
			IMSemanticMatching.performSemanticMatching_IM(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight, cut_threshold);
			return;

		case "BP":
			jsonIn = "./files/TESTING_BYPRODUCT_SHARING/Test_BP_1_2.json";
			
			BPSemanticMatching.performByProductMatching(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight, cut_threshold);
			return;

		default:
			throw new UnsupportedOperationException("Invalid functionality for the semantic matching: " + functionality);


		}

	}

}
