package sparql;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edm.Attribute;
import edm.Material;
import edm.Process;
import query.ConsumerQuery;

public class SparqlQuery {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException, OWLOntologyCreationException {

		String filename = "./MANUSQUARE/files/rfq-attributes.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));

		ConsumerQuery query = ConsumerQuery.createConsumerQuery (filename, onto);

		String test = dynamicAttributeQuery(query);


	}

	public static String dynamicAttributeQuery(ConsumerQuery cq) {

		Set<Material> materials = new HashSet<Material>();
		Set<Attribute> attributes = new HashSet<Attribute>();
		Set<Process> processes = cq.getProcesses();

		//get the attributes and materials associated with processes included in the consumer query
		for (Process p : processes) {
			if (p.getAttributes() != null) {
				attributes.addAll(p.getAttributes());
			}

			if (p.getMaterials() != null) {
				materials.addAll(p.getMaterials());
			}
		}

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";

		//for each attribute key we add a variable
		//FIXME: Dead code warning from Eclips?
		StringBuilder attributeQuery = new StringBuilder();

		if (attributes == null) {
			
			strQuery += "SELECT DISTINCT ?processChain ?processType ?supplier ?materialType ?certificationType \n";
		}

		else {

			for (Attribute att : attributes) {
				attributeQuery.append(" ?" + att.getKey() + "Attr");
			}

			strQuery += "SELECT DISTINCT ?processChain ?processType ?supplier ?materialType ?certificationType" + attributeQuery.toString() + " \n";
		}
		strQuery += "WHERE { \n";

		//get all subclasses of MfgProcess 
		strQuery += "?processChain core:hasProcess ?process .\n";
		strQuery += "?process rdf:type ?processType .\n";
		strQuery += "?processType rdfs:subClassOf* ind:MfgProcess .\n";
		strQuery += "?processChain core:hasSupplier ?supplier .\n";

		//get attributes
		strQuery += queryAttributes(attributes);

		//materials option 1: we use the object property hasAttribute to retrieve materials relevant for our processes
		//strQuery += "OPTIONAL { ?process core:hasAttribute ?attribute . }\n";
		//strQuery += "OPTIONAL { ?attribute core:hasValue ?material . ?material rdf:type ?materialType . }\n";

		//materials option 2: we use the object property hasMaterial to retrieve materials relevant for our processes
		strQuery += "\nOPTIONAL { ?process core:hasMaterial ?materialAttribute . \n";
		strQuery += "?materialAttribute ind:hasValue ?materialAttributeValue . \n";
		strQuery += "?materialAttributeValue rdf:type ?materialType . }\n";

		//certifications (as before we just include all certifications associated with the relevant suppliers, not considering the certifications required by the consumer at this point,
		//this is taken care of by the matchmaking algo)
		strQuery += "\nOPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType .} \n";


		strQuery += "\nFILTER ( ?certificationType not in ( owl:NamedIndividual ))";
		strQuery += "\n}";

		//System.out.println(strQuery);

		return strQuery;
	}


	private static String queryAttributes (Set<Attribute> attributes) {
		StringBuilder attributeQuery = new StringBuilder();

		String attribute, attributeType, attributeClass, attributeValue, attributeVariable = null;

		//retrieved from Attribute object
		String attKey, attValue = null;

		Map<String, String> attributeConditions = mapAttributeConditions(attributes);

		for (Attribute att : attributes) {

			attKey = att.getKey();
			attValue = att.getValue();
			attribute = "?" + att.getKey().toLowerCase() + "Attribute";
			attributeType = "?" + att.getKey().toLowerCase() + "AttributeType";
			attributeClass = "ind:" + att.getKey();
			attributeValue = "?" + att.getKey().toLowerCase() + "Value";
			attributeVariable = "?" + att.getKey() + "Attr";

			attributeQuery.append("\nOPTIONAL {?process core:hasAttribute " + attribute + " . \n");
			attributeQuery.append(attribute + " rdf:type " + attributeType + " . \n");
			attributeQuery.append(" VALUES " + attributeType + " {"+ attributeClass + "} . \n");
			attributeQuery.append(attribute + " core:hasValue " + attributeValue + " . \n");
			attributeQuery.append("} \n");
			attributeQuery.append("BIND ( \n");
			attributeQuery.append("IF (bound(" + attributeValue + ")" + " && " + attributeValue + " " + attributeConditions.get(attKey) + " " + attValue + ", " + "\"Y\"" + ", \n");
			attributeQuery.append("IF (bound(" + attributeValue + ")" + " && " + attributeValue + " " + getOpposite (attributeConditions.get(attKey)) + " " + attValue + ", " + "\"N\"" + ", \n");
			attributeQuery.append("\"O\"))" + " as " + attributeVariable + ") \n");
		}

		return attributeQuery.toString();

	}

	/**
	 * Finds the relevant conditions ('<=', '>=' or '=') for a given sample of attributes.
	 * @param attributes attribute keys and values
	 * @return a map of attribute (key) and the conditions (value) used for determining whether they satisfy attribute reqs from the consumer.
	   Feb 8, 2020
	 */
	public static Map<String, String> mapAttributeConditions (Set<Attribute> attributes) {

		Map<String, String> attributeConditions = new HashMap<String, String>();
		for (Attribute a : attributes) {
			if (a.getKey().equals("Length") || a.getKey().equals("Width") || a.getKey().equals("Depth") || a.getKey().equals("Min Feature Size")
					|| a.getKey().equals("Min Layer Thickness") || a.getKey().equals("Min Kerf Width")) {
				attributeConditions.put(a.getKey(), ">=");
			} else if (a.getKey().equals("Tolerance") || a.getKey().equals("Surface Finishing") || a.getKey().equals("Max Wall Thickness")
					|| a.getKey().equals("Max Part Size X") || a.getKey().equals("Max Part Size Y") || a.getKey().equals("Max Part Size Z")
					|| a.getKey().equals("Max Kerf Width")) {
				attributeConditions.put(a.getKey(), "<=");
			} else if (a.getKey().equals("Axis") || a.getKey().equals("Cutting Speed")) {
				attributeConditions.put(a.getKey(), "=");
			}
		}

		return attributeConditions;

	}

	/**
	 * Returns the opposite condition given an input condition (e.g. the opposite of '>=' would be '<' in the SPARQL query)
	 * @param inputCondition
	 * @return
	   Feb 8, 2020
	 */
	public static String getOpposite (String inputCondition) {
		String opposite = null;

		if (inputCondition.equals("<=")) {
			opposite = ">";
		} else if (inputCondition.equals(">=")) {
			opposite = "<";
		} else { //if the input condition is '='
			opposite = "!=";
		}

		return opposite;
	}


}
