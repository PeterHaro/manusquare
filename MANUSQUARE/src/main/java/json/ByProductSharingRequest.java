package json;

import java.util.List;
import java.util.Set;

import edm.Customer;

public class ByProductSharingRequest extends ConsumerRequest {
	
	private Set<ByProductElement> byProductElements;	
	private List<SupplierAttributeKeys> supplierAttributes;
	private double supplierMaxDistance;
	private Customer customer;
	private String mode;
	private int minNumberOfParticipants;
	private int maxNumberOfParticipants;
	private String purchasingGroupAbilitation;
	
	private ByProductSharingRequest (Builder builder) {
		super(builder);
		
		this.supplierMaxDistance = builder.supplierMaxDistance;
		this.customer = builder.customer;
		this.mode = builder.mode;
		this.minNumberOfParticipants = builder.minNumberOfParticipants;
		this.maxNumberOfParticipants = builder.maxNumberOfParticipants;
		this.purchasingGroupAbilitation = builder.purchasingGroupAbilitation;
		this.byProductElements = builder.byProductElements;
		this.supplierAttributes = builder.supplierAttributes;

	}


	public static class Builder extends ConsumerRequest.Builder<Builder> {
		
		private Set<ByProductElement> byProductElements;	
		private List<SupplierAttributeKeys> supplierAttributes;
		private double supplierMaxDistance;
		private Customer customer;
		private String mode;
		private int minNumberOfParticipants;
		private int maxNumberOfParticipants;
		private String purchasingGroupAbilitation;

		public Builder setByProductElements(Set<ByProductElement> byProductElements) {
			this.byProductElements = byProductElements;
			return this;
		}
		public Builder setSupplierAttributes(List<SupplierAttributeKeys> supplierAttributes) {
			this.supplierAttributes = supplierAttributes;
			return this;
		}
		public Builder setSupplierMaxDistance(double supplierMaxDistance) {
			this.supplierMaxDistance = supplierMaxDistance;
			return this;
		}
		public Builder setCustomer(Customer customer) {
			this.customer = customer;
			return this;
		}
		public Builder setMode(String mode) {
			this.mode = mode;
			return this;
		}
		public Builder setMinNumberOfParticipants(int minNumberOfParticipants) {
			this.minNumberOfParticipants = minNumberOfParticipants;
			return this;
		}
		public Builder setMaxNumberOfParticipants(int maxNumberOfParticipants) {
			this.maxNumberOfParticipants = maxNumberOfParticipants;
			return this;
		}
		public Builder setPurchasingGroupAbilitation(String purchasingGroupAbilitation) {
			this.purchasingGroupAbilitation = purchasingGroupAbilitation;
			return this;
		}
		@Override
		public ConsumerRequest build() {
			return new ByProductSharingRequest(this);
		}
		@Override
		protected Builder self() {
			return this;
		}
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
