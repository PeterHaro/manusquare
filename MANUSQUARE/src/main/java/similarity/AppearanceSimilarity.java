package similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import utilities.MathUtils;

public class AppearanceSimilarity {
	
	public static double computeAppearanceSimilarity(Set<String> consumerByProductAppearances, Set<String> supplierByProductAppearances) throws IOException {
		double appearanceSimilarity = 0;
		List<Double> appearanceSimilarityList = new ArrayList<Double>();
		
		if ((!consumerByProductAppearances.isEmpty() && consumerByProductAppearances != null) && (!supplierByProductAppearances.isEmpty() && supplierByProductAppearances != null)) {

			for (String consumerByProductMaterial : consumerByProductAppearances) {

				for (String supplierMaterial : supplierByProductAppearances) {

					appearanceSimilarityList.add(new JaroWinklerSimilarity().apply(consumerByProductMaterial, supplierMaterial));
				}

			}

			appearanceSimilarity = MathUtils.getMaxFromList(appearanceSimilarityList);

		} else  {
			
			appearanceSimilarity = 1.0;
		}
		

		return appearanceSimilarity;

	}


}
