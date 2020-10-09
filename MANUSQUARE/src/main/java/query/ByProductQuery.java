package query;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import json.ByProductSharingRequest;
import json.ByProductSharingRequest.ByProductAttributes;
import json.ByProductSharingRequest.ByProductElement;
import json.ByProductSharingRequest.SupplierAttributeKeys;
import owlprocessing.OntologyOperations;
import validation.JSONValidation;
import validation.QueryValidation;

public class ByProductQuery {

	private Set<ByProduct> byProducts;
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> language;


	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, Set<String> language) {
		super();
		this.byProducts = byProducts;
		this.certifications = certifications;
		this.language = language;
	}

	public ByProductQuery(Set<ByProduct> byProducts, double supplierMaxDistance, Map<String, String> customerLocationInfo) {
		super();

		this.byProducts = byProducts;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
	}

	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo) {
		super();
		this.byProducts = byProducts;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
	}

	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo, Set<String> language) {
		super();
		this.byProducts = byProducts;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
		this.language = language;
	}


	public ByProductQuery() {
	}

	public Set<ByProduct> getByProducts() {
		return byProducts;
	}

	public Set<Attribute> getAttributes() {
		Set<ByProduct> byProducts = getByProducts();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();
		for (ByProduct p : byProducts) {
			normalisedAttributes.addAll(getNormalisedAttributes(p));
		}

		return normalisedAttributes;

	}

	public Set<Attribute> getNormalisedAttributes(ByProduct bp) {

		Set<ByProductAttributes> attributes = bp.getAttributes();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();

		for (ByProductAttributes bpa : attributes) {
			normalisedAttributes.add(new Attribute(bpa.attributeKey, bpa.attributeValue, bpa.unitOfMeasure));
		}

		return normalisedAttributes;

	}



	public void setByProducts(Set<ByProduct> byProducts) {
		this.byProducts = byProducts;
	}

	public Set<Certification> getCertifications() {
		return certifications;
	}

	public void setCertifications(Set<Certification> certifications) {
		this.certifications = certifications;
	}

	public double getSupplierMaxDistance() {
		return supplierMaxDistance;
	}

	public void setSupplierMaxDistance(double supplierMaxDistance) {
		this.supplierMaxDistance = supplierMaxDistance;
	}


	public Map<String, String> getCustomerLocationInfo() {
		return customerLocationInfo;
	}

	public void setCustomerLocationInfo(Map<String, String> customerLocationInfo) {
		this.customerLocationInfo = customerLocationInfo;
	}

	public Set<String> getLanguage() {
		return language;
	}

	public void setLanguage(Set<String> language) {
		this.language = language;
	}

	/**
	 * Parses a json file and creates a ConsumerQuery object representing the input provided by a consumer in the RFQ establishment process.
	 *
	 * @param filename the path to the input json file.
	 * @param onto     the MANUSQUARE ontology.
	 * @return a ConsumerQuery object representing processes/materials/certifications requested by a consumer.
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws IOException
	 */
	public static ByProductQuery createByProductQuery(String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {
		Set<ByProduct> byProducts = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> languages = new HashSet<String>();
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		ByProductSharingRequest bpsr;

		if (JSONValidation.isJSONValid(filename)) {
			bpsr = new Gson().fromJson(filename, ByProductSharingRequest.class);
		} else {
			bpsr = new Gson().fromJson(new FileReader(filename), ByProductSharingRequest.class);
		}

		//get all byproduct (names) from the JSON
		for (ByProductElement element : bpsr.byProductElements) {
			System.out.println("Validating by-product: " + element.byProductName);
			byProducts.add(new ByProduct(element.byProductId, QueryValidation.validateByProductName(element.byProductName, onto, allOntologyClasses), element.supplyType, element.quantity, element.unitOfMeasureQuantity, element.byProductAttributes));
		}

		ByProductQuery query = null;

		//add geographical information to consumer query
		double supplierMaxDistance = bpsr.supplierMaxDistance;
		Map<String, String> customerInformation = bpsr.customer.customerInfo;

		//get certifications if they are specified by the consumer
		if (bpsr.supplierAttributes == null || bpsr.supplierAttributes.isEmpty()) {
			//if no attributes nor certifications, we only add the processes to the ConsumerQuery object
			//assuming that supplierMaxDistance and customerInformation (name, location, coordinates) are always included
			query = new ByProductQuery(byProducts, supplierMaxDistance, customerInformation);

		} else {

			for (SupplierAttributeKeys supplierAttributes : bpsr.supplierAttributes) {
				if (supplierAttributes.attributeKey.equalsIgnoreCase("certification")) {
					certifications.add(new Certification(supplierAttributes.attributeValue));
				}

				if (supplierAttributes.attributeKey.equalsIgnoreCase("Language")) {
					languages.add(supplierAttributes.attributeValue);
				}
			}

			if (languages != null) {

				query = new ByProductQuery(byProducts, QueryValidation.validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation, languages);

			} else {
				//if there are certifications specified we add those along with processes to the ConsumerQuery object
				query = new ByProductQuery(byProducts, QueryValidation.validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation);
			}
		}


		return query;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/SUPSI/ByProductSharing_30092020.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		ByProductQuery query = createByProductQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);

		for (ByProduct bp : query.getByProducts()) {
			System.out.println("B: " + bp.getName());
			System.out.println("Supply type: " + bp.getSupplyType());
			System.out.println("Quantity: " + bp.getQuantity());
			System.out.println("UOM: " + bp.getUom() + "\n");

			for (Attribute a : bp.getNormalisedAttributes(bp)) {
				System.out.println("   Attribute: " + a.getKey());
			}
		}


		for (Certification cert : query.getCertifications()) {
			System.out.println("Certification: " + cert.getId());
		}

		System.out.println("Max supplier distance: " + query.getSupplierMaxDistance());

		System.out.println("Languages required: " + query.getLanguage());
	}

}
