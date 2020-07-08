package similarity;

import com.google.common.graph.MutableGraph;
import edm.Attribute;
import edm.Certification;
import edm.Material;
import edm.Process;
import owlprocessing.OntologyOperations;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;
import query.ConsumerQuery;
import query.InnovationManagementQuery;
import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityFactory;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParametersFactory;
import supplierdata.InnovationManager;
import supplierdata.Supplier;
import utilities.MathUtils;
import utilities.StringUtilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class SimilarityMeasures_IM {
	
public static List<Double> computeSemanticSimilarity_IM (InnovationManagementQuery query, InnovationManager innovationManager, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) {
		

		List<Certification> certificationList = innovationManager.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		SimilarityParameters parameters = null;

		double certificationSim = 0;

		List<Double> similarityList = new LinkedList<Double>();

				StringBuffer debuggingOutput = new StringBuffer();


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
				debuggingOutput.append("\nRequired certificates by consumer: " + StringUtilities.printSetItems(consumerCertifications));
				debuggingOutput.append("\nCertifications possessed by supplier: " + StringUtilities.printSetItems(supplierCertifications));
				debuggingOutput.append("\ncertificationSim is: " + certificationSim);

				similarityList.add(certificationSim);
				debuggingOutput.append("\nSimilarityList contains: " + similarityList);
				
				System.out.println(debuggingOutput.toString());
				return similarityList;
			}		
			

			public static double computeIndependentWUPSetSim (Set<String> consumerSet, Set<String> supplierSet, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
				ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);
				SimilarityParameters parameters = null;		
				List<Double> simList = new LinkedList<Double>();
				
				//FIXME: should not be here, only for hacking the issue with SUPSI/HOLONIX typing instances using concepts not within the ontology / graph
				Set<String> classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MaterialType", onto));

				if (consumerSet == null || consumerSet.isEmpty()) {
					return 1.0;
				}

				else if (supplierSet == null || supplierSet.isEmpty()) {
					return hard_coded_weight;
				}

				else {
					if (supplierSet.containsAll(consumerSet)) {
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
	
	

	

}
