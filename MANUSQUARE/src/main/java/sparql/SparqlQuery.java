package sparql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Iterables;
import com.google.common.graph.MutableGraph;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edm.Attribute;
import edm.Material;
import edm.Process;
import exceptions.NoAttributeException;
import graph.SimpleGraph;
import owlprocessing.OntologyOperations;
import query.ConsumerQuery;
import supplierdata.Supplier;

public class SparqlQuery {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {

		String filename = "./files/rfq-attributes-custInfo.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));

		ConsumerQuery query = ConsumerQuery.createConsumerQuery (filename, onto);

		String test = createSparqlQuery(query, onto);
		
		System.out.println(test);


	}

	public static String createSparqlQuery (ConsumerQuery cq, OWLOntology onto) throws OWLOntologyCreationException {

		Set<Material> materials = new HashSet<Material>();
		Set<Attribute> attributes = new HashSet<Attribute>();
		Set<Process> processes = cq.getProcesses();

		//27.02.2020: get the Least Common Subsumer (LCS) of the process concepts included by the consumer
		//we need the ontology in order to fetch the superclasses
		String lcs = getLCS(processes, onto);

		//14.02.2020: Added supplierMaxDistance and map holding location, lat, lon from RFQ JSON
		double supplierMaxDistance = cq.getSupplierMaxDistance();

		Map<String, String> customerLocationInfo = cq.getCustomerLocationInfo();
		double lat = 0, lon = 0;

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

		//if supplier max distance is no 0, we should consider geo distance
		if (supplierMaxDistance != 0) {
			
			if (customerLocationInfo != null) {
				lat = Double.valueOf(customerLocationInfo.get("lat"));
				lon = Double.valueOf(customerLocationInfo.get("lon"));
			} else {
				throw new NoAttributeException("No latitude or longitude values are included in the JSON file!");
			}
			
			//include necessary prefixes if supplier distance is to be included
			strQuery += "PREFIX geo: <http://www.opengis.net/ont/geosparql#> \n";
			strQuery += "PREFIX geof: <http://www.opengis.net/def/function/geosparql/> \n";
			strQuery += "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/> \n";
			strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
			strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
		}

		//for each attribute key we add a variable
		//FIXME: Dead code warning from Eclipse?
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

		//get all subclasses of LCS
		strQuery += "?processChain core:hasProcess ?process .\n";
		strQuery += "?process rdf:type ?processType .\n";
		strQuery += "?processType rdfs:subClassOf* ind:" + lcs + " .\n";
		strQuery += "?processChain core:hasSupplier ?supplier .\n";

		//get attributes
		strQuery += queryAttributes(attributes);

		strQuery += "\nOPTIONAL { ?process core:hasAttribute ?materialAttribute . \n";
		strQuery += "?materialAttribute rdf:type ?materialAttributeType . \n";
		strQuery += "VALUES ?materialAttributeType {ind:AttributeMaterial} . \n";
		strQuery += "?materialAttribute ind:hasValue ?materialAttributeValue . \n";
		strQuery += "?materialAttributeValue rdf:type ?materialType . }\n";
		

		//certifications (as before we just include all certifications associated with the relevant suppliers, not considering the certifications required by the consumer at this point,
		//this is taken care of by the matchmaking algo)
		strQuery += "\nOPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType .} \n";

		//filter suppliers
		if (supplierMaxDistance != 0) {

			//include OPTIONAL
			//strQuery += "\nOPTIONAL {\n"; 	
			strQuery += "\n?supplier geo:asWKT ?location .\n"; 	
			strQuery += "BIND((geof:distance(?location, \"POINT("+lat+" "+lon+ ")\"^^geo:wktLiteral, uom:metre)/1000) as ?distance)\n"; 	
			strQuery += "FILTER (xsd:double(?distance)<" + supplierMaxDistance + " ) \n"; 	
			
			//INCLUDE OPTIONAL
			//strQuery += "}\n"; 
		}

		strQuery += "\nFILTER ( ?certificationType not in ( owl:NamedIndividual ))";
		strQuery += "\n}";

		System.out.println(strQuery);

		return strQuery;
	}

