package supplier;

import java.util.List;

import edm.Certification;


public class IMSupplier {
	
	String id;
	String supplierName;
	String supplierNationality;
	String supplierCity;
	List<Certification> certifications;
	List<String> languages;
	List<String> skills;
	List<String> innovationPhases;
	List<String> innovationTypes;
	List<String> sectors;

	
	public IMSupplier(String id, String supplierName, List<Certification> certifications, List<String> skills,
			List<String> innovationPhases, List<String> innovationTypes, List<String> sectors) {
		super();
		this.id = id;
		this.supplierName = supplierName;
		this.certifications = certifications;
		this.skills = skills;
		this.innovationPhases = innovationPhases;
		this.innovationTypes = innovationTypes;
		this.sectors = sectors;
	}

	
	public IMSupplier() {}
	

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

	public List<String> getSkills() {
		return skills;
	}

	public List<String> getInnovationPhases() {
		return innovationPhases;
	}
	public List<String> getInnovationTypes() {
		return innovationTypes;
	}

	public List<String> getSectors() {
		return sectors;
	}

	public List<Certification> getCertifications() {
		return certifications;
	}

	public List<String> getLanguages() {
		return languages;
	}

}
