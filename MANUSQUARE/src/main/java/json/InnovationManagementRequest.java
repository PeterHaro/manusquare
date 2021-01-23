package json;

import java.util.List;

import edm.Customer;

public class InnovationManagementRequest extends ConsumerRequest {
	
	List<String> projectInnovationPhases;
	List<String> projectInnovationTypes;
	List<InnovationManagerSkill> innovationManagerSkills;
	List<InnovationManagerSector> innovationManagerSectors;
	List<InnovationManagerAttribute> innovationManagerAttributes;
	Customer customer;
	
	private InnovationManagementRequest(Builder builder) {
		super(builder);
		
		this.projectInnovationPhases = builder.projectInnovationPhases;
		this.projectInnovationTypes = builder.projectInnovationTypes;
		this.innovationManagerSkills = builder.innovationManagerSkills;
		this.innovationManagerSectors = builder.innovationManagerSectors;
		this.innovationManagerAttributes = builder.innovationManagerAttributes;
		this.customer = builder.customer;
		
	}
	
	public static class Builder extends ConsumerRequest.Builder<Builder> {
		
		List<String> projectInnovationPhases;
		List<String> projectInnovationTypes;
		List<InnovationManagerSkill> innovationManagerSkills;
		List<InnovationManagerSector> innovationManagerSectors;
		List<InnovationManagerAttribute> innovationManagerAttributes;
		Customer customer;
		
		public Builder setProjectInnovationPhases (List<String> projectInnovationPhases) {
			this.projectInnovationPhases = projectInnovationPhases;
			return this;
		}
		
		public Builder setProjectInnovationTypes (List<String> projectInnovationTypes) {
			this.projectInnovationTypes = projectInnovationTypes;
			return this;
		}
		
		public Builder setInnovationManagerSkills (List<InnovationManagerSkill> innovationManagerSkills) {
			this.innovationManagerSkills = innovationManagerSkills;
			return this;
		}
		
		public Builder setInnovationManagerSectors (List<InnovationManagerSector> innovationManagerSectors) {
			this.innovationManagerSectors = innovationManagerSectors;
			return this;
		}
		
		public Builder setInnovationManagerAttributes (List<InnovationManagerAttribute> innovationManagerAttributes) {
			this.innovationManagerAttributes = innovationManagerAttributes;
			return this;
		}
		
		public Builder setCustomer (Customer customer) {
			this.customer = customer;
			return this;
		}

		@Override
		public ConsumerRequest build() {
			return new InnovationManagementRequest(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
		
		
	}

	public List<String> getProjectInnovationPhases() {
		return projectInnovationPhases;
	}

	public List<String> getProjectInnovationTypes() {
		return projectInnovationTypes;
	}

	public List<InnovationManagerSkill> getInnovationManagerSkills() {
		return innovationManagerSkills;
	}

	public List<InnovationManagerSector> getInnovationManagerSectors() {
		return innovationManagerSectors;
	}

	public List<InnovationManagerAttribute> getInnovationManagerAttributes() {
		return innovationManagerAttributes;
	}

	public Customer getCustomer() {
		return customer;
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
	
	public class InnovationManagerSector {

		public String id;
		public String sector;

		public InnovationManagerSector(String id, String sector) {
			super();
			this.id = id;
			this.sector = sector;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getSector() {
			return sector;
		}

		public void setSector(String sector) {
			this.sector = sector;
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
