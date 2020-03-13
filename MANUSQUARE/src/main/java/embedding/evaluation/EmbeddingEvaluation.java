package embedding.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import data.EmbeddingSingletonDataManager;
import embedding.vectoraggregation.VectorAggregationMethod;
import owlprocessing.OntologyOperations;
import query.QueryConceptType;
import utilities.Cosine;
import utilities.StringUtilities;

public class EmbeddingEvaluation {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {

		File ontoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		String embeddingsFile = "./files/EMBEDDINGS/embeddings_2L_NSC_Min2_NN_VBG_WND.txt";
		Set<String> cls = OntologyOperations.getClassesAsString(onto);
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();
		Map<String, double[]> ontologyVectorMap = embeddingManager.createOntologyVectorMap(cls, VectorAggregationMethod.SUM);
		Map<String, double[]> vectorMap = embeddingManager.getVectorMap();

		double minThreshold = 0.8;
		double maxThreshold = 0.9;

		
		//print vectorMap
		String vectorMapPath = "./files/EMBEDDINGS/vectorMap_NN_VBG.txt";
		//printVectorMap(vectorMap, vectorMapPath);
		
		//find relations
		findRelations(vectorMap, ontologyVectorMap, minThreshold, maxThreshold);
		
		//print duplicates in VM and OVM
		String duplicatesPath = "./files/EMBEDDINGS/duplicates_NN_VBG.txt";
		//printDuplicates(vectorMap, ontologyVectorMap, duplicatesPath);

	}
	

	public static Map<String, Set<String>> readCSV (String input) throws IOException {
		Map<String, Set<String>> csvMap = new HashMap<String, Set<String>>();
		
		BufferedReader br = new BufferedReader(new FileReader(input));

		String synonyms = null;
		Set<String> synonymSet = new HashSet<String>();
		String[] lineArray;
		
		String line = br.readLine();
		while (line != null) {
			
			lineArray = line.split(";");

			for (int i = 0; i < lineArray.length; i++) {

				synonyms = lineArray[1];				
				synonymSet = new HashSet<String>(Arrays.asList(synonyms));
				csvMap.put(lineArray[0], synonymSet);
	

			}
			line = br.readLine();
		}
		
		br.close();
		
		return csvMap;
	}

	public static void printVectorMap (Map<String, double[]> vectorMap, String path) throws IOException {

		FileWriter fw = new FileWriter(path);
		PrintWriter pw = new PrintWriter(fw);

		for (Entry<String, double[]> e : vectorMap.entrySet() ) {
			pw.println(e.getKey());
		}

		fw.close();
		pw.close();
	}

	public static void findRelations (Map<String, double[]> vectorMap, Map<String, double[]> ontologyVectorMap, double minThreshold, double maxThreshold) {

		Set<Relation> scores = new HashSet<Relation>();
		double sim = 0;
		for (Entry<String, double[]> vm : vectorMap.entrySet()) {
			for (Entry<String, double[]> ovm : ontologyVectorMap.entrySet()) {
				sim = Cosine.cosineSimilarity(vm.getValue(), ovm.getValue());
				if (sim > minThreshold && sim < maxThreshold)
					scores.add(new Relation(vm.getKey(), ovm.getKey(), sim));
			}
		}

		System.out.println("There are " + scores.size() + " relations having a similarity above " + minThreshold + " and below " + maxThreshold);

		for (Relation rel : scores) {
			System.out.println(rel.toString());
		}

	}

	public static void printDuplicates (Map<String, double[]> vectorMap, Map<String, double[]> ontologyVectorMap, String filePath) throws IOException {

		FileWriter fw = new FileWriter(filePath);
		PrintWriter pw = new PrintWriter(fw);

		for (Entry<String, double[]> vm : vectorMap.entrySet()) {
			for (Entry<String, double[]> ovm : ontologyVectorMap.entrySet()) {
				if (vm.getKey().equals(ovm.getKey())) {
					pw.println(vm.getKey());
				}
			}
		}

		fw.close();
		pw.close();

	}
	










}
