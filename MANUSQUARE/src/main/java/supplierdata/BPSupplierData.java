package supplierdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.ArrayListMultimap;

import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import ontology.OntologyOperations;
import query.BPQuery;
import sparqlconnection.SparqlConnection;
import sparqlquery.BPSparqlQuery;
import sparqlresult.BPSparqlResult;
import supplier.BPSupplier;
import utilities.StringUtilities;
import validation.QueryValidator;

public class BPSupplierData {

	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "BPS_050121_2";


	/**
	 * Retrieves (relevant) data / concepts from the Semantic Infrastructure using
	 * the content of a consumer query as input.
	 * @param query content of a consumer query
	 * @return list of suppliers along with the innovation phases, types, skills and sectors registered in the Semantic Infrastructure.        
	 * @throws IOException 
	 * @throws OWLOntologyCreationException
	 */
	public static List<BPSupplier> createSupplierData(BPQuery query, boolean testing, OWLOntology onto, String SPARQL_ENDPOINT, String AUTHORISATION_TOKEN) throws IOException {

		String strQuery = BPSparqlQuery.createSparqlQuery(query, onto);
		Set<BPSparqlResult> sparqlResults = new HashSet<BPSparqlResult>();

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
			BPSparqlResult sparqlResult = null;			
			Map<String, String> attributeWeightMap = null;


			while (result.hasNext()) {

				BindingSet solution = result.next();


				String certification = null;
				if (solution.getValue("certificationType") != null) {
					certification = StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", ""));
				}

				String material = null;
				if (solution.getValue("materialType") != null) {
					material = StringUtilities.stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", ""));
				}

				String appearance = null;
				if (solution.getValue("attributeType") != null && solution.getValue("attributeType").stringValue().endsWith("Appearance") && solution.getValue("attributeValue") != null) {
					appearance = solution.getValue("attributeValue").stringValue().replaceAll("\\s+", "");
				}

				// deal with attributes ("Y", "N" or "O") according to attributes required in the consumer query
				if (solution.getValue("attributeType") != null 
						&& !solution.getValue("attributeType").stringValue().endsWith("AttributeMaterial") 
						&& !solution.getValue("attributeType").stringValue().endsWith("Appearance")) {				
					
					attributeWeightMap = Attribute.createAttributeWeightMap(solution, supplierAttribute, query);	
					
				} 


				sparqlResult = new BPSparqlResult.Builder(StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue().replaceAll("\\s+", "")), 
						solution.getValue("byProductName").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductSupplyType").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductMinParticipants").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductMaxParticipants").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("purchasingGroupAbilitation").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductQuantity").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductMinQuantity").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductUOM").stringValue().replaceAll("\\s+", ""))
						.setCertification(certification)
						.setMaterial(material)
						.setAppearance(appearance)
						.setAttributeWeightMap(attributeWeightMap)
						.setSupplierId(solution.getValue("supplierId").stringValue().replaceAll("\\s+", ""))
						.setSupplierName(solution.getValue("supplierName").stringValue().replaceAll("\\s+", ""))
						.build();

				sparqlResults.add(sparqlResult);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		// close connection to KB repository
		repository.shutDown();

		// create list of suppliers according to the results from SPARQL
		List<BPSupplier> suppliersList = consolidateSuppliers(sparqlResults, onto);		

		return suppliersList;

	}

	/**
	 * Consolidates the retrieved SPARQL data per supplier
	 * @param sparqlResults set of sparql records retrieved from SI
	 * @return list of supplier objects containing SPARQL results
	   Dec 9, 2020
	 * @throws IOException 
	 */
	public static List<BPSupplier> consolidateSuppliers (Set<BPSparqlResult> sparqlResults, OWLOntology onto) throws IOException {

		List<BPSupplier> supplierList = new ArrayList<BPSupplier>();

		//for validation purposes
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		//get all supplier ids for filtering
		Set<String> supplierids = new HashSet<String>();
		for (BPSparqlResult sr : sparqlResults) {
			supplierids.add(sr.getSupplierId());
		}
		
		//consolidate by-products
		Map<String, List<ByProduct>> consolidatedByProducts = consolidateByProducts (sparqlResults, onto);

		BPSupplier supplier = null;

		for (String sup : supplierids) {

			List<Certification> certifications = new ArrayList<Certification>();
			
			for (BPSparqlResult sr : sparqlResults) {

				Certification cert = null;	

				if (sr.getSupplierId().equals(sup)) {
					
					cert = new Certification(sr.getCertification());
					
					if (!certifications.contains(cert) && cert.getId() != null) {
						certifications.add(cert);
					}					
					
					supplier = new BPSupplier.Builder(consolidatedByProducts.get(sr.getSupplierId()))
							.setSupplierId(sr.getSupplierId())
							.setSupplierName(sr.getSupplierName())
							.setCertifications(certifications)
							.build();


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
	 * @param sparqlResults set of sparql records retrieved from SI
	 * @return a map where the supplier id is used as key and the value consist of a list of consolidated by-products
	   Dec 9, 2020
	 * @throws IOException 
	 */
	private static Map<String, List<ByProduct>> consolidateByProducts (Set<BPSparqlResult> sparqlResults, OWLOntology onto) throws IOException {
		Map<String, ByProduct> byProductMap = new HashMap<String, ByProduct>();

		//for validation purposes
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		Set<String> uniqueByProducts = new HashSet<String>();
		for (BPSparqlResult sr : sparqlResults) {		
			uniqueByProducts.add(sr.getWsProfileId());
		}

		Set<String> uniqueSuppliers = new HashSet<String>();
		for (BPSparqlResult sr : sparqlResults) {
			uniqueSuppliers.add(sr.getSupplierId());
		}

		ByProduct byProduct = null;
		Map<String, String> attributeWeightMap = new HashMap<String, String>();

		for (String s : uniqueByProducts) {

			Set<String> materials = new HashSet<String>();
			Set<String> appearances = new HashSet<String>();

			for (BPSparqlResult sr : sparqlResults) {

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

					String validatedMaterial = null;
					if (material != null) {
						validatedMaterial = QueryValidator.validateMaterialName(material, onto, allOntologyClasses);
					}


					if (!materials.contains(validatedMaterial) && validatedMaterial != null) {
						materials.add(validatedMaterial);
					}

					String appearance = sr.getAppearance();

					if (!appearances.contains(appearance) && appearance != null) {
						appearances.add(appearance);
					}

					attributeWeightMap = sr.getAttributeWeightMap();

					byProduct = new ByProduct.Builder(byProductSupplyType, byProductMinParticipants, byProductMaxParticipants, purchasingGroupAbilitation, quantity, uom)
							.setId(sr.getWsProfileId())
							.setName(byProductName)
							.setMaterials(materials)
							.setAppearance(appearances)
							.setAttributeWeightMap(attributeWeightMap)
							.build();


					byProductMap.put(sr.getWsProfileId(), byProduct);

				}

			}
		}


		ArrayListMultimap<String, String> supplierToByProductMapping = ArrayListMultimap.create();

		for (String supplier : uniqueSuppliers) {

			for (BPSparqlResult sr : sparqlResults) {

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



	//USED FOR TESTING AND CAN BE REMOVED
	public static List<BPSupplier> createTestSupplierData(String strQuery, boolean testing, OWLOntology onto, String SPARQL_ENDPOINT, String AUTHORISATION_TOKEN) throws IOException {

		Set<BPSparqlResult> sparqlResults = new HashSet<BPSparqlResult>();

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

			BPSparqlResult sparqlResult = null;			

			while (result.hasNext()) {

				BindingSet solution = result.next();

				String certification = null;
				if (solution.getValue("certificationType") != null) {
					certification = StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", ""));
				}

				String material = null;
				if (solution.getValue("materialType") != null) {
					material = StringUtilities.stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", ""));
				}

				String appearance = null;
				if (solution.getValue("attributeType") != null && solution.getValue("attributeType").stringValue().endsWith("Appearance") && solution.getValue("attributeValue") != null) {
					appearance = solution.getValue("attributeValue").stringValue().replaceAll("\\s+", "");
				}

				sparqlResult = new BPSparqlResult.Builder(StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue().replaceAll("\\s+", "")), 
						solution.getValue("byProductName").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductSupplyType").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductMinParticipants").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductMaxParticipants").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("purchasingGroupAbilitation").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductQuantity").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductMinQuantity").stringValue().replaceAll("\\s+", ""), 
						solution.getValue("byProductUOM").stringValue().replaceAll("\\s+", "")).
						setCertification(certification).
						setMaterial(material).
						setAppearance(appearance).
						setSupplierId(solution.getValue("supplierId").stringValue().replaceAll("\\s+", "")).
						setSupplierName(solution.getValue("supplierName").stringValue().replaceAll("\\s+", "")).
						build();

				sparqlResults.add(sparqlResult);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		// close connection to KB repository
		repository.shutDown();

		// create list of suppliers according to the results from SPARQL
		List<BPSupplier> suppliersList = consolidateSuppliers(sparqlResults, onto);		
		
		return suppliersList;

	}

}