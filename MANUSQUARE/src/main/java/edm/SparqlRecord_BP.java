package edm;

import java.util.Map;

public class SparqlRecord_BP {
	
	private String wsProfileId;
	private String supplierId;
	private String byProductName;
	private String byProductSupplyType;
	private String byProductDeadline;
	private String byProductMinParticipants;
	private String byProductMaxParticipants;
	private String purchasingGroupAbilitation;
	private String byProductQuantity;
	private String byProductMinQuantity;
	private String byProductUOM;
	private String certification;
	private String material;
	private String appearance;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a condition ('<=', '>=' or '=') to an attributeKey
	

	//dont think we need deadline, but keeping it for now
//	public SparqlRecord_BP(String supplierId, String wsProfileId, String byProductName, String byProductSupplyType,
//			String byProductDeadline, String byProductMinParticipants, String byProductMaxParticipants,
//			String byProductQuantity, String byProductMinQuantity, String byProductUOM, String certification, String material, Map<String, String> attributeWeightMap) {
//		super();
//		this.wsProfileId = wsProfileId;
//		this.supplierId = supplierId;
//		this.byProductName = byProductName;
//		this.byProductSupplyType = byProductSupplyType;
//		this.byProductDeadline = byProductDeadline;
//		this.byProductMinParticipants = byProductMinParticipants;
//		this.byProductMaxParticipants = byProductMaxParticipants;
//		this.byProductQuantity = byProductQuantity;
//		this.byProductUOM = byProductUOM;
//		this.certification = certification;
//		this.material = material;
//		this.attributeWeightMap = attributeWeightMap;
//	}
	
	public SparqlRecord_BP(String supplierId, String wsProfileId, String byProductName, String byProductSupplyType, String byProductMinParticipants, String byProductMaxParticipants,
			String purchasingGroupAbilitation, String byProductQuantity, String byProductMinQuantity, String byProductUOM, String certification, String material, String appearance, 
			Map<String, String> attributeWeightMap) {
		super();
		this.supplierId = supplierId;
		this.wsProfileId = wsProfileId;
		this.byProductName = byProductName;
		this.byProductSupplyType = byProductSupplyType;
		this.byProductMinParticipants = byProductMinParticipants;
		this.byProductMaxParticipants = byProductMaxParticipants;
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
		this.byProductQuantity = byProductQuantity;
		this.byProductMinQuantity = byProductMinQuantity;
		this.byProductUOM = byProductUOM;
		this.certification = certification;
		this.material = material;
		this.appearance = appearance;
		this.attributeWeightMap = attributeWeightMap;
	}

//	public SparqlRecord_BP(String wsProfileId, String supplierId, String certification, Set<String> attributes, double attributeWeight) {
//		super();
//		this.wsProfileId = wsProfileId;
//		this.supplierId = supplierId;
//		this.certification = certification;
//		this.attributes = attributes;
//		this.attributeWeight = attributeWeight;
//	}
	
//	public SparqlRecord_BP(String supplierId, String wsProfileId, 
//			String certification, Set<String> attributes, Map<String, String> attributeWeightMap) {
//		super();
//		this.supplierId = supplierId;
//		this.wsProfileId = wsProfileId;
//		this.certification = certification;
//		this.attributes = attributes;
//		this.attributeWeightMap = attributeWeightMap;
//	}
	
	public SparqlRecord_BP(String supplierId, String wsProfileId, String certification, String material, String appearance, Map<String, String> attributeWeightMap) {
		super();
		this.supplierId = supplierId;
		this.wsProfileId = wsProfileId;
		this.certification = certification;
		this.material = material;
		this.appearance = appearance;
		this.attributeWeightMap = attributeWeightMap;
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


	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}
	
	

	public String getAppearance() {
		return appearance;
	}

	public void setAppearance(String appearance) {
		this.appearance = appearance;
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

	public String getPurchasingGroupAbilitation() {
		return purchasingGroupAbilitation;
	}

	public void setPurchasingGroupAbilitation(String purchasingGroupAbilitation) {
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
	}

	public String getByProductQuantity() {
		return byProductQuantity;
	}

	public void setByProductQuantity(String byProductQuantity) {
		this.byProductQuantity = byProductQuantity;
	}
	

	public String getByProductMinQuantity() {
		return byProductMinQuantity;
	}

	public void setByProductMinQuantity(String byProductMinQuantity) {
		this.byProductMinQuantity = byProductMinQuantity;
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
