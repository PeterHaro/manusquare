package sparqlresult;

import java.util.Map;
import java.util.Set;

public abstract class SparqlResult {

	//mandatory
	private String supplierId;
	private String certification;

	//optional
	private String supplierName;
	private String material;	
	private Set<String> attributes;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap;

	public String getSupplierId() {
		return supplierId;
	}

	public String getCertification() {
		return certification;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public String getMaterial() {
		return material;
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public double getAttributeWeight() {
		return attributeWeight;
	}

	public Map<String, String> getAttributeWeightMap() {
		return attributeWeightMap;
	}

	SparqlResult(Builder<?> builder) {
		this.supplierId = builder.supplierId;
		this.certification = builder.certification;
		this.supplierName = builder.supplierName;
		this.material = builder.material;
		this.attributes = builder.attributes;
		this.attributeWeight = builder.attributeWeight;
		this.attributeWeightMap = builder.attributeWeightMap;
	}


	public abstract static class Builder<T extends Builder> {

		//mandatory
		protected String supplierId;
		protected String certification;

		//optional
		private String supplierName;
		private String material;	
		private Set<String> attributes;
		private double attributeWeight;
		private Map<String, String> attributeWeightMap;
		
		public Builder() {}
		
		public T setSupplierId(String supplierId) {
			this.supplierId = supplierId;
			return self();
		}
		
		public T setCertification(String certification) {
			this.certification = certification;
			return self();
		}

		public T setSupplierName(String supplierName) {
			this.supplierName = supplierName;
			return self();
		}

		public T setMaterial(String material) {
			this.material = material;
			return self();
		}

		public T setAttributes(Set<String> attributes) {
			this.attributes = attributes;
			return self();
		}

		public T setAttributeWeight(double attributeWeight) {
			this.attributeWeight = attributeWeight;
			return self();
		}

		public T setAttributeWeightMap(Map<String, String> attributeWeightMap) {
			this.attributeWeightMap = attributeWeightMap;
			return self();
		}

		public abstract SparqlResult build();
		
		protected abstract T self();

	}

}
