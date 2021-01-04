package supplier;

import java.util.List;

import edm.Certification;
import edm.Material;
import edm.Process;


public class Supplier {
	
	String id;
	String supplierName;
	String supplierNationality;
	String supplierCity;
	List<Certification> certifications;
	List<Process> processes;
	//List<Material> materials;
	
	public Supplier (String id, List<Process> processes, List<Certification> certifications) {
		this.id = id;
		this.processes = processes;
		this.certifications = certifications;
	}
	
	public Supplier (String id, List<Process> processes, List<Material> materials, List<Certification> certifications) {
		this.id = id;
		this.processes = processes;
		//this.materials = materials;
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

	public List<Process> getProcesses() {
		return processes;
	}

//	public List<Material> getMaterials() {
//		return materials;
//	}
//
//	public void setMaterials(List<Material> materials) {
//		this.materials = materials;
//	}

}