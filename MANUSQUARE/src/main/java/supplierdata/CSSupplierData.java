package supplierdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.google.common.graph.MutableGraph;

import edm.Attribute;
import edm.Certification;
import edm.Process;
import query.CSQuery;
import sparqlconnection.SparqlConnection;
import sparqlquery.CSSparqlQuery;
import sparqlresult.CSSparqlResult;
import supplier.CSSupplier;
import utilities.StringUtilities;


public class CSSupplierData {

	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "MANUSQUARE-01072020";


	/**
	 * Retrieves (relevant) data / concepts from the Semantic Infrastructure using the content of a consumer query as input.
	 *
	 * @param query content of a consumer query
	 * @return list of suppliers along with the processes (including relevant materials) and certifications registered in the Semantic Infrastructure.
	 * Nov 9, 2019
	 * @throws OWLOntologyCreationException
	 */
	public static List<CSSupplier> createSupplierData(CSQuery query, boolean testing, MutableGraph<String> graph, String SPARQL_ENDPOINT, String AUTHORISATION_TOKEN) {

		String strQuery = CSSparqlQuery.createSparqlQuery(query);
		Set<CSSparqlResult> sparqlResults = new HashSet<CSSparqlResult>();		

		//use name of processes in query to retrieve subset of relevant supplier data from semantic infrastructure
		List<String> processNames = new ArrayList<String>();

		if (query.getProcesses() == null || query.getProcesses().isEmpty()) {
			System.err.println("There are no processes specified!");
		} else {
			for (Process process : query.getProcesses()) {
				processNames.add(process.getName());
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
				CSSparqlResult sparqlResult = null;
				Map<String, String> attributeWeightMap = null;
				
				while (result.hasNext()) {

					//Map<String, String> attributeMap = new HashMap<String, String>();
					BindingSet solution = result.next();

					String certification = null;
					if (solution.getValue("certificationType") != null) {
						certification = StringUtilities.stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", ""));
					}
					
					String material = null;
					if (solution.getValue("materialType") != null) {
						material = StringUtilities.stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", ""));
					}
					
					// deal with attributes ("Y", "N" or "O") according to attributes required in the consumer query
					if (solution.getValue("attributeType") != null 
							&& !solution.getValue("attributeType").stringValue().endsWith("AttributeMaterial") 
							&& !solution.getValue("attributeType").stringValue().endsWith("Appearance")) {
										
						System.out.println("CSSupplierData: creating attributeweightmap for supplier id: " + solution.getValue("supplier").stringValue().replaceAll("\\s+", ""));
						attributeWeightMap = Attribute.createAttributeWeightMap(solution, supplierAttribute, query);								

					} 
					
					sparqlResult = new CSSparqlResult.Builder(StringUtilities.stripIRI(solution.getValue("processType").stringValue().replaceAll("\\s+", ""))).
							setSupplierId(solution.getValue("supplier").stringValue().replaceAll("\\s+", "")).setMaterial(material).
							setCertification(certification).setAttributeWeightMap(attributeWeightMap).
							build();

					sparqlResults.add(sparqlResult);


				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		

		//close connection to KB repository
		repository.shutDown();
		
		List<CSSupplier> suppliersList = consolidateSuppliers(sparqlResults);
		
		return suppliersList;
		
		
	}


		public static List<CSSupplier> consolidateSuppliers(Set<CSSparqlResult> sparqlResults) {
			
			//get unique supplier ids used for constructing the supplier structure below
			Set<String> supplierIds = new HashSet<String>();

			for (CSSparqlResult sr : sparqlResults) {
				supplierIds.add(sr.getSupplierId());
			}

			Certification certification = null;
			CSSupplier supplier = null;
			List<CSSupplier> suppliersList = new ArrayList<CSSupplier>();

			Map<String, SetMultimap<Object, Object>> supplierToProcessMap = new HashMap<String, SetMultimap<Object, Object>>();

			//perform the same operation for attributes as for materials and then merge the two maps
			Map<String, SetMultimap<String, Map<String,String>>> supplierToProcessMapAttributes = new HashMap<String, SetMultimap<String, Map<String, String>>>();

			for (String id : supplierIds) {
				SetMultimap<Object, Object> process2MaterialMap = HashMultimap.create();
				SetMultimap<String, Map<String, String>> process2AttributeMap = HashMultimap.create();
				String supplierID = null;

				for (CSSparqlResult sr : sparqlResults) {

					if (sr.getSupplierId().equals(id)) { 

						process2MaterialMap.put(sr.getProcess(), sr.getMaterial());
						process2AttributeMap.put(sr.getProcess(), sr.getAttributeWeightMap());
						supplierID = sr.getSupplierId();
					}
				}

				supplierToProcessMap.put(supplierID, process2MaterialMap);
				supplierToProcessMapAttributes.put(supplierID, process2AttributeMap);
			}

			Process process = null;

			//create supplier objects (supplier id, processes (including materials) and certifications) based on the multimap created in the previous step
			for (String id : supplierIds) {
				SetMultimap<Object, Object> processAndMaterialMap = null;
				SetMultimap<String, Map<String, String>> processAndAttributeMap = null;

				List<Certification> certifications = new ArrayList<Certification>();
				List<Process> processes = new ArrayList<Process>();

				for (CSSparqlResult sr : sparqlResults) {

					if (sr.getSupplierId().equals(id)) {

						//add certifications
						certification = new Certification(sr.getCertification());
						if (certification.getId() != null && !certifications.contains(certification)) {
							certifications.add(certification);
						}

						//add processes and associated materials
						processAndMaterialMap = supplierToProcessMap.get(sr.getSupplierId());
						processAndAttributeMap = supplierToProcessMapAttributes.get(sr.getSupplierId());

						Map<String, String> attributeMap = new HashMap<String, String>();

						Set<Map<String, String>> attributeMapSet = new HashSet<>(processAndAttributeMap.values());
						
						for (Map<String, String> aMap : attributeMapSet) {

							//FIXME: Just ignoring null values now, but should check why there are null values earlier in the process.
							if (aMap != null) {
								attributeMap.putAll(aMap);
							}

						}

						String processName = null;
						Set<Object> materialList = new HashSet<Object>();

						//iterate processAndMaterialMap and extract process and relevant materials for that process
						for (Entry<Object, Collection<Object>> e_m : processAndMaterialMap.asMap().entrySet()) {

							Set<String> materialsSet = new HashSet<String>();
							//get list/set of materials
							materialList = new HashSet<>(e_m.getValue());

							//FIXME: Is this transformation really necessary? Why not stick to *either* list or set?
							for (Object o : materialList) {
								if (o != null) { //Audun: if there are no suppliers materials retrieved from SPARQL, don´t add null-valued Material objects to the set of materials (should be handled properly with !isEmpty check in SimilarityMeasures.java)
									materialsSet.add((String) o);
								}
							}

							processName = (String) e_m.getKey();

							//add relevant set of materials and attributes together with process name							
							process = new Process.Builder()
									.setName(processName)
									.setMaterials(materialsSet)
									.setAttributeWeightMap(attributeMap)
									.build();

							//add processes
							if (!processes.contains(process)) {
								processes.add(process);
							}

						}

						supplier = new CSSupplier.Builder(processes)
								.setSupplierId(id)
								.setSupplierName(sr.getSupplierName())
								.setCertifications(certifications)
								.build();
												
						
					}
				}


				suppliersList.add(supplier);
			}

			return suppliersList;
			
		}


		//USED FOR TESTING AND CAN BE REMOVED

		public static List<CSSupplier> createTestSupplierData(String strQuery, boolean testing, OWLOntology onto, String SPARQL_ENDPOINT, String AUTHORISATION_TOKEN) {

			Set<CSSparqlResult> sparqlResults = new HashSet<CSSparqlResult>();		


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

					CSSparqlResult sparqlResult = null;
					
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
						
						
						sparqlResult = new CSSparqlResult.Builder(StringUtilities.stripIRI(solution.getValue("processType").stringValue().replaceAll("\\s+", "")))
								.setSupplierId(solution.getValue("supplier").stringValue().replaceAll("\\s+", ""))
								.setSupplierName(solution.getValue("supplierName").stringValue().replaceAll("\\s+", ""))
								.setMaterial(material)
								.setCertification(certification)
								.build();

						sparqlResults.add(sparqlResult);


					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			

			//close connection to KB repository
			repository.shutDown();
			
			List<CSSupplier> suppliersList = consolidateSuppliers(sparqlResults);
			
			return suppliersList;
			
			
		}
	

}