
package sparqlquery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.graph.MutableGraph;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edm.Attribute;
import edm.Material;
import edm.Process;
import exceptions.NoAttributeException;
import graph.Graph;
import ontology.OntologyOperations;
import query.CSQuery;
import utilities.StringUtilities;


public class CSSparqlQuery {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {

		String filename = "./files/TESTING_CAPACITY_SHARING/Test-Full.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));

		CSQuery query = CSQuery.createConsumerQuery(filename, onto);

		String test = createSparqlQuery(query, onto);

		System.out.println(test);


	}
	
	

	public static String createSparqlQuery(CSQuery cq, OWLOntology onto) {

		Set<String> materials = new HashSet<String>();
		Set<Attribute> attributes = new HashSet<Attribute>();
		Set<edm.Process> processes = cq.getProcesses();
		
		Set<String> languages = cq.getLanguage();        

		//get the Least Common Subsumer (LCS) of the process concepts included by the consumer
		
		String lcs = getLCS(processes, onto);

		//14.02.2020: Added supplierMaxDistance and map holding location, lat, lon from RFQ JSON
		double supplierMaxDistance = cq.getSupplierMaxDistance();

		Map<String, String> customerLocationInfo = cq.getCustomerLocationInfo();
		double lat = 0, lon = 0;

		//merge attributes and materials in order to create VALUES restriction in SPARQL query
		Set<String> materialsAndAttributes = new HashSet<String>();

		//get the attributes and materials associated with processes included in the consumer query
		for (Process p : processes) {
			if (p.getAttributes() != null) {

				for (Attribute a : p.getAttributes()) {
					if (isSupportedAttribute(a)) {
						attributes.add(a);
						materialsAndAttributes.add(a.getKey());
					}
				}
			}

			if (p.getMaterials() != null) {

				for (String m : p.getMaterials()) {
					materials.add(m);
					materialsAndAttributes.add(m);
				}
			}
		}

		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		strQuery += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";

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
		}

		//TODO: Optimise code to differentiate SPARQL query on attributes and materials. Currently, the query asks for attributes even if there are no
		//attributes specified in the consumer query as long as there are materials (included in materialsAndAttributes). Doesn´t seem to cause any issues of any sort though.
		if (isNullOrEmpty(materialsAndAttributes)) {

			strQuery += "\nSELECT DISTINCT ?processChain ?processType ?supplier ?certificationType \n";

		} else {

			strQuery += "\nSELECT DISTINCT ?processChain ?processType ?supplier ?materialType ?certificationType ?attributeType (str(?uom) as ?uomStr) ?attributeValue \n";

		}

		strQuery += "\nWHERE { \n";

		//get all subclasses of LCS
		strQuery += "\n?processChain core:hasProcess ?process .\n";
		strQuery += "?process rdf:type ?processType .\n";
		strQuery += "?processType rdfs:subClassOf* ind:" + lcs + " .\n";
		strQuery += "?processChain core:hasSupplier ?supplier .\n";

		//get attributes
		if (!isNullOrEmpty (materialsAndAttributes)) {

			strQuery += queryAttributes(materialsAndAttributes);

		}

		//certifications (as before we just include all certifications associated with the relevant suppliers, not considering the certifications required by the consumer at this point,
		//this is taken care of by the matchmaking algo)
		strQuery += "\nOPTIONAL {?supplier core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
		strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n";
		strQuery += "} \n";

		//filter suppliers
		if (supplierMaxDistance != 0) {

			//strQuery += "\nOPTIONAL { \n"; TODO: Check if we need OPTIONAL here, this snippet is only included if the consumer adds a supplierMaxDistance other than '0'.
			strQuery += "\n?supplier geo:asWKT ?location .\n";
			strQuery += "BIND((geof:distance(?location, \"POINT(" + lat + " " + lon + ")\"^^geo:wktLiteral, uom:metre)/1000) as ?distance)\n";
			strQuery += "FILTER (xsd:double(?distance)<" + supplierMaxDistance + " ) \n";
			//strQuery += "} \n";
		}

		if (!isNullOrEmpty(languages)) {

			strQuery += "\nOPTIONAL { \n";
			strQuery += "\n?supplier core:hasAttribute ?languageAttribute . \n";
			strQuery += "?languageAttribute rdf:type ?languageAttributeType . \n";       
			strQuery += "?languageAttribute core:hasValue ?language . \n";
			strQuery += "VALUES ?languageAttributeType {ind:Language} . \n";
			strQuery += "} \n";
			strQuery += "FILTER(?language in (" + StringUtilities.printLanguageSetItems(languages) + ")) \n";

		}

		strQuery += "}";

		System.out.println(strQuery);

