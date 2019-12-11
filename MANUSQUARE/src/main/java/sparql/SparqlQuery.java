package sparql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SparqlQuery {
	
	/**
	 * This is a query that simplifies the process-to-material relation using attributes. This query is in line with how suppliers registers resources in MVP (specified by SUPSI)
	 * @param processes
	 * @return
	   Nov 18, 2019
	 */
	public static String createQueryMVP(List<String> processes) {
		
		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "SELECT distinct ?processChain ?supplier ?processType ?materialType ?certificationType \n";
		strQuery += "WHERE { \n";

		strQuery += "?process rdf:type ?processType .\n";
		strQuery += "?processType rdfs:subClassOf ?superProcessType .\n";
		strQuery += "?processChain core:hasProcess ?process .\n";		

		strQuery += "?processChain core:hasSupplier ?supplier .\n";

		strQuery += "optional {?supplier core:hasId ?supplierId .  }\n";
		strQuery += "optional {?supplier core:hasCertification ?certification . }\n";
		
		strQuery += "optional { ?process core:hasAttribute ?attribute . } \n";
		strQuery += "optional { ?attribute core:hasValue ?material . } \n";		
		strQuery += "optional { ?material rdf:type ?materialType . } \n";		
		strQuery += "optional { ?certification rdf:type ?certificationType . } \n";

//"FILTER (?superProcessType in ( " + filteredProcesses + " )\n || ?processType in ( " + filteredProcesses + " ))";

		strQuery += createSubsumptionFilter(processes);

		strQuery += "}";
		
		return strQuery;
	}
	
	/**
	 * This is the "original" query that follows the specification of the Semantic Infrastructure in D2.4
	 * @param processes set of processes included by consumer in RFQ
	 * @return a sparql query used for retrieving data from the Semantic Infrastructure
	   Nov 18, 2019
	 */
	public static String createQuery(List<String> processes) {
		
		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		strQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		strQuery += "PREFIX core: <http://manusquare.project.eu/core-manusquare#> \n";
		strQuery += "PREFIX ind: <http://manusquare.project.eu/industrial-manusquare#> \n";
		strQuery += "SELECT distinct ?processChain ?supplierId ?processType ?materialType ?certificationType \n";
		strQuery += "WHERE { \n";

		strQuery += "?process rdf:type ?processType .\n";
		strQuery += "?processType rdfs:subClassOf ?superProcessType .\n";
		strQuery += "?processChain core:hasProcess ?process .\n";		
		strQuery += "?process core:hasInput ?material . \n";                   
		strQuery += "?processChain core:hasSupplier ?supplier .\n";
		strQuery += "?supplier core:hasId ?supplierId . \n";
		strQuery += "?supplier core:hasCertification ?certification . \n";
		strQuery += "optional { ?material rdf:type ?materialType . } \n";
		strQuery += "optional { ?certification rdf:type ?certificationType . } \n";

		strQuery += createSubsumptionFilter(processes);

		strQuery += "}";
		
		return strQuery;
	}
	
	
	/**
	 * Creates a SPARQL filter that retrieves all process chains including either process concepts included in the processes list (parameter) or process concepts that are subsumed by the 
	 * processes included in the processes list.
	 * @param processes
	 * @return
	   Oct 24, 2019
	 */
	private static String createSubsumptionFilter (List<String> processes) {
		
		if (processes.size() > 1) {

			Set<String> prefixedProcesses = new HashSet<String>();

			for (String s : processes) {
				prefixedProcesses.add("ind:" + s);
			}

			String filteredProcesses = String.join(", ", prefixedProcesses);

			return "FILTER (?superProcessType in ( " + filteredProcesses + " )\n || ?processType in ( " + filteredProcesses + " ))";

		} else {

			return "FILTER (?superProcessType in ( ind:" + processes.get(0) + " )\n || ?processType in ( ind:" + processes.get(0) + " ))";

		}

	}

}
