package sparqlquery;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import query.IMQuery;
import utilities.StringUtilities;


public class IMSparqlQuery {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {

		String filename = "./files/InnovationManagementJSON.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));

		IMQuery query = IMQuery.createQuery(filename, onto);

		String test = createSparqlQuery_IM(query, onto);

		System.out.println(test);


	}

	public static String createSparqlQuery_IM (IMQuery imq, OWLOntology onto) {

		Set<String> languages = imq.getLanguages();
		

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";


		strQuery += "\nSELECT DISTINCT ?improfile ?supplier ?supplierName ?innovationPhaseType ?skillType ?innovationSectorType ?innovationTypeType ?certificationType\n";
		
		strQuery += "\nWHERE { \n";
		
		strQuery += "\n?improfile core:hasSupplier ?supplier . \n";
		strQuery += "\n?supplier core:hasName ?supplierName . \n";
		strQuery += "\n?improfile core:hasInnovationPhase ?innovationPhase . \n";
		strQuery += "\n?innovationPhase rdf:type ?innovationPhaseType . \n";
		strQuery += "\n?improfile core:hasInnovationType ?innovationType . \n";
		strQuery += "\n?innovationType rdf:type ?innovationTypeType . \n";
		strQuery += "\n?improfile core:hasSkill ?skill . \n";
		strQuery += "\n?skill rdf:type ?skillType . \n";
		strQuery += "\n?improfile core:hasSector ?innovationSector . \n";
		strQuery += "\n?innovationSector rdf:type ?innovationSectorType . \n";
		
		strQuery += "\nFILTER ( ?innovationPhaseType not in ( owl:NamedIndividual ) && ?skillType not in ( owl:NamedIndividual ) && ?innovationSectorType not in ( owl:NamedIndividual ) && ?innovationTypeType not in ( owl:NamedIndividual ))\n"; 
	    	    		

		//certifications (as before we just include all certifications associated with the relevant suppliers, not considering the certifications required by the consumer at this point,
		//this is taken care of by the matchmaking algo)
		strQuery += "\nOPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
		strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n";
		strQuery += "} \n";


		if (!isNullOrEmpty(languages)) {

			//strQuery += "\nOPTIONAL { \n";
			strQuery += "\n?supplier core:hasAttribute ?languageAttribute . \n";
			strQuery += "?languageAttribute rdf:type ?languageAttributeType . \n";       
			strQuery += "?languageAttribute core:hasValue ?language . \n";
			strQuery += "VALUES ?languageAttributeType {ind:Language} . \n";
			//strQuery += "} \n";
			strQuery += "FILTER(?language in (" + StringUtilities.printLanguageSetItems(languages) + ")) \n";

		}

		strQuery += "}";

		System.out.println(strQuery);

		return strQuery;


	}


	/**
	 * Checks a collection for null or empty values 
	 * @param c
	 * @return
       May 4, 2020
	 */
	public static boolean isNullOrEmpty( final Collection< ? > c ) {
		return c == null || c.isEmpty();
	}



}
