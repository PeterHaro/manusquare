package sparqlquery;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import edm.ByProduct;
import exceptions.NoAttributeException;
import json.ByProductSharingRequest.ByProductAttributes;
import query.BPQuery;
import utilities.StringUtilities;


public class SparqlQuery_BP {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {

		String filename = "./files/TESTING_BYPRODUCT_SHARING/Radostin_17122020/Radostin_17122020_2.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));

		BPQuery query = BPQuery.createByProductQuery(filename, onto);
		
		System.out.println("min number of participants: "+ query.getMinNumberOfParticipants());
		System.out.println("min number of participants: "+ query.getMaxNumberOfParticipants());

		String test = createSparqlQuery(query, onto);

		System.out.println(test);


	}
	
	public static String createSparqlQuery(BPQuery bpq, OWLOntology onto) {

		Set<ByProductAttributes> attributes = new HashSet<ByProductAttributes>();
		Set<edm.ByProduct> byProducts = bpq.getByProducts();
		String mode = bpq.getMode();
				
		Set<String> languages = bpq.getLanguage();        

		Set<ByProduct> byProductSet = bpq.getByProducts();
		
		for (ByProduct bp : byProductSet ) {
			bp.getSupplyType();
		}
		

		double supplierMaxDistance = bpq.getSupplierMaxDistance();

		Map<String, String> customerLocationInfo = bpq.getCustomerLocationInfo();
		double lat = 0, lon = 0;

		//merge attributes and materials in order to create VALUES restriction in SPARQL query
		Set<String> materialsAndAttributes = new HashSet<String>();

		//get the attributes and materials associated with processes included in the consumer query
		for (ByProduct bp : byProducts) {
			if (bp.getAttributes() != null) {

				for (ByProductAttributes a : bp.getAttributes()) {
					if (Attribute.isSupportedAttribute(a.getAttributeKey())) {
						attributes.add(a);
						materialsAndAttributes.add(a.getAttributeKey());
					}
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

			strQuery += "\nSELECT DISTINCT ?supplierId ?wsProfileId ?byProductName ?byProductSupplyType ?byProductMinParticipants ?byProductMaxParticipants ?purchasingGroupAbilitation ?certificationType "
					+ "?byProductQuantity ?byProductMinQuantity ?byProductUOM ?materialType \n";

		} else {

			strQuery += "\nSELECT DISTINCT ?supplierId ?wsProfileId ?byProductName ?byProductSupplyType ?byProductMinParticipants "
					+ "?byProductMaxParticipants ?purchasingGroupAbilitation ?certificationType ?byProductQuantity ?byProductMinQuantity ?byProductUOM ?attributeType (str(?uom) as ?uomStr) ?attributeValue ?materialType \n";

		}

		strQuery += "\nWHERE { \n\n";

		strQuery += "?wsProfileId core:hasSupplier ?supplierId .\n";
		strQuery +="?wsProfileId core:hasName ?byProductName . \n";
		strQuery +="?wsProfileId ind:hasMode ?byProductMode . \n";
		
		strQuery +="OPTIONAL { ?wsProfileId ind:hasDeadline ?deadline . \n";
		strQuery +="FILTER ( ?deadline >= NOW() ) \n";
		strQuery += "} \n";
		
		strQuery +="?wsProfileId ind:hasMinParticipants ?byProductMinParticipants . \n";
		//TODO: Not sure how the minParticipants is registered with the supplier wsprofile (not in GUI)
		//strQuery +="FILTER ( xsd:integer(?byProductMinParticipants) >= " + minParticipants + " ) \n";
		
		strQuery +="?wsProfileId ind:hasMaxParticipants ?byProductMaxParticipants . \n";
		//TODO: Not sure how the maxParticipants is registered with the supplier wsprofile (not in GUI)
		//strQuery +="FILTER ( xsd:integer(?byProductMaxParticipants) <= " + maxParticipants + " ) \n";
		
		strQuery +="?wsProfileId ind:hasPurchasingGroupAbilitation ?purchasingGroupAbilitation . \n";
		
		//fixed filter not from consumer query
		strQuery +="FILTER ( regex(?byProductMode, \""+mode+"\", \"i\") ) \n";
		
		strQuery +="?wsProfileId ind:hasStatus ?byProductStatus . \n";
		//fixed filter not from consumer query
		strQuery +="FILTER ( regex(?byProductStatus, \"Available\", \"i\") ) \n";
		
		strQuery +="?wsProfileId ind:hasSupplyType ?byProductSupplyType . \n";
		
		//the quantity and unit of measurement of quantity must be compared with reqs in consumer query (in java)
		strQuery +="?wsProfileId core:hasQuantity ?byProductQuantity . \n";
		
		strQuery +="?wsProfileId ind:hasMinQuantity ?byProductMinQuantity . \n";
		
		strQuery +="?wsProfileId ind:hasUnitOfMeasureQuantity ?byProductUOM . \n";
		
		

		//get attributes
		if (!isNullOrEmpty (materialsAndAttributes)) {

			strQuery += queryAttributes(materialsAndAttributes);

		}

		//certifications (as before we just include all certifications associated with the relevant suppliers, not considering the certifications required by the consumer at this point,
		//this is taken care of by the matchmaking algo)
		strQuery += "\nOPTIONAL {?supplierId core:hasCertification ?certification . ?certification rdf:type ?certificationType . \n";
		strQuery += "FILTER ( ?certificationType not in ( owl:NamedIndividual ) && ?certificationType not in ( owl:Class )) \n";
		strQuery += "} \n";

		//filter suppliers
		if (supplierMaxDistance != 0) {

			//strQuery += "\nOPTIONAL { \n"; TODO: Check if we need OPTIONAL here, this snippet is only included if the consumer adds a supplierMaxDistance other than '0'.
			strQuery += "\n?supplierId geo:asWKT ?location .\n";
			strQuery += "BIND((geof:distance(?location, \"POINT(" + lat + " " + lon + ")\"^^geo:wktLiteral, uom:metre)/1000) as ?distance)\n";
			strQuery += "FILTER (xsd:double(?distance)<" + supplierMaxDistance + " ) \n";
			//strQuery += "} \n";
		}

		if (!isNullOrEmpty(languages)) {

			strQuery += "\nOPTIONAL { \n";
			strQuery += "\n?supplierId core:hasAttribute ?languageAttribute . \n";
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

		attributeQuery.append("OPTIONAL {?wsProfileId core:hasAttribute ?attribute . \n");
		attributeQuery.append("?attribute rdf:type ?attributeType . \n");
		
		attributeQuery.append("#GET ATTRIBUTES\n");
		attributeQuery.append("OPTIONAL {?attribute core:hasUnitOfMeasure ?uomInd .  \n");
		attributeQuery.append("?uomInd core:hasName ?uom . \n");
		attributeQuery.append("?attribute core:hasValue ?attributeValue . }\n");
		
		attributeQuery.append("#GET MATERIALS\n");
		attributeQuery.append("OPTIONAL {?attribute core:hasObjectValue ?attributeMaterialValue .\n");
		attributeQuery.append("?attributeMaterialValue rdf:type ?materialType . \n");
		attributeQuery.append("FILTER ( ?materialType not in ( owl:NamedIndividual )) \n");
		attributeQuery.append("} \n");
		
		attributeQuery.append("VALUES ?attributeType {" + restrictedValues + "ind:Appearance} \n"); //these must be retrieved from the consumer query
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