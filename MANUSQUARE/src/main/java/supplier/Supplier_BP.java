package supplier;

import java.util.List;
import java.util.Objects;

import edm.ByProduct;
import edm.Certification;


public class Supplier_BP {
	
	private String id;
	private List<ByProduct> byProducts;
	private List<Certification> certifications;
	
	public Supplier_BP (String id, List<ByProduct> byProducts, List<Certification> certifications) {
		this.id = id;
		this.byProducts = byProducts;
		this.certifications = certifications;
	}
	
	
	public Supplier_BP() {}
	

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

//	public String getSupplierName() {
//		return supplierName;
//	}
//
//	public String getSupplierNationality() {
//		return supplierNationality;
//	}
//
//	public String getSupplierCity() {
//		return supplierCity;
//	}




	public List<Certification> getCertifications() {
		return certifications;
	}

	public List<ByProduct> getByProducts() {
		return byProducts;
	}
	
    @Override
    public boolean equals(Object o) {
        if (o instanceof Supplier_BP && ((Supplier_BP) o).getId().equals(this.id)) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}