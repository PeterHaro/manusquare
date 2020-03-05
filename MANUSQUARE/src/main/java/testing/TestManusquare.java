package embedding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Stopwatch;

import embedding.vectoraggregation.VectorAggregationMethod;
import owlprocessing.OntologyOperations;
import query.QueryConceptType;
import utilities.Cosine;
import utilities.StringUtilities;


public class TestManusquare {

	//currently the embedding vectors have a dimension of 300, but that might change...
	private static final int NUM_VECTOR_DIMS = 300;

	public static void main(String[] args) throws IOException, OWLOntologyCreationException {

		//load the ontology
		File ontoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		//import the produced embeddings file
		String embeddingsFile = "./files/EMBEDDINGS/manusquare_wikipedia_trained.txt";

		String consumerProcess = "crystalization";

		String preProcessedConsumerProcess = preProcess(consumerProcess);

		//process, material or certification type of concept?
		QueryConceptType conceptType = QueryConceptType.PROCESS;

		//should embedding vectors be averaged or summed?
		VectorAggregationMethod vectorAggregationMethod = VectorAggregationMethod.AVG;

		// Creates and starts a new stopwatch
		Stopwatch stopwatch = Stopwatch.createStarted();

		String mostSimilarConcept = getMostSimilarConcept(preProcessedConsumerProcess, conceptType, onto, embeddingsFile, vectorAggregationMethod);

		System.out.println("The most similar class for " + preProcessedConsumerProcess + " is " + mostSimilarConcept);

		stopwatch.stop();

		System.out.println("\nExecution time in seconds: " + stopwatch.elapsed(TimeUnit.SECONDS));


	}



