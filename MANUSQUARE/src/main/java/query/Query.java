package query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edm.Attribute;
import edm.ByProduct;
import edm.Certification;
import json.ByProductSharingRequest.ByProductAttribute;

public class Query {
	
	protected Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> languages;
	private Set<String> country;
	
	public Query(Set<Certification> certifications, Set<String> languages) {
		this.certifications = certifications;
		this.languages = languages;
	}
	
	public Query() {}

		public Set<Attribute> getAttributes() {
		Set<ByProduct> byProducts = getByProducts();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();
		for (ByProduct p : byProducts) {
			normalisedAttributes.addAll(getNormalisedAttributes(p));
		}

		return normalisedAttributes;

	}
		
		public Set<Attribute> getNormalisedAttributes(ByProduct bp) {

			Set<ByProductAttribute> attributes = bp.getAttributes();
			Set<Attribute> normalisedAttributes = new HashSet<Attribute>();

			for (ByProductAttribute bpa : attributes) {
				normalisedAttributes.add(new Attribute(bpa.getKey(), bpa.getValue(), bpa.getUnitOfMeasurement()));
			}

			return normalisedAttributes;

		}

	
}
