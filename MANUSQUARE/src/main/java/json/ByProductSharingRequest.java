package json;

import java.util.List;
import java.util.Set;

import edm.Attribute;
import edm.Customer;

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
	private int minNumberOfParticipants;
	private int maxNumberOfParticipants;
	private String purchasingGroupAbilitation;

	public ByProductSharingRequest(String projectName, String projectDescription, String selectionType, double supplierMaxDistance,
                               Customer customer, String mode, int minNumberOfParticipants, int maxNumberOfParticipants, String purchasingGroupAbilitation, String projectId, Set<ByProductElement> byProductElements, List<SupplierAttributeKeys> supplierAttributes) {
		//super();
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		this.selectionType = selectionType;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customer = customer;
		this.mode = mode;
		this.minNumberOfParticipants = minNumberOfParticipants;
		this.maxNumberOfParticipants = maxNumberOfParticipants;
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
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

	public int getMinNumberOfParticipants() {
		return minNumberOfParticipants;
	}
	
	public int getMaxNumberOfParticipants() {
		return maxNumberOfParticipants;
	}


	public String getPurchasingGroupAbilitation() {
		return purchasingGroupAbilitation;
	}



	public class ByProductElement {
		
		private String byProductId;
		private String byProductName;
		private String supplyType;
		private String quantity;
		private String unitOfMeasureQuantity;
		private Set<ByProductAttribute> byProductAttributes;
		
		public ByProductElement() {}
		
		public ByProductElement(String byProductId, String byProductName, String supplyType, String quantity, String unitOfMeasureQuantity, Set<ByProductAttribute> byProductAttributes) {
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

		public String getQuantity() {
			return quantity;
		}

		public String getUom() {
			return unitOfMeasureQuantity;
		}

		public Set<ByProductAttribute> getByProductAttributes() {
			return byProductAttributes;
		}

		public void setByProductAttributes(Set<ByProductAttribute> byProductAttributes) {
			this.byProductAttributes = byProductAttributes;
		}
		
		


	}
	
	public class ByProductAttribute {

		private String attributeKey;
		private String attributeValue;
		private String unitOfMeasure;
		
		
		public ByProductAttribute(String attributeKey, String attributeValue, String unitOfMeasure) {
			super();

			this.attributeKey = attributeKey;
			this.attributeValue = attributeValue;
			this.unitOfMeasure = unitOfMeasure;
		}
		
		public ByProductAttribute() {}


		public String getKey() {
			return attributeKey;
		}

		public String getValue() {
			return attributeValue;
		}


		public String getUnitOfMeasurement() {
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



}
