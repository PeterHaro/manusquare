package similarity.measures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import NEW.ByProductQuantityComparison;
import NEW.PurchaseGroupAbilitation;
import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import graph.Graph;
import ontology.OntologyOperations;
import query.BPQuery;
import similarity.AppearanceSimilarity;
import similarity.AttributeSimilarity;
import similarity.CertificationSimilarity;
import similarity.MaterialSimilarity;
import similarity.SimilarityMethods;
import similarity.methodologies.ISimilarity;
import similarity.methodologies.SimilarityFactory;
import similarity.methodologies.parameters.SimilarityParameters;
import supplier.BPSupplier;

public class BPSimilarityMeasures {


	public static Map<String, Map<String, Double>> computeSemanticSimilarity (BPQuery query, BPSupplier supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) throws IOException {
		//for validation purposes
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		//for quantities and uoms
		double consumerQuantity = 0;
		String consumerUOM = null;
		double supplierQuantity = 0;
		double supplierMinQuantity = 0;
		String supplierUOM = null;
		String consumerSupplyType = null;
		String supplierSupplyType = null;
		String consumerByProductMaterial = null;
		
		//for purchasingGroupAbilitation
		String consumerPurchasingGroupAbilitation = null;
		int consumerMinNumberOfParticipants = 0;	
		int consumerMaxNumberOfParticipants = 0;	
		String supplierPurchasingGroupAbilitation = null;
		int supplierMinNumberOfParticipants = 0;
		int supplierMaxNumberOfParticipants = 0;
		Set<String> consumerByProductAppearances = null;
		Set<String> consumerMaterials = new HashSet<String>();
		
		List<ByProduct> supplierByProducts = supplier.getByProducts();
		List<Certification> supplierCertificationsList = supplier.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);
		SimilarityParameters parameters = null;

		List<Double> similarityList = new LinkedList<Double>();
		Map<String, Double> byProductScores = new HashMap<String, Double>();
		Map<String, Map<String, Double>> supplierByProductScoresMapping = new HashMap<String, Map<String, Double>>();

