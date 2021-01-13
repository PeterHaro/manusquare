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
import json.ByProductSharingRequest.ByProductAttribute;
import json.ByProductSharingRequest.ByProductElement;
import json.ByProductSharingRequest.SupplierAttributeKeys;
import ontology.OntologyOperations;
import validation.JSONValidator;
import validation.QueryValidator;

public class BPQuery extends Query {

	
	//mandatory attributes	
	private Set<ByProduct> byProducts;
	private String mode;
	private int minNumberOfParticipants;
	private int maxNumberOfParticipants;
	private String purchasingGroupAbilitation;
	
	//optional attributes
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> language;
	private Set<String> country;
	
	private BPQuery(BPQueryBuilder builder) {
		this.byProducts = builder.byProducts;
		this.mode = builder.mode;
		this.minNumberOfParticipants = builder.minNumberOfParticipants;
		this.maxNumberOfParticipants = builder.maxNumberOfParticipants;
		this.purchasingGroupAbilitation = builder.purchasingGroupAbilitation;
		this.certifications = builder.certifications;
		this.supplierMaxDistance = builder.supplierMaxDistance;
		this.customerLocationInfo = builder.customerLocationInfo;
		this.language = builder.language;
		this.country = builder.country;
		
		
	}
	
	public static class BPQueryBuilder {
		
		private Set<ByProduct> byProducts;
		private String mode;
		private int minNumberOfParticipants;
		private int maxNumberOfParticipants;
		private String purchasingGroupAbilitation;

		private Set<Certification> certifications;
		private double supplierMaxDistance;
		private Map<String, String> customerLocationInfo;
		private Set<String> language;
		private Set<String> country;
		
		public BPQueryBuilder(Set<ByProduct> byProducts, String mode, int minNumberOfParticipants, int maxNumberOfParticipants, String purchasingGroupAbilitation) {
			this.byProducts = byProducts;
			this.mode = mode;
			this.minNumberOfParticipants = minNumberOfParticipants;
			this.maxNumberOfParticipants = maxNumberOfParticipants;
			this.purchasingGroupAbilitation = purchasingGroupAbilitation;
		}
		
		public BPQueryBuilder setCertifications(Set<Certification> certifications) {
			this.certifications = certifications;
			return this;
		}
		
		public BPQueryBuilder setSupplierMaxDistance(double supplierMaxDistance) {
			this.supplierMaxDistance = supplierMaxDistance;
			return this;
		}
		
		public BPQueryBuilder setCustomerLocationInfo(Map<String, String> customerLocationInfo) {
			this.customerLocationInfo = customerLocationInfo;
			return this;
		}
		
		public BPQueryBuilder setLanguage(Set<String> language) {
			this.language = language;
			return this;
		}
		
		public BPQueryBuilder setCountry(Set<String> country) {
			this.country = country;
			return this;
		}
		
		public BPQuery build() {
			return new BPQuery(this);
		}
		
	}
	
	

	public BPQuery() {
	}

	public Set<ByProduct> getByProducts() {
		return byProducts;
	}
	
	public String getMode() {
		return mode;
	}
	
	public int getMinNumberOfParticipants() {
		return minNumberOfParticipants;
	}

	public int getMaxNumberOfParticipants() {
		return maxNumberOfParticipants;
	}
	
