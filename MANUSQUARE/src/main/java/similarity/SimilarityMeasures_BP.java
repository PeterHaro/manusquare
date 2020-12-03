package similarity;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.graph.MutableGraph;

import NEW.ByProductQuantityComparison;
import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import json.ByProductSharingRequest.ByProductAttributes;
import owlprocessing.OntologyOperations;
import query.ByProductQuery;
import similarity.SimilarityMethodologies.ISimilarity;
import similarity.SimilarityMethodologies.SimilarityFactory;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParameters;
import similarity.SimilarityMethodologies.SimilarityParameters.SimilarityParametersFactory;
import supplierdata.Supplier_BP;
import utilities.MathUtils;
import utilities.StringUtilities;
import validation.QueryValidation;

public class SimilarityMeasures_BP {

	/* ALT 1 */
	public static List<Double> computeSemanticSimilarity (ByProductQuery query, Supplier_BP supplier, OWLOntology onto, SimilarityMethods similarityMethod, boolean weighted, MutableGraph<String> graph, boolean testing, double hard_coded_weight) throws IOException {


		List<ByProduct> supplierByProducts = supplier.getByProducts();
		
		System.err.println("\nSupplier " + supplier.getId() + " has " + supplierByProducts.size() + " by-products");

		List<Certification> certificationList = supplier.getCertifications();

		ISimilarity similarityMethodology = SimilarityFactory.GenerateSimilarityMethod(similarityMethod);

		//for each process in the query, compute the process facet similarity
		String consumerQueryByProductNode = null;
		String supplierResourceByProductNode = null;

		SimilarityParameters parameters = null;

		List<Double> similarityList = new LinkedList<Double>();

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

		for (ByProduct bpc : query.getByProducts()) {

			consumerQuantity = bpc.getQuantity();
			consumerUOM = bpc.getUom();
			consumerSupplyType = bpc.getSupplyType();

			List<Double> byProductSimList = new LinkedList<Double>();
			
			double materialSim = 0;
			double attributeSim = 0;
			double certificationSim = 0;
			double finalByProductSim = 0;

			for (ByProduct bps : supplierByProducts) {
				
				
				System.err.println("AttributeWeightMap for wsProfile: " + bps.getId() + ":" + bps.getAttributeWeightMap());
				

				debuggingOutput.append("\n------------------ Test: Matching Consumer By-product: " + bpc.getName() + " + and Supplier By-product: " + bps.getName() + " ( " + supplier.getId() + " ) ------------------");

				/* FIRST CHECK IF QUANTITIES OFFERED ARE ACCEPTABLE */

				//considering quantities and uoms		
				supplierQuantity = bps.getQuantity();
				supplierMinQuantity = bps.getMinQuantity();
				supplierUOM = bps.getUom();
				supplierSupplyType = bps.getSupplyType();

				debuggingOutput.append("\nConsumer quantity is: " + consumerQuantity);
				debuggingOutput.append("\nConsumer UOM is: " + consumerUOM);
				debuggingOutput.append("\nSupplier quantity is: " + supplierQuantity);
				debuggingOutput.append("\nSupplier min quantity is: " + supplierMinQuantity);
				debuggingOutput.append("\nSupplier UOM is: " + supplierUOM);
				debuggingOutput.append("\nConsumer SupplyType is: " + consumerSupplyType);
				debuggingOutput.append("\nSupplier SupplyType is: " + supplierSupplyType);

				boolean supplyTypeReqSatisfied = ByProductQuantityComparison.supplyTypeReqSatisfied(consumerSupplyType, consumerQuantity, consumerUOM, supplierSupplyType, supplierQuantity, supplierMinQuantity, supplierUOM);

				debuggingOutput.append("\nSupply type requirement for by-product id " + bps.getId() + " offered by supplier " + supplier.getId() + " is: " + supplyTypeReqSatisfied);

				if (supplyTypeReqSatisfied) {

					/* BY-PRODUCT SIMILARITY BASED ON MATERIAL ATTRIBUTE */	

					//represent by-product names as graph nodes
					consumerQueryByProductNode = bpc.getName();
					debuggingOutput.append("\nconsumerQueryByProductNode is " + consumerQueryByProductNode);

					//validate material associated with by-product
					System.err.println("SimilarityMeasures_BP: Material is " + bps.getMaterial());
					String validatedMaterial = QueryValidation.validateMaterialName(bps.getMaterial(), onto, allOntologyClasses);

					parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, consumerQueryByProductNode, validatedMaterial, onto, graph);
					materialSim = similarityMethodology.ComputeSimilaritySimpleGraph(parameters);

					supplierResourceByProductNode = validatedMaterial;
					debuggingOutput.append("\nMaterial similarity for supplier by-product " + validatedMaterial + " ( " + supplier.getId() + " ) is: " + materialSim);


					/* ATTRIBUTE SIMILARITY */		

					Set<ByProductAttributes> consumerAttributes = bpc.getAttributes();
					debuggingOutput.append("\n Number of consumer attributes: " + consumerAttributes.size());
					debuggingOutput.append("\n Attribute values with supplier process: " + bps.getAttributeWeightMap().entrySet());
					double avgAttributeSim = 0;

					//if there are any consumer attributes, we use these to influence the processAndMaterialSim
					//FIXME: If there are no consumer attributes this should be null!
					if (consumerAttributes != null) {
						debuggingOutput.append("\nAttributes with consumer process " + bpc.getName() + ": ");

						for (ByProductAttributes a : consumerAttributes) {
							debuggingOutput.append(a.getAttributeKey() + " ");
						}

						int counter = 0; 
						double sum = 0;

						//check which value ("Y", "N" or "O") the corresponding supplier process has
						for (ByProductAttributes a_c : consumerAttributes) {

							if (bps.getAttributeWeightMap().containsKey(a_c.getAttributeKey())) {

								if (bps.getAttributeWeightMap().get(a_c.getAttributeKey()).equals("Y")) {
									attributeSim = 1.0;
								} else if (bps.getAttributeWeightMap().get(a_c.getAttributeKey()).equals("O")) {
									attributeSim = hard_coded_weight;
								} else if (bps.getAttributeWeightMap().get(a_c.getAttributeKey()).equals("N")) {
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

						debuggingOutput.append("\nAverage attributeSim is " + avgAttributeSim);

						//if there are no consumer attributes, return an avgAttributeSim of 1.0
					} else {

						avgAttributeSim = 1.0;

						debuggingOutput.append("\nThere are no consumer attributes");
						debuggingOutput.append("\nAverage attributeSim is " + avgAttributeSim);
					}


					finalByProductSim = (materialSim * 0.7) + (avgAttributeSim * 0.3);


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

					certificationSim = computeIndependentWUPSetSim (consumerCertifications, supplierCertifications, similarityMethod, onto, graph, hard_coded_weight);
					debuggingOutput.append("\nRequired certificates by consumer: " + StringUtilities.printSetItems(consumerCertifications));
					debuggingOutput.append("\nCertifications possessed by supplier: " + StringUtilities.printSetItems(supplierCertifications));
					debuggingOutput.append("\ncertificationSim is: " + certificationSim);

					finalByProductSim = (finalByProductSim * 0.7) + (certificationSim * 0.3);

					debuggingOutput.append("\nfinalByProductSim for supplier by-product " + bps.getName() + " is: " + finalByProductSim);

					byProductSimList.add(finalByProductSim);
					System.out.println("Adding " + finalByProductSim + " to similarityList for consumer by-product " + bpc.getName());

					//if supply type requirements nor quantity requirements are satisfied for this by-product add zero for this by-product
				} else {
					finalByProductSim = 0;
					System.out.println("Adding " + finalByProductSim + " to similarityList for consumer by-product " + bpc.getName());
					byProductSimList.add(finalByProductSim);
				}


				System.out.println(debuggingOutput.toString());


			}		

			
			similarityList.add(getHighestScore(byProductSimList));


		}


		//return the list of scores for all supplier by-products
		System.out.println("similarityList: " + similarityList.toString());
		return similarityList;

	}

	/**
	 * Sorts the scores for each resource offered by a supplier (from highest to lowest)
	 *
	 * @param inputScores a list of scores for each supplier resource assigned by the semantic matching
	 * @return the n highest scores from a list of input scores
	 * Oct 12, 2019
	 */
	private static double getHighestScore(List<Double> inputScores) {

		inputScores.sort(Collections.reverseOrder());
		return inputScores.get(0);

	}



	public static double computeIndependentWUPSetSim (Set<String> consumerSet, Set<String> supplierSet, SimilarityMethods similarityMethod, OWLOntology onto, MutableGraph<String> graph, double hard_coded_weight) {
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


							s = getMostSimilarConceptSyntactically(s, classes);

							parameters = SimilarityParametersFactory.CreateSimpleGraphParameters(similarityMethod, c, s, onto, graph);			
							simList.add(similarityMethodology.ComputeSimilaritySimpleGraph(parameters));

						}

					}
				}

