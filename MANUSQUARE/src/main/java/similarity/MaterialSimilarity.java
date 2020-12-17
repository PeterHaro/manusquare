package similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParametersFactory;
import utilities.MathUtils;
import validation.QueryValidation;

public class MaterialSimilarity {

	public static double computeMaterialSimilarity(Set<String> consumerByProductMaterials, Set<String> supplierMaterials, OWLOntology onto, ISimilarity similarityMethodology, SimilarityMethods similarityMethod, MutableGraph<String> graph, Set<String> allOntologyClasses) throws IOException {
		double materialSimilarity = 0;
		List<Double> materialSimilarityList = new ArrayList<Double>();

		SimilarityParameters parameters = null;

		String validatedConsumerMaterial = null;
		String validatedSupplierMaterial = null;

		if ((!consumerByProductMaterials.isEmpty() && consumerByProductMaterials != null) && (!supplierMaterials.isEmpty() && supplierMaterials != null)) {

			for (String consumerByProductMaterial : consumerByProductMaterials) {
				
				for (String supplierMaterial : supplierMaterials) {
					validatedConsumerMaterial = QueryValidation.validateMaterialName(consumerByProductMaterial, onto, allOntologyClasses);
					validatedSupplierMaterial = QueryValidation.validateMaterialName(supplierMaterial, onto, allOntologyClasses);
					parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, validatedConsumerMaterial, validatedSupplierMaterial, onto, graph);
					materialSimilarityList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));
				}

			}

			materialSimilarity = MathUtils.getMaxFromList(materialSimilarityList);

		} else  {
			//assuming that consumerByProductMaterials (combining by-product name and material attributes) will never be empty or null
			materialSimilarity = 0;
		}

		return materialSimilarity;

	}

}