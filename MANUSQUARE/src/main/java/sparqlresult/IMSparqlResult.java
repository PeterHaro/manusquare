package sparqlresult;

import java.util.Set;

public class IMSparqlResult extends SparqlResult{
	
	private String imProfileId;
	private String sector;
	private String skill;
	private String innovationType;
	private String innovationPhase;
	
	public IMSparqlResult (String processChainId, String supplierId, String supplierName, String sector, String skill,
			String innovationType, String innovationPhase, String certification, Set<String> attributes, double attributeWeight) {
		super(supplierId, supplierName, certification, attributes, attributeWeight);
		this.imProfileId = processChainId;
		this.sector = sector;
		this.skill = skill;
		this.innovationType = innovationType;
		this.innovationPhase = innovationPhase;

	}
	
	
	public IMSparqlResult () {}

	public String getImProfileId() {
		return imProfileId;
	}

	public void setImProfileId(String processChainId) {
		this.imProfileId = processChainId;
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

	
}
