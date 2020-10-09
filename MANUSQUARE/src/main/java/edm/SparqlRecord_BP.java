package edm;

import java.util.Map;
import java.util.Set;

public class SparqlRecord_BP {
	
	private String processChainId;
	private String supplierId;
	private String byProduct;
	private String certification;
	private Set<String> attributes;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a condition ('<=', '>=' or '=') to an attributeKey
	
	public SparqlRecord_BP(String processChainId, String supplierId, String byProduct, 
			String certification, Set<String> attributes, double attributeWeight) {
		super();
		this.processChainId = processChainId;
		this.supplierId = supplierId;
		this.byProduct = byProduct;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeight = attributeWeight;
	}
	
	public SparqlRecord_BP(String processChainId, String supplierId, String byProduct, 
			String certification, Set<String> attributes, Map<String, String> attributeWeightMap) {
		super();
		this.processChainId = processChainId;
		this.supplierId = supplierId;
		this.byProduct = byProduct;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	public SparqlRecord_BP(String processChainId, String supplierId, String byProduct, 
			String certification) {
		super();
		this.processChainId = processChainId;
		this.supplierId = supplierId;
		this.byProduct = byProduct;

		this.certification = certification;
	}
	
	public SparqlRecord_BP(String supplierId, String byProduct, 
			String certification) {
		super();
		this.supplierId = supplierId;
		this.byProduct = byProduct;

		this.certification = certification;
	}
	
	public SparqlRecord_BP () {}

	public String getProcessChainId() {
		return processChainId;
	}

	public void setProcessChainId(String processChainId) {
		this.processChainId = processChainId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getByProduct() {
		return byProduct;
	}

	public void setByProduct(String byProduct) {
		this.byProduct = byProduct;
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

	public void setAttributes(Set<String> attributes) {
		this.attributes = attributes;
	}

	public double getAttributeWeight() {
		return attributeWeight;
	}

	public void setAttributeWeight(double attributeWeight) {
		this.attributeWeight = attributeWeight;
	}

	public Map<String, String> getAttributeWeightMap() {
		return attributeWeightMap;
	}

	public void setAttributeWeightMap(Map<String, String> attributeWeightMap) {
		this.attributeWeightMap = attributeWeightMap;
	}
	
	
	
	
}
