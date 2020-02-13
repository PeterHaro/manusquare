package supplierdata;

import java.util.List;

import edm.Certification;
import edm.Process;


public class Supplier {
	
	String id;
	String supplierName;
	String supplierNationality;
	String supplierCity;
	List<Certification> certifications;
	List<Process> processes;
	
	public Supplier (String id, List<Process> processes, List<Certification> certifications) {
		this.id = id;
		this.processes = processes;
		this.certifications = certifications;
	}
	
	
	public Supplier(String supplierName) {
		super();
		this.supplierName = supplierName;
	}
	
	public Supplier() {}
	

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

	public List<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(List<Process> processes) {
		this.processes = processes;
	}

}