		for (ByProduct bpc : query.getByProducts()) {
			double materialSim = 0;
			double certificationSim = 0;
			double finalByProductSim = 0;
			double appearanceSim = 0;
			
			//FIXME: Find a better solution to handle situations where quantity is "" from the consumer query
			if (bpc.getQuantity().equals("") || bpc.getQuantity().equals(" ")) {
				
				consumerQuantity = 0;
				
			} else {
				
			consumerQuantity = Double.parseDouble(bpc.getQuantity());
			
			}
			consumerUOM = bpc.getUom();
			consumerSupplyType = bpc.getSupplyType();

			consumerPurchasingGroupAbilitation = bpc.getPurchasingGroupAbilitation();	
			consumerMinNumberOfParticipants = bpc.getMinParticipants();
			consumerMaxNumberOfParticipants = bpc.getMaxParticipants();
			
			consumerByProductAppearances = bpc.getAppearances();
			consumerMaterials = bpc.getMaterials();

			for (ByProduct bps : supplierByProducts) {
	
				
				supplierQuantity = Double.parseDouble(bps.getQuantity());
				
				System.out.println("\nSupplier quantity for " + bps.getId() + " is " + supplierQuantity);
				
				supplierMinQuantity = bps.getMinQuantity();
				
				System.out.println("Supplier minQuantity for " + bps.getId() + " is " + supplierMinQuantity);
	
				supplierUOM = bps.getUom();
				supplierSupplyType = bps.getSupplyType();
				
				supplierPurchasingGroupAbilitation = bps.getPurchasingGroupAbilitation();
				supplierMinNumberOfParticipants = bps.getMinParticipants();
				supplierMaxNumberOfParticipants = bps.getMaxParticipants();

				boolean supplyTypeReqSatisfied = ByProductQuantityComparison.supplyTypeReqSatisfied(consumerSupplyType, consumerQuantity, consumerUOM, supplierSupplyType, supplierQuantity, supplierMinQuantity, supplierUOM);
				boolean validPurchasingGroupAbility = PurchaseGroupAbilitation.validPurchaseGroupAbilitation(consumerPurchasingGroupAbilitation, consumerMinNumberOfParticipants, consumerMaxNumberOfParticipants, supplierPurchasingGroupAbilitation, supplierMinNumberOfParticipants, supplierMaxNumberOfParticipants);

				System.out.println("supplyTypeReqSatisfied is: " + supplyTypeReqSatisfied + " for wsProfileId: " + bps.getId());
				System.out.println("validPurchasingGroupAbility is: " + validPurchasingGroupAbility + " for wsProfileId: " + bps.getId());
				
				Set<String> supplierByProductAppearances = bps.getAppearances();
				
				if (supplyTypeReqSatisfied && validPurchasingGroupAbility) {


					/* BY-PRODUCT SIMILARITY BASED ON MATERIAL ATTRIBUTE */	
					
					//TODO: Should byProductName be considered in the matching? consumerByProductMaterial = bpc.getName();
					
					//TODO: Can be removed?if there are no consumer materials from attributes, create a new hashset to keep byProductName
//					if (consumerMaterials == null) {
//						consumerMaterials = new HashSet<String>();
//					}
					
					//TODO: Can be removed? consumerMaterials.add(consumerByProductMaterial);			
					
					Set<String> supplierMaterials = bps.getMaterials();
					
					//TODO:  Check if this is a good approach for updating the graph with supplier-defined concepts
					List<String> supplierMaterialsList = new ArrayList<>(supplierMaterials);
					Graph.addMaterialsToGraph(graph, supplierMaterialsList);
					
					System.out.println("consumerMaterials: " + consumerMaterials);
					System.out.println("supplierMaterials: " + supplierMaterials);
					
					materialSim = MaterialSimilarity.computeMaterialSimilarity(consumerMaterials, supplierMaterials, onto, similarityMethodology, similarityMethod, graph, allOntologyClasses);				
					System.out.println("materialSim: " + materialSim);
					
					/* APPEARANCE SIMILARITY */
					appearanceSim = AppearanceSimilarity.computeAppearanceSimilarity(consumerByProductAppearances, supplierByProductAppearances);					
					System.out.println("appearanceSim: " + appearanceSim);
					
					/* ATTRIBUTE SIMILARITY */		

					Set<Attribute> consumerAttributes = bpc.getAttributes();
					Map<String, String> attributeWeightMap = bps.getAttributeWeightMap();
					System.out.println("BPSimilarityMeasures: attributeWeightMap: " + attributeWeightMap);
					double avgAttributeSim = AttributeSimilarity.computeAttributeSimilarity(consumerAttributes, attributeWeightMap, hard_coded_weight);
					System.out.println("avgAttributeSim: " + avgAttributeSim);
					finalByProductSim = (materialSim * 0.6) + (appearanceSim * 0.2) + (avgAttributeSim * 0.2);
					System.out.println("finalByProductSim (before certificationSim): " + finalByProductSim);

					/* CERTIFICATION SIMILARITY */

					Set<Certification> initialConsumerCertifications = query.getCertifications();
					certificationSim = CertificationSimilarity.computeCertificationSimilarity(initialConsumerCertifications, supplierCertificationsList, similarityMethod, onto, graph, hard_coded_weight);
					finalByProductSim = (finalByProductSim * 0.7) + (certificationSim * 0.3);
					System.out.println("finalByProductSim (after certificationSim): " + finalByProductSim);
					

					//if supply type requirements nor quantity requirements are satisfied for this by-product add zero for this by-product
				} else {
					
					finalByProductSim = 0;
				}
				
				if (finalByProductSim != 0) {

				byProductScores.put(bps.getId(), finalByProductSim);
				
				}

			}		

		}
		
		System.out.println("Adding: " + supplier.getSupplierId() + ": " + byProductScores + " to supplierByProductScoresMapping");
		System.out.println("\n");
		supplierByProductScoresMapping.put(supplier.getSupplierId(), byProductScores);
		
		return supplierByProductScoresMapping;

	}


}