package json;

import edm.Customer;

public class ConsumerRequest {
	
	private String projectName;
	private String projectDescription;
	private String selectionType;
	private String projectId;
	private String projectType;
	
	private Customer customerInformation;
	
	ConsumerRequest(Builder<?> builder) {
		this.projectName = builder.projectName;
		this.projectDescription = builder.projectDescription;
		this.selectionType = builder.selectionType;
		this.projectId = builder.projectId;
		this.projectType = builder.projectType;
	}
	
	
	public abstract static class Builder<T extends Builder> {
		
		private String projectName;
		private String projectDescription;
		private String selectionType;
		private String projectId;
		private String projectType;
		
		private Customer customerInformation;
		
		public Builder() {}
		
		public T setProjectName(String projectName) {
			this.projectName = projectName;
			return self();
		}
		
		public T setProjectDescription(String projectDescription) {
			this.projectDescription = projectDescription;
			return self();
		}
		
		public T setSelectionType(String selectionType) {
			this.selectionType = selectionType;
			return self();
		}
		
		public T setProjectId(String projectId) {
			this.projectId = projectId;
			return self();
		}
		
		public T setProjectType(String projectType) {
			this.projectType = projectType;
			return self();
		}
		
		public T setCustomerInformation(Customer customerInformation) {
			this.customerInformation = customerInformation;
			return self();
		}
		
		public abstract ConsumerRequest build();
		
		protected abstract T self();
		
	}


	public String getProjectName() {
		return projectName;
	}


	public String getProjectDescription() {
		return projectDescription;
	}


	public String getSelectionType() {
		return selectionType;
	}


	public String getProjectId() {
		return projectId;
	}


	public String getProjectType() {
		return projectType;
	}


	public Customer getCustomerInformation() {
		return customerInformation;
	}
	
	
	
	

}
