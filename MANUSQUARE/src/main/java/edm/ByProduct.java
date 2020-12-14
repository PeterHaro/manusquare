package edm;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import json.ByProductSharingRequest.ByProductAttributes;

public class ByProduct {

    private String id;
    private String name;
    private String supplyType;
    private int minParticipants;
    private int maxParticipants;
    private String purchasingGroupAbilitation;
    private String quantity;
    private double minQuantity;
    private String uom;
    private Set<ByProductAttributes> attributes;
    private Map<String, String> attributeWeightMap; 
    private String material; //added 02.12.2020
    private Set<String> appearances;
    private Set<String> materials;


 //TODO: Clean up constructors and implement as builder DP. 
    
    //used by SupplierData_BP
	public ByProduct(String id, String name, String supplyType, int minParticipants, int maxParticipants, String purchasingGroupAbilitation, String quantity, double minQuantity, String uom, Set<String> materials,
			Set<String> appearances, Map<String, String> attributeWeightMap) {
		super();
		this.id = id;
		this.name = name;
		this.supplyType = supplyType;
		this.minParticipants = minParticipants;
		this.maxParticipants = maxParticipants;
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
		this.quantity = quantity;
		this.minQuantity = minQuantity;
		this.uom = uom;
		this.materials = materials;
		this.appearances = appearances;
		this.attributeWeightMap = attributeWeightMap;
	}
	
	//Used by ByProductQuery
    public ByProduct(String id, String name, String supplyType, int minParticipants, int maxParticipants, String purchasingGroupAbilitation, String quantity, String uom,  Set<String> materials, Set<String> appearances, Set<ByProductAttributes> attributes) {
        super();
        this.id = id;
        this.name = name;
        this.supplyType = supplyType;
		this.minParticipants = minParticipants;
		this.maxParticipants = maxParticipants;
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
        this.quantity = quantity;
        this.uom = uom;
        this.materials = materials;
        this.appearances = appearances; 
        this.attributes = attributes;
    }
    
  //Used by ByProductQuery
//    public ByProduct(String id, String name, String supplyType, String quantity, String uom, int minParticipants, int maxParticipants, String purchasingGroupAbilitation, Set<ByProductAttributes> attributes) {
//        super();
//        this.id = id;
//        this.name = name;
//        this.supplyType = supplyType;
//		this.minParticipants = minParticipants;
//		this.maxParticipants = maxParticipants;
//		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
//        this.quantity = quantity;
//        this.uom = uom;
//        this.attributes = attributes;
//    }
    
    
    
    //Used by ByProductQuery
    public ByProduct(String id, String name, String supplyType, String quantity, String uom,int minParticipants, int maxParticipants, String purchasingGroupAbilitation) {
        super();
        this.id = id;
        this.name = name;
        this.supplyType = supplyType;
		this.minParticipants = minParticipants;
		this.maxParticipants = maxParticipants;
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
        this.quantity = quantity;
        this.uom = uom;
    }

    
//	public ByProduct(String id, String name, String supplyType, String quantity, double minQuantity, String uom, String material,
//			Map<String, String> attributeWeightMap) {
//		super();
//		this.id = id;
//		this.name = name;
//		this.supplyType = supplyType;
//		this.quantity = quantity;
//		this.minQuantity = minQuantity;
//		this.uom = uom;
//		this.material = material;
//		this.attributeWeightMap = attributeWeightMap;
//	}
	
	//test constructor
//	public ByProduct(String id, Set<String> materials,
//			Map<String, String> attributeWeightMap) {
//		super();
//		this.id = id;
//		this.materials = materials;
//		this.attributeWeightMap = attributeWeightMap;
//	}
	

	public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSupplyType() {
		return supplyType;
	}

    
    
	public double getMinQuantity() {
		return minQuantity;
	}



	public String getQuantity() {
		return quantity;
	}

	public String getUom() {
		return uom;
	}
	
	

	public String getMaterial() {
		return material;
	}


	public Set<ByProductAttributes> getAttributes() {
        return attributes;
    }
	
	public Set<Attribute> getNormalisedAttributes(ByProduct bp) {
		
		Set<ByProductAttributes> attributes = bp.getAttributes();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();
		
		for (ByProductAttributes bpa : attributes) {
			normalisedAttributes.add(new Attribute(bpa.getAttributeKey(), bpa.getAttributeValue(), bpa.getAttributeUnitOfMeasurement()));
		}
		
		return normalisedAttributes;
		
	}
	
	

    public Set<String> getMaterials() {
		return materials;
	}



	public void setMaterials(Set<String> materials) {
		this.materials = materials;
	}
	


	public Set<String> getAppearances() {
		return appearances;
	}

	public void setAppearances(Set<String> appearances) {
		this.appearances = appearances;
	}

	public Map<String, String> getAttributeWeightMap() {
        return attributeWeightMap;
    }


    
    public int getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(int minParticipants) {
		this.minParticipants = minParticipants;
	}

	public int getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(int maxParticipants) {
		this.maxParticipants = maxParticipants;
	}
	
	

	public String getPurchasingGroupAbilitation() {
		return purchasingGroupAbilitation;
	}

	public void setPurchasingGroupAbilitation(String purchasingGroupAbilitation) {
		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
	}

	@Override
    public boolean equals(Object o) {
        if (o instanceof ByProduct && ((ByProduct) o).getId().equals(this.id) && ((ByProduct) o).getAttributeWeightMap() != null && this.getAttributeWeightMap() != null) {
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
//    public String toString() {
//
//        StringBuffer returnedString = new StringBuffer();
//        
//        if (this.attributeWeightMap != null) {
//        //get attributeKeys associated with by-product
//        Map<String, String> attributeWeightMap = this.getAttributeWeightMap();
//        
//        Set<String> attributes = new HashSet<String>();
//        Set<String> attributeValue = new HashSet<String>();
//        
//        for (Entry<String, String> e : attributeWeightMap.entrySet()) {
//        		attributes.add(e.getKey());
//        		attributeValue.add(e.getValue());
//        }
//
//        returnedString.append(this.name);
//        
//        returnedString.append("\n\n- Attributes:");
//
//        if (attributes == null || attributes.isEmpty()) {
//            returnedString.append(" ( no attributes )");
//        } else {
//            for (Entry<String, String> e : attributeWeightMap.entrySet()) {
//            	returnedString.append(e.getKey() + ": " + e.getValue() + " ");
//            }
//
//        }
//        returnedString.append("\n");
//        
//        } else {
//        	returnedString.append("By-product Id: " + this.id);
//        	returnedString.append("\nBy-product name: " + this.name);
//        	
//        	
//        }
//
//        return returnedString.toString();
//        
//        }
    
    }