	private static String getLCS (Set<Process> consumerProcesses, OWLOntology onto) throws OWLOntologyCreationException {

		//27.02.2020: Find the LCS of an arbitrary set of process concepts
		List<List<String>> supersList = new LinkedList<List<String>>();

		for (Process p : consumerProcesses) {
			supersList.add(OntologyOperations.getEntitySuperclassesFragmentsAsList(onto, OntologyOperations.getClass(p.getName(), onto)));
		}

		//collect all super-lists into a common list
		List<List<String>> lists = new ArrayList<List<String>>();
		for (List<String> l : supersList) {
			lists.add(l);
		}

		Set<String> commonSupers = getCommonElements(lists);

		//get the depth of the superclasses and let the superclass with highest depth be the LCS
		MutableGraph<String> ontoGraph = SimpleGraph.createGraph (onto); 
		Map<String, Integer> ontologyHierarchyMap = SimpleGraph.getOntologyHierarchy (onto, ontoGraph);

		Map<String, Integer> supersAndDepthsMap = new LinkedHashMap<String, Integer>();
		for (String s : commonSupers) {
			if (ontologyHierarchyMap.containsKey(s)) {
				supersAndDepthsMap.put(s, ontologyHierarchyMap.get(s));
			}
		}

		Map<String, Integer> sortedOntologyHierarchy = sortDescending(supersAndDepthsMap);

		Map.Entry<String,Integer> entry = sortedOntologyHierarchy.entrySet().iterator().next();
		String lcs = entry.getKey();

		return lcs;
	}


	private static String queryAttributes (Set<Attribute> attributes) {
		
		StringBuilder attributeQuery = new StringBuilder();

		String attribute, attributeType, attributeClass, attributeValue, attributeVariable, updatedAttributeValue = null;

		//retrieved from Attribute object
		String attKey, attValue = null;

		Map<String, String> attributeConditions = mapAttributeConditions(attributes);
		
		int attCounter = 0;

		for (Attribute att : attributes) {
			
			attKey = att.getKey();
			attValue = att.getValue();						
			attribute = "?" + att.getKey().toLowerCase() + "Attribute";
			attributeType = "?" + att.getKey().toLowerCase() + "AttributeType";
			attributeClass = "ind:" + att.getKey();
			attributeValue = "?" + att.getKey().toLowerCase() + "Value";
			attributeVariable = "?" + att.getKey() + "Attr";
			updatedAttributeValue = "?updated" + attKey + "Value" + attCounter;

			attributeQuery.append("\nOPTIONAL {?process core:hasAttribute " + attribute + " . \n");
			attributeQuery.append(attribute + " rdf:type " + attributeType + " . \n");
			
			//if unit of measurement is included
			if (att.getunitOfMeasurement() != null) {
			attributeQuery.append(attribute + " core:hasUnitOfMeasure " + "?uomInd .\n");
			attributeQuery.append("?uomInd" + " core:hasName " + "?uom .\n");
			
			attributeQuery.append(" VALUES " + attributeType + " {"+ attributeClass + "} . \n");
			attributeQuery.append(attribute + " core:hasValue " + attributeValue + " . \n");

			attributeQuery.append("} \n");
			
			attributeQuery.append("BIND ( \n");
			attributeQuery.append("IF (bound(?uom) && ?uom = \"m\", " + attributeValue + " * 1000, \n");
			attributeQuery.append("IF (bound(?uom) && ?uom = \"dm\", " + attributeValue + " * 100,\n");
			attributeQuery.append("IF (bound(?uom) && ?uom = \"cm\", " + attributeValue + " * 10,\n");
			attributeQuery.append(attributeValue + "))) as " + updatedAttributeValue + ") \n");
			
			attributeQuery.append("\n");
			
			attributeQuery.append("BIND ( \n");
			attributeQuery.append("IF (bound("+ updatedAttributeValue + ") && " + updatedAttributeValue + " " + attributeConditions.get(attKey) + " " + attValue + ", " + "\"Y\"" + ", \n");
			attributeQuery.append("IF (bound("+ updatedAttributeValue + ") && " + updatedAttributeValue + " " + getOpposite (attributeConditions.get(attKey)) + " " + attValue + ", " + "\"N\"" + ", \n");
			attributeQuery.append("\"O\"))" + " as " + attributeVariable + ") \n");
			
			attCounter++;
			
		} else {
			
			attributeQuery.append(" VALUES " + attributeType + " {"+ attributeClass + "} . \n");
			attributeQuery.append(attribute + " core:hasValue " + attributeValue + " . \n");
			
			attributeQuery.append("} \n");
			
			attributeQuery.append("BIND ( \n");
			attributeQuery.append("IF (bound(" + attributeValue + ")" + " && " + attributeValue + " " + attributeConditions.get(attKey) + " " + attValue + ", " + "\"Y\"" + ", \n");
			attributeQuery.append("IF (bound(" + attributeValue + ")" + " && " + attributeValue + " " + getOpposite (attributeConditions.get(attKey)) + " " + attValue + ", " + "\"N\"" + ", \n");
			attributeQuery.append("\"O\"))" + " as " + attributeVariable + ") \n");
			
			
		}
			
		}

		return attributeQuery.toString();

	}
	


