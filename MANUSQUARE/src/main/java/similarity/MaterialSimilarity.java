package similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import similarity.methodologies.ISimilarity;
import similarity.methodologies.parameters.SimilarityParameters;
import similarity.methodologies.parameters.SimilarityParametersFactory;
import utilities.MathUtilities;

public class MaterialSimilarity {

	public static double computeMaterialSimilarity(Set<String> consumerByProductMaterials, Set<String> supplierMaterials, OWLOntology onto, ISimilarity similarityMethodology, SimilarityMethods similarityMethod, MutableGraph<String> graph, Set<String> allOntologyClasses) throws IOException {
		
		double materialSimilarity = 0;
		List<Double> materialSimilarityList = new ArrayList<Double>();

		SimilarityParameters parameters = null;

		if ((!consumerByProductMaterials.isEmpty() && consumerByProductMaterials != null) && (!supplierMaterials.isEmpty() && supplierMaterials != null)) {

			for (String consumerByProductMaterial : consumerByProductMaterials) {
				
				for (String supplierMaterial : supplierMaterials) {
					
					System.out.println("Computing Material Similarity for consumer material: " + consumerByProductMaterial + " and supplier material: " + supplierMaterial);		

					if (consumerByProductMaterial != null) {
						parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerByProductMaterial, supplierMaterial, onto, graph);
						materialSimilarityList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));
					} 
					
				}

			}

			materialSimilarity = MathUtilities.getMaxFromList(materialSimilarityList);

		} else  {
			//assuming that consumerByProductMaterials (combining by-product name and material attributes) will never be empty or null
			materialSimilarity = 0;
		}

		return materialSimilarity;

	}

}
