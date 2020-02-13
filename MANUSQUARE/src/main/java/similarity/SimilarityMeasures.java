package similarity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Attribute;
import edm.Certification;
import edm.Material;
import edm.Process;
import query.ConsumerQuery;
import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityFactory;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParametersFactory;
import supplierdata.Supplier;
import utilities.StringUtilities;

public class SimilarityMeasures {

	public static List<Double> computeSemanticSimilarity (ConsumerQuery query, Supplier supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) {		

		//get the list of processes and certifications for this supplier
		List<Process> processList = supplier.getProcesses();

		List<Certification> certificationList = supplier.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		//for each process in the query, compute the process facet similarity
		String consumerQueryProcessNode = null;
		String supplierResourceProcessNode = null;

		SimilarityParameters parameters = null;

		Set<String> consumerMaterials = new HashSet<String>();
		Set<String> supplierMaterials = new HashSet<String>();

		double processAndMaterialSim = 0;
		double processSim = 0;
		double materialSim = 0;
		double certificateSim = 0;
		double allCombinedSim = 0;

		List<Double> similarityList = new LinkedList<Double>();

		for (Process pc : query.getProcesses()) {

			for (Process ps : processList) {		
				
				StringBuffer debuggingOutput = new StringBuffer();
				
				debuggingOutput.append("\n------------------ Test: Matching Consumer Process: " + pc.getName() + " + and Supplier Process: " + ps.getName() + " ( " + supplier.getId() + " ) ------------------");

				//represent processes as graph nodes
				consumerQueryProcessNode = pc.getName();
				supplierResourceProcessNode = ps.getName();

				Set<String> equivalentProcesses = pc.getEquivalentProcesses();

				//if supplier process ps is a part of the equivalent process concepts of consumer process pc, the processSim is 1.0
				if (equivalentProcesses != null && equivalentProcesses.contains(ps.getName())) {
					debuggingOutput.append("\nEquivalent processes to " + pc.getName() + ": " + StringUtilities.printSetItems(equivalentProcesses));
					processSim = 1.0;
				} else {
					parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerQueryProcessNode, supplierResourceProcessNode, onto, graph);
					processSim = similarityMethodology.ComputeSimilaritySimpleGraph(parameters);
				}
				
				//System.out.println("Test: The processSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + processSim);
				debuggingOutput.append("\nprocessSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + processSim);

				//Check if there are materials specified in the query
				if (pc.getMaterials() == null || pc.getMaterials().isEmpty()) {
					//processAndMaterialSim = processSim;
					processAndMaterialSim = processSim * hard_coded_weight; //Audun: Reduce process similarity if there are no supplier materials specified.
					debuggingOutput.append("\nNo materials associated with process so processAndMaterialSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + processAndMaterialSim);
					
				} else {
					//materials related to consumer process
					for (Material m : pc.getMaterials()) {
						consumerMaterials.add(m.getName());
					}

					//materials related to supplier process
					Set<Material> materials = ps.getMaterials();
					for (Material material : materials) {
						supplierMaterials.add(material.getName());
					}


					//if the set of materials in the supplier process contains all materials requested by the consumer --> 1.0, otherwise compute Jaccard
					//TODO: Use Wu-Palmer instead of Jaccard?
					if (supplierMaterials.containsAll(consumerMaterials)) {
						materialSim = 1.0;
						debuggingOutput.append("\nAll supplier materials match consumer materials so materialSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + materialSim);
					} else { //if not, localMaterialSim is the Jaccard set similarity between the supplierMaterials and the consumerMaterials
						materialSim = Jaccard.jaccardSetSim(supplierMaterials, consumerMaterials);
						debuggingOutput.append("\nEither no matching or some matching materials between pc and ps so materialSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + materialSim);
					}

					//we should probably prioritise processes over materials
					if (weighted) {
						debuggingOutput.append("\nMultiplying processSim: " + processSim + " and materialSim: " + materialSim);
						processAndMaterialSim = (processSim * 0.75) + (materialSim * 0.25);
					} else {
						processAndMaterialSim = (processSim + materialSim) / 2;
					}

					debuggingOutput.append("\nFinal processAndMaterialSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + processAndMaterialSim);
				}

				//consider attributeWeight if a weighted process and if the consumer has defined any...							
				Set<Attribute> consumerAttributes = pc.getAttributes();
								
				//if there are any consumer attributes, we use these to influence the processAndMaterialSim
				if (consumerAttributes != null || consumerAttributes.isEmpty()) {
				debuggingOutput.append("\nAttributes with consumer process " + pc.getName() + ": ");
				
				for (Attribute a : consumerAttributes) {
					debuggingOutput.append(a.getKey());
				}

				double attributeWeight = 0;


				//check which value ("Y", "N" or "O") the corresponding supplier process has
				for (Attribute a_c : consumerAttributes) {

					if (ps.getAttributeWeightMap().containsKey(a_c.getKey())) {

						if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("Y")) {
							debuggingOutput.append("\nProcess " + ps.getName() + " has attribute " + ps.getAttributeWeightMap().get(a_c.getKey()) + " for attributeKey: " + a_c.getKey());
							attributeWeight = 1.0;
						} else if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("O")) {
							debuggingOutput.append("\nProcess " + ps.getName() + " has attribute " + ps.getAttributeWeightMap().get(a_c.getKey())+ " for attributeKey: " + a_c.getKey());
							attributeWeight = hard_coded_weight;
						} else if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("N")) {
							debuggingOutput.append("\nProcess " + ps.getName() + " has attribute " + ps.getAttributeWeightMap().get(a_c.getKey())+ " for attributeKey: " + a_c.getKey());
							attributeWeight = 0.9;
						}
					} else {//this means that the supplier attributes are different from the consumer attributes TODO: Shouldn´t the same weight as for "O" be imposed?
						attributeWeight = hard_coded_weight;
						debuggingOutput.append("\nThere are no equivalent attributeKeys for process " + ps.getName());
					}

				}

				debuggingOutput.append("\nThe initial processAndMaterialSim for process " + ps.getName() + " is " + processAndMaterialSim);

				//test
				double initialProcessAndMaterialSim = processAndMaterialSim;

				if (weighted) {
					processAndMaterialSim = processAndMaterialSim * attributeWeight;
				}
				
				debuggingOutput.append("\nThe processAndMaterialSim for process " + ps.getName() + " after attributeWeight is " + processAndMaterialSim + " (- " + (processAndMaterialSim / initialProcessAndMaterialSim) * 100 + " % )");
				} 
				
				//certificate facet similarity

				//if the consumer hasn´t specified any required certifications we only compute similarity based on processes (and materials)
				if (query.getCertifications() == null || query.getCertifications().isEmpty()) {

					allCombinedSim = processAndMaterialSim;

				} else { //if the consumer has specified required certifications we compute similarity based on processes (and materials) and certifications

					//if the supplier has no certifications
					if (certificationList == null || certificationList.isEmpty()) {

						//Audun: Reduce allCombined similarity if there are no supplier certifications specified and the consumer has certification requirements.
						allCombinedSim = processAndMaterialSim * hard_coded_weight;

					} else {

						//get required certifications 
						Set<String> requiredCertificates= new HashSet<String>();

						for (Certification c : query.getCertifications()) {
							requiredCertificates.add(c.getId());
						}
						
						debuggingOutput.append("\nRequired certificates by consumer: " + StringUtilities.printSetItems(requiredCertificates));
						
						//get supplier certifications
						Set<String> possessedCertificates = new HashSet<String>();
						for (Certification c : certificationList) {
							possessedCertificates.add(c.getId());
							
						}

						debuggingOutput.append("\nPossessed certificates by supplier: " + StringUtilities.printSetItems(possessedCertificates));
						
						if (possessedCertificates.containsAll(requiredCertificates)) {
							certificateSim = 1.0;
						} else {
							certificateSim = Jaccard.jaccardSetSim(requiredCertificates, possessedCertificates);
						} 

						debuggingOutput.append("\nThe certificateSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + certificateSim);

						if (weighted) {
							allCombinedSim = (processAndMaterialSim * 0.75)  + (certificateSim * 0.25);
						} else {
							allCombinedSim = (processAndMaterialSim + certificateSim) / 2;
						}
					}
				}

				debuggingOutput.append("\nThe allCombinedSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + allCombinedSim);
				similarityList.add(allCombinedSim);
				
				if (testing == true) {
				System.out.println(debuggingOutput.toString());
				}
			}			
		}	


		return similarityList;

	}

}
