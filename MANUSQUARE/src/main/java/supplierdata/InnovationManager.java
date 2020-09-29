package supplierdata;

import java.util.List;

import edm.Certification;


public class InnovationManager {
	
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

	
	
//	public InnovationManager(String id, String supplierName, String supplierNationality, String supplierCity,
//			List<Certification> certifications, List<String> languages, List<String> skills,
//			List<String> innovationPhases, List<String> innovationTypes, List<String> sectors) {
//		super();
//		this.id = id;
//		this.supplierName = supplierName;
//		this.supplierNationality = supplierNationality;
//		this.supplierCity = supplierCity;
//		this.certifications = certifications;
//		this.languages = languages;
//		this.skills = skills;
//		this.innovationPhases = innovationPhases;
//		this.innovationTypes = innovationTypes;
//		this.sectors = sectors;
//	}
	
	public InnovationManager(String id, String supplierName, List<Certification> certifications, List<String> skills,
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

//	public InnovationManager (String id, List<Certification> certifications) {
//		this.id = id;
//		this.certifications = certifications;
//	}
//	
//	public InnovationManager(String supplierName) {
//		super();
//		this.supplierName = supplierName;
//	}
	
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
	

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public List<String> getInnovationPhases() {
		return innovationPhases;
	}

	public void setInnovationPhases(List<String> innovationPhases) {
		this.innovationPhases = innovationPhases;
	}

	public List<String> getInnovationTypes() {
		return innovationTypes;
	}

	public void setInnovationTypes(List<String> innovationTypes) {
		this.innovationTypes = innovationTypes;
	}

	public List<String> getSectors() {
		return sectors;
	}

	public void setSectors(List<String> sectors) {
		this.sectors = sectors;
	}

	public List<Certification> getCertifications() {
		return certifications;
	}

	public void setCertifications(List<Certification> certifications) {
		this.certifications = certifications;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	
	


}
