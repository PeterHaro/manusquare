package similarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Certification;
import owlprocessing.OntologyOperations;
import query.InnovationManagementQuery;
import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityFactory;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParametersFactory;
import supplierdata.InnovationManager;
import utilities.MathUtils;
import utilities.StringUtilities;

public class SimilarityMeasures_IM {

	public static List<Double> computeSemanticSimilarity_IM (InnovationManagementQuery query, InnovationManager innovationManager, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) {


		List<Certification> certificationList = innovationManager.getCertifications();
		List<String> supplierInnovationPhases = innovationManager.getInnovationPhases();
		List<String> supplierInnovationTypes = innovationManager.getInnovationTypes();
		List<String> supplierSkills = innovationManager.getSkills();
		List<String> supplierSectors = innovationManager.getSectors();
		String supplierName = innovationManager.getSupplierName();
		String supplierID = innovationManager.getId();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		SimilarityParameters parameters = null;

		double innovationPhaseSim = 0;
		double innovationTypeSim = 0;
		double skillSim = 0;
		double sectorSim = 0;
		double certificationSim = 0;
		double finalSim = 0;


		List<Double> similarityList = new LinkedList<Double>();

		StringBuffer debuggingOutput = new StringBuffer();

		/* INNOVATION PHASE SIMILARITY */
		List<String> initialConsumerInnovationPhases = query.getInnovationPhases();

		if (initialConsumerInnovationPhases == null) {

			innovationPhaseSim = 0;

		} else {

			innovationPhaseSim = computeIndependentWUPSetSim (initialConsumerInnovationPhases, supplierInnovationPhases, similarityMethod, onto, graph, hard_coded_weight);
		}
		debuggingOutput.append("\nThe supplier name is: " + supplierName);
		debuggingOutput.append("\nThe supplier ID is: " + supplierID);
		debuggingOutput.append("\nThe consumer´s requested innovation phases are: " + initialConsumerInnovationPhases);
		debuggingOutput.append("\nThe supplier´s innovation phases are: " + supplierInnovationPhases);
		debuggingOutput.append("\nInnovationPhaseSim is: " + innovationPhaseSim);


		/* INNOVATION TYPE SIMILARITY */
		List<String> initialConsumerInnovationTypes = query.getInnovationTypes();

		if (initialConsumerInnovationTypes == null) {
			innovationTypeSim = 0;
		} else {
			innovationTypeSim = computeIndependentWUPSetSim (initialConsumerInnovationTypes, supplierInnovationTypes, similarityMethod, onto, graph, hard_coded_weight);
		}

		debuggingOutput.append("\nThe consumer´s requested innovation types are: " + initialConsumerInnovationTypes);
		debuggingOutput.append("\nThe supplier´s innovation types are: " + supplierInnovationTypes);
		debuggingOutput.append("\nInnovationTypeSim is: " + innovationTypeSim);


		/* SKILL SIMILARITY */
		List<String> initialConsumerSkills = query.getSkills();

		if (initialConsumerSkills == null) {
			skillSim = 0;
		} else {
			skillSim = computeIndependentWUPSetSim (initialConsumerSkills, supplierSkills, similarityMethod, onto, graph, hard_coded_weight);
		}

		debuggingOutput.append("\nThe consumer´s requested innovation skills are: " + initialConsumerSkills);
		debuggingOutput.append("\nThe supplier´s innovation skills are: " + supplierSkills);
		debuggingOutput.append("\nSkillSim is: " + skillSim);


		/* SECTOR SIMILARITY */
		List<String> initialConsumerSectors = query.getSectors();

		if (initialConsumerSectors == null) {
			sectorSim = 0;
		} else {
			sectorSim = computeIndependentWUPSetSim (initialConsumerSectors, supplierSectors, similarityMethod, onto, graph, hard_coded_weight);
		}

		debuggingOutput.append("\nThe consumer´s requested innovation sectors are: " + initialConsumerSectors);
		debuggingOutput.append("\nThe supplier´s innovation sectors are: " + supplierSectors);
		debuggingOutput.append("\nSectorSim is: " + sectorSim);


		/* CERTIFICATION SIMILARITY */

		Set<Certification> initialConsumerCertifications = query.getCertifications();
		List<String> consumerCertifications = new ArrayList<String>();

		if (initialConsumerCertifications != null) {
			for (Certification c : initialConsumerCertifications) {
				consumerCertifications.add(c.getId());				
			}
		}

		List<String> supplierCertifications = new ArrayList<String>();
		for (Certification c : certificationList) {
			supplierCertifications.add(c.getId());
		}

		certificationSim = computeIndependentWUPSetSim (consumerCertifications, supplierCertifications, similarityMethod, onto, graph, hard_coded_weight);
		debuggingOutput.append("\nRequired certificates by consumer: " + StringUtilities.printListItems(consumerCertifications));
		debuggingOutput.append("\nCertifications possessed by supplier: " + StringUtilities.printListItems(supplierCertifications));
		debuggingOutput.append("\ncertificationSim is: " + certificationSim);
		
		
		//FIXME: Find a better solution to ensure that suppliers having innovationPhaseSim / innovationTypeSim = 1.0 are included in the returned list of suppliers
		if (innovationPhaseSim == 1.0 || innovationTypeSim == 1.0) {
			finalSim = (((innovationPhaseSim + innovationTypeSim) / 2) * 0.9) + (((sectorSim + skillSim) / 2) * 0.05) + (certificationSim * 0.05);
		} else {
		finalSim = (((innovationPhaseSim + innovationTypeSim + sectorSim) / 3) * 0.3) + (skillSim * 0.5) + (certificationSim * 0.2);
		}

		similarityList.add(finalSim);
		debuggingOutput.append("\nSimilarityList contains: " + similarityList);



		System.out.println(debuggingOutput.toString());
		return similarityList;
	}		

