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
	private String mode;
	private int minNumberOfPartecipants;
	private int maxNumberOfPartecipants;
	private Set<String> language;
	private Set<String> country;
	
	
	public ByProductQuery(Set<ByProduct> byProducts, double supplierMaxDistance, Map<String, String> customerLocationInfo, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants) {
		super();
		this.byProducts = byProducts;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
		this.mode = mode;
		this.minNumberOfPartecipants = minNumberOfPartecipants;
		this.maxNumberOfPartecipants = maxNumberOfPartecipants;
	}
	
	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants) {
		super();
		this.byProducts = byProducts;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
		this.mode = mode;
		this.minNumberOfPartecipants = minNumberOfPartecipants;
		this.maxNumberOfPartecipants = maxNumberOfPartecipants;
	}
	
	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants, Set<String> language) {
		super();
		this.byProducts = byProducts;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
		this.mode = mode;
		this.minNumberOfPartecipants = minNumberOfPartecipants;
		this.maxNumberOfPartecipants = maxNumberOfPartecipants;
		this.language = language;
	}
	
//	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants, Set<String> country) {
//		super();
//		this.byProducts = byProducts;
//		this.certifications = certifications;
//		this.supplierMaxDistance = supplierMaxDistance;
//		this.customerLocationInfo = customerLocationInfo;
//		this.mode = mode;
//		this.minNumberOfPartecipants = minNumberOfPartecipants;
//		this.maxNumberOfPartecipants = maxNumberOfPartecipants;
//		this.country = country;
//	}
	
	public ByProductQuery(Set<ByProduct> byProducts, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants, Set<String> language, Set<String> country) {
		super();
		this.byProducts = byProducts;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
		this.mode = mode;
		this.minNumberOfPartecipants = minNumberOfPartecipants;
		this.maxNumberOfPartecipants = maxNumberOfPartecipants;
		this.language = language;
		this.country = country;
	}
	
