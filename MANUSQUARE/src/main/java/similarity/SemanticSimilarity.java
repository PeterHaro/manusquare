package similarity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import ontology.OntologyOperations;
import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityFactory;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParametersFactory;
import utilities.MathUtils;
import utilities.StringUtilities;


public class SemanticSimilarity {
	
	public static double computeSemanticSetSimilarity (Set<String> consumerSet, Set<String> supplierSet, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
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