	public String getPurchasingGroupAbilitation() {
		return purchasingGroupAbilitation;
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
	
	public Set<Attribute> getAttributes() {
		Set<ByProduct> byProducts = getByProducts();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();
		for (ByProduct p : byProducts) {
			normalisedAttributes.addAll(getNormalisedAttributes(p));
		}

		return normalisedAttributes;

	}

	public Set<Attribute> getNormalisedAttributes(ByProduct bp) {

		Set<Attribute> attributes = bp.getAttributes();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();

		for (Attribute bpa : attributes) {
			normalisedAttributes.add(new Attribute(bpa.getKey(), bpa.getValue(), bpa.getUnitOfMeasurement()));
		}

		return normalisedAttributes;

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
	public static BPQuery createByProductQuery(String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {
		Set<ByProduct> byProducts = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> languages = new HashSet<String>();
		Set<String> countries = new HashSet<String>();
		Set<String> materials = new HashSet<String>();
		Set<String> appearances = new HashSet<String>();
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		ByProductSharingRequest bpsr;
		

		if (JSONValidator.isJSONValid(filename)) {
			bpsr = new Gson().fromJson(filename, ByProductSharingRequest.class);
		} else {
			bpsr = new Gson().fromJson(new FileReader(filename), ByProductSharingRequest.class);
		}
		
		String purchasingGroupAbilitation = bpsr.getPurchasingGroupAbilitation();
		int minNumberOfParticipants = bpsr.getMinNumberOfParticipants();
		int maxNumberOfParticipants = bpsr.getMaxNumberOfParticipants();
		

		//get all byproduct (names) from the JSON
		for (ByProductElement element : bpsr.getByProductElements()) {
						
			Set<Attribute> attributeSet = normaliseAttributes(element.getByProductAttributes());
			
			for (Attribute bp : attributeSet) {
				
				if (bp.getKey() == null || bp.getKey().equals("")) {
					byProducts.add(
							
							new ByProduct.Builder(element.getSupplyType(), minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation, element.getQuantity(), element.getUom())
							.setId(element.getByProductId())
							.setName(QueryValidator.validateByProductName(element.getByProductName(), onto, allOntologyClasses))
							.build());

				} else {
					
					if (bp.getKey().equals("AttributeMaterial")) {
						materials.add(bp.getValue());
					}
					
					if (bp.getKey().equals("Appearance")) {
						appearances.add(bp.getValue());
					}
					
					byProducts.add(
							
							new ByProduct.Builder(element.getSupplyType(), minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation, element.getQuantity(), element.getUom())
							.setId(element.getByProductId())
							.setName(QueryValidator.validateByProductName(element.getByProductName(), onto, allOntologyClasses))
							.setMaterials(materials)
							.setAppearance(appearances)
							.setAttributes(attributeSet)
							.build());
							

				}
			}
			
		}

		BPQuery query = null;

		//add geographical information to consumer query
		double supplierMaxDistance = bpsr.getSupplierMaxDistance();
		Map<String, String> customerInformation = bpsr.getCustomer().getCustomerInfo();
		
		String mode = bpsr.getMode();

		//get certifications if they are specified by the consumer
		if (bpsr.getSupplierAttributes() == null || bpsr.getSupplierAttributes().isEmpty()) {
			//if no attributes nor certifications, we only add the processes to the ConsumerQuery object
			//assuming that supplierMaxDistance and customerInformation (name, location, coordinates) are always included
						
			query = new BPQuery.BPQueryBuilder(byProducts, mode, minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation).
					setCustomerLocationInfo(customerInformation).
					setSupplierMaxDistance(supplierMaxDistance).
					build();

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

				query = new BPQuery.BPQueryBuilder(byProducts, mode, minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation).
						setCustomerLocationInfo(customerInformation).
						setSupplierMaxDistance(supplierMaxDistance).
						setCountry(countries).
						setLanguage(languages).
						setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
						build();


			} else if (!languages.isEmpty() && countries.isEmpty()) {
				
				query = new BPQuery.BPQueryBuilder(byProducts, mode, minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation).
						setCustomerLocationInfo(customerInformation).
						setSupplierMaxDistance(supplierMaxDistance).
						setLanguage(languages).
						setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
						build();
				
				
			} else if (!countries.isEmpty() && languages.isEmpty())  {
				
				query = new BPQuery.BPQueryBuilder(byProducts, mode, minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation).
						setCustomerLocationInfo(customerInformation).
						setSupplierMaxDistance(supplierMaxDistance).
						setCountry(countries).
						setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
						build();
			}
			else {
			
				//if there are certifications specified we add those along with processes to the ConsumerQuery object
				query = new BPQuery.BPQueryBuilder(byProducts, mode, minNumberOfParticipants, maxNumberOfParticipants, purchasingGroupAbilitation).
						setCustomerLocationInfo(customerInformation).
						setSupplierMaxDistance(supplierMaxDistance).
						setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
						build();
			}
		

		return query;
	}
	
	private static Set<Attribute> normaliseAttributes (Set<ByProductAttribute> bps) {
		Set<Attribute> attrs = new HashSet<Attribute>();
		Attribute attr = new Attribute();
		for (ByProductAttribute bp :bps) {
			
			attr.setKey(bp.getKey());
			attr.setValue(bp.getValue());
			attr.setUnitOfMeasurement(bp.getUnitOfMeasurement());
			attrs.add(attr);
		}

		return attrs;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/TESTING_BYPRODUCT_SHARING/Radostin_17122020/Radostin_17122020_1.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		BPQuery query = createByProductQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);

		for (ByProduct bp : query.getByProducts()) {
			System.out.println("Byproduct name: " + bp.getName());
			System.out.println("Supply type: " + bp.getSupplyType());
			System.out.println("Quantity: " + bp.getQuantity());
			System.out.println("UOM: " + bp.getUom() + "\n");
			System.out.println("Materials: " + bp.getMaterials());
			System.out.println("Appearances: " + bp.getAppearances());

			System.out.println("\nOther attributes: ");
			System.err.println(bp.getAttributes());
			if (bp.getAttributes() != null) {
			for (Attribute a : bp.getAttributes()) {
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
		System.out.println("Min number of participants: " + query.getMinNumberOfParticipants());
		System.out.println("Max number of participants: " + query.getMaxNumberOfParticipants());
		System.out.println("purchaseGroupAbilitation: " + query.getPurchasingGroupAbilitation() + "\n");
		System.out.println("Languages required: " + query.getLanguage());
	}

}
