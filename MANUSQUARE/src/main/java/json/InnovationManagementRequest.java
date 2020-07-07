package json;

import java.util.List;

public class InnovationManagementRequest {

	String projectName;
	String projectDescription;
	String selectionType;
	String projectId;
	String projectType;
	public List<String> projectInnovationPhases;
	public List<String> projectInnovationTypes;
	public List<InnovationManagerSkill> innovationManagerSkills;
	public List<InnovationManagerAttribute> innovationManagerAttributes;

	public InnovationManagementRequest(String projectName, String projectDescription, String selectionType,
			String projectId, String projectType, List<String> projectInnovationPhases,
			List<String> projectInnovationTypes, List<InnovationManagerSkill> innovationManagerSkills,
			List<InnovationManagerAttribute> innovationManagerAttributes) {
		super();
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		this.selectionType = selectionType;
		this.projectId = projectId;
		this.projectType = projectType;
		this.projectInnovationPhases = projectInnovationPhases;
		this.projectInnovationTypes = projectInnovationTypes;
		this.innovationManagerSkills = innovationManagerSkills;
		this.innovationManagerAttributes = innovationManagerAttributes;
	}
	
	

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(String selectionType) {
		this.selectionType = selectionType;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public List<String> getProjectInnovationPhases() {
		return projectInnovationPhases;
	}

	public void setProjectInnovationPhases(List<String> projectInnovationPhases) {
		this.projectInnovationPhases = projectInnovationPhases;
	}

	public List<String> getProjectInnovationTypes() {
		return projectInnovationTypes;
	}

	public void setProjectInnovationTypes(List<String> projectInnovationTypes) {
		this.projectInnovationTypes = projectInnovationTypes;
	}

	public List<InnovationManagerSkill> getInnovationManagerSkills() {
		return innovationManagerSkills;
	}

	public void setInnovationManagerSkills(List<InnovationManagerSkill> innovationManagerSkills) {
		this.innovationManagerSkills = innovationManagerSkills;
	}

	public List<InnovationManagerAttribute> getInnovationManagerAttributes() {
		return innovationManagerAttributes;
	}

	public void setInnovationManagerAttributes(List<InnovationManagerAttribute> innovationManagerAttributes) {
		this.innovationManagerAttributes = innovationManagerAttributes;
	}



	public class InnovationManagerSkill {

		public String id;
		public String skill;

		public InnovationManagerSkill(String id, String skill) {
			super();
			this.id = id;
			this.skill = skill;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getSkill() {
			return skill;
		}

		public void setSkill(String skill) {
			this.skill = skill;
		}

	}

	public class InnovationManagerAttribute {

		public String id;
		public String attributeKey;
		public String attributeValue;

		public InnovationManagerAttribute(String id, String attributeKey, String attributeValue) {
			super();
			this.id = id;
			this.attributeKey = attributeKey;
			this.attributeValue = attributeValue;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getAttributeKey() {
			return attributeKey;
		}

		public void setAttributeKey(String attributeKey) {
			this.attributeKey = attributeKey;
		}

		public String getAttributeValue() {
			return attributeValue;
		}

		public void setAttributeValue(String attributeValue) {
			this.attributeValue = attributeValue;
		}
	}

}
