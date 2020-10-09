package json;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edm.Resource;

public class ByProductSharingRequest {

	String projectName;
	String projectDescription;
	String selectionType;
	String projectId;
	
	public Set<ByProductElement> byProductElements;	
	public List<SupplierAttributeKeys> supplierAttributes;
	public double supplierMaxDistance;
	public Customer customer;
	

	public ByProductSharingRequest(String projectName, String projectDescription, String selectionType, double supplierMaxDistance,
                               Customer customer, String projectId, Set<ByProductElement> byProductElements, List<SupplierAttributeKeys> supplierAttributes) {
		super();
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		this.selectionType = selectionType;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customer = customer;
		this.projectId = projectId;
		this.byProductElements = byProductElements;
		this.supplierAttributes = supplierAttributes;

	}

	public class ByProductElement {
		
		public String byProductId;
		public String byProductName;
		public String supplyType;
		public int quantity;
		public String unitOfMeasureQuantity;
		public Set<ByProductAttributes> byProductAttributes;
		
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

		public void setByProductId(String byProductId) {
			this.byProductId = byProductId;
		}

		public String getByProductName() {
			return byProductName;
		}

		public void setByProductName(String byProductName) {
			this.byProductName = byProductName;
		}

		public String getSupplyType() {
			return supplyType;
		}

		public void setSupplyType(String supplyType) {
			this.supplyType = supplyType;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public String getUom() {
			return unitOfMeasureQuantity;
		}

		public void setUom(String unitOfMeasureQuantity) {
			this.unitOfMeasureQuantity = unitOfMeasureQuantity;
		}

		public Set<ByProductAttributes> getAttributes() {
			return byProductAttributes;
		}

		public void setAttributes(Set<ByProductAttributes> attributes) {
			this.byProductAttributes = attributes;
		}

	}
	
	public class ByProductAttributes {

		public String attributeKey;
		public String attributeValue;
		public String unitOfMeasure;
		
		
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
	
	public class Customer {
		
		public String customerName;		
		public Map<String, String> customerInfo;

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

		public void setCustomerInfo(Map<String, String> customerInfo) {
			this.customerInfo = customerInfo;
		}

		
		
	}


}
