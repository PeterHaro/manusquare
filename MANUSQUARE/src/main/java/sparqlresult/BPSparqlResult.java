package sparqlresult;

import java.util.Map;

public class BPSparqlResult extends SparqlResult {
	
	private String wsProfileId;
	private String byProductName;
	private String byProductSupplyType;
	private String byProductDeadline;
	private String byProductMinParticipants;
	private String byProductMaxParticipants;
	private String purchasingGroupAbilitation;
	private String byProductQuantity;
	private String byProductMinQuantity;
	private String byProductUOM;
	private String appearance;
	
	
	public BPSparqlResult(String supplierId, String wsProfileId, String byProductName, String byProductSupplyType, String byProductMinParticipants, String byProductMaxParticipants,
			String purchasingGroupAbilitation, String byProductQuantity, String byProductMinQuantity, String byProductUOM, String certification, String material, String appearance, 
			Map<String, String> attributeWeightMap) {
		super(supplierId, material, certification, attributeWeightMap);
		this.wsProfileId = wsProfileId;
		this.byProductName = byProductName;
		this.byProductSupplyType = byProductSupplyType;
		this.byProductMinParticipants = byProductMinParticipants;
		this.byProductMaxParticipants = byProductMaxParticipants;
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
		this.byProductQuantity = byProductQuantity;
		this.byProductMinQuantity = byProductMinQuantity;
		this.byProductUOM = byProductUOM;
		this.appearance = appearance;

	}

	
	public BPSparqlResult(String supplierId, String wsProfileId, String certification, String material, String appearance, Map<String, String> attributeWeightMap) {
		super(supplierId, material, certification, attributeWeightMap);
		
		this.wsProfileId = wsProfileId;
		this.appearance = appearance;

	}
	
	
	public BPSparqlResult () {}


	public String getWsProfileId() {
		return wsProfileId;
	}

	public void setWsProfileId(String byProductId) {
		this.wsProfileId = byProductId;
	}


	public String getAppearance() {
		return appearance;
	}

	public void setAppearance(String appearance) {
		this.appearance = appearance;
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
		return "SupplierID: " + this.getSupplierId() + "\n" + "WSProfileID: " + this.getWsProfileId() + "\nBy-product name: " + this.getByProductName();
	}
	
	
}
