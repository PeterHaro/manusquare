package sparqlresult;

import java.util.Map;
import java.util.Set;

public class SparqlResult {
	
	private String supplierId;
	private String supplierName;
	private String material;
	private String certification;
	private Set<String> attributes;
	private double attributeWeight;
	private Map<String, String> attributeWeightMap;
	
	//used by BP
	public SparqlResult (String supplierId, String material, String certification, Map<String, String> attributeWeightMap) {
		this.supplierId = supplierId;
		this.material = material;
		this.certification = certification;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	//used by IM
	public SparqlResult (String supplierId, String supplierName, String material, String certification, Set<String> attributes, double attributeWeight) {
		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.material = material;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeight = attributeWeight;
	}
	
	public SparqlResult (String supplierId, String material, String certification, Set<String> attributes, double attributeWeight) {
		this.supplierId = supplierId;
		this.material = material;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeight = attributeWeight;
	}
	
	public SparqlResult (String supplierId, String material, String certification, Set<String> attributes, Map<String, String> attributeWeightMap) {
		this.supplierId = supplierId;
		this.material = material;
		this.certification = certification;
		this.attributes = attributes;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	
	public SparqlResult(String supplierId, String material, String certification) {
		this.supplierId = supplierId;
		this.material = material;
		this.certification = certification;

	}
	
	public SparqlResult() {}

	public String getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}
	
	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
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
