package similarity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Certification;

public class CertificationSimilarity {

	public static double computeCertificationSimilarity(Set<Certification> initialConsumerCertifications, List<Certification> supplierCertificationsList, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {

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
			
			certificationSimilarity = SemanticSimilarity.computeSemanticSetSimilarity(consumerCertifications, supplierCertifications, similarityMethod, onto, graph, hard_coded_weight);

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

}
