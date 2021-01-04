package sparqlresult;

import java.util.Map;
import java.util.Set;

public class SparqlRecord_IM {
	
	private String imProfileId;
	private String supplierId;
	private String supplierName;
	private String sector;
	private String skill;
	private String innovationType;
	private String innovationPhase;
	private String certification;
	private Set<String> attributes;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a condition ('<=', '>=' or '=') to an attributeKey
	
	public SparqlRecord_IM (String processChainId, String supplierId, String supplierName, String sector, String skill,
			String innovationType, String innovationPhase, String certification, Set<String> attributes, double attributeWeight) {
		super();
		this.imProfileId = processChainId;
		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.sector = sector;
		this.skill = skill;
		this.innovationType = innovationType;
		this.innovationPhase = innovationPhase;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeight = attributeWeight;
	}
	
	
	public SparqlRecord_IM () {}

	public String getImProfileId() {
		return imProfileId;
	}

	public void setImProfileId(String processChainId) {
		this.imProfileId = processChainId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public String getSector() {
		return sector;
	}


	public void setSector(String sector) {
		this.sector = sector;
	}


	public String getSkill() {
		return skill;
	}


	public void setSkill(String skill) {
		this.skill = skill;
	}


	public String getInnovationType() {
		return innovationType;
	}


	public void setInnovationType(String innovationType) {
		this.innovationType = innovationType;
	}


	public String getInnovationPhase() {
		return innovationPhase;
	}


	public void setInnovationPhase(String innovationPhase) {
		this.innovationPhase = innovationPhase;
	}


	public String getCertification() {
		return certification;
	}

	public void setCertification(String certification) {
		this.certification = certification;
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<String> attributes) {
		this.attributes = attributes;
	}

	public double getAttributeWeight() {
		return attributeWeight;
	}

	public void setAttributeWeight(double attributeWeight) {
		this.attributeWeight = attributeWeight;
	}

	public Map<String, String> getAttributeWeightMap() {
		return attributeWeightMap;
	}

	public void setAttributeWeightMap(Map<String, String> attributeWeightMap) {
		this.attributeWeightMap = attributeWeightMap;
	}
	
	
	
	
}
