package utilities;

import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
		getAllByProductResults();
	

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
				strQuery += "?wsProfileId ind:hasMode ?byProductMode . \n";
				strQuery += "OPTIONAL { ?wsProfileId ind:hasDeadline ?deadline . \n";
				strQuery += "} \n";
				strQuery += "?wsProfileId ind:hasMinParticipants ?byProductMinParticipants . \n";
				strQuery += "?wsProfileId ind:hasMaxParticipants ?byProductMaxParticipants . \n";
				strQuery += "?wsProfileId ind:hasPurchasingGroupAbilitation ?purchasingGroupAbilitation . \n";
				strQuery += "?wsProfileId ind:hasStatus ?byProductStatus . \n";
				strQuery += "?wsProfileId ind:hasSupplyType ?byProductSupplyType . \n";
				strQuery += "?wsProfileId core:hasQuantity ?byProductQuantity . \n";
				strQuery += "?wsProfileId ind:hasMinQuantity ?byProductMinQuantity . \n";
				strQuery += "?wsProfileId ind:hasUnitOfMeasureQuantity ?byProductUOM . \n";
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

			try (TupleQueryResult result = tupleQuery.evaluate()) {
				while (result.hasNext()) {
					BindingSet solution = result.next();  

					System.out.println("WSProfile ID: " + StringUtilities.stripIRI(solution.getValue("wsProfileId").stringValue()));
					System.out.println("By-product Name: " + StringUtilities.stripIRI(solution.getValue("byProductName").stringValue()));
					System.out.println("Supplier ID: " + StringUtilities.stripIRI(solution.getValue("supplierId").stringValue()));
					System.out.println("Supplier Name: " + StringUtilities.stripIRI(solution.getValue("supplierName").stringValue()));
					System.out.println("\n");

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
	}
	
	public static String getAllByProducts () {
		
		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"; 
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#>\n"; 
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";


		strQuery += "SELECT DISTINCT ?wsProfileId ?byProductName ?supplierId ?supplierName  \n";
		

		strQuery += "WHERE { \n";


				strQuery += "?wsProfileId core:hasName ?byProductName . \n";
				strQuery += "?wsProfileId core:hasSupplier ?supplierId . \n";
				strQuery += "?supplierId core:hasName ?supplierName . \n";
				strQuery += "?wsProfileId ind:hasPurchasingGroupAbilitation ?purchasingGroupAbilitation . \n";
				strQuery += "?wsProfileId ind:hasStatus ?byProductStatus . \n";
				strQuery += "?wsProfileId ind:hasSupplyType ?byProductSupplyType . \n";
				
				strQuery += "}";

				return strQuery;
		
	}
	}

