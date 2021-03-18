package similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Certification;
import ontology.OntologyOperations;
import similarity.methodologies.ISimilarity;
import similarity.methodologies.SimilarityFactory;
import similarity.methodologies.parameters.SimilarityParameters;
import similarity.methodologies.parameters.SimilarityParametersFactory;
import utilities.MathUtilities;
import utilities.StringUtilities;


public class SemanticSimilarity {
	
	public static double computeResourceSimilarity(Collection<String> consumerResources, Collection<String> supplierResources, OWLOntology onto, ISimilarity similarityMethodology, SimilarityMethods similarityMethod, MutableGraph<String> graph) throws IOException {

		double similarity = 0;
		List<Double> similarityList = new ArrayList<Double>();
				
		SimilarityParameters parameters = null;
		
		//if consumer hasn´t specified any resource we should return sim 1
		if (consumerResources.isEmpty() || consumerResources == null) {
			similarity = 1.0;
		}
		
		//if the consumer has specified any resource and the supplier hasn´t, we should return sim 0
		else if (supplierResources.isEmpty() || supplierResources == null) {
			similarity = 0.0;
		}

		//if both the consumer and the supplier has specified resources we do a pairwise comparison of all consumer resources and all supplier resources
		else {
			
			for (String consumerResource : consumerResources) {

				for (String supplierResource : supplierResources) {
					
					//if this particular consumer resource or this particular supplier resource is null or empty we add a sim of 0
					if (consumerResource == null || consumerResource.isEmpty() || supplierResource == null || supplierResource.isEmpty()) {
						similarityList.add(0.0);
					}
					
					//if this particular consumer resource is syntactical equal to supplier resource we add a sim of 1
					else if (consumerResource.equalsIgnoreCase(supplierResource)) {
						similarityList.add(1.0);
					}
					
					//if neither of the above applies, and both consumer resource and supplier resource reside in the graph, 
					//we compute the semantic similarity between this particular consumer resource and this particular supplier resource
					else {
						
						if (StringUtilities.containsIgnoreCase(graph.nodes(), consumerResource) && StringUtilities.containsIgnoreCase(graph.nodes(), supplierResource)) {
							
							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerResource, supplierResource, onto, graph);
							similarityList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));
						}
						
						//if for some reason the consumer resource and the supplier resource does not reside in the graph we add sim 0
						else {
							similarityList.add(0.0);
						}
					}

				}

			}

			//we return the highest sim for all resources
			similarity = MathUtilities.getMaxFromList(similarityList);
		}


		return similarity;

	}
	
	public static double computeCertificationSimilarity(Collection<Certification> initialConsumerCertifications, Collection<Certification> supplierCertificationsList, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {

		double certificationSimilarity = 0;

		Set<String> consumerCertifications = new HashSet<String>();
		if (initialConsumerCertifications != null) {
			for (Certification c : initialConsumerCertifications) {
				if (c.getId() != null) {
				consumerCertifications.add(c.getId());				
			}
			}
		}

		Set<String> supplierCertifications = new HashSet<String>();
		if (supplierCertificationsList != null) {
			for (Certification c : supplierCertificationsList) {
				if (c.getId() != null) {
				supplierCertifications.add(c.getId());
				}
			}
		}
		

		if (consumerCertifications != null && supplierCertifications != null ) {
			
			certificationSimilarity = computeSemanticSetSimilarity(consumerCertifications, supplierCertifications, similarityMethod, onto, graph, hard_coded_weight);

		} else if (supplierCertifications == null) {
			certificationSimilarity = hard_coded_weight;
		} else {
			certificationSimilarity = 1.0;			
		}


		return certificationSimilarity;
	}
	
	public static boolean containsCertifications (Set<Certification> initialConsumerCertifications) {
		
		int counter = 0;
		
		for (Certification cert : initialConsumerCertifications) {
			if (cert.getId() != null && !cert.getId().isEmpty()) {
				counter++;
			} 
		}
				
		if (counter > 0) {
			return true;
		}
		
		return false;
	}
	
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


							s = SyntacticSimilarity.getMostSimilarConceptSyntactically(s, classes);

							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
							simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

						}

					}
				}

				return MathUtilities.sum(simList) / (double)simList.size();

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


}
