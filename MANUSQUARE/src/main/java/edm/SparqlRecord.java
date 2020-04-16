package edm;

import java.util.Map;
import java.util.Set;

public class SparqlRecord {
	
	private String processChainId;
	private String supplierId;
	private String process;
	private String material;
	private String certification;
	private Set<String> attributes;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a condition ('<=', '>=' or '=') to an attributeKey
	
	public SparqlRecord(String processChainId, String supplierId, String process, String material,
			String certification, Set<String> attributes, double attributeWeight) {
		super();
		this.processChainId = processChainId;
		this.supplierId = supplierId;
		this.process = process;
		this.material = material;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeight = attributeWeight;
	}
	
	public SparqlRecord(String processChainId, String supplierId, String process, String material,
			String certification, Set<String> attributes, Map<String, String> attributeWeightMap) {
		super();
		this.processChainId = processChainId;
		this.supplierId = supplierId;
		this.process = process;
		this.material = material;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	public SparqlRecord(String processChainId, String supplierId, String process, String material,
			String certification) {
		super();
		this.processChainId = processChainId;
		this.supplierId = supplierId;
		this.process = process;
		this.material = material;
		this.certification = certification;
	}
	
	public SparqlRecord(String supplierId, String process, String material,
			String certification) {
		super();
		this.supplierId = supplierId;
		this.process = process;
		this.material = material;
		this.certification = certification;
	}
	
	public SparqlRecord () {}

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

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
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