				return MathUtils.sum(simList) / (double)simList.size();

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

	/* FIXME: THESE SHOULD NOT BE HERE, ONLY FOR RESOLVING THE ISSUE WITH SUPSI/HOLONIX TYPING INSTANCES USING CONCEPTS NOT WITHIN THE ONTOLOGY /GRAPH */

	/**
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 *
	 * @param input                   the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @return the best matching concept from the MANUSQUARE ontology
	 * Nov 13, 2019
	 */
	private static String getMostSimilarConceptSyntactically(String input, Set<String> ontologyClassesAsString) {

		Map<String, Double> similarityMap = new HashMap<String, Double>();
		String mostSimilarConcept = null;

		for (String s : ontologyClassesAsString) {

			similarityMap.put(s, new JaroWinklerSimilarity().apply(input, s));
		}

		mostSimilarConcept = getConceptWithHighestSim(similarityMap);


		return mostSimilarConcept;

	}

	/**
	 * Returns the concept (name) with the highest (similarity) score from a map of concepts
	 *
	 * @param similarityMap a map of concepts along with their similarity scores
	 * @return single concept (name) with highest similarity score
	 * Nov 13, 2019
	 */
	private static String getConceptWithHighestSim(Map<String, Double> similarityMap) {
		Map<String, Double> rankedResults = sortDescending(similarityMap);
		Entry<String, Double> entry = rankedResults.entrySet().iterator().next();
		String conceptWithHighestSim = entry.getKey();
		return conceptWithHighestSim;
	}

	/**
	 * Sorts a map based on similarity scores (values in the map)
	 *
	 * @param map the input map to be sorted
	 * @return map with sorted values
	 * May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				if (compare == 0) return 1;
				else return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);

		sortedByValues.putAll(map);

		return sortedByValues;
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

		//FIXME: Temporary hack to ensure that case is ignored when comparing sets, should standardised lowercase/uppercase everywhere!
		else {
			if (StringUtilities.containsAllIgnoreCase(consumerSet, supplierSet)) {
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