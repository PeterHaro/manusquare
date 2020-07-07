package supplierdata;

import java.util.List;

import edm.Certification;
import edm.Material;
import edm.Process;


public class InnovationManager {
	
	String id;
	String supplierName;
	String supplierNationality;
	String supplierCity;
	List<Certification> certifications;

	
	public InnovationManager (String id, List<Certification> certifications) {
		this.id = id;
		this.certifications = certifications;
	}
	
	public InnovationManager(String supplierName) {
		super();
		this.supplierName = supplierName;
	}
	
	public InnovationManager() {}
	

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


}
