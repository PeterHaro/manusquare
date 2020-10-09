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
    private int quantity;
    private String uom;
    private Set<ByProductAttributes> attributes;
    private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a weight to an attributeKey


    //TODO: Clean up constructors!
    public ByProduct(String id, String name, String supplyType, int quantity, String uom, Set<ByProductAttributes> attributes) {
        super();
        this.id = id;
        this.name = name;
        this.supplyType = supplyType;
        this.quantity = quantity;
        this.uom = uom;
        this.attributes = attributes;
    }

    public ByProduct(String name) {
        this.name = name;
    }
//
//    public ByProduct(String name, Set<ByProductAttribute> attributes) {
//        super();
//        this.name = name;
//        this.attributes = attributes;
//    }
//
//    public ByProduct(String name, Map<String, String> attributeWeightMap) {
//        super();
//        this.name = name;
//        this.attributeWeightMap = attributeWeightMap;
//    }
//
//    public ByProduct(String name, Set<ByProductAttribute> attributes, Map<String, String> attributeWeightMap) {
//        super();
//        this.name = name;
//        this.attributes = attributes;
//        this.attributeWeightMap = attributeWeightMap;
//    }



//    public ByProduct() {
//    }

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
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

	public Set<ByProductAttributes> getAttributes() {
        return attributes;
    }
	
	public Set<Attribute> getNormalisedAttributes(ByProduct bp) {
		
		Set<ByProductAttributes> attributes = bp.getAttributes();
		Set<Attribute> normalisedAttributes = new HashSet<Attribute>();
		
		for (ByProductAttributes bpa : attributes) {
			normalisedAttributes.add(new Attribute(bpa.attributeKey, bpa.attributeValue, bpa.unitOfMeasure));
		}
		
		return normalisedAttributes;
		
	}

    public void setAttributes(Set<ByProductAttributes> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributeWeightMap() {
        return attributeWeightMap;
    }

    public void setAttributeWeightMap(Map<String, String> attributeWeightMap) {
        this.attributeWeightMap = attributeWeightMap;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof ByProduct && ((ByProduct) o).getName().equals(this.name)) {
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
//        //get attributeKeys associated with process
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
//        }
//
//        return returnedString.toString();
//        
//        }
    
    }



