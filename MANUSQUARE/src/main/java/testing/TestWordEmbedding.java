package testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import embedding.VectorExtractor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Iterables;

import similarity.Cosine;

public class TestWordEmbedding {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {


		String processName = "PocketMilling";

		String vectorFile = "./files/EMBEDDINGS/wikipedia-300.txt";

		File ontoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontoFile);

		Map<String, double[]> vectorMap = createVectorMap (ontology, vectorFile);
		
		System.out.println("The closest concept is " + retrieveClosestConcept(processName, vectorFile, vectorMap));


	}

	public static String retrieveClosestConcept(String processName, String vectorFile, Map<String, double[]> vectorMap) throws FileNotFoundException {

		Map<String, Double> closestConceptMap = new HashMap<String, Double>();


		Map<String, double[]> vectorFileMap = createVectorMap(new File(vectorFile));
		
		System.out.println("vectorFileMap contains " + vectorFileMap.size() + " entries");
		for (Entry<String, double[]> e : vectorFileMap.entrySet()) {
			System.out.println(e.getKey());
		}

		double[] processNameVectors = getVectors(processName, vectorFileMap);
		
		double cosineSim = 0;
		
		for (Entry<String, double[]> e : vectorFileMap.entrySet()) {
			
			System.out.println("Matching " + processNameVectors + " and " + e.getValue());
			
			cosineSim = Cosine.cosineSimilarity(processNameVectors, e.getValue());
			closestConceptMap.put(e.getKey(), cosineSim);
		}
		
		//sort the map and get the most similar process name
		Map<String, Double> sortedMap = sortDescending(closestConceptMap);
		
		Iterable<Entry<String, Double>> closestConcept =
				Iterables.limit(sortedMap.entrySet(), 1);
		
		String closestConceptString = null;
		
		for (Entry<String, Double> e : closestConcept) {
			closestConceptString = e.getKey();
		}
		
		return closestConceptString;

	}

	public static double[] getVectors (String processName, Map<String, double[]> vectorMap) {

		double[] vectors = vectorMap.get(processName);

		return vectors;
	}

	
	public static Map<String, double[]> createVectorMap (OWLOntology onto, String vectorFile) throws IOException {

		Map<String, double[]> vectors = new HashMap<String, double[]>();

		//create the vector map from the source vector file
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap (new File(vectorFile));
		ArrayList<Double> labelVector = new ArrayList<Double>();


		for (OWLClass cls : onto.getClassesInSignature()) {

			if (vectorMap.containsKey(cls.getIRI().getFragment().toLowerCase())) {				

				labelVector = VectorExtractor.getLabelVector(cls.getIRI().getFragment(), vectorMap);

				double[] labelVectorArray = new double[labelVector.size()];
				for (int i = 0; i < labelVectorArray.length; i++) {
					labelVectorArray[i] = labelVector.get(i);
				}
				vectors.put(cls.getIRI().getFragment().toLowerCase(), labelVectorArray);
			}	
		}

		return vectors;


	}

	/**
	 * Takes a file of words and corresponding vectors and creates a Map where the word in each line is key and the vectors are values (as ArrayList<Double>)
	 * @param vectorFile A file holding a word and corresponding vectors on each line
	 * @return A Map<String, ArrayList<Double>> where the key is a word and the value is a list of corresponding vectors
	 * @throws FileNotFoundException
	 */
	public static Map<String, double[]> createVectorMap (File vectorFile) throws FileNotFoundException {

		Map<String, ArrayList<Double>> vectorMap = new HashMap<String, ArrayList<Double>>();

		Scanner sc = new Scanner(vectorFile);

		//read the file holding the vectors and extract the concept word (first word in each line) as key and the vectors as ArrayList<Double> as value in a Map
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			String[] strings = line.split(" ");

			//get the word, not the vectors
			String word1 = strings[0];

			//get the vectors and put them in an array list
			ArrayList<Double> vec = new ArrayList<Double>();
			for (int i = 1; i < strings.length; i++) {
				vec.add(Double.valueOf(strings[i]));
			}
			//put the word and associated vectors in the vectormap
			vectorMap.put(word1, vec);

		}
		sc.close();
		
		
		//convert to Map<String, double[]>
		Map<String, double[]> vectorsMap = new HashMap<String, double[]>();
		for (Entry<String, ArrayList<Double>> e : vectorMap.entrySet()) {
			
			double [] vectors = new double [e.getValue().size()];
			
			 for (int i = 0; i < vectors.length; i++) {
				 vectors[i] = e.getValue().get(i);
			 }
			 
			 vectorsMap.put(e.getKey(), vectors);
			
			
		}

		return vectorsMap;
	}
	
	/** 
	 * Sorts a map based on similarity scores (values in the map)
	 * @param map the input map to be sorted
	 * @return map with sorted values
	   May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator =  new Comparator<K>() {
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

}
