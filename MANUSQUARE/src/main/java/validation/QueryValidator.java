package validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Iterables;

import data.EmbeddingSingletonDataManager;
import edm.Attribute;
import edm.Certification;
import embedding.vectoraggregation.VectorAggregationMethod;
import ontology.OntologyOperations;
import query.QueryConceptType;
import similarity.techniques.Cosine;
import utilities.StringUtilities;

public class QueryValidator {
	
	final static double SEMANTIC_SIMILARITY_TRESHOLD = 0.7;
	final static double SYNTACTIC_SIMILARITY_TRESHOLD = 0.9;
		
	public static String validateByProductName(String byProductName, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		
		String validatedByProductName = null;

		if (!allOntologyClasses.contains(byProductName)) {
			validatedByProductName = getMostSimilarConcept(byProductName.trim(), QueryConceptType.BYPRODUCT, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedByProductName = byProductName;
		}
						
		return validatedByProductName;
	}
	
	/**
	 * Ensures that an innovation phase included by the consumer is in fact in the ontology (if not, the closest matching concept from the ontology is returned)
	 * @param innovationPhase innovation phase included by the consumer
	 * @param onto the MANU-SQUARE ontology
	 * @param allOntologyClasses string representation of all ontology concepts
	 * @return an innovation phase verified to exist in the MANU-SQUARE ontology
	 * @throws IOException
	   Aug 26, 2020
	 */
	public static String validateInnovationPhase(String innovationPhase, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {

		String validatedInnovationPhase = null;

		if (!allOntologyClasses.contains(innovationPhase)) {
			validatedInnovationPhase = getMostSimilarConcept(innovationPhase.trim(), QueryConceptType.INNOVATIONPHASE, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedInnovationPhase = innovationPhase;
		}
				
		return validatedInnovationPhase;
	}
	
	/**
	 * Ensures that an innovation type included by the consumer is in fact in the ontology (if not, the closest matching concept from the ontology is returned)
	 * @param innovationType innovation type included by the consumer
	 * @param onto the MANU-SQUARE ontology
	 * @param allOntologyClasses string representation of all ontology concepts
	 * @return an innovation type verified to exist in the MANU-SQUARE ontology
	 * @throws IOException
	   Aug 26, 2020
	 */
	public static String validateInnovationType(String innovationType, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {

		String validatedInnovationType = null;

		if (!allOntologyClasses.contains(innovationType)) {
			validatedInnovationType = getMostSimilarConcept(innovationType.trim(), QueryConceptType.INNOVATIONTYPE, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedInnovationType = innovationType;
		}
				
		return validatedInnovationType;
	}
	
	/**
	 * Ensures that a skill included by the consumer is in fact in the ontology (if not, the closest matching concept from the ontology is returned)
	 * @param skill skill included by the consumer
	 * @param onto the MANU-SQUARE ontology
	 * @param allOntologyClasses string representation of all ontology concepts
	 * @return a skill verified to exist in the MANU-SQUARE ontology
	 * @throws IOException
	   Aug 26, 2020
	 */
	public static String validateSkill(String skill, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {

		String validatedSkill = null;

		if (!allOntologyClasses.contains(skill)) {
			validatedSkill = getMostSimilarConcept(skill.trim(), QueryConceptType.SKILL, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedSkill = skill;
		}
				
		return validatedSkill;
	}
	
	/**
	 * Ensures that a sector included by the consumer is in fact in the ontology (if not, the closest matching concept from the ontology is returned)
	 * @param sector sector included by the consumer
	 * @param onto the MANU-SQUARE ontology
	 * @param allOntologyClasses string representation of all ontology concepts
	 * @return a sector verified to exist in the MANU-SQUARE ontology
	 * @throws IOException
	   Aug 26, 2020
	 */
	public static String validateSector(String sector, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {

		String validatedSector = null;

		if (!allOntologyClasses.contains(sector)) {
			validatedSector = getMostSimilarConcept(sector.trim(), QueryConceptType.SECTOR, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedSector = sector;
		}
				
		return validatedSector;
	}
	
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
			validatedProcessName = getMostSimilarConcept(processName.trim(), QueryConceptType.PROCESS, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedProcessName = processName;
		}
				
		return validatedProcessName;
	}
	
	/**
	 * Ensures that a material included by the consumer is in fact in the ontology (if not, the closest matching concept from the ontology is returned)
	 * @param materialName included by the consumer
	 * @param onto the MANU-SQUARE ontology
	 * @param allOntologyClasses string representation of all ontology concepts
	 * @return a material name verified to exist in the MANU-SQUARE ontology
	 * @throws IOException
	   Dec 3, 2020
	 */
	public static String validateMaterialName(String materialName, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		
		String validatedMaterialName = null;

		if (!allOntologyClasses.contains(materialName)) {
			validatedMaterialName = getMostSimilarConcept(materialName.trim(), QueryConceptType.MATERIAL, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedMaterialName = materialName;
		}
				
		return validatedMaterialName;
		
	}

	
	/**
	 * Checks if all materials specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialMaterials set of materials specified by the consumer in the RFQ process
	 * @return set of materials we´re sure exist as concepts in the ontology
	 * Nov 13, 2019
	 * @throws IOException
	 */
	public static Set<String> validateMaterials(Set<String> initialMaterials, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<String> validatedMaterials = new HashSet<String>();
		if (initialMaterials.isEmpty() || initialMaterials == null) {
			return null;
		} else {
			for (String m : initialMaterials) {
				if (!allOntologyClasses.contains(m)) { //if not, get the concept from the ontology with the highest similarity
					m = getMostSimilarConcept(m, QueryConceptType.MATERIAL, onto, EmbeddingSingletonDataManager.VAM);
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
		Set<Certification> validatedCertifications = new HashSet<Certification>();

		for (Certification c : initialCertifications) {
			if (!allOntologyClasses.contains(c.getId())) { //if not, get the concept from the ontology with the highest similarity
				c.setId(getMostSimilarConcept(c.getId(), QueryConceptType.CERTIFICATION, onto, EmbeddingSingletonDataManager.VAM));
				validatedCertifications.add(c);
			} else {
				validatedCertifications.add(c);
			}
		}

		return validatedCertifications;

	}
	
	/**
	 * Checks if all certifications specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialCertifications set of certifications specified by the consumer in the RFQ process
	 * @return set of certifications we´re sure exist as concepts in the ontology
	 * Nov 13, 2019
	 * @throws IOException
	 */
	public static List<Certification> validateCertifications (List<Certification> initialCertifications, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		
		List<Certification> validatedCertifications = new ArrayList<Certification>();

		for (Certification c : initialCertifications) {
			if (!allOntologyClasses.contains(c.getId())) { //if not, get the concept from the ontology with the highest similarity
				c.setId(getMostSimilarConcept(c.getId(), QueryConceptType.CERTIFICATION, onto, EmbeddingSingletonDataManager.VAM));
				validatedCertifications.add(c);
			} else {
				validatedCertifications.add(c);
			}
		}

		return validatedCertifications;

	}
	
	public static Certification validateCertification (Certification cert, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		
		Certification validatedCertification = new Certification();
		
		if (cert != null) {
		if (!allOntologyClasses.contains(cert.getId())) {
			validatedCertification.setId(getMostSimilarConcept(validatedCertification.getId(), QueryConceptType.CERTIFICATION, onto, EmbeddingSingletonDataManager.VAM));
		}
		
		return validatedCertification;
		} else {
			return null;
		}
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
		
		String mostSimilarConcept = null;
		String mostSyntacticallySimilarConcept = null;
		String mostSemanticallySimilarConcept = null;
		
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();
			
		Set<String> classes = getRelevantOntologyClasses (conceptType, onto);

		//if the consumerInput equals an ontology concept we return this without using syntactic/semantic matching
		if (containsIgnoreCase(classes, consumerInput)) {
			
			mostSimilarConcept = findConceptName(consumerInput, classes);

		//else check the sim score of the most syntactically similar concept, if this is above SYNTACTIC_SIMILARITY_THRESHOLD, use this as the most similar concept
		} else {
						
			String preProcessedConsumerInput = preProcess(consumerInput);			
			mostSyntacticallySimilarConcept = getMostSimilarConceptSyntactically(preProcessedConsumerInput, classes, SYNTACTIC_SIMILARITY_TRESHOLD);
					
			if (mostSyntacticallySimilarConcept != null) {
				
				mostSimilarConcept = mostSyntacticallySimilarConcept;
				
			//if not, we do the semantic matching process	
			} else {

			//create a vector map from the embeddings file for ontology concepts
			Map<String, double[]> vectorOntologyMap = embeddingManager.createOntologyVectorMap(classes, vectorAggregationMethod);
			double[] consumerInputVectors = embeddingManager.getLabelVector(preProcessedConsumerInput, vectorAggregationMethod);
						
			//as long as there are relevant embedding vectors for the consumer input, we do to the semantic matching
			if (consumerInputVectors != null) {
								
				//check if vectormap/embeddings file contains consumerProcess as-is
				if (vectorOntologyMap.containsKey(preProcessedConsumerInput.toLowerCase())) {
										
					mostSemanticallySimilarConcept = preProcessedConsumerInput;
					
				} else { //if not, retrieve the most similar concept based on vector similarity										
					
					mostSemanticallySimilarConcept = getMostSemanticallySimilarConcept(consumerInputVectors, vectorOntologyMap, classes, SEMANTIC_SIMILARITY_TRESHOLD);							
					
				}
				
				if (mostSemanticallySimilarConcept != null) {
				
					mostSimilarConcept = mostSemanticallySimilarConcept;				
				
				} else {
					
					mostSimilarConcept = null;
				}
			}
			}

		}
				
		return mostSimilarConcept;
	}
	

	
	/**
	 * Uses cosine similarity to find the most similar word in the embeddings file by its vector representation
	 *
	 * @param consumerConceptVectors the vectors of the query inserted by the consumer
	 * @param vectorOntologyMap      a map holding the ontology concept name as key and its vector representation as value
	 * @return the ontology concept having the vector most similar to the vector of the consumer input (process or material)
	 * Mar 3, 2020
	 */
	private static String getMostSemanticallySimilarConcept(double[] consumerConceptVectors, Map<String, double[]> vectorOntologyMap, Set<String> allClasses, double SEMANTIC_SIMILARITY_TRESHOLD) {
		double sim = 0;
		double localSim = 0;
		String mostSimilar = null;

		for (Entry<String, double[]> e : vectorOntologyMap.entrySet()) {
			localSim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			if (localSim > sim && localSim >= SEMANTIC_SIMILARITY_TRESHOLD) {
				mostSimilar = e.getKey();
				sim = localSim;
								
			}
		}
		
		//return with proper casing
		String mostSimilarityWithProperCasing = null;
		if (mostSimilar != null) {
			mostSimilarityWithProperCasing = findConceptName(mostSimilar, allClasses);
			return mostSimilarityWithProperCasing;
		} else { 
			return null;
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
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 *
	 * @param input                   the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @return the best matching concept from the MANUSQUARE ontology
	 * Nov 13, 2019
	 */
	private static String getMostSimilarConceptSyntactically(String input, Set<String> ontologyClassesAsString, double SYNTACTIC_SIMILARITY_TRESHOLD) {

		Map<String, Double> similarityMap = new HashMap<String, Double>();
		String mostSimilarConcept = null;

		for (String s : ontologyClassesAsString) {

			similarityMap.put(s, new JaroWinklerSimilarity().apply(input, s));
		}

		mostSimilarConcept = getConceptWithHighestSim(similarityMap);
		
		//only return if similarity is higher than threshold
		if (similarityMap.get(mostSimilarConcept) >= SYNTACTIC_SIMILARITY_TRESHOLD) {
			return mostSimilarConcept;
		} else {
			return null;
		}

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
		Entry<String, Double> entry = rankedResults.entrySet().iterator().next();
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
	
private static Set<String> getRelevantOntologyClasses (QueryConceptType conceptType, OWLOntology onto) {
		
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
		} else if (conceptType == QueryConceptType.INNOVATIONPHASE) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("InnovationPhase", onto));
		} else if (conceptType == QueryConceptType.INNOVATIONTYPE) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("InnovationType", onto));
		} else if (conceptType == QueryConceptType.SECTOR) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("Industry", onto));
			classes.addAll(OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("TCSectorClass", onto)));
			classes.addAll(OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("AGVSectorClass", onto)));
		} else if (conceptType == QueryConceptType.SKILL) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("CapabilityType", onto));
		} else if (conceptType == QueryConceptType.BYPRODUCT) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MaterialType", onto));
		} 
		
		return classes;
	}
	

}
