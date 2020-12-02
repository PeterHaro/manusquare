package edm;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import json.ByProductSharingRequest.ByProductAttributes;

public class ByProduct {

    private String id;
    private String name;
    private String supplyType;
    private double quantity;
    private String uom;
    private Set<ByProductAttributes> attributes;
    private Map<String, String> attributeWeightMap; //added 11.02.2020 to associate a weight to an attributeKey

//
//    //TODO: Clean up constructors!
    public ByProduct(String id, String name, String supplyType, int quantity, String uom, Set<ByProductAttributes> attributes) {
        super();
        this.id = id;
        this.name = name;
        this.supplyType = supplyType;
        this.quantity = quantity;
        this.uom = uom;
        this.attributes = attributes;
    }
    
    //FIXME: Without attributes
    public ByProduct(String id, String name, String supplyType, double d, String uom) {
        super();
        this.id = id;
        this.name = name;
        this.supplyType = supplyType;
        this.quantity = d;
        this.uom = uom;
    }

    
	public ByProduct(String id, String name, String supplyType, double quantity, String uom,
			Map<String, String> attributeWeightMap) {
		super();
		this.id = id;
		this.name = name;
		this.supplyType = supplyType;
		this.quantity = quantity;
		this.uom = uom;
		this.attributeWeightMap = attributeWeightMap;
	}

	public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSupplyType() {
		return supplyType;
	}

	public double getQuantity() {
		return quantity;
	}

	public String getUom() {
		return uom;
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

    public Map<String, String> getAttributeWeightMap() {
        return attributeWeightMap;
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
    public String toString() {

        StringBuffer returnedString = new StringBuffer();
        
        if (this.attributeWeightMap != null) {
        //get attributeKeys associated with by-product
        Map<String, String> attributeWeightMap = this.getAttributeWeightMap();
        
        Set<String> attributes = new HashSet<String>();
        Set<String> attributeValue = new HashSet<String>();
        
        for (Entry<String, String> e : attributeWeightMap.entrySet()) {
        		attributes.add(e.getKey());
        		attributeValue.add(e.getValue());
        }

        returnedString.append(this.name);
        
        returnedString.append("\n\n- Attributes:");

        if (attributes == null || attributes.isEmpty()) {
            returnedString.append(" ( no attributes )");
        } else {
            for (Entry<String, String> e : attributeWeightMap.entrySet()) {
            	returnedString.append(e.getKey() + ": " + e.getValue() + " ");
            }

        }
        returnedString.append("\n");
        
        } else {
        	returnedString.append("By-product Id: " + this.id);
        	returnedString.append("\nBy-product name: " + this.name);
        	
        	
        }

        return returnedString.toString();
        
        }
    
    }



