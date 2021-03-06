package validation;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Iterables;

import data.EmbeddingSingletonDataManager;
import edm.Attribute;
import edm.Certification;
import edm.Material;
import embedding.vectoraggregation.VectorAggregationMethod;
import owlprocessing.OntologyOperations;
import query.QueryConceptType;
import similarity.Cosine;
import utilities.StringUtilities;

public class QueryValidation {
	
	public QueryValidation () {}
	
	/**
	 * Ensures that a process included by the consumer is in fact in the ontology (if not, the closest matching concept from the ontology is returned)
	 * @param processName process included by the consumer
	 * @param onto the MANU-SQUARE ontology
	 * @param allOntologyClasses string representation of all ontology concepts
	 * @return a process name verified to exist in the MANU-SQUARE ontology
	 * @throws IOException
	   Mar 27, 2020
	 */
	public static String validateProcessName(String processName, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {

		String validatedProcessName = null;

		if (!allOntologyClasses.contains(processName)) {
			validatedProcessName = getMostSimilarConcept(processName, QueryConceptType.PROCESS, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedProcessName = processName;
		}
				
		return validatedProcessName;
	}


	/**
	 * Checks if all materials specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialMaterials set of materials specified by the consumer in the RFQ process
	 * @return set of materials we´re sure exist as concepts in the ontology
	 * Nov 13, 2019
	 * @throws IOException
	 */
	public static Set<Material> validateMaterials(Set<Material> initialMaterials, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<Material> validatedMaterials = new HashSet<Material>();
		if (initialMaterials.isEmpty() || initialMaterials == null) {
			return null;
		} else {
			for (Material m : initialMaterials) {
				if (!allOntologyClasses.contains(m.getName())) { //if not, get the concept from the ontology with the highest similarity
					m.setName(getMostSimilarConcept(m.getName(), QueryConceptType.MATERIAL, onto, EmbeddingSingletonDataManager.VAM));
					validatedMaterials.add(m);
				} else {
					validatedMaterials.add(m);
				}
			}
			return validatedMaterials;
		}
	}

	/**
	 * Checks if all attribute keys specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialAttributes set of attribute keys specified by the consumer in the RFQ process
	 * @return set of attribute keys we´re sure exist as concepts in the ontology
	 * Jan 7, 2020
	 * @throws IOException
	 */
	public static Set<Attribute> validateAttributeKeys(Set<Attribute> initialAttributes, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<Attribute> validatedAttributes = new HashSet<Attribute>();
		if (initialAttributes.isEmpty() || initialAttributes == null) {
			return null;
		} else {

			for (Attribute a : initialAttributes) {
				if (!allOntologyClasses.contains(a.getKey())) { //if not, get the concept from the ontology with the highest similarity
					a.setKey(getMostSimilarConcept(a.getKey(), QueryConceptType.ATTRIBUTE, onto, EmbeddingSingletonDataManager.VAM));
					validatedAttributes.add(a);
				} else {
					validatedAttributes.add(a);
				}
			}

			return validatedAttributes;
		}

	}

	/**
	 * Checks if all certifications specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialCertifications set of certifications specified by the consumer in the RFQ process
	 * @return set of certifications we´re sure exist as concepts in the ontology
	 * Nov 13, 2019
	 * @throws IOException
	 */
	public static Set<Certification> validateCertifications(Set<Certification> initialCertifications, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<Certification> validatedMaterials = new HashSet<Certification>();

		for (Certification c : initialCertifications) {
			if (!allOntologyClasses.contains(c.getId())) { //if not, get the concept from the ontology with the highest similarity
				c.setId(getMostSimilarConcept(c.getId(), QueryConceptType.CERTIFICATION, onto, EmbeddingSingletonDataManager.VAM));
				validatedMaterials.add(c);
			} else {
				validatedMaterials.add(c);
			}
		}

		return validatedMaterials;

	}
	
	/**
	 * Retrieves the most similar concept from a concept included by the consumer in the GUI. 
	 * @param consumerInput				the input (i.e. requested process, material, attribute type or certification) from the consumer
	 * @param conceptType   				the ontology class from which a relevant subset of concepts to be compared is retrieved (for processes this is 'MfgProcess', for materials this is 'MaterialType')
	 * @param onto 						the ontology from which the most semantically similar concept is retrieved
	 * @param vectorAggregationMethod	whether the embedding vectors associated with a concept (if compound) are summed or averaged.
	 * @return 							the most similar ontology concept given the consumerInput
	 * @throws IOException
	   Mar 27, 2020
	 */
	private static String getMostSimilarConcept(String consumerInput, QueryConceptType conceptType, OWLOntology onto, VectorAggregationMethod vectorAggregationMethod) throws IOException {
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();

		Set<String> classes = new HashSet<String>();
		//we only consider the classes below the stated ontology classes (e.g. MfgProcess for processes)
		if (conceptType == QueryConceptType.PROCESS) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MfgProcess", onto));
		} else if (conceptType == QueryConceptType.MATERIAL) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MaterialType", onto));
		} else if (conceptType == QueryConceptType.CERTIFICATION) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("Certification", onto));
		} else if (conceptType == QueryConceptType.ATTRIBUTE) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("AttributeType", onto));
		}

		//if the consumerInput equals an ontology concept we return this without using syntactic/semantic matching
		if (containsIgnoreCase(classes, consumerInput)) {
			
			return findConceptName(consumerInput, classes);
			
		} else {//if not, we do the semantic and syntactic matching process

			//basic pre-processing of the consumerInput
			String preProcessedConsumerProcess = preProcess(consumerInput);

			//create a vector map from the embeddings file for ontology concepts
			Map<String, double[]> vectorOntologyMap = embeddingManager.createOntologyVectorMap(classes, vectorAggregationMethod);
			double[] consumerInputVectors = embeddingManager.getLabelVector(preProcessedConsumerProcess, vectorAggregationMethod);
			String mostSemanticallySimilarConcept = null;
			
			//if there are no relevant embedding vectors for the consumer input, we do to the syntactic matching
			if (consumerInputVectors == null) {
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

	}
	
	/**
	 * Returns an ontology concept name (with proper casing) from a word in the embeddings file
	 *
	 * @param concept    the word in the embeddings file for which the relevant ontology concept name is retrieved.
	 * @param allClasses set of ontology concept names from the ontology
	 * @return an ontology concept name with proper casing (camel-case)
	 * Mar 3, 2020
	 */
	private static String findConceptName(String concept, Set<String> allClasses) {
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
	 *
	 * @param consumerConceptVectors the vectors of the query inserted by the consumer
	 * @param vectorOntologyMap      a map holding the ontology concept name as key and its vector representation as value
	 * @return the ontology concept having the vector most similar to the vector of the consumer input (process or material)
	 * Mar 3, 2020
	 */
	private static String findMostSimilarVector(double[] consumerConceptVectors, Map<String, double[]> vectorOntologyMap) {
		double sim = 0;
		double localSim = 0;
		String mostSimilar = null;

		for (Entry<String, double[]> e : vectorOntologyMap.entrySet()) {
			localSim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			if (localSim > sim) {
				mostSimilar = e.getKey();
				sim = localSim;
			}
		}

		return mostSimilar;
	}


	/**
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 *
	 * @param input                   the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @return the best matching concept from the MANUSQUARE ontology
	 * Nov 13, 2019
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
	 *
	 * @param similarityMap a map of concepts along with their similarity scores
	 * @return single concept (name) with highest similarity score
	 * Nov 13, 2019
	 */
	private static String getConceptWithHighestSim(Map<String, Double> similarityMap) {
		Map<String, Double> rankedResults = sortDescending(similarityMap);
		Map.Entry<String, Double> entry = rankedResults.entrySet().iterator().next();
		String conceptWithHighestSim = entry.getKey();
		return conceptWithHighestSim;
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

	/**
	 * (currently) removes whitespace from the consumer input received in the JSON file
	 * @param input consumer input (i.e. process, material, attribute, certification)
	 * @return pre-processed consumer input
	   Mar 27, 2020
	 */
	private static String preProcess(String input) {
		

		input = new String(StringUtilities.capitaliseWord(input).replaceAll("\\s+", ""));
		

		return input;
	}
	
	/**
	 * Checks if a set of strings includes the given string while ignoring the casing
	 * @param set set of strings
	 * @param soughtFor the string to check if exists in the set of strings
	 * @return true if the set of strings contains the given string, false otherwise
	   Mar 27, 2020
	 */
	private static boolean containsIgnoreCase(Set<String> set, String soughtFor) {
		for (String current : set) {
			if (current.equalsIgnoreCase(soughtFor)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * USED FOR EVALUATION OF EMBEDDINGS ONLY!
	 */
	public static Map<String, Double> getMostSimilarConceptWithScore(String consumerInput, QueryConceptType conceptType, OWLOntology onto, VectorAggregationMethod vectorAggregationMethod) throws IOException {
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();

		Map<String, Double> mostSimilarConceptMap = new HashMap<String, Double>();

		Set<String> classes = new HashSet<String>();
		//we only consider the classes below the stated ontology classes (e.g. MfgProcess for processes)
		if (conceptType == QueryConceptType.PROCESS) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MfgProcess", onto));
		} else if (conceptType == QueryConceptType.MATERIAL) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MaterialType", onto));
		} else if (conceptType == QueryConceptType.CERTIFICATION) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("Certification", onto));
		} else if (conceptType == QueryConceptType.ATTRIBUTE) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("AttributeType", onto));
		}

		//if the consumerInput equals an ontology concept we return this
		if (containsIgnoreCase(classes, consumerInput)) {
			mostSimilarConceptMap.put(findConceptName(consumerInput, classes), 1.0);
			return mostSimilarConceptMap;
		
		} else {//if not, we do the semantic and syntactic matching process

			//basic pre-processing of the consumerInput (whitespace removal, etc.)
			String preProcessedConsumerProcess = preProcess(consumerInput);

			//create a vector map from the embeddings file for ontology concepts
			Map<String, double[]> vectorOntologyMap = embeddingManager.createOntologyVectorMap(classes, vectorAggregationMethod);
			double[] consumerInputVectors = embeddingManager.getLabelVector(preProcessedConsumerProcess, vectorAggregationMethod);

			//if there are no relevant embedding vectors for the consumer input, we do to the syntactic matching
			if (consumerInputVectors == null) {
				
				mostSimilarConceptMap = getMostSimilarConceptSyntacticallyMap(preProcessedConsumerProcess, classes);
				
			} else {
				//check if vectormap/embeddings file contains consumerProcess as-is
				if (vectorOntologyMap.containsKey(preProcessedConsumerProcess.toLowerCase())) {
					
					mostSimilarConceptMap.put(preProcessedConsumerProcess, 1.0);
					
				} else { //if not, retrieve the most similar concept based on vector similarity
					
					mostSimilarConceptMap = findMostSimilarVectorMapEntry(consumerInputVectors, vectorOntologyMap);
					
				}

			}
		}

		return mostSimilarConceptMap;

	}

	/**
	 * USED FOR EVALUATION OF EMBEDDINGS ONLY!
	 */
	private static Map<String, Double> extractHighestMapEntry(Map<String, Double> supplierScores, int numResults) {
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
	 * USED FOR EVALUATION OF EMBEDDINGS ONLY!
	 */
	private static Map<String, Double> getMostSimilarConceptSyntacticallyMap(String input, Set<String> ontologyClassesAsString) {

		Map<String, Double> similarityMap = new HashMap<String, Double>();

		for (String s : ontologyClassesAsString) {

			similarityMap.put(s, new JaroWinklerSimilarity().apply(input, s));
		}

		Map<String, Double> mostSimilarConceptSyntactically = extractHighestMapEntry(similarityMap, 1);


		return mostSimilarConceptSyntactically;

	}

	/**
	 * USED FOR EVALUATION OF EMBEDDINGS ONLY!
	 */
	public static Map<String, Double> findMostSimilarVectorMapEntry(double[] consumerConceptVectors, Map<String, double[]> vectorOntologyMap) {
		Map<String, Double> mostSimilarVectorMapEntry = new HashMap<String, Double>();
		double sim = 0;
		double localSim = 0;
		String mostSimilar = null;

		for (Entry<String, double[]> e : vectorOntologyMap.entrySet()) {
			localSim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			if (localSim > sim) {
				mostSimilar = e.getKey();
				sim = localSim;
			}
		}

		mostSimilarVectorMapEntry.put(mostSimilar, sim);

		return mostSimilarVectorMapEntry;
	}

}
