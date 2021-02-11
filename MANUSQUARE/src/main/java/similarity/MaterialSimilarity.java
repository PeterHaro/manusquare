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
import utilities.StringUtilities;

public class MaterialSimilarity {

	public static double computeMaterialSimilarity(Set<String> consumerMaterials, Set<String> supplierMaterials, OWLOntology onto, ISimilarity similarityMethodology, SimilarityMethods similarityMethod, MutableGraph<String> graph, Set<String> allOntologyClasses) throws IOException {

		double materialSimilarity = 0;
		List<Double> materialSimilarityList = new ArrayList<Double>();
				
		SimilarityParameters parameters = null;
		
		//if consumer hasn´t specified any material we should return sim 1
		if (consumerMaterials.isEmpty() || consumerMaterials == null) {
			materialSimilarity = 1.0;
		}
		
		//if the consumer has specified any material and the supplier hasn´t for this process, we should return sim 0
		else if (supplierMaterials.isEmpty() || supplierMaterials == null) {
			materialSimilarity = 0.0;
		}

		//if both the consumer and the supplier has specified materials for this process we do a pairwise comparison of all consumer materials and all supplier materials
		else {
			
			for (String consumerMaterial : consumerMaterials) {

				for (String supplierMaterial : supplierMaterials) {
					
					//if this particular consumer material or this particular supplier material is null or empty we add a sim of 0
					if (consumerMaterial == null || consumerMaterial.isEmpty() || supplierMaterial == null || supplierMaterial.isEmpty()) {
						materialSimilarityList.add(0.0);
					}
					
					//if this particular consumer material is syntactical equal to supplier material we add a sim of 1
					else if (consumerMaterial.equalsIgnoreCase(supplierMaterial)) {
						materialSimilarityList.add(1.0);
					}
					
					//if neither of the above applies, and both consumer material and supplier material reside in the graph, 
					//we compute the semantic similarity between this particular consumer material and this particular supplier material
					else {
						
						if (StringUtilities.containsIgnoreCase(graph.nodes(), consumerMaterial) && StringUtilities.containsIgnoreCase(graph.nodes(), supplierMaterial)) {
							
							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerMaterial, supplierMaterial, onto, graph);
							materialSimilarityList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));
						}
						
						//if for some reason the consumer material and the supplier material reside in the graph we add sim 0
						else {
							materialSimilarityList.add(0.0);
						}
					}

				}

			}

			//we return the highest sim for all materials for this process
			materialSimilarity = MathUtilities.getMaxFromList(materialSimilarityList);
		}


		return materialSimilarity;

	}

}
