package edm;

import java.util.Set;

public class IMProfile {
	
	private String imProfileID;
	private String imProfileName;
	private Set<String> innovationPhases;
	private Set<String> innovationTypes;
	private Set<String> skills;
	private Set<String> sectors;
	
	public IMProfile(String IMProfileID, String IMProfileName,
			Set<String> innovationPhases, Set<String> innovationTypes, Set<String> skills,
			Set<String> sectors) {
		super();
		this.imProfileID = IMProfileID;
		this.imProfileName = IMProfileName;
		this.innovationPhases = innovationPhases;
		this.innovationTypes = innovationTypes;
		this.skills = skills;
		this.sectors = sectors;
	}
	
	public IMProfile() {}

	public String getIMProfileID() {
		return imProfileID;
	}

	public void setIMProfileID(String iMProfileID) {
		imProfileID = iMProfileID;
	}

	public String getIMProfileName() {
		return imProfileName;
	}

	public void setIMProfileName(String iMProfileName) {
		imProfileName = iMProfileName;
	}

	public Set<String> getInnovationPhases() {
		return innovationPhases;
	}

	public void setInnovationPhases(Set<String> innovationPhases) {
		this.innovationPhases = innovationPhases;
	}

	public Set<String> getInnovationTypes() {
		return innovationTypes;
	}

	public void setInnovationTypes(Set<String> innovationTypes) {
		this.innovationTypes = innovationTypes;
	}

	public Set<String> getSkills() {
		return skills;
	}

	public void setSkills(Set<String> skills) {
		this.skills = skills;
	}

	public Set<String> getSectors() {
		return sectors;
	}

	public void setSectors(Set<String> sectors) {
		this.sectors = sectors;
	}

	

}
