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

	//TODO: Double-check and simplify
	public static double computeMaterialSimilarity(Set<String> consumerMaterials, Set<String> supplierMaterials, OWLOntology onto, ISimilarity similarityMethodology, SimilarityMethods similarityMethod, MutableGraph<String> graph, Set<String> allOntologyClasses) throws IOException {
		
		double materialSimilarity = 0;
		List<Double> materialSimilarityList = new ArrayList<Double>();

		SimilarityParameters parameters = null;

		if ((consumerMaterials != null && !consumerMaterials.isEmpty()) && (supplierMaterials != null && !supplierMaterials.isEmpty())) {

			for (String consumerMaterial : consumerMaterials) {
				
				for (String supplierMaterial : supplierMaterials) {
					
					if (consumerMaterial != null && supplierMaterial != null) {
						
						if (consumerMaterial.equals(supplierMaterial)) {
							materialSimilarityList.add(1.0);
						
						} else {
							
							if (graph.nodes().contains(consumerMaterial) && graph.nodes().contains(supplierMaterial)) {
								parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerMaterial, supplierMaterial, onto, graph);
								materialSimilarityList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));
							} else {
								
								materialSimilarityList.add(0.0);
							}

						}
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
