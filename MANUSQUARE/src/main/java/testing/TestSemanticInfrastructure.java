package testing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import similarity.SimilarityMethods;

public class TestSemanticInfrastructure {

	static SimilarityMethods similarityMethod = SimilarityMethods.WU_PALMER;

	//configuration of the MANUSQUARE Semantic Infrastructure
	static String WorkshopSpaql = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0";
	static String SPARQL_ENDPOINT = WorkshopSpaql; //"http://116.203.187.118/semantic-registry-test/repository/manusquare?infer=false&limit=0&offset=0";
	static String Workshop_token = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static String AUTHORISATION_TOKEN = Workshop_token; //"c5ec0a8b494a30ed41d4d6fe3107990b";

	//if the MANUSQUARE ontology is fetched from url
	//static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://116.203.187.118/semantic-registry/repository/manusquare/ontology.owl");

	public static void main(String[] args) {

		//logging(false);
		//testSI();
		testIM();
	}

	
	
	public static void testIM() {

		Repository repository;

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", AUTHORISATION_TOKEN);
		headers.put("accept", "application/JSON");
		repository = new SPARQLRepository(SPARQL_ENDPOINT);
		repository.initialize();
		((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);

		String strQuery = imQuery();


		try (RepositoryConnection conn = repository.getConnection()) {
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

			try (TupleQueryResult result = tupleQuery.evaluate()) {
				while (result.hasNext()) {
					BindingSet solution = result.next();  

					//Set<String> bindings = solution.getBindingNames();
					//System.out.println("Bindings are " + bindings);
					System.out.println("Supplier: " + stripIRI(solution.getValue("supplier").stringValue()));
					System.out.println("Supplier Name: " + stripIRI(solution.getValue("supplierName").stringValue()));
					System.out.println("Innovation Phase Type: " + stripIRI(solution.getValue("innovationPhaseType").stringValue()));
					System.out.println("Skill Type: " + stripIRI(solution.getValue("skillType").stringValue()));
					System.out.println("Innovation Sector Type: " + stripIRI(solution.getValue("innovationSectorType").stringValue()));
					System.out.println("Innovation Type: " + stripIRI(solution.getValue("innovationTypeType").stringValue()));
//					if (solution.getValue("certificationType").stringValue() != null) {
//					System.out.println("Certification: " + stripIRI(solution.getValue("certificationType").stringValue()));
//					}
					System.out.println("\n");

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("The SPARQL query is: ");
		System.out.println(strQuery);

	}

	public static String imQuery () {
		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
				strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
				strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
				strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
				strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

				strQuery += "SELECT DISTINCT ?supplier ?supplierName ?innovationPhaseType ?skillType ?innovationSectorType ?innovationTypeType ?certificationType\n";

				strQuery += "WHERE { \n";

				strQuery += "?improfile core:hasSupplier ?supplier . \n";
				strQuery +="?supplier core:hasName ?supplierName . \n";
				strQuery += "?improfile core:hasInnovationPhase ?innovationPhase . \n";

				strQuery += "?innovationPhase rdf:type ?innovationPhaseType . \n";

				strQuery += "?improfile core:hasInnovationType ?innovationType . \n";

				strQuery += "?innovationType rdf:type ?innovationTypeType . \n";

				strQuery += "?improfile core:hasSkill ?skill . \n";

				strQuery += "?skill rdf:type ?skillType . \n";

				strQuery += "?improfile core:hasSector ?innovationSector . \n";

				strQuery += "?innovationSector rdf:type ?innovationSectorType . \n";

//				strQuery += "FILTER ( ?innovationPhaseType not in ( owl:NamedIndividual ) && ?skillType not in ( owl:NamedIndividual ) && ?innovationSectorType not in ( owl:NamedIndividual ) && ?innovationTypeType not in ( owl:NamedIndividual ))\n";
//
//				strQuery += "OPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType .\n"; 
//				strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n";
//				strQuery += "} \n";
//
//				strQuery += "?supplier core:hasAttribute ?languageAttribute . \n";
//				strQuery += "?languageAttribute rdf:type ?languageAttributeType . \n";
//				strQuery += "?languageAttribute core:hasValue ?language . \n";
//				strQuery += "VALUES ?languageAttributeType {ind:Language} . \n";
				strQuery += "}\n";
				
				return strQuery;
				
	}
	
	public static String imQueryOnlySuppliers () {
		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
				strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
				strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
				strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
				strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

				strQuery += "SELECT DISTINCT ?supplier ?supplierName \n";

				strQuery += "WHERE { \n";

				strQuery += "?improfile core:hasSupplier ?supplier . \n";
				strQuery +="?supplier core:hasName ?supplierName . \n";
				
				strQuery += "}\n";
				
				return strQuery;
				
	}
	
	
	public static void testSI() {

		Repository repository;

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", AUTHORISATION_TOKEN);
		headers.put("accept", "application/JSON");
		repository = new SPARQLRepository(SPARQL_ENDPOINT);
		repository.initialize();
		((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);

		String supplier = "stakeholders:194a7794-9f3c-4f9c-9855-bc243a536594";


		String strQuery = testQuery();


		try (RepositoryConnection conn = repository.getConnection()) {
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

			try (TupleQueryResult result = tupleQuery.evaluate()) {
				while (result.hasNext()) {
					BindingSet solution = result.next();  

					Set<String> bindings = solution.getBindingNames();
					System.out.println("Bindings are " + bindings);
					//System.out.println("ProcessChain: " + solution.getValue("processChain"));
					System.out.println("ProcessType: " + solution.getValue("processType"));
					System.out.println("Supplier: " + solution.getValue("supplier"));
					System.out.println("SupplierName: " + solution.getValue("supplierName").stringValue());
					System.out.println("MaterialType: " + solution.getValue("materialType"));
					System.out.println("Property: " + solution.getValue("property"));
					System.out.println("AttributeType: " + solution.getValue("attributeType"));
					System.out.println("AttributeValue: " + solution.getValue("attributeValue"));
					System.out.println("UOM: " + solution.getValue("uomStr"));
					System.out.println("Certification: " + solution.getValue("certificationType"));			
					System.out.println("\n");

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("The SPARQL query is: ");
		System.out.println(strQuery);

	}
						

				public static String testQuery () {

			String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
			strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
			strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
			strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";

			strQuery += "SELECT DISTINCT ?processChain ?processType ?supplier ?supplierName ?materialType ?certificationType ?attributeType (str(?uom) as ?uomStr) ?attributeValue \n";

			strQuery += "WHERE { \n";


			strQuery +="?processChain core:hasProcess ?process .\n";
			strQuery += "?process rdf:type ?processType . \n";
			strQuery +="?processType rdfs:subClassOf* ind:MfgProcess . \n";
			strQuery +="?processChain core:hasSupplier ?supplier . \n";
			strQuery +="?supplier core:hasName ?supplierName . \n";
			strQuery += "OPTIONAL {?process core:hasAttribute ?attribute . \n";
			strQuery += "?attribute rdf:type ?attributeType . \n";
			strQuery += "?attribute core:hasUnitOfMeasure ?uomInd . \n";
			strQuery += "?uomInd core:hasName ?uom . \n";	
			strQuery += "?attribute core:hasValue ?attributeValue . \n";
			strQuery += "VALUES ?attributeType {ind:MaxPartSizeX ind:AttributeMaterial}  \n";
			strQuery += "FILTER ( ?attributeType not in ( owl:NamedIndividual )) \n";
			strQuery += "} \n";

			//get materials
			strQuery += "OPTIONAL {?process core:hasAttribute ?attribute . \n";
			strQuery += "?attribute core:hasObjectValue ?attributeMaterialValue .  \n";
			strQuery += "?attributeMaterialValue rdf:type ?materialType .  \n";		
			//strQuery += "VALUES ?attributeType {ind:AttributeMaterial}  \n";
			//strQuery += "FILTER ( ?attributeType not in ( owl:NamedIndividual )) \n";
			strQuery += "} \n";

			strQuery +="OPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
			strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n"; 
			strQuery += "}\n";
			strQuery += "}\n";

			return strQuery;

		}

		public static String currentQuery () {

			String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
			strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
			strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
			strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";


			strQuery += "SELECT DISTINCT ?processChain ?processType ?supplier ?materialType ?certificationType ?ToleranceAttr ?updatedToleranceValue0 \n"; 
			strQuery += " WHERE { \n";
			strQuery += "?processChain core:hasProcess ?process . \n";
			strQuery += "?process rdf:type ?processType . \n";
			strQuery += "?processType rdfs:subClassOf* ind:RapidPrototyping .\n";
			strQuery += "?processChain core:hasSupplier ?supplier .\n";

			strQuery += "OPTIONAL {?process core:hasAttribute ?toleranceAttribute . \n";
			strQuery += "?toleranceAttribute rdf:type ?toleranceAttributeType . \n";
			strQuery += "?toleranceAttribute core:hasUnitOfMeasure ?uomInd . \n";
			strQuery += "?uomInd core:hasName ?uom .\n";
			strQuery += "VALUES ?toleranceAttributeType {ind:Tolerance} . \n";
			strQuery += "?toleranceAttribute core:hasValue ?toleranceValue . \n";
			strQuery += "} \n";

			strQuery += "BIND ( \n";
			strQuery += "IF (bound(?uom) && ?uom = \"mm\"^^rdfs:Literal, xsd:decimal(?toleranceValue) * 1, \n";
			strQuery += "IF (bound(?uom) && ?uom = \"cm\"^^rdfs:Literal, xsd:decimal(?toleranceValue) * 10, \n";
			strQuery += "IF (bound(?uom) && ?uom = \"dm\"^^rdfs:Literal, xsd:decimal(?toleranceValue) * 100, \n";
			strQuery += "xsd:decimal(?toleranceValue)))) as ?updatedToleranceValue0) \n";

			strQuery += "BIND ( \n";
			strQuery += "IF (bound(?updatedToleranceValue0) && ?updatedToleranceValue0 <= 1000.0, \"Y\", \n";
			strQuery += "IF (bound(?updatedToleranceValue0) && ?updatedToleranceValue0 > 1000.0, \"N\", \n";
			strQuery += "\"O\")) as ?ToleranceAttr) \n";

			strQuery += "OPTIONAL { ?process core:hasAttribute ?materialAttribute . \n";
			strQuery += "?materialAttribute rdf:type ?materialAttributeType . \n";
			strQuery += "VALUES ?materialAttributeType {core:Material} . \n";
			strQuery += "?materialAttribute core:hasValue ?materialAttributeValue . \n";
			strQuery += "?materialAttributeValue rdf:type ?materialType . } \n";

			strQuery += "OPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
			strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual )) \n";
			strQuery += "} \n";

			strQuery += "}";

			System.out.println(strQuery);

			return strQuery;
		}

		public static String getAllForSingleProcessQuery () {

			String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
			strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
			strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";

			strQuery += "SELECT ?processChain ?process ?processType ?supplier ?supplierName ?attributeType ?attributeValue ?uom ?materialType ?certificationType  \n"; //?attributeKey  \n";
			strQuery += "WHERE { \n";

			strQuery += "?processChain core:hasSupplier ?supplier .\n";
			//strQuery += "?processChain core:hasSupplier ind:stakeholders:71f9f13a-54f2-4ff9-8e7a-82127fd114f2 .\n";
			strQuery += "?supplier core:hasName ?supplierName .\n";


			strQuery += "?processChain core:hasProcess ?process .\n";
			strQuery += "?process rdf:type ?processType .\n";
			strQuery += "VALUES ?processType {ind:3DPrinting} . \n";



			strQuery += "OPTIONAL { ?process core:hasAttribute ?attribute .\n";
			strQuery += "?attribute rdf:type ?attributeType . \n";
			//strQuery += "VALUES ?attributeType {ind:MaxPartSizeZ} . \n";
			strQuery += "?attribute core:hasValue ?attributeValue . \n";
			strQuery += "?attribute core:hasUnitOfMeasure ?uomInd . \n";
			strQuery += "?uomInd core:hasName ?uom . }\n";

			strQuery += "OPTIONAL { ?process core:hasAttribute ?materialAttribute .\n";
			strQuery += "?materialAttribute rdf:type ?materialAttributeType .  \n";
			strQuery += "VALUES ?materialAttributeType {ind:AttributeMaterial} . \n";
			strQuery += "?materialAttribute ind:hasValue ?materialAttributeValue . \n";	
			strQuery += "?materialAttributeValue rdf:type ?materialType . } \n";

			strQuery += "OPTIONAL {?supplier core:hasCertification ?certification. \n";
			strQuery += "?certification rdf:type ?certificationType . }\n";

			strQuery += "} \n";

			System.out.println(strQuery);

			return strQuery;

		}

		public static String getAllQuery () {

			String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
			strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
			strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";

			strQuery += "SELECT ?process ?processType ?supplier ?supplierName ?attributeType ?certificationType  \n"; //?attributeKey  \n";
			strQuery += "WHERE { \n";

			strQuery += "?processChain core:hasSupplier ?supplier .\n";
			strQuery += "?supplier core:hasName ?supplierName .\n";

			strQuery += "?processChain core:hasProcess ?process .\n";
			strQuery += "?process rdf:type ?processType .\n";

			strQuery += "?process core:hasAttribute ?attribute .\n";
			strQuery += "?attribute rdf:type ?attributeType .\n";



			strQuery += "?supplier core:hasCertification ?certification. \n";
			strQuery += "?certification rdf:type ?certificationType .\n";

			strQuery += "} \n";

			System.out.println(strQuery);

			return strQuery;

		}

		public static String createTestSparqlQuery () {

			String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
			strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
			strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";

			strQuery += "SELECT DISTINCT ?processChain ?process ?processType ?supplier ?materialType ?certificationType ?attribute ?attributeType \n"; //?attributeKey  \n";
			strQuery += "WHERE { \n";

			//get all subclasses of LCS
			strQuery += "?processChain core:hasProcess ?process .\n";
			strQuery += "?process rdf:type ?processType .\n";
			strQuery += "?processChain core:hasSupplier ?supplier .\n";

			strQuery += "?process core:hasAttribute ?attribute .\n";
			strQuery += "?attribute rdf:type ?attributeType .\n";

			//materials option 2: we use the object property hasMaterial to retrieve materials relevant for our processes
			strQuery += "\nOPTIONAL { ?process core:hasMaterial ?materialAttribute . \n";
			strQuery += "?materialAttribute ind:hasValue ?materialAttributeValue . \n";
			strQuery += "?materialAttributeValue rdf:type ?materialType . }\n";

			//certifications (as before we just include all certifications associated with the relevant suppliers, not considering the certifications required by the consumer at this point,
			//this is taken care of by the matchmaking algo)
			strQuery += "\nOPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType .} \n";

			strQuery += "\nFILTER ( ?certificationType not in ( owl:NamedIndividual ))";
			strQuery += "\n}";

			System.out.println(strQuery);

			return strQuery;
		}

		public static String createTestSparqlQuery2 () {

			String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
			strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
			strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
			strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";

			strQuery += "SELECT DISTINCT ?processChain ?processType ?process ?supplier ?suppplierName \n";// ?materialType ?certificationType ?toleranceAttribute ?toleranceAttributeType  \n"; //?attributeKey  \n";
			strQuery += "WHERE { \n";

			strQuery += "?processChain core:hasProcess ?process .\n";
			strQuery += "?process rdf:type ?processType .\n";
			//strQuery += "?processType rdfs:subClassOf* ind:MfgProcess .\n";
			strQuery += "?processChain core:hasSupplier ?supplier .\n";
			strQuery += "?supplier core:hasName ?supplierName .\n";

			//		strQuery += "?processChain core:hasSupplier ?supplier .\n";
			//		strQuery += "?supplier core:hasName ?supplierName .\n";

			strQuery += "\nOPTIONAL { ?process core:hasAttribute ?toleranceAttribute . \n";
			strQuery += "?toleranceAttribute rdf:type ?toleranceAttributeType . \n";
			strQuery += "?toleranceAttribute core:hasUnitOfMeasure ?uomInd . \n";
			strQuery += "?uomInd core:hasName ?uom . \n";
			//strQuery += "VALUES ?toleranceAttributeType {ind:Tolerance} . \n";
			strQuery += "?toleranceAttribute core:hasValue ?toleranceValue . \n";
			strQuery += "} \n";

			strQuery += "\n";

			strQuery += "BIND ( \n";
			strQuery += "IF (bound(?uom) && ?uom = \"m\", ?toleranceValue * 1000, \n";
			strQuery += "IF (bound(?uom) && ?uom = \"dm\", ?toleranceValue * 100,\n";
			strQuery += "IF (bound(?uom) && ?uom = \"cm\", ?toleranceValue * 10, \n";
			strQuery += "?toleranceValue))) as ?updatedToleranceValue0)  \n";

			strQuery += "\n";

			strQuery += "BIND ( \n";
			strQuery += "IF (bound(?updatedToleranceValue0) && ?updatedToleranceValue0 <= 10.0, \"Y\", \n";
			strQuery += "IF (bound(?updatedToleranceValue0) && ?updatedToleranceValue0 > 10.0, \"N\",  \n";
			strQuery += "\"O\")) as ?ToleranceAttr) \n";

			strQuery += "\n";

			strQuery += "OPTIONAL { ?process core:hasAttribute ?materialAttribute .\n";
			strQuery += "?materialAttribute rdf:type ?materialAttributeType .  \n";
			strQuery += "VALUES ?materialAttributeType {ind:AttributeMaterial} . \n";
			strQuery += "?materialAttribute ind:hasValue ?materialAttributeValue . \n";	
			strQuery += "?materialAttributeValue rdf:type ?materialType . } \n";

			strQuery += "OPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType .}  \n";

			strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual )) \n";

			strQuery += "} \n";

			System.out.println(strQuery);

			return strQuery;

		}



		/**
		 * Checks if the bindings associated with the SPARQL results contains attributes. Requires that the binding variable names end with "Attr". 
		 * @param bindings
		 * @return
       Feb 9, 2020
		 */
		private static boolean containsAttributes (Set<String> bindings) {

			boolean containsAttribute = false;

			for (String s : bindings) {
				if (s.endsWith("Attr")) {
					containsAttribute = true;
				}
			}

			return containsAttribute;

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

		//private static void logging(boolean logging) {
		//	Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "org.eclipse.rdf4j"));
		//
		//	if (logging == false) {			
		//		for(String log:loggers) { 
		//			Logger logger = (Logger)LoggerFactory.getLogger(log);
		//			logger.setLevel(Level.ERROR);
		//			logger.setAdditive(false);
		//		}
		//	} else {
		//
		//		System.out.println("Logging:");
		//
		//	}
		//
		//}
	}
