package similarity;

import java.util.Map;
import java.util.Set;

import json.ByProductSharingRequest.ByProductAttributes;

public class AttributeSimilarity {
	
	public static double computeAttributeSimilarity(Set<ByProductAttributes> consumerAttributes, Map<String, String> attributeWeightMap, double hard_coded_weight) {
		
		double attributeSim = 0;
		double avgAttributeSim = 0;
		
		if (consumerAttributes != null) {

			if (attributeWeightMap != null) {


				int counter = 0; 
				double sum = 0;

				//check which value ("Y", "N" or "O") the corresponding supplier process has
				for (ByProductAttributes a_c : consumerAttributes) {

					if (attributeWeightMap.containsKey(a_c.getAttributeKey())) {

						if (attributeWeightMap.get(a_c.getAttributeKey()).equals("Y")) {
							attributeSim = 1.0;
						} else if (attributeWeightMap.get(a_c.getAttributeKey()).equals("O")) {
							attributeSim = hard_coded_weight;
						} else if (attributeWeightMap.get(a_c.getAttributeKey()).equals("N")) {
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

				
			} else {
				
				avgAttributeSim = hard_coded_weight;
			}

				//if there are no consumer attributes, return an avgAttributeSim of 1.0
			} else {

				avgAttributeSim = 1.0;

			}
		
		return avgAttributeSim;
	}

}
