package utilities;

import java.io.IOException;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edm.Process;
import supplier.CSSupplier;
import supplierdata.CSSupplierData;

public class TestSI_CSSuppliers {
	
	//configuration of the MANUSQUARE Semantic Infrastructure
	static String SPARQL_ENDPOINT = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0"; 
	static String AUTHORISATION_TOKEN = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare/ontology.owl");
	
	public static void main(String[] args) throws IOException {

		
		String query = csQuery();
		boolean testing = false;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		String sparql_endpoint_by_env = System.getenv("ONTOLOGY_ADDRESS");

		if (sparql_endpoint_by_env != null) {	
			SPARQL_ENDPOINT = sparql_endpoint_by_env;	
		}

		if (System.getenv("ONTOLOGY_KEY") != null) {		
			AUTHORISATION_TOKEN = System.getenv("ONTOLOGY_KEY");
		}

		OWLOntology ontology = null;

		try {			
			ontology = manager.loadOntology(MANUSQUARE_ONTOLOGY_IRI);			
		} catch (OWLOntologyCreationException e) {
			System.err.println("It seems the MANUSQUARE ontology is not available from " + MANUSQUARE_ONTOLOGY_IRI.toString() + "\n");
			e.printStackTrace();
		}
		
		List<CSSupplier> supplierData = CSSupplierData.createTestSupplierData(query, testing, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);
		
		for (CSSupplier cs : supplierData) {
			System.out.println("\nSupplier ID: " + cs.getSupplierId());
			System.out.println("Supplier Name: " + cs.getSupplierName());
			System.out.println("Certifications: " + cs.getCertifications());
			
			for (Process p : cs.getProcesses()) {
				
				System.out.println("Process name: " + p.getName());
				System.out.println("Materials: " + p.getMaterials());
				System.out.println("Attributes: " + p.getAttributes());
								
			}
			
		}
		
	}
	
	public static String csQuery () {

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
		strQuery += "} \n";

		strQuery +="OPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
		strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n"; 
		strQuery += "}\n";
		strQuery += "}\n";

		return strQuery;

	}
	

}
