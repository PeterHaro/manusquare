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
	
	
	public Supplier_BP() {}
	

	public String getId() {
		return id;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public String getSupplierNationality() {
		return supplierNationality;
	}

	public String getSupplierCity() {
		return supplierCity;
	}

	public List<Certification> getCertifications() {
		return certifications;
	}

	public List<ByProduct> getByProducts() {
		return byProducts;
	}

}