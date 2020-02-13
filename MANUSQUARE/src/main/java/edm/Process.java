package edm;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import utilities.StringUtilities;

public class Process {

	private String id;
	private String name;
	private String description;
	private String version;
	private Set<Material> materials;
	private Set<Attribute> attributes;
	private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a weight to an attributeKey
	private Set<String> equivalentProcesses; //added 11.02.2020 to compare equivalent processes in SimilarityMeasures.java
	

	//TODO: Clean up constructors!
	public Process(String id, String name, String description, String version, Set<Material> materials, Set<Attribute> attributes) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.version = version;
		this.materials = materials;
		this.attributes = attributes;
	}

	public Process(String name) {
		this.name = name;
	}

	public Process(String name, Set<Material> materials, Set<Attribute> attributes) {
		super();
		this.name = name;
		this.materials = materials;
		this.attributes = attributes;
	}
	
	public Process(String name, Set<Material> materials, Set<Attribute> attributes, Set<String> equivalentProcesses) {
		super();
		this.name = name;
		this.materials = materials;
		this.attributes = attributes;
		this.equivalentProcesses = equivalentProcesses;
	}
	
	public Process(String name, Set<Material> materials, Map<String, String> attributeWeightMap) {
		super();
		this.name = name;
		this.materials = materials;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	public Process(String name, Set<Material> materials, Set<Attribute> attributes, Map<String, String> attributeWeightMap) {
		super();
		this.name = name;
		this.materials = materials;
		this.attributes = attributes;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	public Process(String name, Set<Material> materials) {
		super();
		this.name = name;
		this.materials = materials;
	}

	public Process() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Set<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(Set<Material> materials) {
		this.materials = materials;
	}
	
	
	public Set<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}
	

	public Map<String, String> getAttributeWeightMap() {
		return attributeWeightMap;
	}

	public void setAttributeWeightMap(Map<String, String> attributeWeightMap) {
		this.attributeWeightMap = attributeWeightMap;
	}

	public Set<String> getEquivalentProcesses() {
		return equivalentProcesses;
	}

	public void setEquivalentProcesses(Set<String> equivalentProcesses) {
		this.equivalentProcesses = equivalentProcesses;
	}

	@Override
	public boolean equals (Object o) {
		if ( o instanceof Process && ((Process) o).getName().equals(this.name) ) {
			return true;
		} else {
			return false;
		}
	}

	
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    //a toString() method that prints processes along with relevant materials
    public String toString() {
    	
    	//String returnedString = null;
    	
    	Set<String> attributeKeys = new HashSet<String>();
    	Set<Attribute> attributes = this.getAttributes();
    	
    	//System.out.println("Test: Number of attributes:"  + attributes.size());
    	
    	StringBuffer returnedString = new StringBuffer();
    	
    	returnedString.append(this.name);
    	
    	//TODO: Check why there are no attributes being printed 
    	//need to check if there are no attributes associated with the process
    	if (attributes == null || attributes.isEmpty()) {
    		returnedString.append(" ( no attributes )");
    	} else {
    		for (Attribute attribute : attributes) {
    			attributeKeys.add(attribute.getKey());
    		}
    		
    		returnedString.append(StringUtilities.printSetItems(attributeKeys) + " )");
    	}
    	
    	Set<String> materialNames = new HashSet<String>();
    	Set<Material> materials = this.getMaterials();
    	
    	//need to check if there are no materials associated with the process
    	if (materials == null || materials.isEmpty()) {
    		
    		returnedString.append(" ( no materials )");
    	
    
    } else {
    	
		for (Material material : materials) {
		materialNames.add(material.getName());
	}   	

		returnedString.append("( " + StringUtilities.printSetItems(materialNames) + " )");
    	
    }
    
    return returnedString.toString();
    
    }

}
