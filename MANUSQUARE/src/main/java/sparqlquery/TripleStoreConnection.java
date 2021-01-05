package sparqlquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import edm.Attribute;
import edm.Certification;
import edm.Material;
import edm.Process;
import query.CSQuery;
import sparqlresult.SparqlRecord;
import supplier.Supplier;

//TODO: This class should be seriously checked and very probably improved. 
public class TripleStoreConnection {

	//configuration of the local GraphDB knowledge base (testing)
	static final String GRAPHDB_SERVER = "http://localhost:7200/"; // Should be configurable., Now we manually fix ths in the docker img
	static final String REPOSITORY_ID = "MANUSQUARE-01072020";

	//configuration of the MANUSQUARE Semantic Infrastructure
	static String WorkshopSpaql = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0";
	static String SPARQL_ENDPOINT = WorkshopSpaql; //"http://116.203.187.118/semantic-registry-test/repository/manusquare?infer=false&limit=0&offset=0";
	static String Workshop_token = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static String AUTHORISATION_TOKEN = Workshop_token; //"c5ec0a8b494a30ed41d4d6fe3107990b";

	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://116.203.187.118/semantic-registry/repository/manusquare/ontology.owl");

	/**
	 * Retrieves (relevant) data / concepts from the Semantic Infrastructure using the content of a consumer query as input.
	 *
	 * @param query content of a consumer query
	 * @return list of suppliers along with the processes (including relevant materials) and certifications registered in the Semantic Infrastructure.
	 * Nov 9, 2019
	 * @throws OWLOntologyCreationException
	 */
	public static List<Supplier> createSupplierData(CSQuery query, boolean testing, OWLOntology onto) {
		String sparql_endpoint_by_env = System.getenv("ONTOLOGY_ADDRESS");
		if (sparql_endpoint_by_env != null) {
			SPARQL_ENDPOINT = sparql_endpoint_by_env;
		}
		if (System.getenv("ONTOLOGY_KEY") != null) {
			AUTHORISATION_TOKEN = System.getenv("ONTOLOGY_KEY");
		}

		Repository repository;

		//use name of processes in query to retrieve subset of relevant supplier data from semantic infrastructure
		List<String> processNames = new ArrayList<String>();

		if (query.getProcesses() == null || query.getProcesses().isEmpty()) {
			System.err.println("There are no processes specified!");
		} else {
			for (Process process : query.getProcesses()) {
				processNames.add(process.getName());
			}
		}


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

		String strQuery = SparqlQuery.createSparqlQuery(query, onto);

		//open connection to GraphDB and run SPARQL query
		Set<SparqlRecord> recordSet = new HashSet<SparqlRecord>();


		try (RepositoryConnection conn = repository.getConnection()) {
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

			//if querying the local KB, we need to set setIncludeInferred to false, otherwise inference will include irrelevant results.
			//when querying the Semantic Infrastructure the non-inference is set in the http parameters.
			if (testing) {
				//do not include inferred statements from the KB
				tupleQuery.setIncludeInferred(false);
			}


			try (TupleQueryResult result = tupleQuery.evaluate()) {

				Attribute supplierAttribute = new Attribute();

				SparqlRecord record = null;

				while (result.hasNext()) {

					//Map<String, String> attributeMap = new HashMap<String, String>();
					BindingSet solution = result.next();

					record = new SparqlRecord();

					record.setSupplierId(solution.getValue("supplier").stringValue().replaceAll("\\s+", ""));
					record.setProcess(stripIRI(solution.getValue("processType").stringValue().replaceAll("\\s+", "")));

					if (solution.getValue("materialType") != null) {

						record.setMaterial(stripIRI(solution.getValue("materialType").stringValue().replaceAll("\\s+", "")));

					}

					if (solution.getValue("certificationType") != null) {

						record.setCertification(stripIRI(solution.getValue("certificationType").stringValue().replaceAll("\\s+", "")));

					}


					//deal with attributes ("Y", "N" or "O") according to consumer query - we do not want to include material attributes hence the check on AttributeMaterial
					if ((solution.getValue("attributeType") != null && !solution.getValue("attributeType").stringValue().endsWith("AttributeMaterial"))) {

						//create supplierAttribute that can be compared to consumerAttribute
						supplierAttribute.setKey(stripIRI(solution.getValue("attributeType").stringValue().replaceAll("\\s+", "")));
						supplierAttribute.setunitOfMeasurement(solution.getValue("uomStr").stringValue().replaceAll("\\s+", ""));
						supplierAttribute.setValue(solution.getValue("attributeValue").stringValue().replaceAll("\\s+", ""));

						Set<Attribute> consumerAttributes = query.getAttributes();

						String condition = Attribute.mapAttributeConditions(supplierAttribute.getKey());

						//FIXME: Wrong position of updatedSupplierAttribute contributes to null values of attributeMap being added to record?
						Attribute updatedSupplierAttribute = new Attribute();

						Map<String, String> attributeMap = null;

						for (Attribute ca : consumerAttributes) {


							if (stripIRI(solution.getValue("attributeType").stringValue().replaceAll("\\s+", "")).equals(ca.getKey())) {

								updatedSupplierAttribute = alignValues(supplierAttribute, ca);

								if (condition.equals(">=")) {

									attributeMap = new HashMap<String, String>();

									if (Double.parseDouble(ca.getValue()) >= Double.parseDouble(updatedSupplierAttribute.getValue())) {
										attributeMap.put(updatedSupplierAttribute.getKey(), "Y");									
										record.setAttributeWeightMap(attributeMap);	

									} else {
										attributeMap.put(updatedSupplierAttribute.getKey(), "N");									
										record.setAttributeWeightMap(attributeMap);	
									}

								}

								else if (condition.equals("<=")) {

									attributeMap = new HashMap<String, String>();

									updatedSupplierAttribute = alignValues(supplierAttribute, ca);

									if (Double.parseDouble(updatedSupplierAttribute.getValue()) <= Double.parseDouble(ca.getValue()) ) {

										attributeMap.put(updatedSupplierAttribute.getKey(), "Y");									
										record.setAttributeWeightMap(attributeMap);	

									} else {

										attributeMap.put(updatedSupplierAttribute.getKey(), "N");									
										record.setAttributeWeightMap(attributeMap);	

									}

								}

								else if (condition.equals("=")) {

									attributeMap = new HashMap<String, String>();

									updatedSupplierAttribute = alignValues(supplierAttribute, ca);

									if (Double.parseDouble(ca.getValue()) == Double.parseDouble(updatedSupplierAttribute.getValue())) {
										attributeMap.put(updatedSupplierAttribute.getKey(), "Y");								
										record.setAttributeWeightMap(attributeMap);	

									} else {

										attributeMap.put(updatedSupplierAttribute.getKey(), "N");									
										record.setAttributeWeightMap(attributeMap);	
									}

								}

							}
						}

					}

					recordSet.add(record);


				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//close connection to KB repository
		repository.shutDown();



		//get unique supplier ids used for constructing the supplier structure below
		Set<String> supplierIds = new HashSet<String>();

		for (SparqlRecord sr : recordSet) {
			supplierIds.add(sr.getSupplierId());
		}

		Certification certification = null;
		Supplier supplier = null;
		List<Supplier> suppliersList = new ArrayList<Supplier>();

		Map<String, SetMultimap<Object, Object>> supplierToProcessMap = new HashMap<String, SetMultimap<Object, Object>>();

		//perform the same operation for attributes as for materials and then merge the two maps
		Map<String, SetMultimap<String, Map<String,String>>> supplierToProcessMapAttributes = new HashMap<String, SetMultimap<String, Map<String, String>>>();

		for (String id : supplierIds) {
			SetMultimap<Object, Object> process2MaterialMap = HashMultimap.create();
			SetMultimap<String, Map<String, String>> process2AttributeMap = HashMultimap.create();
			String supplierID = null;

			for (SparqlRecord sr : recordSet) {

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

			for (SparqlRecord sr : recordSet) {

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

						Set<Material> materialsSet = new HashSet<Material>();
						//get list/set of materials
						materialList = new HashSet<>(e_m.getValue());

						//transform to Set<Material>
						//FIXME: Is this transformation really necessary? Why not stick to *either* list or set?
						for (Object o : materialList) {
							if (o != null) { //Audun: if there are no suppliers materials retrieved from SPARQL, don´t add null-valued Material objects to the set of materials (should be handled properly with !isEmpty check in SimilarityMeasures.java)
								materialsSet.add(new Material((String) o));
							}
						}

						processName = (String) e_m.getKey();

						//add relevant set of materials and attributes together with process name

						process = new Process(processName, materialsSet, attributeMap);

						//add processes
						if (!processes.contains(process)) {
							processes.add(process);
						}

					}


					supplier = new Supplier(id, processes, certifications);
				}
			}


			suppliersList.add(supplier);
		}

		return suppliersList;

	}

	/**
	 * Removes the IRIs in front of processes etc. retrieved from the Semantic Infrastructure
	 *
	 * @param inputConcept an input ontology concept (with full IRI)
	 * @return ontology concept with the IRI removed
	 * Nov 5, 2019
	 */
	private static String stripIRI(String inputConcept) {
		String returnedConceptName = null;
		if (inputConcept.contains("http://manusquare.project.eu/industrial-manusquare#")) {
			returnedConceptName = inputConcept.replaceAll("http://manusquare.project.eu/industrial-manusquare#", "");
		} else if (inputConcept.contains("http://manusquare.project.eu/core-manusquare#")) {
			returnedConceptName = inputConcept.replaceAll("http://manusquare.project.eu/core-manusquare#", "");
		} else {
			returnedConceptName = inputConcept;
		}
		return returnedConceptName;

	}

	private static Attribute alignValues (Attribute supplierAttribute, Attribute consumerAttribute) {

		double newValue = 0;

		if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10;
				supplierAttribute.setValue(Double.toString(newValue));
			}


		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				return supplierAttribute;
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 10;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 100;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) / 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			}

		} else if (consumerAttribute.getunitOfMeasurement().equalsIgnoreCase("µm")) {

			if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("µm")) {
				return supplierAttribute;
			} 	else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("mm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("cm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 10000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("dm")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 100000;
				supplierAttribute.setValue(Double.toString(newValue));
			} else if (supplierAttribute.getunitOfMeasurement().equalsIgnoreCase("m")) {
				newValue = Double.parseDouble(supplierAttribute.getValue()) * 1000000;
				supplierAttribute.setValue(Double.toString(newValue));
			}
		}

		return supplierAttribute;

	}

}