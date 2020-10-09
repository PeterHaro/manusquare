package supplierdata;

import java.util.List;

import edm.ByProduct;
import edm.Certification;


public class Supplier_BP {
	
	String id;
	String supplierName;
	String supplierNationality;
	String supplierCity;
	List<Certification> certifications;
	List<ByProduct> byProducts;
	
	public Supplier_BP (String id, List<ByProduct> byProducts, List<Certification> certifications) {
		this.id = id;
		this.byProducts = byProducts;
		this.certifications = certifications;
	}
	
	
	
	public Supplier_BP(String supplierName) {
		super();
		this.supplierName = supplierName;
	}
	
	public Supplier_BP() {}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getSupplierNationality() {
		return supplierNationality;
	}

	public void setSupplierNationality(String supplierNationality) {
		this.supplierNationality = supplierNationality;
	}

	public String getSupplierCity() {
		return supplierCity;
	}

	public void setSupplierCity(String supplierCity) {
		this.supplierCity = supplierCity;
	}


	public List<Certification> getCertifications() {
		return certifications;
	}

	public void setCertifications(List<Certification> certifications) {
		this.certifications = certifications;
	}

	public List<ByProduct> getByProducts() {
		return byProducts;
	}

	public void setProcesses(List<ByProduct> byProducts) {
		this.byProducts = byProducts;
	}

}