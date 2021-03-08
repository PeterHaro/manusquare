package ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import semanticmatching.BPSemanticMatching;
import semanticmatching.CSSemanticMatching;
import semanticmatching.IMSemanticMatching;
import similarity.results.ExtendedMatchingResult;


public class Test {
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, ParseException, /*JSONException,*/ OWLOntologyStorageException {
		
		String csFolder = "./files/TESTING_CAPACITY_SHARING";
		String bpFolder = "files/TESTING_BYPRODUCT_SHARING";
		String imFolder = "./files/TESTING_INNOVATION_MANAGEMENT";

		int numMatchingResults = 10;

		String jsonOut = "./files/matchingResults.json";

		boolean testing = false;
		boolean weighted = true;		
		double hard_coded_weight = 0.5;

		BufferedWriter writer = testing ? new BufferedWriter(new FileWriter(jsonOut)) : new BufferedWriter(new OutputStreamWriter(System.out));

		//either "CS", "IM" or "BP"
		String functionality = "BP";


		File folder = null;
		File[] files = null;
		BufferedWriter bfwriter = null;
		switch (functionality) {

		case "CS":
			
			//iterate all test files and return number of matching results
			folder = new File(csFolder);
			files = folder.listFiles();
			
			Map<String, Double> csResult = new HashMap<String, Double>();
			bfwriter = new BufferedWriter(new FileWriter("./files/TEST_OUTPUT/cs.txt"));
			
			for (File f : files) {
				
				System.out.println("Testing " + f.getPath());
				
				csResult = CSSemanticMatching.testSemanticMatching(f.getPath(), numMatchingResults, writer, testing, weighted, hard_coded_weight);
				
				if (csResult != null) {
					bfwriter.append("\n\n" + f.getName() + "( " + csResult.size() + ")");
					for (Entry<String, Double> e : csResult.entrySet()) {
						bfwriter.append("\n" + e.getKey() + ": " + e.getValue());
					}
					
					
				} else {
					
					bfwriter.append("\n\n" + f.getName() + " does not pass query validation!");
				}
			}
			
			bfwriter.close();
			
			return;

		case "IM":
				
			//iterate all test files and return number of matching results
			folder = new File(imFolder);
			files = folder.listFiles();
			
			Map<String, Double> imResult = new HashMap<String, Double>();
			
			imResult = new HashMap<String, Double>();
			bfwriter = new BufferedWriter(new FileWriter("./files/TEST_OUTPUT/im.txt"));
			
			for (File f : files) {
				
				System.out.println("Testing " + f.getPath());
				
				imResult = IMSemanticMatching.testSemanticMatching(f.getPath(), numMatchingResults, writer, testing, weighted, hard_coded_weight);
				
				if (imResult != null) {
					bfwriter.append("\n\n" + f.getName() + " (" + imResult.size() + ")");
					for (Entry<String, Double> e : imResult.entrySet()) {
						bfwriter.append("\n" + e.getKey() + ": " + e.getValue());
					}
				} else {
					
					bfwriter.append("\n\n" + f.getName() + " does not pass query validation!");
				}
			}
			
			bfwriter.close();
			
			return;

		case "BP":
				
			//iterate all test files and return number of matching results
			folder = new File(bpFolder);
			files = folder.listFiles();
			
			List<ExtendedMatchingResult> bpResult = new ArrayList<ExtendedMatchingResult>();
			

			bfwriter = new BufferedWriter(new FileWriter("./files/TEST_OUTPUT/bp.txt"));
			
			for (File f : files) {
				
				System.out.println("Testing " + f.getPath());
				
				bpResult = BPSemanticMatching.testByProductMatching(f.getPath(), numMatchingResults, writer, testing, weighted, hard_coded_weight);
				
				if (bpResult != null) {
					bfwriter.append("\n\n" + f.getName() + " (" + bpResult.size() + ")");
					for (ExtendedMatchingResult e : bpResult) {
						bfwriter.append("\n" + e.getSupplierId());
						for (Entry<String, Double> m : e.getByProductScores().entrySet()) {
							bfwriter.append("\n" + m.getKey() + ": " + m.getValue());
						}
					}
				} else {
					
					bfwriter.append("\n\n" + f.getName() + " does not pass query validation!");
				}
			}
			
			bfwriter.close();
			
			return;

		default:
			throw new UnsupportedOperationException("Invalid functionality for the semantic matching: " + functionality);


		}

	}


}
