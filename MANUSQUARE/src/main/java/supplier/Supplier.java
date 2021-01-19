package supplier;

import java.util.List;


import edm.Certification;


public abstract class Supplier {
	
	private String supplierId;
	private String supplierName;
	private String supplierCity;
	private String supplierNationality;
	private List<Certification> certifications;
	private List<String> languages;
	
	
	Supplier(Builder<?> builder) {
		this.supplierId = builder.supplierId;
		this.supplierName = builder.supplierName;
		this.supplierCity = builder.supplierCity;
		this.supplierNationality = builder.supplierNationality;
		this.certifications = builder.certifications;
		this.languages = builder.languages;
		
	}
	
	
	public abstract static class Builder<T extends Builder> {
		
		private String supplierId;
		private String supplierName;
		private String supplierCity;
		private String supplierNationality;
		private List<Certification> certifications;
		private List<String> languages;
		
		public Builder() {}
		
		public T setSupplierId(String supplierId) {
			this.supplierId = supplierId;
			return self();
		}
		
		public T setSupplierName(String supplierName) {
			this.supplierName = supplierName;
			return self();
		}
		
		public T setSupplierCity(String supplierCity) {
			this.supplierCity = supplierCity;
			return self();
		}
		
		public T setSupplierNationality(String supplierNationality) {
			this.supplierNationality = supplierNationality;
			return self();
		}
		
		public T setCertifications(List<Certification> certifications) {
			this.certifications = certifications;
			return self();
		}
		
		public T setLanguages(List<String> languages) {
			this.languages = languages;
			return self();
		}
		
		public abstract Supplier build();
		
		protected abstract T self();
		
	}


	public String getSupplierId() {
		return supplierId;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public String getSupplierCity() {
		return supplierCity;
	}


	public String getSupplierNationality() {
		return supplierNationality;
	}


	public List<Certification> getCertifications() {
		return certifications;
	}


	public List<String> getLanguages() {
		return languages;
	}
	
	

}
