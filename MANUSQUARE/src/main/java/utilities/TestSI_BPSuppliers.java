package utilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
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

import com.ontotext.trree.big.collections.Iterator;

import edm.ByProduct;
import sparqlconnection.SparqlConnection;
import supplier.BPSupplier;
import supplierdata.BPSupplierData;

public class TestSI_BPSuppliers {

	static String SPARQL_ENDPOINT = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0"; 
	static String AUTHORISATION_TOKEN = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare/ontology.owl");	
	static final boolean TESTING = false;

	public static void main(String[] args) throws IOException, OWLOntologyCreationException {

		fullBPQueryResults();
		//getAllByProductResults();
		//getAllPropertiesResults(); 


	}

	public static void fullBPQueryResults() throws IOException, OWLOntologyCreationException {

		String query = fullBPQuery();

		OWLOntology ontology = null;	
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntology(MANUSQUARE_ONTOLOGY_IRI);			

		List<BPSupplier> supplierData = BPSupplierData.createTestSupplierData(query, TESTING, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);

		System.out.println("There are " + supplierData.size() + " suppliers.");

		for (BPSupplier bps : supplierData) {
			System.out.println("\nSupplier ID: " + bps.getSupplierId());
			System.out.println("Supplier Name: " + bps.getSupplierName());

			for (ByProduct bp : bps.getByProducts()) {
				System.out.println("\nBy-product ID: " + bp.getId());
				System.out.println("By-product Materials: " + bp.getMaterials());
				System.out.println("By-product Appearances: " + bp.getAppearances());
				System.out.println("By-product Supply Type: " + bp.getSupplyType());
				System.out.println("By-product Name: " + bp.getName());
				System.out.println("By-product Quantity: " + bp.getQuantity());
				System.out.println("By-product Min Quantity: " + bp.getMinQuantity());
				System.out.println("By-product Purchasing Group Abilitation: " + bp.getPurchasingGroupAbilitation());
				System.out.println("By-product Min Participants: " + bp.getMinParticipants());
				System.out.println("By-product Max Participants: " + bp.getMaxParticipants());
				//System.out.println("By-product Attributes: " + bp.getAttributeWeightMap());

			}
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
		strQuery += "OPTIONAL { ?wsProfileId core:hasStatus ?byProductStatus . } \n";
//		strQuery += "?wsProfileId ind:hasSupplyType ?byProductSupplyType . \n";
		strQuery += "?wsProfileId core:hasCreationDate ?creationDate . \n";
		//strQuery += "?wsProfileId ind:hasMode ?mode . \n";

		strQuery += "}";

		System.out.println(strQuery);

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

