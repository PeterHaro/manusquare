package embedding.evaluation;

import com.google.common.collect.Iterables;
import data.EmbeddingSingletonDataManager;
import embedding.vectoraggregation.VectorAggregationMethod;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import owlprocessing.OntologyOperations;
import query.QueryConceptType;
import similarity.Cosine;
import utilities.MathUtils;
import validation.QueryValidation;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class EmbeddingEvaluation {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {

		File ontoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Set<String> cls = OntologyOperations.getClassesAsString(onto);
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();
		Map<String, double[]> ontologyVectorMap = embeddingManager.createOntologyVectorMap(cls, VectorAggregationMethod.SUM);
		Map<String, double[]> vectorMap = embeddingManager.getVectorMap();

		double minThreshold = 0.8;
		double maxThreshold = 0.9;


		//print vectorMap
		String vectorMapPath = "./files/EMBEDDINGS/VM_2L_NSC_Min2_NN_VBG.txt";
		printVectorMapKeys(vectorMap, vectorMapPath);

		//find relations
		findRelations(vectorMap, ontologyVectorMap, minThreshold, maxThreshold);

		measureEmbeddingsRelevance("./files/EMBEDDINGS/Evaluation/EmbeddingsRelevance/synonyms.txt");
		
		Set<String> processListFromWikipedia = readListFromFile("./files/EMBEDDINGS/Evaluation/QueryReformulation/manufacturing_processes_wikipedia.txt");		
		
		Map<String, Map<String, Double>> queryReformulationMap = evaluateQueryReformulationAccuracy("./files/EMBEDDINGS/Evaluation/QueryReformulation/manufacturing_processes_wikipedia.txt", QueryConceptType.PROCESS, onto);
		
		printQueryReformulationMap(queryReformulationMap, "./files/EMBEDDINGS/Evaluation/QueryReformulation/queryReformulationResultsProcesses.csv");
		

	}
	
	
	private static Map<String, Map<String, Double>> evaluateQueryReformulationAccuracy(String input, QueryConceptType type, OWLOntology onto) throws IOException {
		Map<String, Map<String, Double>> queryReformulationMap = new HashMap<String, Map<String, Double>>();
		
		Set<String> processList = readListFromFile(input);
				
		Map<String, Double> mostSimilarMap = new HashMap<String, Double>();
		
		for (String s : processList) {
			
			mostSimilarMap = QueryValidation.getMostSimilarConceptWithScore(s.toLowerCase(), type, onto, VectorAggregationMethod.AVG);
			
			queryReformulationMap.put(s, mostSimilarMap);
			
		}
		

		return queryReformulationMap;
	}
	
	private static void printQueryReformulationMap(Map<String, Map<String, Double>> queryReformulationMap, String path) throws IOException {
		
		FileWriter fw = new FileWriter(path);
		PrintWriter pw = new PrintWriter(fw);
		
		String query = null;

		
		for (Entry<String, Map<String, Double>> e : queryReformulationMap.entrySet() ) {
			
			query = e.getKey();
			String bestQueryReformulationCandidate = null;
			double score = 0;
			
			for (Entry<String, Double> qf : e.getValue().entrySet()) {
				bestQueryReformulationCandidate = qf.getKey();
				score = qf.getValue();
			}
			
			pw.println(query + ";" + bestQueryReformulationCandidate + ";" + score);
		}

		fw.close();
		pw.close();
		
	}
	


	private static double measureEmbeddingsRelevance (String synonymsPath) throws IOException {
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();
		Map<String, Set<String>> synonymMap = readCSV(synonymsPath);
		Map<String, double[]> vectorMap = embeddingManager.getVectorMap();
		
		int correctOnes = 0;
		int notFound = 0;
		double[] synonymVectors = null;
		Set<String> bestFiveSet = new HashSet<String>();

		for (Entry<String, Set<String>> e : synonymMap.entrySet()) {
			System.out.println("The ontology concept is " + e.getKey());
			
			//extract the 5 most similar words (but not equal) from the vectormap by their embeddings			
			if (vectorMap.containsKey(e.getKey().toLowerCase())) {
			synonymVectors = vectorMap.get(e.getKey().toLowerCase());
			bestFiveSet = findMostSimilarEmbeddings(synonymVectors, vectorMap);
			
			if (matchInSets(e.getValue(), bestFiveSet)) {
				System.out.println("We have a match!");
				System.out.println("There are " + e.getValue().size() + " synonyms for " + e.getKey() + ", these are " + e.getValue().toString());
				System.out.println("The bestFiveSet contains " + bestFiveSet.toString());
				correctOnes++;
			}

		} else {
			System.out.println("- There are no vectors for " + e.getKey());
			notFound++;
		}
		}
		
		System.out.println("Not found in embeddings file: " + notFound);
		
		double accuracy = (double)correctOnes / (double)synonymMap.size() * 100;
		
		System.out.println("Synonym accuracy (correct ones: " + correctOnes + "), (synonym map size: " + synonymMap.size() + ") : " +  MathUtils.round(accuracy, 0) + " percent");

		return accuracy;

	}
	
	public static boolean matchInSets(Set<String> set1, Set<String> set2) {
		
		Set<String> intersection = new HashSet<String>(set1);
		intersection.retainAll(set2);
		
		if (intersection.size() > 0) {
			return true;
		} else {
			return false;
		}
		
	}

	public static Set<String> findMostSimilarEmbeddings(double[] consumerConceptVectors, Map<String, double[]> vectorMap) {
		double sim = 0;
		
		Map<String, Double> similarityMap = new HashMap<String, Double>();

		for (Entry<String, double[]> e : vectorMap.entrySet()) {
			sim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			similarityMap.put(e.getKey(), sim);
		}
		
		//return the 5 most similar vectors
		Map<String, Double> mostSimilarMap = extractBestEmbeddings(similarityMap, 5);
		Set<String> bestFiveEmbeddings = new HashSet<String>();
		
		for (Entry<String, Double> e : mostSimilarMap.entrySet()) {
			bestFiveEmbeddings.add(e.getKey().replaceAll("\\s+",""));
		}

		return bestFiveEmbeddings;
	}



	private static Map<String, Double> extractBestEmbeddings(Map<String, Double> supplierScores, int numResults) {
		//sort the results from highest to lowest score and return the [numResults] highest scores
		Map<String, Double> rankedResults = sortDescending(supplierScores);
		Iterable<Entry<String, Double>> firstEntries =
				Iterables.limit(rankedResults.entrySet(), numResults);

		//return the [numResults] best embedding words according to highest scores
		Map<String, Double> finalEmbeddingMap = new LinkedHashMap<String, Double>();
		for (Entry<String, Double> e : firstEntries) {
			finalEmbeddingMap.put(e.getKey(), e.getValue());
		}

		return finalEmbeddingMap;

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
	
	public static Set<String> readListFromFile (String input) throws IOException {
		
		Set<String> list = new HashSet<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = br.readLine();
		
		while (line != null) {
			
			list.add(line);
			
			line = br.readLine();
		}
		
		br.close();
		
		return list;
		
	}

	public static Map<String, Set<String>> readCSV (String input) throws IOException {
		Map<String, Set<String>> csvMap = new HashMap<String, Set<String>>();

		BufferedReader br = new BufferedReader(new FileReader(input));

		String subjectWord = null;
		String synonyms = null;
		Set<String> synonymSet = new HashSet<String>();
		String[] lineArray;
		String[] synonymsArray;

		String line = br.readLine();
		while (line != null) {

			lineArray = line.split(";");
			
			subjectWord = lineArray[0];
			
			synonyms = lineArray[1];
			
			synonymsArray = synonyms.split(",");
			
			synonymSet = new HashSet<String>(Arrays.asList(synonymsArray));
			
			csvMap.put(subjectWord, synonymSet);

			line = br.readLine();
		}

		br.close();

		return csvMap;
	}

	public static void printVectorMapKeys (Map<String, double[]> vectorMap, String path) throws IOException {

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



}
