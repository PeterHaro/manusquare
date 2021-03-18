package similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import edm.Attribute;
import utilities.MathUtilities;

public class SyntacticSimilarity {
	
	
	
	public static double computeAppearanceSimilarity(Set<String> consumerByProductAppearances, Set<String> supplierByProductAppearances) throws IOException {
		double appearanceSimilarity = 0;
		List<Double> appearanceSimilarityList = new ArrayList<Double>();
		
		if ((!consumerByProductAppearances.isEmpty() && consumerByProductAppearances != null) && (!supplierByProductAppearances.isEmpty() && supplierByProductAppearances != null)) {

			for (String consumerByProductMaterial : consumerByProductAppearances) {

				for (String supplierMaterial : supplierByProductAppearances) {

					appearanceSimilarityList.add(new JaroWinklerSimilarity().apply(consumerByProductMaterial.replaceAll("\\s", ""), supplierMaterial.replaceAll("\\s", "")));
				}

			}

			appearanceSimilarity = MathUtilities.getMaxFromList(appearanceSimilarityList);

		} else  {
			
			appearanceSimilarity = 1.0;
		}
		

		return appearanceSimilarity;

	}

	public static double computeAttributeSimilarity(Set<Attribute> consumerAttributes, Map<String, String> attributeWeightMap, double hard_coded_weight) {

		double attributeSim = 0;
		double avgAttributeSim = 0;
		

		if (consumerAttributes != null && containsAttributes(consumerAttributes)) {

			if (attributeWeightMap != null) {

				int counter = 0; 
				double sum = 0;

				//check which value ("Y", "N" or "O") the corresponding supplier process has
				for (Attribute a_c : consumerAttributes) {

					if (!a_c.getKey().equals("AttributeMaterial") && !a_c.getKey().equals("Appearance")) {

						if (attributeWeightMap.containsKey(a_c.getKey())) {

							if (attributeWeightMap.get(a_c.getKey()).equals("Y")) {
								attributeSim = 1.0;
							} else if (attributeWeightMap.get(a_c.getKey()).equals("O")) {
								attributeSim = hard_coded_weight;
							} else if (attributeWeightMap.get(a_c.getKey()).equals("N")) {
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

				}

				avgAttributeSim = sum / (double) counter;


			} else {

				avgAttributeSim = hard_coded_weight;
			}

			//if there are no consumer attributes, return an avgAttributeSim of 1.0
		} else {

			avgAttributeSim = 1.0;

		}

		return avgAttributeSim;
	}
	
	public static boolean containsAttributes (Set<Attribute> attributes) {
		
		int counter = 0;
		
		for (Attribute att : attributes) {
			if (!att.getKey().equalsIgnoreCase("AttributeMaterial") && !att.getKey().equalsIgnoreCase("Appearance")) {
				counter++;
			} 
		}
				
		if (counter > 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 *
	 * @param input                   the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @return the best matching concept from the MANUSQUARE ontology
	 * Nov 13, 2019
	 */
	public static String getMostSimilarConceptSyntactically(String input, Set<String> ontologyClassesAsString) {

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
