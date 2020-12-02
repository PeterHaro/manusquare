package edm;

import java.util.Map;
import java.util.Set;

public class SparqlRecord_BP {
	
	private String wsProfileId;
	private String supplierId;
	private String byProductName;
	private String byProductSupplyType;
	private String byProductDeadline;
	private String byProductMinParticipants;
	private String byProductMaxParticipants;
	private String byProductQuantity;
	private String byProductUOM;
	private String certification;
	private Set<String> attributes;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a condition ('<=', '>=' or '=') to an attributeKey
	

	
	public SparqlRecord_BP(String wsProfileId, String supplierId, String byProductName, String byProductSupplyType,
			String byProductDeadline, String byProductMinParticipants, String byProductMaxParticipants,
			String byProductQuantity, String byProductUOM, String certification, Map<String, String> attributeWeightMap) {
		super();
		this.wsProfileId = wsProfileId;
		this.supplierId = supplierId;
		this.byProductName = byProductName;
		this.byProductSupplyType = byProductSupplyType;
		this.byProductDeadline = byProductDeadline;
		this.byProductMinParticipants = byProductMinParticipants;
		this.byProductMaxParticipants = byProductMaxParticipants;
		this.byProductQuantity = byProductQuantity;
		this.byProductUOM = byProductUOM;
		this.certification = certification;
//		this.attributes = attributes;
//		this.attributeWeight = attributeWeight;
		this.attributeWeightMap = attributeWeightMap;
	}

	public SparqlRecord_BP(String wsProfileId, String supplierId, String certification, Set<String> attributes, double attributeWeight) {
		super();
		this.wsProfileId = wsProfileId;
		this.supplierId = supplierId;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeight = attributeWeight;
	}
	
	public SparqlRecord_BP(String supplierId, String wsProfileId, 
			String certification, Set<String> attributes, Map<String, String> attributeWeightMap) {
		super();
		this.supplierId = supplierId;
		this.wsProfileId = wsProfileId;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	public SparqlRecord_BP(String wsProfileId, String supplierId, String certification) {
		super();
		this.wsProfileId = wsProfileId;
		this.supplierId = supplierId;
		this.certification = certification;
	}
	
	
	public SparqlRecord_BP () {}


	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getWsProfileId() {
		return wsProfileId;
	}

	public void setWsProfileId(String byProductId) {
		this.wsProfileId = byProductId;
	}


	public String getCertification() {
		return certification;
	}

	public void setCertification(String certification) {
		this.certification = certification;
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public double getAttributeWeight() {
		return attributeWeight;
	}


	public Map<String, String> getAttributeWeightMap() {
		return attributeWeightMap;
	}

	public void setAttributeWeightMap(Map<String, String> attributeWeightMap) {
		this.attributeWeightMap = attributeWeightMap;
	}

	public String getByProductName() {
		return byProductName;
	}

	public void setByProductName(String byProductName) {
		this.byProductName = byProductName;
	}

	public String getByProductSupplyType() {
		return byProductSupplyType;
	}

	public void setByProductSupplyType(String byProductSupplyType) {
		this.byProductSupplyType = byProductSupplyType;
	}

	public String getByProductDeadline() {
		return byProductDeadline;
	}

	public void setByProductDeadline(String byProductDeadline) {
		this.byProductDeadline = byProductDeadline;
	}

	public String getByProductMinParticipants() {
		return byProductMinParticipants;
	}

	public void setByProductMinParticipants(String byProductMinParticipants) {
		this.byProductMinParticipants = byProductMinParticipants;
	}

	public String getByProductMaxParticipants() {
		return byProductMaxParticipants;
	}

	public void setByProductMaxParticipants(String byProductMaxParticipants) {
		this.byProductMaxParticipants = byProductMaxParticipants;
	}

	public String getByProductQuantity() {
		return byProductQuantity;
	}

	public void setByProductQuantity(String byProductQuantity) {
		this.byProductQuantity = byProductQuantity;
	}

	public String getByProductUOM() {
		return byProductUOM;
	}

	public void setByProductUOM(String byProductUOM) {
		this.byProductUOM = byProductUOM;
	}

	
	public String toString() {
		return "SupplierID: " + this.supplierId + "\n" + "WSProfileID: " + this.wsProfileId + "\nBy-product name: " + this.byProductName;
	}
	
	
}