	/**
	 * Retrieves the most semantically similar concept using embeddings created by Word2Vec
	 * @param consumerInput the input (e.g. requested process or material) from the consumer
	 * @param conceptType the ontology class from which a relevant subset of concepts to be compared is retrieved (for processes this is 'MfgProcess', for materials this is 'MaterialType')
	 * @param onto the ontology from which the most semantically similar concept is retrieved
	 * @param embeddingsFile a file holding word-to-embedding vectors
	 * @param numVectorDims number of dimensions of the vectors in the embeddingsFile
	 * @return the ontology concept having the highest (semantic) similarity with the consumerInput
	 * @throws IOException
	   Mar 3, 2020
	 */
	private static String getMostSimilarConcept(String consumerInput, QueryConceptType conceptType, OWLOntology onto, String embeddingsFile, VectorAggregationMethod vectorAggregationMethod) throws IOException {

		//basic pre-processing of the consumerInput
		String preProcessedConsumerProcess = preProcess(consumerInput);
		
		//create a vector map for the entire embeddingsfile
		Map<String, double[]> vectorMap = createVectorMap (embeddingsFile);

		Set<String> classes = new HashSet<String>();

		//we only consider the 
		if (conceptType == QueryConceptType.PROCESS) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MfgProcess", onto));
		} else if (conceptType == QueryConceptType.MATERIAL) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MaterialType", onto));
		} else if (conceptType == QueryConceptType.CERTIFICATION) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("Certification", onto));
		}

		//create a vector map from the embeddings file for ontology concepts
		Map<String, double[]> vectorOntologyMap = createOntologyVectorMap(classes, embeddingsFile, vectorAggregationMethod);

		double[] consumerInputVectors = getLabelVector(preProcessedConsumerProcess, vectorMap, vectorAggregationMethod);

		String mostSemanticallySimilarConcept = null;

		//if there are no relevant embedding vectors for the consumer input
		if (consumerInputVectors == null) {

			System.err.println(preProcessedConsumerProcess + " does not have any embedding vectors, so using a syntactic matching approach instead...");
			return getMostSimilarConceptSyntactically(preProcessedConsumerProcess, classes);

		} else {

			//check if vectormap/embeddings file contains consumerProcess as-is
			if (vectorOntologyMap.containsKey(preProcessedConsumerProcess.toLowerCase())) {

				mostSemanticallySimilarConcept = preProcessedConsumerProcess;

			} else { //if not, retrieve the most similar concept based on vector similarity

				mostSemanticallySimilarConcept = findMostSimilarVector(consumerInputVectors, vectorOntologyMap);

			}

			return findConceptName(mostSemanticallySimilarConcept, classes);

		}

	}

	/**
	 * Returns an ontology concept name (with proper casing) from a word in the embeddings file
	 * @param concept the word in the embeddings file for which the relevant ontology concept name is retrieved.
	 * @param allClasses set of ontology concept names from the ontology
	 * @return an ontology concept name with proper casing (camel-case)
	   Mar 3, 2020
	 */
	private static String findConceptName (String concept, Set<String> allClasses) {
		String conceptName = null;
		for (String s : allClasses) {
			if (concept.equalsIgnoreCase(s)) {

				conceptName = s;
				break;//break process if a concept name is found

			} else {

				conceptName = null;
			}
		}

		return conceptName;
	}


	/**
	 * Uses cosine similarity to find the most similar word in the embeddings file by its vector representation
	 * @param consumerProcessVectors the vectors of the query inserted by the consumer
	 * @param vectorOntologyMap a map holding the ontology concept name as key and its vector representation as value
	 * @return the ontology concept having the vector most similar to the vector of the consumer input (process or material)
	   Mar 3, 2020
	 */
	public static String findMostSimilarVector (double[] consumerConceptVectors, Map<String, double[]> vectorOntologyMap) {
		double sim = 0;
		double localSim = 0;
		String mostSimilar = null;

		for (Entry<String,double[]> e : vectorOntologyMap.entrySet()) {
			localSim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			if (localSim > sim) {
				mostSimilar = e.getKey();
				sim = localSim;
			}
		}

		return mostSimilar;

	}

	/**
	 * Creates a map holding a class as key along with an array of vectors as value.
	 * @param onto an OWL ontology
	 * @param vectorFile a file holding terms and corresponding embedding vectors.
	 * @return a Map<String, double[]) representing classes and corresponding embedding vectors.
	 * @throws IOException
	   Jul 15, 2019
	 */
	public static Map<String, double[]> createOntologyVectorMap (Set<String> ontologyClasses, String vectorFile, VectorAggregationMethod vectorAggregationMethod) throws IOException {

		Map<String, double[]> vectors = new HashMap<String, double[]>();

		//create the vector map from the source vector file
		Map<String, double[]> vectorMap = createVectorMap (vectorFile);
		double[] labelVector = new double[NUM_VECTOR_DIMS];

		for (String s : ontologyClasses) {

			//if the embeddings file contains the whole concept name as-is
			if (vectorMap.containsKey(s.toLowerCase())) {

				labelVector = getLabelVector(s, vectorMap, vectorAggregationMethod);
				vectors.put(s.toLowerCase(), labelVector);

				//check if any of the compound parts are included in the embeddings file
			} else if (StringUtilities.isCompoundWord(s)) {

				String[] compounds = StringUtilities.getCompoundParts(s);
				List<double[]> compoundsWithVectors = new ArrayList<double[]>();
				for (int i = 0; i < compounds.length; i++) {
					if (vectorMap.containsKey(compounds[i].toLowerCase())) {
						double[] vectorArray = vectorMap.get(compounds[i].toLowerCase());		
						compoundsWithVectors.add(vectorArray);
					}
				}

				vectors.put(s.toLowerCase(), getAVGVectors(compoundsWithVectors, NUM_VECTOR_DIMS));

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
	public static Map<String, double[]> createVectorMap (String vectorFile) throws FileNotFoundException {

		Map<String, double[]> vectorMap = new HashMap<String, double[]>();

		Scanner sc = new Scanner(new File(vectorFile));

		//read the file holding the vectors and extract the concept word (first word in each line) as key and the vectors as ArrayList<Double> as value in a Map
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			String[] strings = line.split(" ");

			//get the word, not the vectors
			String word1 = strings[0];

			//initialize an array that includes only the vector entries
			double[] labelVectorArray = new double[strings.length-1];

			for (int i = 1; i < strings.length; i++) {

				labelVectorArray[i-1] = Double.valueOf(strings[i]);
			}

			//put the word and associated vectors in the vectormap
			vectorMap.put(word1, labelVectorArray);

		}
		sc.close();

		return vectorMap;
	}

	/**
	 * Checks if the vectorMap contains the label of an OWL class as key and if so the vectors of the label are returned. 
	 * @param cls An input OWL class
	 * @param vectorMap The Map holding words and corresponding vectors
	 * @return a set of vectors (as a string) associated with the label
	 */
	public static double[] getLabelVector(String label, Map<String, double[]> vectorMap, VectorAggregationMethod vectorAggregationMethod) {

		List<double[]> aggregatedLabelVectors = new ArrayList<double[]>();

		double[] labelVectorArray = new double[NUM_VECTOR_DIMS];
		double[] localVectorArray = new double[NUM_VECTOR_DIMS];

		//if the class name is not a compound, turn it into lowercase, 
		if (!StringUtilities.isCompoundWord(label)) {

			String lcLabel = label.toLowerCase();

			//if the class name is in the vectormap, get its vectors
			if (vectorMap.containsKey(lcLabel)) {
				labelVectorArray = vectorMap.get(lcLabel);

			} else {

				labelVectorArray = null;
			}

			//if the class name is a compound, split the compounds, and if the vectormap contains ANY of the compounds, extract the vectors from 
			//the compound parts and average them in order to return the vector for the compound class name
			//TODO: The compound head should probably have more impact on the score than the compound modifiers
		} else if (StringUtilities.isCompoundWord(label)) {

			//get the compound parts and check if any of them are in the vector file			
			String[] compounds = StringUtilities.getCompoundParts(label);

			for (int i = 0; i < compounds.length; i++) {

				if (vectorMap.containsKey(compounds[i].toLowerCase())) {

					localVectorArray = vectorMap.get(compounds[i].toLowerCase());

					aggregatedLabelVectors.add(localVectorArray);


				} else {

					labelVectorArray = null;
				}
			}

			//average or sum all vector arraylists
			if (vectorAggregationMethod == VectorAggregationMethod.AVG) {

				labelVectorArray = getAVGVectors(aggregatedLabelVectors, NUM_VECTOR_DIMS);

			} else if (vectorAggregationMethod == VectorAggregationMethod.SUM) {

				labelVectorArray = getSummedVectors(aggregatedLabelVectors, NUM_VECTOR_DIMS);
			}
		}

		return labelVectorArray;
	}

	/**
	 * Receives a list of vectors and averages each vector component
	 * @param a_input list of vectors
	 * @param numVectorDims number of vector dimensions (e.g. 300)
	 * @return an averaged vector
	   Mar 3, 2020
	 */
	private static double[] getAVGVectors(List<double[]> a_input, int numVectorDims) {

		double[] avg = new double[numVectorDims];
		double[] temp = new double[numVectorDims];

		for (double[] singleVector : a_input) {
			for (int i = 0; i < temp.length; i++) {
				temp[i] += singleVector[i];
			}
		}

		for (int i = 0; i < temp.length; i++) {
			avg[i] = temp[i] / (double) a_input.size();
		}

		return avg;
	}

	/**
	 * Receives a list of vectors and sums each vector component
	 * @param a_input list of vectors
	 * @param numVectorDims number of vector dimensions (e.g. 300)
	 * @return a summed vector
	   Mar 3, 2020
	 */
	private static double[] getSummedVectors(List<double[]> a_input, int numVectorDims) {

		double[] sum = new double[numVectorDims];

		for (double[] singleVector : a_input) {
			for (int i = 0; i < sum.length; i++) {
				sum[i] += singleVector[i];
			}
		}

		return sum;
	}

	/**
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 * @param input the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @param method the similarity method applied
	 * @return the best matching concept from the MANUSQUARE ontology
	   Nov 13, 2019
	 */
	private static String getMostSimilarConceptSyntactically(String input, Set<String> ontologyClassesAsString) {

		Map<String, Double> similarityMap = new HashMap<String, Double>();
		String mostSimilarConcept = null;

		for (String s : ontologyClassesAsString) {

			similarityMap.put(s, new JaroWinklerSimilarity().apply(input, s));
		}

		mostSimilarConcept = getConceptWithHighestSim(similarityMap);


		return mostSimilarConcept;

	}

	/**
	 * Returns the concept (name) with the highest (similarity) score from a map of concepts
	 * @param similarityMap a map of concepts along with their similarity scores
	 * @return single concept (name) with highest similarity score
	   Nov 13, 2019
	 */
	private static String getConceptWithHighestSim (Map<String, Double> similarityMap) {

		Map<String, Double> rankedResults = sortDescending(similarityMap);

		Map.Entry<String,Double> entry = rankedResults.entrySet().iterator().next();
		String conceptWithHighestSim = entry.getKey();


		return conceptWithHighestSim;

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

	private static String preProcess (String input) {

		input = new String(StringUtilities.capitaliseWord(input).replaceAll("\\s+", ""));

		return input;
	}



}
