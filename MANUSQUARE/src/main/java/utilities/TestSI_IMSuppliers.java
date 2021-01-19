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
import supplier.IMSupplier;
import supplierdata.CSSupplierData;
import supplierdata.IMSupplierData;

public class TestSI_IMSuppliers {
	
	//configuration of the MANUSQUARE Semantic Infrastructure
	static String SPARQL_ENDPOINT = "http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare?infer=false&limit=0&offset=0"; 
	static String AUTHORISATION_TOKEN = "7777e8ed0d5eb1b63ab1815a56e31ff1";
	//if the MANUSQUARE ontology is fetched from url
	static final IRI MANUSQUARE_ONTOLOGY_IRI = IRI.create("http://manusquaredev.holonix.biz:8080/semantic-registry/repository/manusquare/ontology.owl");
	
	public static void main(String[] args) throws IOException {

		
		String query = imQuery();
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
		
		List<IMSupplier> supplierData = IMSupplierData.createTestSupplierData(query, testing, ontology, SPARQL_ENDPOINT, AUTHORISATION_TOKEN);
		
		for (IMSupplier im : supplierData) {
			System.out.println("\nSupplier ID: " + im.getSupplierId());
			System.out.println("Supplier Name: " + im.getSupplierName());
			System.out.println("Certifications: " + im.getCertifications());
			
			System.out.println("Innovation Phases: " + im.getInnovationPhases());
			System.out.println("Innovation Types: " + im.getInnovationTypes());
			System.out.println("Skills: " + im.getSkills());
			System.out.println("Sectors: " + im.getSectors());
			
		}
		
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

		strQuery += "}\n";

		return strQuery;

	}

}
