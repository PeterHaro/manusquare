package json;

import java.util.List;
import java.util.Set;

import edm.Customer;

public class RequestForQuotation extends ConsumerRequest {

	String id;
	Set<String> processNames;
	List<ProjectAttributeKeys> projectAttributes;
	List<SupplierAttributeKeys> supplierAttributes;
	double supplierMaxDistance;
	Customer customer;
	
	private RequestForQuotation(Builder builder) {
		super(builder);
		
		this.id = builder.id;
		this.processNames = builder.processNames;
		this.customer = builder.customer;
		this.projectAttributes = builder.projectAttributes;
		this.supplierAttributes = builder.supplierAttributes;
		this.supplierMaxDistance = builder.supplierMaxDistance;
		
	}
	
	public static class Builder extends ConsumerRequest.Builder<Builder> {
		
		String id;
		Set<String> processNames;
		List<ProjectAttributeKeys> projectAttributes;
		List<SupplierAttributeKeys> supplierAttributes;
		double supplierMaxDistance;
		Customer customer;
		
		public Builder setId (String id) {
			this.id = id;
			return this;
		}
		
		public Builder setProcessNames (Set<String> processNames) {
			this.processNames = processNames;
			return this;
		}
		
		public Builder setProjectAttributes (List<ProjectAttributeKeys> projectAttributes) {
			this.projectAttributes = projectAttributes;
			return this;
		}
		
		public Builder setSupplierAttributes (List<SupplierAttributeKeys> supplierAttributes) {
			this.supplierAttributes = supplierAttributes;
			return this;
		}
		
		public Builder setSupplierMaxDistance (double supplierMaxDistance) {
			this.supplierMaxDistance = supplierMaxDistance;
			return this;
		}
		
		public Builder setCustomer (Customer customer) {
			this.customer = customer;
			return this;
		}

		@Override
		public ConsumerRequest build() {
			return new RequestForQuotation(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
		
	}
	
	
	
	public String getId() {
		return id;
	}

	public Set<String> getProcessNames() {
		return processNames;
	}

	public List<ProjectAttributeKeys> getProjectAttributes() {
		return projectAttributes;
	}

	public List<SupplierAttributeKeys> getSupplierAttributes() {
		return supplierAttributes;
	}

	public double getSupplierMaxDistance() {
		return supplierMaxDistance;
	}
	

	public Customer getCustomer() {
		return customer;
	}

	public class ProjectAttributeKeys {
		
		public String attributeId;
		public String processName;
		public String attributeKey;
		public String attributeValue;
		public String unitOfMeasure;
		
		//if attribute is material
		public ProjectAttributeKeys(String attributeId, String processId, String attributeKey, String attributeValue) {
			this.attributeId = attributeId;
			this.processName = processId;
			this.attributeKey = attributeKey;
			this.attributeValue = attributeValue;
		}
		
		//if attribute is other attribute than material
		public ProjectAttributeKeys(String attributeId, String processId, String attributeKey, String attributeValue, String unitOfMeasure) {
			this.attributeId = attributeId;
			this.processName = processId;
			this.attributeKey = attributeKey;
			this.attributeValue = attributeValue;
			this.unitOfMeasure = unitOfMeasure;
		}

		public String getAttributeId() {
			return attributeId;
		}

		public void setAttributeId(String attributeId) {
			this.attributeId = attributeId;
		}

		public String getProcessName() {
			return processName;
		}

		public void setProcessName(String processName) {
			this.processName = processName;
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

		public String getAttributeUnitOfMeasurement() {
			return unitOfMeasure;
		}

		public void setAttributeUnitOfMeasurement(String unitOfMeasure) {
			this.unitOfMeasure = unitOfMeasure;
		}
		

	}
	
	public class SupplierAttributeKeys {
		
		public String id;
		public String attributeKey;
		public String attributeValue;
		
		public SupplierAttributeKeys(String id, String attributeKey, String attributeValue) {
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