//	public static class ByProductQueryBuilder {
//		
//		private Set<ByProduct> byProducts;
//		private Set<Certification> certifications;
//		private double supplierMaxDistance;
//		private Map<String, String> customerLocationInfo;
//		private String mode;
//		private int minNumberOfPartecipants;
//		private int maxNumberOfPartecipants;
//		private Set<String> language;
//		
//		public ByProductQueryBuilder(Set<ByProduct> byProducts, double supplierMaxDistance, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants) {
//			this.byProducts = byProducts;
//			this.supplierMaxDistance = supplierMaxDistance;
//			this.mode = mode;
//			this.minNumberOfPartecipants = minNumberOfPartecipants;
//			this.maxNumberOfPartecipants = maxNumberOfPartecipants;
//		}
//		
//		public ByProductQueryBuilder certifications (Set<Certification> certifications) {
//			this.certifications = certifications;
//			return this;
//		}
//		
//		public ByProductQueryBuilder customerLocationInfo (Map<String, String> customerLocationInfo) {
//			this.customerLocationInfo = customerLocationInfo;
//			return this;
//		}
//		
//		public ByProductQueryBuilder languages (Set<String> language) {
//			this.language = language;
//			return this;
//		}
//		
//		public ByProductQuery build() {
//			ByProductQuery query = new ByProductQuery(this);
//			return query;
//		}
//	}


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
			normalisedAttributes.add(new Attribute(bpa.getAttributeKey(), bpa.getAttributeValue(), bpa.getAttributeUnitOfMeasurement()));
		}

		return normalisedAttributes;

	}


	public void setByProducts(Set<ByProduct> byProducts) {
		this.byProducts = byProducts;
	}

	public Set<Certification> getCertifications() {
		return certifications;
	}

	public double getSupplierMaxDistance() {
		return supplierMaxDistance;
	}

	public Map<String, String> getCustomerLocationInfo() {
		return customerLocationInfo;
	}

	public Set<String> getLanguage() {
		return language;
	}
	
	public Set<String> getCountry() {
		return country;
	}
	
	public String getMode() {
		return mode;
	}

	public int getMinNumberOfPartecipants() {
		return minNumberOfPartecipants;
	}

	public int getMaxNumberOfPartecipants() {
		return maxNumberOfPartecipants;
	}


	/**
	 * Parses a json file and creates a ByProductQuery object representing the input provided by a consumer in the RFQ establishment process.
	 *
	 * @param filename the path to the input json file.
	 * @param onto     the MANUSQUARE ontology.
	 * @return a ByProductQuery object representing information requested by a consumer.
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws IOException
	 */
	public static ByProductQuery createByProductQuery(String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {
		Set<ByProduct> byProducts = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> languages = new HashSet<String>();
		Set<String> countries = new HashSet<String>();
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		ByProductSharingRequest bpsr;

		if (JSONValidation.isJSONValid(filename)) {
			bpsr = new Gson().fromJson(filename, ByProductSharingRequest.class);
		} else {
			bpsr = new Gson().fromJson(new FileReader(filename), ByProductSharingRequest.class);
		}

		//get all byproduct (names) from the JSON
		for (ByProductElement element : bpsr.getByProductElements()) {
			
			Set<ByProductAttributes> attributeSet = element.getByProductAttributes();
			
			//TODO: Check that this check on empty attributes work!
			for (ByProductAttributes bp : attributeSet) {
				if (bp.getAttributeKey() == null || bp.getAttributeKey().equals("")) {
					byProducts.add(new ByProduct(element.getByProductId(), QueryValidation.validateByProductName(element.getByProductName(), onto, allOntologyClasses), element.getSupplyType(), element.getQuantity(), element.getUom()));
				} else {
					byProducts.add(new ByProduct(element.getByProductId(), QueryValidation.validateByProductName(element.getByProductName(), onto, allOntologyClasses), element.getSupplyType(), element.getQuantity(), element.getUom(), element.getByProductAttributes()));
				}
			}
			
		}

		ByProductQuery query = null;

		//add geographical information to consumer query
		double supplierMaxDistance = bpsr.getSupplierMaxDistance();
		Map<String, String> customerInformation = bpsr.getCustomer().getCustomerInfo();
		
		String mode = bpsr.getMode();
		int minNumberOfPartecipants = bpsr.getMinNumberOfPartecipants();
		int maxNumberOfPartecipants = bpsr.getMaxNumberOfPartecipants();

		//get certifications if they are specified by the consumer
		if (bpsr.getSupplierAttributes() == null || bpsr.getSupplierAttributes().isEmpty()) {
			//if no attributes nor certifications, we only add the processes to the ConsumerQuery object
			//assuming that supplierMaxDistance and customerInformation (name, location, coordinates) are always included
			query = new ByProductQuery(byProducts, supplierMaxDistance, customerInformation, mode, minNumberOfPartecipants, maxNumberOfPartecipants);

		} else {

			for (SupplierAttributeKeys supplierAttributes : bpsr.getSupplierAttributes()) {
				if (supplierAttributes.getAttributeKey().equalsIgnoreCase("certification")) {
					certifications.add(new Certification(supplierAttributes.getAttributeValue()));
				}

				if (supplierAttributes.getAttributeKey().equalsIgnoreCase("Language")) {
					languages.add(supplierAttributes.getAttributeValue());
				}
				
				if (supplierAttributes.getAttributeKey().equalsIgnoreCase("Country")) {
					countries.add(supplierAttributes.getAttributeValue());
				}
			}
		}

			//if both languages and countries
			if (!languages.isEmpty() && !countries.isEmpty()) {

				query = new ByProductQuery(byProducts, QueryValidation.validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation, mode, minNumberOfPartecipants, maxNumberOfPartecipants, languages, countries);

			} else if (!languages.isEmpty() && countries.isEmpty()) {
				
				query = new ByProductQuery(byProducts, QueryValidation.validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation, mode, minNumberOfPartecipants, maxNumberOfPartecipants, languages);
				
			} else if (!countries.isEmpty() && languages.isEmpty())  {
				
				query = new ByProductQuery(byProducts, QueryValidation.validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation, mode, minNumberOfPartecipants, maxNumberOfPartecipants, countries);
			}
			else {
			
				//if there are certifications specified we add those along with processes to the ConsumerQuery object
				query = new ByProductQuery(byProducts, QueryValidation.validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation, mode, minNumberOfPartecipants, maxNumberOfPartecipants);
			}
		


		return query;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/TESTING_BYPRODUCT_SHARING/Test_BP_11.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		ByProductQuery query = createByProductQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);

		for (ByProduct bp : query.getByProducts()) {
			System.out.println("Byproduct name: " + bp.getName());
			System.out.println("Supply type: " + bp.getSupplyType());
			System.out.println("Quantity: " + bp.getQuantity());
			System.out.println("UOM: " + bp.getUom() + "\n");

			if (bp.getAttributes() != null) {
			for (Attribute a : bp.getNormalisedAttributes(bp)) {
				System.out.println("   Attribute: " + a.getKey());
			}
		} else {
			System.out.println("There are no attributes!");
		}
		}


//		for (Certification cert : query.getCertifications()) {
//			System.out.println("Certification: " + cert.getId());
//		}

		System.out.println("Max supplier distance: " + query.getSupplierMaxDistance());
		System.out.println("Mode: " + query.getMode());
		System.out.println("Min number of participants: " + query.getMinNumberOfPartecipants());
		System.out.println("Max number of participants: " + query.getMaxNumberOfPartecipants());

		System.out.println("Languages required: " + query.getLanguage());
	}

}