	/**
	 * Finds the relevant conditions ('<=', '>=' or '=') for a given sample of attributes.
	 * @param attributes attribute keys and values
	 * @return a map of attribute (key) and the conditions (value) used for determining whether they satisfy attribute reqs from the consumer.
	   Feb 8, 2020
	 */
	private static Map<String, String> mapAttributeConditions (Set<Attribute> attributes) {

		Map<String, String> attributeConditions = new HashMap<String, String>();
		for (Attribute a : attributes) {
			if (a.getKey().equals("Length") || a.getKey().equals("Width") || a.getKey().equals("Depth") || a.getKey().equals("MinFeatureSize")
					|| a.getKey().equals("MinLayerThickness") || a.getKey().equals("MinKerfWidth") || a.getKey().equals("WorkingVolumeX")
					|| a.getKey().equals("MinSheetThickness") || a.getKey().equals("PartSizeX") || a.getKey().equals("PartSizeY") || a.getKey().equals("PartSizeZ")
					|| a.getKey().equals("MoldSizeX") || a.getKey().equals("MoldSizeY") || a.getKey().equals("MoldSizeZ") || a.getKey().equals("Capacity")
					|| a.getKey().equals("WorkingAreaX") || a.getKey().equals("WorkingAreaY") || a.getKey().equals("WorkingAreaZ")
					|| a.getKey().equals("AspectRatio")) {
				attributeConditions.put(a.getKey(), ">=");
			} else if (a.getKey().equals("Tolerance") || a.getKey().equals("SurfaceFinishing") || a.getKey().equals("MaxWallThickness")
					|| a.getKey().equals("MaxPartSizeX") || a.getKey().equals("MaxPartSizeY") || a.getKey().equals("MaxPartSizeZ")
					|| a.getKey().equals("MaxKerfWidth") || a.getKey().equals("MaxSheetThickness")) {
				attributeConditions.put(a.getKey(), "<=");
			} else if (a.getKey().equals("Axis") || a.getKey().equals("CuttingSpeed")) {
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
	private static String getOpposite (String inputCondition) {
		
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

	/**
	 * Get the elements that are common from a variable number of sets
	 * @param collections input sets
	 * @return set of common elements in the input sets
	   Feb 27, 2020
	 */
	private static <T> Set<T> getCommonElements(Collection<? extends Collection<T>> collections) {

		Set<T> common = new LinkedHashSet<T>();
		if (!collections.isEmpty()) {
			Iterator<? extends Collection<T>> iterator = collections.iterator();
			common.addAll(iterator.next());
			while (iterator.hasNext()) {
				common.retainAll(iterator.next());
			}
		}
		return common;
	}


	/**
	 * Sorts a map based on similarity scores (values in the map)
	 *
	 * @param map the input map to be sorted
	 * @return map with sorted values
	 * May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				if (compare == 0) return 1;
				else return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);

		sortedByValues.putAll(map);

		return sortedByValues;
	}


}
