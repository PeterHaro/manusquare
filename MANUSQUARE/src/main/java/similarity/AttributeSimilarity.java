package similarity;

import java.util.Map;
import java.util.Set;

import edm.Attribute;


public class AttributeSimilarity {

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
	
	private static boolean containsAttributes (Set<Attribute> attributes) {
		
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

}
