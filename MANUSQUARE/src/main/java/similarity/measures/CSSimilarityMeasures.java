package similarity.measures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import edm.Attribute;
import edm.Certification;
import edm.Process;
import graph.Graph;
import ontology.OntologyOperations;
import query.CSQuery;
import similarity.MaterialSimilarity;
import similarity.SemanticSimilarity;
import similarity.SimilarityMethods;
import similarity.methodologies.ISimilarity;
import similarity.methodologies.SimilarityFactory;
import similarity.methodologies.parameters.SimilarityParameters;
import similarity.methodologies.parameters.SimilarityParametersFactory;
import supplier.CSSupplier;
import utilities.MathUtilities;

public class CSSimilarityMeasures {

	public static List<Double> computeSemanticSimilarity (CSQuery query, CSSupplier supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) throws IOException {

		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		List<Process> processList = supplier.getProcesses();
		List<Certification> certificationList = supplier.getCertifications();
		
		//TODO:  Check if this is a good approach for updating the graph with supplier-defined concepts
		List<String> supplierDefinedProcesses = new ArrayList<String>();
		List<String> supplierDefinedMaterials = new ArrayList<String>();
		
		for (Process p : processList) {
			supplierDefinedProcesses.add(p.getName());
			supplierDefinedMaterials.addAll(p.getMaterials());
		}
		
		Graph.addProcessesToGraph(graph, supplierDefinedProcesses);
		Graph.addMaterialsToGraph(graph, supplierDefinedMaterials);

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		//for each process in the query, compute the process facet similarity
		String consumerQueryProcessNode = null;
		String supplierResourceProcessNode = null;

		SimilarityParameters parameters = null;

		double processSim = 0;
		double materialSim = 0;
		double attributeSim = 0;
		double certificationSim = 0;
		double finalProcessSim = 0;

		List<Double> similarityList = new LinkedList<Double>();

		for (Process pc : query.getProcesses()) {
			Set<String> consumerMaterials = new HashSet<String>();

			//check if there are any materials specified in the consumer query
			if (pc.getMaterials() != null) {

				for (String m : pc.getMaterials()) {
					consumerMaterials.add(m);
				}
			}


			List<Double> processSimList = new LinkedList<Double>();

			for (Process ps : processList) {


				/* PROCESS SIMILARITY */	

				//represent processes as graph nodes
				consumerQueryProcessNode = pc.getName();
				supplierResourceProcessNode = ps.getName();
				
				System.out.println("\nconsumerQueryProcessNode: " + consumerQueryProcessNode);
				System.out.println("supplierResourceProcessNode: " + supplierResourceProcessNode);

				Set<String> equivalentProcesses = pc.getEquivalentProcesses();

				//if supplier process ps is a part of the equivalent process concepts of consumer process pc, the processSim is 1.0
				if (equivalentProcesses != null && equivalentProcesses.contains(ps.getName())) {
					processSim = 1.0;
				} else {
					parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerQueryProcessNode, supplierResourceProcessNode, onto, graph);
					processSim = similarityMethodology.ComputeSimilaritySimpleGraph(parameters);
				}

				System.out.println("processSim for supplier " + supplier.getSupplierId() + ": " + processSim);
				
				/* MATERIAL SIMILARITY */	

				Set<String> supplierMaterials = ps.getMaterials();

				
				System.out.println("consumerMaterials: " + consumerMaterials);
				System.out.println("supplierMaterials: " + supplierMaterials);

				materialSim = MaterialSimilarity.computeMaterialSimilarity(consumerMaterials, supplierMaterials, onto, similarityMethodology, similarityMethod, graph, allOntologyClasses);
				
				System.out.println("materialSim for supplier " + supplier.getSupplierId() + ": " + materialSim);

				/* ATTRIBUTE SIMILARITY */		

				Set<Attribute> consumerAttributes = pc.getAttributes();
				double avgAttributeSim = 0;
				
				System.out.println("CSSimilarityMeasures: AttributeWeightMap: " + ps.getAttributeWeightMap());

				//if there are any consumer attributes, we use these to influence the processAndMaterialSim
				if (consumerAttributes != null && !consumerAttributes.isEmpty()) {

					int counter = 0; 
					double sum = 0;

					//check which value ("Y", "N" or "O") the corresponding supplier process has
					for (Attribute a_c : consumerAttributes) {

						if (ps.getAttributeWeightMap().containsKey(a_c.getKey())) {

							if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("Y")) {
								attributeSim = 1.0;
							} else if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("O")) {
								attributeSim = hard_coded_weight;
							} else if (ps.getAttributeWeightMap().get(a_c.getKey()).equals("N")) {
								attributeSim = hard_coded_weight;
							}

							sum += attributeSim;
							counter++;

						} else {
							attributeSim = hard_coded_weight;
							sum += attributeSim;
							counter++;

						}

					}

					avgAttributeSim = sum / (double) counter;


					//if there are no consumer attributes, return an avgAttributeSim of 1.0
				} else {

					avgAttributeSim = 1.0;

				}

				System.out.println("avgAttributeSim for supplier " + supplier.getSupplierId() + ": " + avgAttributeSim);

				finalProcessSim = (processSim * 0.7) + (materialSim * 0.15) + (avgAttributeSim * 0.15);

				System.out.println("finalProcessSim for supplier " + supplier.getSupplierId() + ": " + finalProcessSim);

				/* CERTIFICATION SIMILARITY */

				Set<Certification> initialConsumerCertifications = query.getCertifications();
				Set<String> consumerCertifications = new HashSet<String>();

				if (initialConsumerCertifications != null) {
					for (Certification c : initialConsumerCertifications) {
						consumerCertifications.add(c.getId());				
					}
				}

				Set<String> supplierCertifications = new HashSet<String>();
				for (Certification c : certificationList) {
					supplierCertifications.add(c.getId());
				}

				certificationSim = SemanticSimilarity.computeSemanticSetSimilarity (consumerCertifications, supplierCertifications, similarityMethod, onto, graph, hard_coded_weight);

				System.out.println("certificationSim for supplier " + supplier.getSupplierId() + ": " + certificationSim);

				double finalSim = (finalProcessSim * 0.7) + (certificationSim * 0.3);

				System.out.println("finalSim for supplier " + supplier.getSupplierId() + ": " + finalSim);

				processSimList.add(finalSim);

			}		

			similarityList.add(MathUtilities.getHighest(processSimList));

			System.out.println("FINAL AND BEST SIMILARITY for supplier " + supplier.getSupplierId() + ": " + MathUtilities.getHighest(processSimList));

		}	

		return similarityList;

	}


}