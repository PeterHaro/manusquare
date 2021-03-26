package similarity.measures;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Certification;
import graph.Graph;
import query.IMQuery;
import similarity.SemanticSimilarity;
import similarity.SimilarityMethods;
import similarity.methodologies.ISimilarity;
import similarity.methodologies.SimilarityFactory;
import supplier.IMSupplier;

public class IMSimilarityMeasures {

	final static double CUT_THRESHOLD = 0.75;

	public static List<Double> computeSemanticSimilarity_IM (IMQuery query, IMSupplier innovationManager, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight, double cut_threshold) throws IOException {

		List<String> supplierInnovationPhases = innovationManager.getInnovationPhases();
		List<String> supplierInnovationTypes = innovationManager.getInnovationTypes();
		List<String> supplierSkills = innovationManager.getSkills();
		List<String> supplierSectors = innovationManager.getSectors();

		Graph.addInnovationPhasesToGraph(graph, supplierInnovationPhases);
		Graph.addInnovationTypesToGraph(graph, supplierInnovationTypes);
		Graph.addSkillsToGraph(graph, supplierSkills);
		Graph.addSectorsToGraph(graph, supplierSectors);

		List<Certification> supplierCertificationsList = innovationManager.getCertifications();
		String supplierName = innovationManager.getSupplierName();
		String supplierID = innovationManager.getSupplierId();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		double innovationPhaseSim = 0;
		double innovationTypeSim = 0;
		double skillSim = 0;
		double sectorSim = 0;
		double certificationSim = 0;
		double finalSim = 0;


		List<Double> similarityList = new LinkedList<Double>();

		StringBuffer debuggingOutput = new StringBuffer();

		/* INNOVATION PHASE SIMILARITY */
		List<String> initialConsumerInnovationPhases = query.getInnovationPhases();

		if (initialConsumerInnovationPhases == null || initialConsumerInnovationPhases.isEmpty()) {

			innovationPhaseSim = 0;

		} else {

			innovationPhaseSim = SemanticSimilarity.computeResourceSimilarity(initialConsumerInnovationPhases, supplierInnovationPhases, onto, similarityMethodology, similarityMethod, graph);
		}

		debuggingOutput.append("\nThe supplier name is: " + supplierName);
		debuggingOutput.append("\nThe supplier ID is: " + supplierID);
		debuggingOutput.append("\nThe consumer´s requested innovation phases are: " + initialConsumerInnovationPhases);
		debuggingOutput.append("\nThe supplier´s innovation phases are: " + supplierInnovationPhases);
		debuggingOutput.append("\nInnovationPhaseSim is: " + innovationPhaseSim);


		/* INNOVATION TYPE SIMILARITY */
		List<String> initialConsumerInnovationTypes = query.getInnovationTypes();

		if (initialConsumerInnovationTypes == null || initialConsumerInnovationTypes.isEmpty()) {
			innovationTypeSim = 0;
		} else {
			innovationTypeSim = SemanticSimilarity.computeResourceSimilarity(initialConsumerInnovationTypes, supplierInnovationTypes, onto, similarityMethodology, similarityMethod, graph);
		}

		debuggingOutput.append("\nThe consumer´s requested innovation types are: " + initialConsumerInnovationTypes);
		debuggingOutput.append("\nThe supplier´s innovation types are: " + supplierInnovationTypes);
		debuggingOutput.append("\nInnovationTypeSim is: " + innovationTypeSim);


		/* SKILL SIMILARITY */
		List<String> initialConsumerSkills = query.getSkills();

		if (initialConsumerSkills == null || initialConsumerSkills.isEmpty()) {
			skillSim = 0;
		} else {
			skillSim = SemanticSimilarity.computeResourceSimilarity(initialConsumerSkills, supplierSkills, onto, similarityMethodology, similarityMethod, graph);
		}

		debuggingOutput.append("\nThe consumer´s requested innovation skills are: " + initialConsumerSkills);
		debuggingOutput.append("\nThe supplier´s innovation skills are: " + supplierSkills);
		debuggingOutput.append("\nSkillSim is: " + skillSim);


		/* SECTOR SIMILARITY */
		List<String> initialConsumerSectors = query.getSectors();

		if (initialConsumerSectors == null || initialConsumerSectors.isEmpty()) {
			sectorSim = 0;
		} else {
			sectorSim = SemanticSimilarity.computeResourceSimilarity(initialConsumerSectors, supplierSectors, onto, similarityMethodology, similarityMethod, graph);
		}

		debuggingOutput.append("\nThe consumer´s requested innovation sectors are: " + initialConsumerSectors);
		debuggingOutput.append("\nThe supplier´s innovation sectors are: " + supplierSectors);
		debuggingOutput.append("\nSectorSim is: " + sectorSim);


		/* CERTIFICATION SIMILARITY */

		Set<Certification> initialConsumerCertifications = query.getCertifications();

		certificationSim = SemanticSimilarity.computeCertificationSimilarity(initialConsumerCertifications, supplierCertificationsList, similarityMethod, onto, graph, hard_coded_weight);

		debuggingOutput.append("\ncertificationSim is: " + certificationSim);


		//FIXME: Find a better solution to ensure that suppliers having innovationPhaseSim / innovationTypeSim = 1.0 are included in the returned list of suppliers

//		if (innovationPhaseSim == 1.0 || innovationTypeSim == 1.0) {
//			finalSim = (((innovationPhaseSim + innovationTypeSim) / 2) * 0.8) + (((sectorSim + skillSim) / 2) * 0.15) + (certificationSim * 0.05);
//		} else {
//			finalSim = (((innovationPhaseSim + innovationTypeSim + sectorSim) / 3) * 0.3) + (skillSim * 0.5) + (certificationSim * 0.2);
//		}
		
		if (innovationPhaseSim == 1.0 || innovationTypeSim == 1.0) {
			finalSim = (((innovationPhaseSim + innovationTypeSim) / 2) * 0.7) + (((sectorSim + skillSim) / 2) * 0.25) + (certificationSim * 0.05);
		} else {
			finalSim = (((innovationPhaseSim + innovationTypeSim + sectorSim + skillSim ) / 4) * 0.9) + (certificationSim * 0.1);
		}

		//TODO: CHECK!
		if (finalSim >= cut_threshold) {

			similarityList.add(finalSim);

		}

		debuggingOutput.append("\nSimilarityList contains: " + similarityList);

		System.out.println(debuggingOutput.toString());
		return similarityList;
	}		


}
