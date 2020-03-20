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
import utilities.MathUtils;
import utilities.StringUtilities;

public class SimilarityMeasures {

	public static List<Double> computeSemanticSimilarity (ConsumerQuery query, Supplier supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) {		

		List<Process> processList = supplier.getProcesses();
		List<Certification> certificationList = supplier.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		//for each process in the query, compute the process facet similarity
		String consumerQueryProcessNode = null;
		String supplierResourceProcessNode = null;

		SimilarityParameters parameters = null;

		double processAndMaterialSim = 0;
		double processSim = 0;
		double materialSim = 0;
		double certificationSim = 0;
		double allCombinedSim = 0;

		List<Double> similarityList = new LinkedList<Double>();

		for (Process pc : query.getProcesses()) {

			Set<String> consumerMaterials = new HashSet<String>();
			for (Material m : pc.getMaterials()) {
				consumerMaterials.add(m.getName());
			}

			for (Process ps : processList) {		

				StringBuffer debuggingOutput = new StringBuffer();

				debuggingOutput.append("\n------------------ Test: Matching Consumer Process: " + pc.getName() + " + and Supplier Process: " + ps.getName() + " ( " + supplier.getId() + " ) ------------------");

				/* PROCESS SIMILARITY */	
				
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

				debuggingOutput.append("\nprocessSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + processSim);
				
				/* MATERIAL SIMILARITY */	

				Set<String> supplierMaterials = new HashSet<String>();
				for (Material material : ps.getMaterials()) {
					supplierMaterials.add(material.getName());
				}

				debuggingOutput.append("\nComputing materialSim between consumerMaterials:" + consumerMaterials + " and supplierMaterials: " + supplierMaterials);
				materialSim = computeWUPSetSim (consumerMaterials, supplierMaterials, processSim, similarityMethod, onto, graph, hard_coded_weight);
				debuggingOutput.append("\nMaterialSim is: " + materialSim);

				//we should probably prioritise processes over materials
				if (weighted) {
					debuggingOutput.append("\nCombining processSim: " + processSim + " and materialSim: " + materialSim);
					processAndMaterialSim = (processSim * 0.75) + (materialSim * 0.25);
				} else {
					processAndMaterialSim = (processSim + materialSim) / 2;
				}

				debuggingOutput.append("\nFinal processAndMaterialSim for supplier process " + ps.getName() + " ( " + supplier.getId() + " ) is: " + processAndMaterialSim);

				/* ATTRIBUTE SIMILARITY */		
				
				Set<Attribute> consumerAttributes = pc.getAttributes();

				//if there are any consumer attributes, we use these to influence the processAndMaterialSim
				if (consumerAttributes != null) {
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
						} else {
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

				/* CERTIFICATION SIMILARITY */
				
				Set<Certification> initialConsumerCertifications = query.getCertifications();
				Set<String> consumerCertifications = new HashSet<String>();
				for (Certification c : initialConsumerCertifications) {
					consumerCertifications.add(c.getId());				
				}
				
				Set<String> supplierCertifications = new HashSet<String>();
				for (Certification c : certificationList) {
					supplierCertifications.add(c.getId());
				}
				
				certificationSim = computeWUPSetSim (consumerCertifications, supplierCertifications, processAndMaterialSim, similarityMethod, onto, graph, hard_coded_weight);
				debuggingOutput.append("\nRequired certificates by consumer: " + StringUtilities.printSetItems(consumerCertifications));
				debuggingOutput.append("\nCertifications possessed by supplier: " + StringUtilities.printSetItems(supplierCertifications));
				debuggingOutput.append("\ncertificationSim is: " + certificationSim);
				
				if (weighted) {
					allCombinedSim = (processAndMaterialSim * 0.90)  + (certificationSim * 0.10);
				} else {
					allCombinedSim = (processAndMaterialSim + certificationSim) / 2;
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

	public static double computeWUPSetSim (Set<String> consumerSet, Set<String> supplierSet, double initialSim, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);
		SimilarityParameters parameters = null;		
		List<Double> simList = new LinkedList<Double>();

		if (consumerSet == null || consumerSet.isEmpty()) {
			return 1.0;
		}

		else if (supplierSet == null || supplierSet.isEmpty()) {
			return initialSim * hard_coded_weight;
		}

		else {
			if (supplierSet.containsAll(consumerSet)) {
				return 1.0;
			}

			else {

				for (String c : consumerSet) {
					for (String s : supplierSet) {

						parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
						simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

					}
				}

				return MathUtils.sum(simList) / (double)simList.size();

			}
		}

	}

}
