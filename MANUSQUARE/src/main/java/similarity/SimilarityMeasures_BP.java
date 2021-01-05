package similarity;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import NEW.ByProductQuantityComparison;
import NEW.PurchaseGroupAbilitation;
import edm.ByProduct;
import edm.Certification;
import json.ByProductSharingRequest.ByProductAttributes;
import ontology.OntologyOperations;
import query.BPQuery;
import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityFactory;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import supplier.Supplier_BP;

public class SimilarityMeasures_BP {


	public static Map<String, Map<String, Double>> computeSemanticSimilarity (BPQuery query, Supplier_BP supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) throws IOException {

		List<ByProduct> supplierByProducts = supplier.getByProducts();

		List<Certification> supplierCertificationsList = supplier.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		String consumerByProductMaterial = null;

		SimilarityParameters parameters = null;

		List<Double> similarityList = new LinkedList<Double>();
		Map<String, Double> byProductScores = new HashMap<String, Double>();
		Map<String, Map<String, Double>> supplierByProductScoresMapping = new HashMap<String, Map<String, Double>>();

		StringBuffer debuggingOutput = new StringBuffer();

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
		
		//for purchasingGroupAbilitation
		String consumerPurchasingGroupAbilitation = null;
		int consumerMinNumberOfParticipants = 0;	
		int consumerMaxNumberOfParticipants = 0;	
		String supplierPurchasingGroupAbilitation = null;
		int supplierMinNumberOfParticipants = 0;
		int supplierMaxNumberOfParticipants = 0;
		Set<String> consumerByProductAppearances = null;
		Set<String> consumerMaterials = null;

		for (ByProduct bpc : query.getByProducts()) {

			//FIXME: Find a better solution to handle situations where quantity is "" from the consumer query
			if (bpc.getQuantity().equals("") || bpc.getQuantity().equals(" ")) {
				consumerQuantity = 0;
			} else {
			consumerQuantity = Double.parseDouble(bpc.getQuantity());
			
			}
			consumerUOM = bpc.getUom();
			consumerSupplyType = bpc.getSupplyType();

			double materialSim = 0;
			double certificationSim = 0;
			double finalByProductSim = 0;
			double appearanceSim = 0;
			
			consumerPurchasingGroupAbilitation = bpc.getPurchasingGroupAbilitation();	
			consumerMinNumberOfParticipants = bpc.getMinParticipants();
			consumerMaxNumberOfParticipants = bpc.getMaxParticipants();
			
			consumerByProductAppearances = bpc.getAppearances();
			consumerMaterials = bpc.getMaterials();

			for (ByProduct bps : supplierByProducts) {

				/* FIRST CHECK IF SUPPLY TYPE AND QUANTITIES OFFERED ARE ACCEPTABLE */
	
				//FIXME: Find a better solution to handle situations where quantity is "" from the consumer query
				if (bps.getQuantity().equals("") || bps.getQuantity().equals(" ")) {
					supplierQuantity = 0;
				} else {
				supplierQuantity = Double.parseDouble(bps.getQuantity());
				}
				supplierMinQuantity = bps.getMinQuantity();
				
				//FIXME: Find a better solution to handle situations where minQuantity is not specified by supplier
				if (supplierMinQuantity == 0) {
					supplierMinQuantity = supplierQuantity;
				} else {
					
				}
				
				
				supplierUOM = bps.getUom();
				supplierSupplyType = bps.getSupplyType();

				debuggingOutput.append("\nConsumer quantity is: " + consumerQuantity);
				debuggingOutput.append("\nConsumer UOM is: " + consumerUOM);
				debuggingOutput.append("\nSupplier quantity is: " + supplierQuantity);
				debuggingOutput.append("\nSupplier min quantity is: " + supplierMinQuantity);
				debuggingOutput.append("\nSupplier UOM is: " + supplierUOM);
				debuggingOutput.append("\nConsumer SupplyType is: " + consumerSupplyType);
				debuggingOutput.append("\nSupplier SupplyType is: " + supplierSupplyType);
				
				supplierPurchasingGroupAbilitation = bps.getPurchasingGroupAbilitation();
				supplierMinNumberOfParticipants = bps.getMinParticipants();
				supplierMaxNumberOfParticipants = bps.getMaxParticipants();
				
				debuggingOutput.append("\nConsumer purchasingGroupAbilitation: " + consumerPurchasingGroupAbilitation);	
				debuggingOutput.append("\nconsumerMinNumberOfParticipants: " + consumerMinNumberOfParticipants);
				debuggingOutput.append("\nconsumerMaxNumberOfParticipants: " + consumerMaxNumberOfParticipants);
				
				debuggingOutput.append("\nSupplier purchasingGroupAbilitation: " + supplierPurchasingGroupAbilitation);
				debuggingOutput.append("\nSupplier minNumberOfParticipants: " + supplierMinNumberOfParticipants);
				debuggingOutput.append("\nSupplier maxNumberOfParticipants: " + supplierMaxNumberOfParticipants);

				boolean supplyTypeReqSatisfied = ByProductQuantityComparison.supplyTypeReqSatisfied(consumerSupplyType, consumerQuantity, consumerUOM, supplierSupplyType, supplierQuantity, supplierMinQuantity, supplierUOM);
				boolean validPurchasingGroupAbility = PurchaseGroupAbilitation.validPurchaseGroupAbilitation(consumerPurchasingGroupAbilitation, consumerMinNumberOfParticipants, consumerMaxNumberOfParticipants, supplierPurchasingGroupAbilitation, supplierMinNumberOfParticipants, supplierMaxNumberOfParticipants);
				debuggingOutput.append("\nSupply type requirement for by-product id " + bps.getId() + " offered by supplier " + supplier.getId() + " is: " + supplyTypeReqSatisfied);
				debuggingOutput.append("\nValid purchasing group abilitation for by-product id " + bps.getId() + " offered by supplier " + supplier.getId() + " is: " + validPurchasingGroupAbility);
				
				Set<String> supplierByProductAppearances = bps.getAppearances();
				debuggingOutput.append("\nConsumer Appearances: " + consumerByProductAppearances);
				debuggingOutput.append("\nSupplier Appearances: " + supplierByProductAppearances);
				
				if (supplyTypeReqSatisfied && validPurchasingGroupAbility) {


					/* BY-PRODUCT SIMILARITY BASED ON MATERIAL ATTRIBUTE */	
					
					//should also consider eventual material attributes from the consumer query
					consumerByProductMaterial = bpc.getName();
					consumerMaterials.add(consumerByProductMaterial);
					
					Set<String> supplierMaterials = bps.getMaterials();
					
					materialSim = MaterialSimilarity.computeMaterialSimilarity(consumerMaterials, supplierMaterials, onto, similarityMethodology, similarityMethod, graph, allOntologyClasses);				
					debuggingOutput.append("\nmaterialSim: for supplier material(s): " + supplierMaterials + " : " + materialSim);
					
					/* APPEARANCE SIMILARITY */
					appearanceSim = AppearanceSimilarity.computeAppearanceSimilarity(consumerByProductAppearances, supplierByProductAppearances);					
					debuggingOutput.append("\nappearanceSim: for supplier appearance attributes: " + supplierByProductAppearances + " : " + appearanceSim);
					
					
					/* ATTRIBUTE SIMILARITY */		

					Set<ByProductAttributes> consumerAttributes = bpc.getAttributes();
					debuggingOutput.append("\n Number of consumer attributes: " + consumerAttributes.size());

					Map<String, String> attributeWeightMap = bps.getAttributeWeightMap();

					double avgAttributeSim = AttributeSimilarity.computeAttributeSimilarity(consumerAttributes, attributeWeightMap, hard_coded_weight);
					debuggingOutput.append("\n avgAttributeSim is: " + avgAttributeSim);

					finalByProductSim = (materialSim * 0.6) + (appearanceSim * 0.2) + (avgAttributeSim * 0.2);


					/* CERTIFICATION SIMILARITY */

					Set<Certification> initialConsumerCertifications = query.getCertifications();

					certificationSim = CertificationSimilarity.computeCertificationSimilarity(initialConsumerCertifications, supplierCertificationsList, similarityMethod, onto, graph, hard_coded_weight);

					debuggingOutput.append("\ncertificationSim is: " + certificationSim);

					finalByProductSim = (finalByProductSim * 0.7) + (certificationSim * 0.3);

					debuggingOutput.append("\nfinalByProductSim for supplier by-product " + bps.getName() + " is: " + finalByProductSim);

					debuggingOutput.append("\nAdding " + finalByProductSim + " to similarityList for consumer by-product " + bpc.getName() + "\n");

					//if supply type requirements nor quantity requirements are satisfied for this by-product add zero for this by-product
				} else {
					
					finalByProductSim = 0;
					debuggingOutput.append("\nAdding " + finalByProductSim + " to similarityList for consumer by-product " + bpc.getName() + "\n");
				}
				
				if (finalByProductSim != 0) {

				byProductScores.put(bps.getId(), finalByProductSim);
				
				}

			}		

		}

		supplierByProductScoresMapping.put(supplier.getId(), byProductScores);

		System.out.println(debuggingOutput.toString());
		
		return supplierByProductScoresMapping;

	}


}