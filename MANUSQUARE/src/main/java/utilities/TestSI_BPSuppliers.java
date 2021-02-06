package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import sparqlconnection.SparqlConnection;
import supplier.BPSupplier;
import supplierdata.BPSupplierData;

public class TestSI_BPSuppliers {

	static String SPARQL_ENDPOINT = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0"; 
	static String AUTHORISATION_TOKEN = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare/ontology.owl");	
	static final boolean TESTING = false;

	public static void main(String[] args) throws IOException, OWLOntologyCreationException {

		//supplierDataResults();
		//fullBPQueryResults();
		//getAllByProductResults();
		//getAllPropertiesResults(); 
		materialResults();


	}
	
	public static void materialResults () throws IOException, OWLOntologyCreationException {
		
		String query = material();
		
		System.out.println(query);
		
		Repository repository = SparqlConnection.initRepository();	

		TupleQuery tupleQuery = SparqlConnection.connect(repository, TESTING, query);
		
		BufferedWriter bfwriter = new BufferedWriter(new FileWriter("./files/TEST_OUTPUT/bp_materials.txt"));

		try (TupleQueryResult result = tupleQuery.evaluate()) {

			while (result.hasNext()) {
				BindingSet solution = result.next();  
				
				System.out.println("Bindings: " + solution.getBindingNames());
				System.out.println("\nWS Profile ID: " + solution.getValue("wsProfileId").stringValue());
				System.out.println("Supplier ID: " + solution.getValue("supplierId").stringValue());
				System.out.println("Supplier Name: " + StringUtilities.stripIRI(solution.getValue("supplierName").stringValue()));
				
				bfwriter.append("\n\nWS Profile ID: " + StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue()));
				bfwriter.append("\nSupplier ID: " + StringUtilities.stripIRI(solution.getValue("supplierId").stringValue()));
				bfwriter.append("\nSupplier Name: " + solution.getValue("supplierName").stringValue());
				
				if (solution.getValue("materialType") != null) {
				System.out.println("Material: " + StringUtilities.stripIRI(solution.getValue("materialType").stringValue()));
				bfwriter.append("\nMaterial: " + StringUtilities.stripIRI(solution.getValue("materialType").stringValue()));
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		bfwriter.close();
		
	}
	
	
	public static String material () {

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

		strQuery += "SELECT DISTINCT ?wsProfileId ?supplierId ?supplierName ?materialType \n";

		strQuery += "WHERE { \n";

		strQuery += "?wsProfileId core:hasSupplier ?supplierId. \n";
		strQuery += "?supplierId core:hasName ?supplierName . \n";

		strQuery += "OPTIONAL {?wsProfileId core:hasAttribute ?attribute . \n";
		strQuery += "?attribute rdf:type ?attributeType . \n";
		strQuery += "#GET MATERIALS \n";
		strQuery += "OPTIONAL {?attribute core:hasObjectValue ?attributeMaterialValue . \n";
		strQuery += "?attributeMaterialValue rdf:type ?materialType .  \n";
		strQuery += "FILTER ( ?materialType not in ( owl:NamedIndividual ))  \n";
		strQuery += "}  \n";
		strQuery += "VALUES ?attributeType {ind:AttributeMaterial}  \n";
		strQuery += "FILTER ( ?attributeType not in ( owl:NamedIndividual ))  \n";
		strQuery += "} \n";

		strQuery += "}";

		return strQuery;
	}
	

	public static void supplierDataResults() throws IOException, OWLOntologyCreationException {

		String query = supplierData();
		
		System.out.println(query);

		Repository repository = SparqlConnection.initRepository();	

		TupleQuery tupleQuery = SparqlConnection.connect(repository, TESTING, query);
		
		BufferedWriter bfwriter = new BufferedWriter(new FileWriter("./files/TEST_OUTPUT/bp_suppliers.txt"));

		try (TupleQueryResult result = tupleQuery.evaluate()) {

			while (result.hasNext()) {
				BindingSet solution = result.next();  
				
				//System.out.println("Bindings: " + solution.getBindingNames());
				
				System.out.println("Supplier ID: " + solution.getValue("supplierId").stringValue());
				System.out.println("\nSupplier Name: " + StringUtilities.stripIRI(solution.getValue("supplierName").stringValue()));
				
				bfwriter.append("\n\nSupplier ID: " + StringUtilities.stripIRI(solution.getValue("supplierId").stringValue()));
				bfwriter.append("\n\nSupplier Name: " + solution.getValue("supplierName").stringValue());
				
				if (solution.getValue("language") != null) {
				System.out.println("Language: " + StringUtilities.stripIRI(solution.getValue("language").stringValue()));
				bfwriter.append("\nLanguage: " + StringUtilities.stripIRI(solution.getValue("language").stringValue()));
				}
				if (solution.getValue("country") != null) {
				System.out.println("Country: " + StringUtilities.stripIRI(solution.getValue("country").stringValue()));
				bfwriter.append("\nCountry: " + StringUtilities.stripIRI(solution.getValue("country").stringValue()));
				}
				
				if (solution.getValue("certificationType") != null) {
				System.out.println("Certification: " + StringUtilities.stripIRI(solution.getValue("certificationType").stringValue()));
				bfwriter.append("\nCertification " + StringUtilities.stripIRI(solution.getValue("certificationType").stringValue()));
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		bfwriter.close();

	}

	public static String supplierData () {

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

		strQuery += "SELECT DISTINCT ?supplierId ?supplierName ?language ?country ?certificationType \n";

		strQuery += "WHERE { \n";

		strQuery += "?wsProfileId core:hasSupplier ?supplierId. \n";
		strQuery += "?supplierId core:hasName ?supplierName . \n";


		strQuery += "OPTIONAL {?supplierId core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
		strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n";
		strQuery += "} \n";


		//strQuery += "\nOPTIONAL { \n";
		strQuery += "\n?supplierId core:hasAttribute ?languageAttribute . \n";
		strQuery += "?languageAttribute rdf:type ?languageAttributeType . \n";       
		strQuery += "?languageAttribute core:hasValue ?language . \n";
		strQuery += "VALUES ?languageAttributeType {ind:Language} . \n";
		//strQuery += "} \n";

		//strQuery += "\nOPTIONAL { \n";
		strQuery += "\n?supplierId core:hasAttribute ?countryAttribute . \n";
		strQuery += "?countryAttribute rdf:type ?countryAttributeType . \n";       
		strQuery += "?countryAttribute core:hasValue ?country . \n";
		strQuery += "VALUES ?countryAttributeType {ind:Country} . \n";
		//strQuery += "} \n";

		strQuery += "}";

		return strQuery;
	}



	public static void getAllByProductResults() {

		String strQuery = getAllByProducts();
		Repository repository = SparqlConnection.initRepository();	

		TupleQuery tupleQuery = SparqlConnection.connect(repository, TESTING, strQuery);

		int counter = 0;
		try (TupleQueryResult result = tupleQuery.evaluate()) {

			while (result.hasNext()) {
				counter++;
				BindingSet solution = result.next();  

				//				System.out.println("Bindings: " + solution.getBindingNames());

				System.out.println("\nWSProfile ID: " + StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue()));
				//				System.out.println("By-product Name: " + StringUtilities.stripIRI(solution.getValue("byProductName").stringValue()));
				//				System.out.println("Supplier ID: " + StringUtilities.stripIRI(solution.getValue("supplierId").stringValue()));
				//				System.out.println("Supplier Name: " + StringUtilities.stripIRI(solution.getValue("supplierName").stringValue()));
				if (solution.getValue("purchasingGroupAbilitation") != null) {
					System.out.println("Purchasing Group Abilitation: " + solution.getValue("purchasingGroupAbilitation").stringValue());
				} else {
					System.out.println("There is no Purchasing Group Abilitation defined for by-product: " + StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue()));
				}

				if (solution.getValue("byProductStatus") != null) {
					System.out.println("Status: " + solution.getValue("byProductStatus").stringValue());
				} else {
					System.out.println("There is no ByProductStatus defined for this by-product: " + StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue()));
				}
				//				System.out.println("Supply Type: " + solution.getValue("byProductSupplyType").stringValue());
				System.out.println("Creation Date: " + StringUtilities.stripIRI(solution.getValue("creationDate").stringValue()));
				//System.out.println("Mode: " + StringUtilities.stripIRI(solution.getValue("mode").stringValue()));
				//				System.out.println("\n");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.err.println("There are " + counter + " records retrieved.");

	}

	public static String getAllByProducts () {

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n"; 
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";


		strQuery += "SELECT ?wsProfileId ?byProductName ?supplierId ?supplierName ?creationDate ?byProductStatus ?purchasingGroupAbilitation \n";
		//strQuery += "SELECT distinct ?wsProfileId ?byProductName ?supplierId ?supplierName ?byProductStatus ?byProductSupplyType \n";
		//strQuery += "SELECT ?wsProfileId  \n";


		strQuery += "WHERE { \n";


		strQuery += "?wsProfileId rdf:type core:WsProfile . \n";
		strQuery += "?wsProfileId core:hasName ?byProductName . \n";
		strQuery += "?wsProfileId core:hasSupplier ?supplierId . \n";
		strQuery += "?supplierId core:hasName ?supplierName . \n";
		strQuery += "OPTIONAL { ?wsProfileId core:hasPurchasingGroupAbilitation ?purchasingGroupAbilitation . } \n";
		strQuery += "OPTIONAL { ?wsProfileId core:hasStatus ?byProductStatus .  } \n";

		//		strQuery +="FILTER (?byProductStatus=\"Available\"^^xsd:string)  \n";	

		//		strQuery +="FILTER regex(?byProductStatus, \"Available\")  \n";	

		//		strQuery += "?wsProfileId ind:hasSupplyType ?byProductSupplyType . \n";
		strQuery += "?wsProfileId core:hasCreationDate ?creationDate . \n";
		//strQuery += "?wsProfileId ind:hasMode ?mode . \n";

		strQuery += "}";

		System.out.println(strQuery);

		return strQuery;

	}

	public static void fullBPQueryResults() throws IOException, OWLOntologyCreationException {

		String query = fullBPQuery();

		OWLOntology ontology = null;	
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntology(MANUSQUARE_ONTOLOGY_IRI);			

		List<BPSupplier> supplierData = BPSupplierData.createTestSupplierData(query, TESTING, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);

		System.out.println("There are " + supplierData.size() + " suppliers.");

		for (BPSupplier bps : supplierData) {

			System.out.println(bps.toString());
		}
	}


	public static String fullBPQuery () {

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

		strQuery += "SELECT DISTINCT ?supplierId ?supplierName ?wsProfileId ?byProductName ?byProductSupplyType ?byProductMinParticipants ?byProductMaxParticipants ?purchasingGroupAbilitation ?certificationType ?byProductQuantity ?byProductMinQuantity ?byProductUOM ?attributeType (str(?uom) as ?uomStr) ?attributeValue ?materialType \n";

		strQuery += "WHERE { \n";

		strQuery += "?wsProfileId core:hasSupplier ?supplierId . \n";
		strQuery += "?supplierId core:hasName ?supplierName . \n";
		strQuery += "?wsProfileId core:hasName ?byProductName . \n";
		strQuery += "?wsProfileId core:hasMode ?byProductMode . \n";
		strQuery += "OPTIONAL { ?wsProfileId ind:hasDeadline ?deadline . \n";
		strQuery += "} \n";
		strQuery += "?wsProfileId core:hasMinParticipants ?byProductMinParticipants . \n";
		strQuery += "?wsProfileId core:hasMaxParticipants ?byProductMaxParticipants . \n";
		strQuery += "?wsProfileId core:hasPurchasingGroupAbilitation ?purchasingGroupAbilitation . \n";
		strQuery += "?wsProfileId core:hasStatus ?byProductStatus . \n";
		strQuery += "?wsProfileId core:hasSupplyType ?byProductSupplyType . \n";
		strQuery += "?wsProfileId core:hasQuantity ?byProductQuantity . \n";
		strQuery += "?wsProfileId core:hasMinQuantity ?byProductMinQuantity . \n";
		strQuery += "?wsProfileId core:hasUnitOfMeasureQuantity ?byProductUOM . \n";
		strQuery += "OPTIONAL {?wsProfileId core:hasAttribute ?attribute . \n";
		strQuery += "?attribute rdf:type ?attributeType . \n";
		strQuery += "#GET ATTRIBUTES \n";
		strQuery += "OPTIONAL {?attribute core:hasUnitOfMeasure ?uomInd .  \n";
		strQuery += "?uomInd core:hasName ?uom . \n";
		strQuery += "?attribute core:hasValue ?attributeValue . } \n";
		strQuery += "#GET MATERIALS \n";
		strQuery += "OPTIONAL {?attribute core:hasObjectValue ?attributeMaterialValue . \n";
		strQuery += "?attributeMaterialValue rdf:type ?materialType .  \n";
		strQuery += "FILTER ( ?materialType not in ( owl:NamedIndividual ))  \n";
		strQuery += "}  \n";
		strQuery += "VALUES ?attributeType {ind:AttributeMaterial ind:Appearance}  \n";
		strQuery += "FILTER ( ?attributeType not in ( owl:NamedIndividual ))  \n";
		strQuery += "} \n";

		strQuery += "OPTIONAL {?supplierId core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
		strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n";
		strQuery += "} \n";
		strQuery += "}";

		return strQuery;
	}

	public static void getAllPropertiesResults() {

		Repository repository;

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", AUTHORISATION_TOKEN);
		headers.put("accept", "application/JSON");
		repository = new SPARQLRepository(SPARQL_ENDPOINT);
		repository.initialize();
		((SPARQLRepository) repository).setAdditionalHttpHeaders(headers);

		String strQuery = getAllProperties();


		try (RepositoryConnection conn = repository.getConnection()) {
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, strQuery);

			try (TupleQueryResult result = tupleQuery.evaluate()) {

				while (result.hasNext()) {
					BindingSet solution = result.next();  

					System.out.println("WsProfile individual: " + StringUtilities.stripIRI(solution.getValue("wsProfileInd").stringValue()));
					System.out.println("Property: " + solution.getValue("prop").stringValue());
					System.out.println("Value: " + StringUtilities.stripIRI(solution.getValue("value").stringValue()));

					//System.out.println("\n");

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("The SPARQL query is: ");
		System.out.println(strQuery);

	}

	public static String getAllProperties() {

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

		strQuery += "SELECT DISTINCT ?wsProfileInd ?prop ?value \n";

		strQuery += "WHERE { \n";

		strQuery += "?wsProfileInd a core:WsProfile . \n";
		strQuery += "?wsProfileInd ?prop ?value . \n";


		strQuery += "}\n";
		return strQuery;

	}
}

