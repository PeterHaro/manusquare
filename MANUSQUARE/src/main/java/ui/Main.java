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

		BufferedWriter writer = testing ? new BufferedWriter(new FileWriter(jsonOut)) : new BufferedWriter(new OutputStreamWriter(System.out));

		//either "CS", "IM" or "BP"
		String functionality = "BP";

		switch (functionality) {

		case "CS":
			String jsonIn = "./files/Davide_040221/Audun_CS_040221.json";
			CSSemanticMatching.performSemanticMatching(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight);
			return;

		case "IM":
			jsonIn = "./files/TESTING_INNOVATION_MANAGEMENT/Test_IM_7.json";
			IMSemanticMatching.performSemanticMatching_IM(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight);
			return;

		case "BP":
			jsonIn = "./files/Davide_040221/Davide_BP_2_040221.json";
			BPSemanticMatching.performByProductMatching(jsonIn, numMatchingResults, writer, testing, weighted, hard_coded_weight);
			return;

		default:
			throw new UnsupportedOperationException("Invalid functionality for the semantic matching: " + functionality);


		}

	}

}
