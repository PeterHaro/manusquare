package similarity.measures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import query.BPQuery;
import similarity.SemanticSimilarity;
import similarity.SimilarityMethods;
import similarity.SyntacticSimilarity;
import similarity.methodologies.ISimilarity;
import similarity.methodologies.SimilarityFactory;
import supplier.BPSupplier;

public class BPSimilarityMeasures {


	public static Map<String, Map<String, Double>> computeSemanticSimilarity (BPQuery query, BPSupplier supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight, double cut_threshold) throws IOException {

		//for quantities and uoms
		double consumerQuantity = 0;
		String consumerUOM = null;
		double supplierQuantity = 0;
		double supplierMinQuantity = 0;
		String supplierUOM = null;
		String consumerSupplyType = null;
		String supplierSupplyType = null;
		
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
					
					Set<String> supplierMaterials = bps.getMaterials();
					
					//TODO:  Check if this is a good approach for updating the graph with supplier-defined concepts
					List<String> supplierMaterialsList = new ArrayList<>(supplierMaterials);
					Graph.addMaterialsToGraph(graph, supplierMaterialsList);
					
					System.out.println("consumerMaterials: " + consumerMaterials);
					System.out.println("supplierMaterials: " + supplierMaterials);
					
					materialSim = SemanticSimilarity.computeResourceSimilarity(consumerMaterials, supplierMaterials, onto, similarityMethodology, similarityMethod, graph);				
					System.out.println("materialSim: " + materialSim);
					
					/* APPEARANCE SIMILARITY */
					appearanceSim = SyntacticSimilarity.computeAppearanceSimilarity(consumerByProductAppearances, supplierByProductAppearances);					
					System.out.println("appearanceSim for consumer appearancaes ( " + consumerByProductAppearances + " ) and supplier appearances (" +  supplierByProductAppearances + " : " + appearanceSim);
					
					/* ATTRIBUTE SIMILARITY */		

					Set<Attribute> consumerAttributes = bpc.getAttributes();
					
					if (SyntacticSimilarity.containsAttributes(consumerAttributes)) {
						
						Map<String, String> attributeWeightMap = bps.getAttributeWeightMap();
						if (attributeWeightMap != null) {
						System.out.println("BPSimilarityMeasures: bps.getAttributeWeightMap() contains " + attributeWeightMap.size() + " entry.");
						}
						System.out.println("BPSimilarityMeasures: attributeWeightMap for by-product " + bps.getId() + ": " + attributeWeightMap);
						double avgAttributeSim = SyntacticSimilarity.computeAttributeSimilarity(consumerAttributes, attributeWeightMap, hard_coded_weight);
						System.out.println("avgAttributeSim: " + avgAttributeSim);
						finalByProductSim = (materialSim * 0.7) + (appearanceSim * 0.1) + (avgAttributeSim * 0.2);
					
					} else {
						
						finalByProductSim = (materialSim * 0.9) + (appearanceSim * 0.1);
					}
					

					System.out.println("finalByProductSim (before certificationSim): " + finalByProductSim);

					/* CERTIFICATION SIMILARITY */

					Set<Certification> initialConsumerCertifications = query.getCertifications();
										
					if (SemanticSimilarity.containsCertifications(initialConsumerCertifications)) {
						
						certificationSim = SemanticSimilarity.computeCertificationSimilarity(initialConsumerCertifications, supplierCertificationsList, similarityMethod, onto, graph, hard_coded_weight);
						finalByProductSim = (finalByProductSim * 0.7) + (certificationSim * 0.3);
						System.out.println("finalByProductSim (after certificationSim): " + finalByProductSim);
						
					} 

					

					//if supply type requirements nor quantity requirements are satisfied for this by-product add zero for this by-product
				} else {
					
					finalByProductSim = 0;
				}
				
				if (finalByProductSim != 0 && finalByProductSim >= cut_threshold) {

				byProductScores.put(bps.getId(), finalByProductSim);
				
				}

			}		

		}
		
		System.out.println("Adding: " + supplier.getSupplierId() + " with name: " + supplier.getSupplierName() + ": " + byProductScores + " to supplierByProductScoresMapping");
		System.out.println("\n");
		supplierByProductScoresMapping.put(supplier.getSupplierId(), byProductScores);
		
		return supplierByProductScoresMapping;

	}


}