		return strQuery;
	}
	

	private static String queryAttributes (Set<String> materialsAndAttributes) {

		String restrictedValues = getRestrictedValues(materialsAndAttributes);

		StringBuilder attributeQuery = new StringBuilder();

		attributeQuery.append("OPTIONAL {?process core:hasAttribute ?attribute . \n");
		attributeQuery.append("?attribute rdf:type ?attributeType . \n");
		
		attributeQuery.append("#GET ATTRIBUTES\n");
		attributeQuery.append("OPTIONAL {?attribute core:hasUnitOfMeasure ?uomInd .} \n");
		attributeQuery.append("OPTIONAL {?uomInd core:hasName ?uom . }\n");
		attributeQuery.append("OPTIONAL {?attribute core:hasValue ?attributeValue . }\n");
		
		attributeQuery.append("#GET MATERIALS\n");
		attributeQuery.append("OPTIONAL {?attribute core:hasObjectValue ?attributeMaterialValue .\n");
		attributeQuery.append("?attributeMaterialValue rdf:type ?materialType . \n");
		attributeQuery.append("FILTER ( ?materialType not in ( owl:NamedIndividual )) \n");
		attributeQuery.append("} \n");
		
		attributeQuery.append("VALUES ?attributeType {" + restrictedValues + "ind:AttributeMaterial} \n"); //these must be retrieved from the consumer query
		attributeQuery.append("FILTER ( ?attributeType not in ( owl:NamedIndividual )) \n");
		attributeQuery.append("} \n");

		return attributeQuery.toString();

	}
	

	private static String getRestrictedValues (Set<String> materialsAndAttributes) {

		StringBuffer values = new StringBuffer();

		for (String s : materialsAndAttributes) {

			values.append("ind:" + s + " ");

		}

		return values.toString();
	}

	private static String getLCS(Set<Process> consumerProcesses, OWLOntology onto) {

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
		MutableGraph<String> ontoGraph = Graph.createGraph(onto);
		Map<String, Integer> ontologyHierarchyMap = Graph.getOntologyHierarchy(onto, ontoGraph);
		

		Map<String, Integer> supersAndDepthsMap = new LinkedHashMap<String, Integer>();
		for (String s : commonSupers) {
			if (ontologyHierarchyMap.containsKey(s)) {
				supersAndDepthsMap.put(s, ontologyHierarchyMap.get(s));
			}
		}		

		Map<String, Integer> sortedOntologyHierarchy = sortDescending(supersAndDepthsMap);
		
		Entry<String, Integer> entry = sortedOntologyHierarchy.entrySet().iterator().next();
		String lcs = entry.getKey();
		
		return lcs;
	}


	public static boolean isSupportedAttribute (Attribute attribute) {

		Set<String> supportedAttributes = new HashSet<String>();
		supportedAttributes.add("Length");
		supportedAttributes.add("Width");
		supportedAttributes.add("Depth");
		supportedAttributes.add("MinFeatureSize");
		supportedAttributes.add("MinLayerThickness");
		supportedAttributes.add("MinKerfWidth");
		supportedAttributes.add("WorkingVolumeX");
		supportedAttributes.add("MinSheetThickness");
		supportedAttributes.add("PartSizeX");
		supportedAttributes.add("PartSizeY");
		supportedAttributes.add("PartSizeZ");
		supportedAttributes.add("MoldSizeX");
		supportedAttributes.add("MoldSizeY");
		supportedAttributes.add("MoldSizeZ");
		supportedAttributes.add("Capacity");
		supportedAttributes.add("WorkingAreaX");
		supportedAttributes.add("WorkingAreaY");
		supportedAttributes.add("WorkingAreaZ");
		supportedAttributes.add("AspectRatio");
		supportedAttributes.add("Tolerance");
		supportedAttributes.add("SurfaceFinishing");
		supportedAttributes.add("MaxWallThickness");
		supportedAttributes.add("SurfaceFinishing");
		supportedAttributes.add("MaxPartSizeX");
		supportedAttributes.add("MaxPartSizeY");
		supportedAttributes.add("MaxPartSizeZ");
		supportedAttributes.add("MaxKerfWidth");
		supportedAttributes.add("MaxSheetThickness");
		supportedAttributes.add("Axis");
		supportedAttributes.add("CuttingSpeed");

		if (supportedAttributes.contains(attribute.getKey())) {
			return true;
		} else {
			return false;
		}


	}


	/**
	 * Get the elements that are common from a variable number of sets
	 *
	 * @param collections input sets
	 * @return set of common elements in the input sets
	 * Feb 27, 2020
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

	/**
	 * Checks a collection for null or empty values TODO: this resolves earlier "dead code" warnings and can probably be used elsewhere (+ used as generic utility method and put elsewhere)
	 * @param c
	 * @return
       May 4, 2020
	 */
	public static boolean isNullOrEmpty( final Collection< ? > c ) {
		return c == null || c.isEmpty();
	}



}