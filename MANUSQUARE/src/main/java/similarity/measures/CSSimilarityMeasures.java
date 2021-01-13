package similarity.measures;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Attribute;
import edm.Certification;
import edm.Process;
import ontology.OntologyOperations;
import query.CSQuery;
import similarity.SimilarityMethods;
import similarity.methodologies.ISimilarity;
import similarity.methodologies.SimilarityFactory;
import similarity.methodologies.parameters.SimilarityParameters;
import similarity.methodologies.parameters.SimilarityParametersFactory;
import supplier.CSSupplier;
import utilities.MathUtilities;
import utilities.StringUtilities;

public class CSSimilarityMeasures {

	public static List<Double> computeSemanticSimilarity (CSQuery query, CSSupplier supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) {

		List<Process> processList = supplier.getProcesses();
		List<Certification> certificationList = supplier.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		//for each process in the query, compute the process facet similarity
		String consumerQueryProcessNode = null;
		String supplierResourceProcessNode = null;

		SimilarityParameters parameters = null;

		double processSim = 0;
		double materialSim = 0;
		double attributeSim = 0;
		double certificationSim = 0;
		double finalProcessSim = 0;

		List<Double> similarityList = new LinkedList<Double>();
		int numConsumerProcesses = query.getProcesses().size();

		for (Process pc : query.getProcesses()) {
			Set<String> consumerMaterials = new HashSet<String>();

			//check if there are any materials specified in the consumer query
			if (pc.getMaterials() != null) {

				for (String m : pc.getMaterials()) {
					consumerMaterials.add(m);
				}
			}


			List<Double> processSimList = new LinkedList<Double>();

			for (Process ps : processList) {


				/* PROCESS SIMILARITY */	

				//represent processes as graph nodes
				consumerQueryProcessNode = pc.getName();
				supplierResourceProcessNode = ps.getName();

				Set<String> equivalentProcesses = pc.getEquivalentProcesses();

				//if supplier process ps is a part of the equivalent process concepts of consumer process pc, the processSim is 1.0
				if (equivalentProcesses != null && equivalentProcesses.contains(ps.getName())) {
					processSim = 1.0;
				} else {
					parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerQueryProcessNode, supplierResourceProcessNode, onto, graph);
					processSim = similarityMethodology.ComputeSimilaritySimpleGraph(parameters);
				}


				/* MATERIAL SIMILARITY */	

				Set<String> supplierMaterials = new HashSet<String>();
				for (String material : ps.getMaterials()) {
					supplierMaterials.add(material);
				}


				materialSim = computeIndependentWUPSetSim (consumerMaterials, supplierMaterials, similarityMethod, onto, graph, hard_coded_weight);

				/* ATTRIBUTE SIMILARITY */		

				Set<Attribute> consumerAttributes = pc.getAttributes();
				double avgAttributeSim = 0;

				//if there are any consumer attributes, we use these to influence the processAndMaterialSim
				if (consumerAttributes != null && !consumerAttributes.isEmpty()) {

					int counter = 0; 
					double sum = 0;

					//check which value ("Y", "N" or "O") the corresponding supplier process has
					for (Attribute a_c : consumerAttributes) {

						if (ps.getAttributeWeightMap().containsKey(a_c.getKey())) {

							if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("Y")) {
								attributeSim = 1.0;
							} else if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("O")) {
								attributeSim = hard_coded_weight;
							} else if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("N")) {
								attributeSim = hard_coded_weight;
							}

							sum += attributeSim;
							counter++;

						} else {
							attributeSim = hard_coded_weight;
							sum += attributeSim;
							counter++;

						}

					}

					avgAttributeSim = sum / (double) counter;


					//if there are no consumer attributes, return an avgAttributeSim of 1.0
				} else {

					avgAttributeSim = 1.0;

				}


				finalProcessSim = (processSim * 0.7) + (materialSim * 0.15) + (avgAttributeSim * 0.15);

				/* CERTIFICATION SIMILARITY */

				Set<Certification> initialConsumerCertifications = query.getCertifications();
				Set<String> consumerCertifications = new HashSet<String>();

				if (initialConsumerCertifications != null) {
					for (Certification c : initialConsumerCertifications) {
						consumerCertifications.add(c.getId());				
					}
				}

				Set<String> supplierCertifications = new HashSet<String>();
				for (Certification c : certificationList) {
					supplierCertifications.add(c.getId());
				}

				certificationSim = computeIndependentWUPSetSim (consumerCertifications, supplierCertifications, similarityMethod, onto, graph, hard_coded_weight);


				double finalSim = (finalProcessSim * 0.7) + (certificationSim * 0.3);

				processSimList.add(finalSim);

			}		

			similarityList.add(getHighestScore(processSimList));

		}	

		return similarityList;

	}

	/**
	 * Sorts the scores for each resource offered by a supplier (from highest to lowest)
	 *
	 * @param inputScores a list of scores for each supplier resource assigned by the semantic matching
	 * @return the n highest scores from a list of input scores
	 * Oct 12, 2019
	 */
	private static double getHighestScore(List<Double> inputScores) {
		inputScores.sort(Collections.reverseOrder());
		return inputScores.get(0);

	}

	public static double computeIndependentWUPSetSim (Set<String> consumerSet, Set<String> supplierSet, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
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
		else {
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


							s = getMostSimilarConceptSyntactically(s, classes);

							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
							simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

						}

					}
				}

				return MathUtilities.sum(simList) / (double)simList.size();

			}
		}

	}

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


	public static double computeWUPSetSim (Set<String> consumerSet, Set<String> supplierSet, double initialSim, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);
		SimilarityParameters parameters = null;		
		List<Double> simList = new LinkedList<Double>();

		if (consumerSet == null || consumerSet.isEmpty()) {
			return 1.0;
		}
		


		else if (supplierSet == null || supplierSet.isEmpty()) {
			return initialSim * hard_coded_weight;
		}

		//FIXME: Temporary hack to ensure that case is ignored when comparing sets, should standardised lowercase/uppercase everywhere!
		else {
			if (StringUtilities.containsAllIgnoreCase(consumerSet, supplierSet)) {
				return 1.0;
			}

			else {

				for (String c : consumerSet) {
					for (String s : supplierSet) {

						parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
						simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

					}
				}

				return MathUtilities.sum(simList) / (double)simList.size();

			}
		}

	}



}