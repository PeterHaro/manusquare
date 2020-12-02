package json;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ByProductSharingRequest {

	String projectName;
	String projectDescription;
	String selectionType;
	String projectId;
	
	private Set<ByProductElement> byProductElements;	
	private List<SupplierAttributeKeys> supplierAttributes;
	private double supplierMaxDistance;
	private Customer customer;
	private String mode;
	private int minNumberOfPartecipants;
	private int maxNumberOfPartecipants;

	public ByProductSharingRequest(String projectName, String projectDescription, String selectionType, double supplierMaxDistance,
                               Customer customer, String mode, int minNumberOfPartecipants, int maxNumberOfPartecipants, String projectId, Set<ByProductElement> byProductElements, List<SupplierAttributeKeys> supplierAttributes) {
		//super();
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		this.selectionType = selectionType;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customer = customer;
		this.mode = mode;
		this.minNumberOfPartecipants = minNumberOfPartecipants;
		this.maxNumberOfPartecipants = maxNumberOfPartecipants;
		this.projectId = projectId;
		this.byProductElements = byProductElements;
		this.supplierAttributes = supplierAttributes;

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

	public Set<ByProductElement> getByProductElements() {
		return byProductElements;
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

	public String getMode() {
		return mode;
	}

	public int getMinNumberOfPartecipants() {
		return minNumberOfPartecipants;
	}
	
	public int getMaxNumberOfPartecipants() {
		return maxNumberOfPartecipants;
	}


	public class ByProductElement {
		
		private String byProductId;
		private String byProductName;
		private String supplyType;
		private int quantity;
		private String unitOfMeasureQuantity;
		private Set<ByProductAttributes> byProductAttributes;
		
		public ByProductElement() {}
		
		public ByProductElement(String byProductId, String byProductName, String supplyType, int quantity, String unitOfMeasureQuantity, Set<ByProductAttributes> byProductAttributes) {
			this.byProductId = byProductId;
			this.byProductName = byProductName;
			this.supplyType = supplyType;
			this.quantity = quantity;
			this.unitOfMeasureQuantity = unitOfMeasureQuantity;
			this.byProductAttributes = byProductAttributes;
			
		}

		public String getByProductId() {
			return byProductId;
		}


		public String getByProductName() {
			return byProductName;
		}

		public String getSupplyType() {
			return supplyType;
		}

		public int getQuantity() {
			return quantity;
		}

		public String getUom() {
			return unitOfMeasureQuantity;
		}

		public Set<ByProductAttributes> getByProductAttributes() {
			return byProductAttributes;
		}

		public void setByProductAttributes(Set<ByProductAttributes> byProductAttributes) {
			this.byProductAttributes = byProductAttributes;
		}
		
		


	}
	
	public class ByProductAttributes {

		private String attributeKey;
		private String attributeValue;
		private String unitOfMeasure;
		
		
		public ByProductAttributes(String attributeKey, String attributeValue, String unitOfMeasure) {
			super();

			this.attributeKey = attributeKey;
			this.attributeValue = attributeValue;
			this.unitOfMeasure = unitOfMeasure;
		}
		
		public ByProductAttributes() {}


		public String getAttributeKey() {
			return attributeKey;
		}

		public String getAttributeValue() {
			return attributeValue;
		}


		public String getAttributeUnitOfMeasurement() {
			return unitOfMeasure;
		}
		

	}
	
	
	public class SupplierAttributeKeys {
		
		private String id;
		private String attributeKey;
		private String attributeValue;
		
		public SupplierAttributeKeys(String id, String attributeKey, String attributeValue) {
			super();
			this.id = id;
			this.attributeKey = attributeKey;
			this.attributeValue = attributeValue;
		}

		public String getId() {
			return id;
		}


		public String getAttributeKey() {
			return attributeKey;
		}


		public String getAttributeValue() {
			return attributeValue;
		}

	}
	
	public class Customer {
		
		private String customerName;		
		private Map<String, String> customerInfo;

		public Customer(String customerName, Map<String, String> customerInfo) {
			super();
			this.customerName = customerName;
			this.customerInfo = customerInfo;
		}

		public Customer(Map<String, String> customerInfo) {
			super();
			this.customerInfo = customerInfo;

		}
		
		public Customer() {}

		public String getCustomerName() {
			return customerName;
		}

		public void setCustomerName(String customerName) {
			this.customerName = customerName;
		}

		public Map<String, String> getCustomerInfo() {
			return customerInfo;
		}
	
		
	}


}
