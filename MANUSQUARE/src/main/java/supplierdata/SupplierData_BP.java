package supplierdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import edm.Material;
import edm.Process;
import query.ByProductQuery;
import sparqlconnection.SparqlConnection;
import sparqlquery.SparqlQuery_BP;
import sparqlresult.SparqlRecord;
import sparqlresult.SparqlRecord_BP;
import supplier.Supplier_BP;
import utilities.StringUtilities;

public class SupplierData_BP {
	
	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "BPS";


	/**
	 * Retrieves (relevant) data / concepts from the Semantic Infrastructure using
	 * the content of a consumer query as input.
	 * @param query content of a consumer query
	 * @return list of suppliers along with the innovation phases, types, skills and sectors registered in the Semantic Infrastructure.        
	 * @throws OWLOntologyCreationException
	 */
	public static List<Supplier_BP> createSupplierData(ByProductQuery query, boolean testing, OWLOntology onto, String SPARQL_ENDPOINT, String AUTHORISATION_TOKEN) {

		String strQuery = SparqlQuery_BP.createSparqlQuery(query, onto);
		Set<SparqlRecord_BP> recordSet = new HashSet<SparqlRecord_BP>();

		// use name of processes in query to retrieve subset of relevant supplier data
		// from semantic infrastructure
		List<String> byProductNames = new ArrayList<String>();

		if (query.getByProducts() == null || query.getByProducts().isEmpty()) {
			System.err.println("There are no by products specified!");
		} else {
			for (ByProduct byProduct : query.getByProducts()) {
				byProductNames.add(byProduct.getName());
			}
		}
		
		Repository repository;
		
		if (!testing) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", AUTHORISATION_TOKEN);
			headers.put("accept", "application/JSON");
			
			repository = new SPARQLRepository(SPARQL_ENDPOINT);
			repository.initialize();
			((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);
			
		} else {
			//connect to GraphDB
			repository = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
			HTTPRepository repo = new HTTPRepository(GRAPHDB_SERVER, REPOSITORY_ID);
			System.out.println(repo.getRepositoryURL());
			System.out.println(repo.getPreferredRDFFormat());
			repository.initialize();
			System.out.println(repository.isInitialized());
		}
		

		//connect to triplestore
		TupleQuery tupleQuery = SparqlConnection.connect(repository, testing, strQuery);


		try (TupleQueryResult result = tupleQuery.evaluate()) {

			Attribute supplierAttribute = new Attribute();
			SparqlRecord_BP record = null;			
			Map<String, String> attributeWeightMap = null;
						
			while (result.hasNext()) {
				
				BindingSet solution = result.next();
				
//				System.err.println("\nSupplierData_BP: Results from SI: ");
//				System.err.println("Supplier ID: " + solution.getValue("supplierId").stringValue().replaceAll("\\s+", ""));
//				System.err.println("By-product ID: " + StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue().replaceAll("\\s+", "")));
//				System.err.println("By-product Name: " + solution.getValue("byProductName").stringValue().replaceAll("\\s+", ""));
//				System.err.println("Supply type: " + solution.getValue("byProductSupplyType").stringValue().replaceAll("\\s+", ""));
//				System.err.println("Min participants: " + solution.getValue("byProductMinParticipants").stringValue().replaceAll("\\s+", ""));
//				System.err.println("Max participants: " + solution.getValue("byProductMaxParticipants").stringValue().replaceAll("\\s+", ""));
//				System.err.println("Purchasing group abilitation: " + solution.getValue("purchasingGroupAbilitation").stringValue().replaceAll("\\s+", ""));
//				System.err.println("Quantity: " + solution.getValue("byProductQuantity").stringValue().replaceAll("\\s+", ""));
//				System.err.println("Min quantity: " + solution.getValue("byProductMinQuantity").stringValue().replaceAll("\\s+", ""));
//				System.err.println("UOM: " + solution.getValue("byProductUOM").stringValue().replaceAll("\\s+", ""));
//				
//				if (solution.getValue("certificationType") != null) {
//				System.err.println("Certification type: " + StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));
//				}
//				
//				if (solution.getValue("materialType") != null) {
//				System.err.println("Material type: " + StringUtilities.stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", "")));
//				}
//				
//				if (solution.getValue("attributeType") != null && solution.getValue("attributeType").stringValue().endsWith("Appearance") && solution.getValue("attributeValue") != null) {
//				System.err.println("Apperance type: " + solution.getValue("attributeValue").stringValue().replaceAll("\\s+", ""));
//				}
//				
//				if (solution.getValue("attributeType") != null 
//						&& !solution.getValue("attributeType").stringValue().endsWith("AttributeMaterial") 
//						&& !solution.getValue("attributeType").stringValue().endsWith("Appearance")) {
//					
//					attributeWeightMap = Attribute.createAttributeWeightMap(solution, supplierAttribute, query);
//					
//				System.err.println("Attribute weight map: " + attributeWeightMap);
//				}
				
				record = new SparqlRecord_BP();
				record.setSupplierId(solution.getValue("supplierId").stringValue().replaceAll("\\s+", ""));
				record.setWsProfileId(StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue().replaceAll("\\s+", "")));			
				record.setByProductName(solution.getValue("byProductName").stringValue().replaceAll("\\s+", ""));
				record.setByProductSupplyType(solution.getValue("byProductSupplyType").stringValue().replaceAll("\\s+", ""));						
				record.setByProductMinParticipants(solution.getValue("byProductMinParticipants").stringValue().replaceAll("\\s+", ""));
				record.setByProductMaxParticipants(solution.getValue("byProductMaxParticipants").stringValue().replaceAll("\\s+", ""));
				record.setPurchasingGroupAbilitation(solution.getValue("purchasingGroupAbilitation").stringValue().replaceAll("\\s+", ""));
				

				
				record.setByProductQuantity(solution.getValue("byProductQuantity").stringValue().replaceAll("\\s+", ""));
				record.setByProductMinQuantity(solution.getValue("byProductMinQuantity").stringValue().replaceAll("\\s+", ""));
				record.setByProductUOM(solution.getValue("byProductUOM").stringValue().replaceAll("\\s+", ""));

				if (solution.getValue("certificationType") != null) {
				record.setCertification(StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));
				}

				
				if (solution.getValue("materialType") != null) {
					record.setMaterial(StringUtilities.stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", "")));
				} else {
					record.setMaterial(null);
				}
				
				if (solution.getValue("attributeType") != null && solution.getValue("attributeType").stringValue().endsWith("Appearance") && solution.getValue("attributeValue") != null) {
					
					record.setAppearance(solution.getValue("attributeValue").stringValue().replaceAll("\\s+", ""));
					
				}
				
				// deal with attributes ("Y", "N" or "O") according to attributes required in the consumer query
				if (solution.getValue("attributeType") != null 
						&& !solution.getValue("attributeType").stringValue().endsWith("AttributeMaterial") 
						&& !solution.getValue("attributeType").stringValue().endsWith("Appearance")) {
					
					
					attributeWeightMap = Attribute.createAttributeWeightMap(solution, supplierAttribute, query);
										
					record.setAttributeWeightMap(attributeWeightMap);

				} 
				
				
				recordSet.add(record);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		// close connection to KB repository
		repository.shutDown();

		// create list of suppliers according to the results from SPARQL
		List<Supplier_BP> suppliersList = consolidateSuppliers(recordSet);		

		return suppliersList;

	}
	
	/**
	 * Consolidates the retrieved SPARQL data per supplier
	 * @param recordSet set of sparql records retrieved from SI
	 * @return list of supplier objects containing SPARQL results
	   Dec 9, 2020
	 */
	public static List<Supplier_BP> consolidateSuppliers (Set<SparqlRecord_BP> recordSet) {

		List<Supplier_BP> supplierList = new ArrayList<Supplier_BP>();

		//get all supplier ids for filtering
		Set<String> supplierids = new HashSet<String>();
		for (SparqlRecord_BP sr : recordSet) {
			supplierids.add(sr.getSupplierId());
		}
		
		//consolidate by-products
		Map<String, List<ByProduct>> consolidatedByProducts = consolidateByProducts (recordSet);
		
		Supplier_BP supplier = null;

		for (String sup : supplierids) {

			List<Certification> certifications = new ArrayList<Certification>();

			for (SparqlRecord_BP sr : recordSet) {

				Certification cert = null;	

				if (sr.getSupplierId().equals(sup)) {

					cert = new Certification(sr.getCertification());
					
					if (!certifications.contains(cert) && cert.getId() != null) {
						certifications.add(cert);
					}

					supplier = new Supplier_BP(sr.getSupplierId(), consolidatedByProducts.get(sr.getSupplierId()), certifications);	

				}

				//add supplier to supplierList
				if (!supplierList.contains(supplier) && supplier != null) {
					supplierList.add(supplier);
				}
			}
		}		


		return supplierList;

	}

	/**
	 * Consolidates all parameters associated with by-products and maps these by-products to the correct supplier
	 * @param recordSet set of sparql records retrieved from SI
	 * @return a map where the supplier id is used as key and the value consist of a list of consolidated by-products
	   Dec 9, 2020
	 */
	private static Map<String, List<ByProduct>> consolidateByProducts (Set<SparqlRecord_BP> recordSet) {
		Map<String, ByProduct> byProductMap = new HashMap<String, ByProduct>();

		Set<String> uniqueByProducts = new HashSet<String>();
		for (SparqlRecord_BP sr : recordSet) {		
			uniqueByProducts.add(sr.getWsProfileId());
		}

		Set<String> uniqueSuppliers = new HashSet<String>();
		for (SparqlRecord_BP sr : recordSet) {
			uniqueSuppliers.add(sr.getSupplierId());
		}

		ByProduct byProduct = null;
		Map<String, String> attributeWeightMap = new HashMap<String, String>();
		
		for (String s : uniqueByProducts) {

			Set<String> materials = new HashSet<String>();
			Set<String> appearances = new HashSet<String>();
			
			for (SparqlRecord_BP sr : recordSet) {

				if (sr.getWsProfileId().equals(s)) {
					
					String byProductName = sr.getByProductName();
					String byProductSupplyType = sr.getByProductSupplyType();
					
					//FIXME: sometimes minParticipants is "", so converting this to 0 to avoid errors later (according to the spec from SUPSI it could then be ignored in the match).
					
					int byProductMinParticipants = 0;
					if (sr.getByProductMinParticipants().equals("") || sr.getByProductMinParticipants().equals(" ")) {
						byProductMinParticipants = 0;
					
					} else {
					
					byProductMinParticipants = Integer.parseInt(sr.getByProductMinParticipants());
					
					}
					int byProductMaxParticipants = 0;
					
					if (sr.getByProductMaxParticipants().equals("") || sr.getByProductMaxParticipants().equals(" ")) {
						byProductMaxParticipants = 0;
					
					} else {
					
					byProductMaxParticipants = Integer.parseInt(sr.getByProductMaxParticipants());
					
					}
					
					String purchasingGroupAbilitation = sr.getPurchasingGroupAbilitation();
														
					String quantity = sr.getByProductQuantity();
	
					double minQuantity = 0;
					
					//TODO: Check if minQuantity is mandatory
					if (sr.getByProductMinQuantity() != null && !sr.getByProductMinQuantity().isEmpty()) {
					
						minQuantity = Double.parseDouble(sr.getByProductMinQuantity());
					
					} else {
						
						minQuantity = 0;
						
					}
					
					String uom = sr.getByProductUOM();

					String material = sr.getMaterial();

					if (!materials.contains(material) && material != null) {
						materials.add(material);
					}
					
					String appearance = sr.getAppearance();
					
					if (!appearances.contains(appearance) && appearance != null) {
						appearances.add(appearance);
					}

					attributeWeightMap = sr.getAttributeWeightMap();
					
					byProduct = new ByProduct(sr.getWsProfileId(), byProductName, byProductSupplyType, byProductMinParticipants, byProductMaxParticipants, purchasingGroupAbilitation, quantity, minQuantity, uom, materials, appearances, attributeWeightMap);

					byProductMap.put(sr.getWsProfileId(), byProduct);

				}

			}
		}


		ArrayListMultimap<String, String> supplierToByProductMapping = ArrayListMultimap.create();

		for (String supplier : uniqueSuppliers) {

			for (SparqlRecord_BP sr : recordSet) {

				if (sr.getSupplierId().equals(supplier)) {
					supplierToByProductMapping.put(sr.getSupplierId(), sr.getWsProfileId());
				}

			}
		}


		Map<String, List<ByProduct>> finalSupplierToByProductMap = new HashMap<String, List<ByProduct>>();

		for (String sup : uniqueSuppliers) {
			Set<ByProduct> byProductList = new HashSet<ByProduct>();

			List<String> byProducts = supplierToByProductMapping.get(sup);

			for (String s : byProducts) {
				byProductList.add(byProductMap.get(s));
			}

			finalSupplierToByProductMap.put(sup, new ArrayList<>(byProductList));
		}

		return finalSupplierToByProductMap;

	}
	
}