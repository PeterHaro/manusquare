package supplierdata;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import edm.SparqlRecord_BP;
import query.ByProductQuery;
import sparql.SparqlQuery_BP;
import utilities.SparqlConnection;
import utilities.StringUtilities;

public class SupplierData_BP {
	
	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "BP_2";


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

				record = new SparqlRecord_BP();
				record.setWsProfileId(StringUtilities
						.stripIRI(solution.getValue("wsProfileId").stringValue().replaceAll("\\s+", "")));
				record.setSupplierId(solution.getValue("supplierId").stringValue().replaceAll("\\s+", ""));
				record.setByProductName(solution.getValue("byProductName").stringValue().replaceAll("\\s+", ""));
				record.setByProductSupplyType(solution.getValue("byProductSupplyType").stringValue().replaceAll("\\s+", ""));
				
				if (solution.getValue("byProductDeadline") != null) {
				
				record.setByProductDeadline(solution.getValue("byProductDeadline").stringValue().replaceAll("\\s+", ""));
				
				}
				record.setByProductMinParticipants(solution.getValue("byProductMinParticipants").stringValue().replaceAll("\\s+", ""));
				record.setByProductMaxParticipants(solution.getValue("byProductMaxParticipants").stringValue().replaceAll("\\s+", ""));
				record.setByProductQuantity(solution.getValue("byProductQuantity").stringValue().replaceAll("\\s+", ""));
				record.setByProductMinQuantity(solution.getValue("byProductMinQuantity").stringValue().replaceAll("\\s+", ""));
				record.setByProductUOM(solution.getValue("byProductUOM").stringValue().replaceAll("\\s+", ""));

				if (solution.getValue("certificationType") != null) {
					record.setCertification(StringUtilities
							.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));

				}

			  // deal with attributes ("Y", "N" or "O") according to attributes required in
				// the consumer query
				if (solution.getValue("attributeType") != null && !solution.getValue("attributeType").stringValue().endsWith("AttributeMaterial") /*&& solution.getValue("uomStr") != null*/) {
					
					attributeWeightMap = Attribute.createAttributeWeightMap(solution,
							supplierAttribute, query);
										
					record.setAttributeWeightMap(attributeWeightMap);
					

				}
				
				if (solution.getValue("materialType") != null) {
					record.setMaterial(StringUtilities
							.stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", "")));
				}
				

				recordSet.add(record);

			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		// close connection to KB repository
		repository.shutDown();
		
		
		// create list of suppliers according to the results from SPARQL
		List<Supplier_BP> suppliersList = createSupplierList(recordSet);
		
		
		return suppliersList;

	}

	/**
	 * Aggregates the retrieved SPARQL data per supplier
	 * @param recordSet
	 * @return list of supplier objects containing SPARQL results
	   Nov 25, 2020
	 */
	private static List<Supplier_BP> createSupplierList(Set<SparqlRecord_BP> recordSet) {
		

		// get unique supplier ids used for constructing the supplier structure below
		Set<String> supplierIds = new HashSet<String>();

		for (SparqlRecord_BP sr : recordSet) {
			supplierIds.add(sr.getSupplierId());
		}
		
		Set<String> wsProfileIds = new HashSet<String>();
		
		// get unique wsProfile ids for constructing the byproduct structure
		for (SparqlRecord_BP sr : recordSet) {
			wsProfileIds.add(sr.getWsProfileId());
		}

	
		Certification certification = null;
		Supplier_BP supplier = null;
		List<Supplier_BP> suppliersList = new ArrayList<Supplier_BP>();

		Map<String, SetMultimap<String, Map<String, String>>> supplierToByProductMapAttributes = new HashMap<String, SetMultimap<String, Map<String, String>>>();

		for (String id : supplierIds) {
			SetMultimap<String, Map<String, String>> byProduct2AttributeMap = HashMultimap.create();
			String supplierID = null;

			for (SparqlRecord_BP sr : recordSet) {

				if (sr.getSupplierId().equals(id) && sr.getAttributeWeightMap() != null) {

					byProduct2AttributeMap.put(sr.getWsProfileId(), sr.getAttributeWeightMap());		
					supplierID = sr.getSupplierId();
				}
			}
			
			supplierToByProductMapAttributes.put(supplierID, byProduct2AttributeMap);
			
		}
		
		ByProduct byProduct = null;

		// create supplier objects 
		for (String id : supplierIds) {
			SetMultimap<String, Map<String, String>> byProductAndAttributeMap = null;

			List<Certification> certifications = new ArrayList<Certification>();
			List<ByProduct> byProducts = new ArrayList<ByProduct>();

			for (SparqlRecord_BP sr : recordSet) {

				if (sr.getSupplierId().equals(id)) {					

					// add certifications
					certification = new Certification(sr.getCertification());
					if (certification.getId() != null && !certifications.contains(certification)) {
						certifications.add(certification);
					}

					// add byProducts
					byProductAndAttributeMap = supplierToByProductMapAttributes.get(sr.getSupplierId());
					

					if (byProductAndAttributeMap != null) {
					
					for (String key : byProductAndAttributeMap.keySet()) {
						if (sr.getWsProfileId().equals(key)) {
							
							for (Map<String, String> aMap : byProductAndAttributeMap.get(key)) {

								if (aMap != null) {
									byProduct = new ByProduct(sr.getWsProfileId(), sr.getByProductName(), sr.getByProductSupplyType(), Double.parseDouble(sr.getByProductQuantity()), Double.parseDouble(sr.getByProductMinQuantity()), sr.getByProductUOM(), 
											sr.getMaterial(), aMap);

								}

							}

						}
					}
					
					}
					

					//add byproducts
					if (!byProducts.contains(byProduct)) {
						byProducts.add(byProduct);
					}


					supplier = new Supplier_BP(id, byProducts, certifications);
										
				}
			}

			suppliersList.add(supplier);
		}
		
		
		return suppliersList;
	}
	

	
}