	public static double computeIndependentWUPSetSim (List<String> consumerSet, List<String> supplierSet, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);
		SimilarityParameters parameters = null;		
		List<Double> simList = new LinkedList<Double>();

		//get all ontology classes for the syntactical matching
		Set<String> classes = OntologyOperations.getClassesAsString(onto);

		if (consumerSet == null || consumerSet.isEmpty()) {
			return 1.0;
		}

		else if (supplierSet == null || supplierSet.isEmpty()) {
			return hard_coded_weight;
		}

		//FIXME: Temporary hack to ensure that casing is ignored between consumer items and supplier items
		//FIXME: //include labels of concepts in consumerSet
		else {
			
			//List<String> classesAndLabels = getConceptNamesAndLabels(consumerSet, onto);
			
			if (StringUtilities.containsAllIgnoreCase(consumerSet, supplierSet)) {
				return 1.0;
			}

			else {

				for (String c : consumerSet) {
					for (String s : supplierSet) {

						//FIXME: must ensure that both nodes are within the ontology graph. This is not always the case since some materials (e.g. StainlessSteel-301) are added incorrectly (e.g. StainlessSteel301) from SUPSI/HOLONIX
						if (nodeInGraph (s, graph)) {

							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
							simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

						} else {

							//System.err.println("Transforming from " + s + " to " + getMostSimilarConceptSyntactically(s, classes));

							//find ontology concept / graph node with highest string sim (jaro winkler)								
							//System.err.println("Transforming from " + s + " to " + getMostSimilarConceptSyntactically(s, classes));

							s = getMostSimilarConceptSyntactically(s, classes);

							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
							simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

						}

					}
				}

				return MathUtils.sum(simList) / (double)simList.size();

			}
		}

	}
	
//	public static List<String> getConceptNamesAndLabels (List<String> inputSet, OWLOntology onto) {
//		
//		List<String> labels = new ArrayList<String>();
//		
//		OWLClass cls = null;
//		
//		for (String s : inputSet) {
//			
//			cls = OntologyOperations.getClass(s, onto);
//			labels.add(OntologyOperations.getLabelFromClass(onto, cls));
//			
//		}
//		
//		inputSet.addAll(labels);
//		
//		return inputSet;
//		
//	}


	private static boolean nodeInGraph(String node, MutableGraph graph) {

		if (graph.nodes().contains(node)) {
			return true;
		} else {
			return false;
		}


	}

	/* FIXME: THESE SHOULD NOT BE HERE, ONLY FOR RESOLVING THE ISSUE WITH SUPSI/HOLONIX TYPING INSTANCES USING CONCEPTS NOT WITHIN THE ONTOLOGY /GRAPH */

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

			similarityMap.put(s, new JaroWinklerSimilarity().apply(input.toLowerCase(), s.toLowerCase()));
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





}
