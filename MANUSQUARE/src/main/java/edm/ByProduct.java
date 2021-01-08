package edm;

import java.util.Set;

public class ByProduct extends Resource {

	//mandatory
    private String supplyType;
    private int minParticipants;
    private int maxParticipants;
    private String purchasingGroupAbilitation;
    private String quantity;
    private double minQuantity;
    private String uom;
    
    //optional
    private Set<String> appearances;

    private ByProduct(Builder builder) {
    	super(builder);

		this.supplyType = builder.supplyType;
		this.minParticipants = builder.minParticipants;
		this.maxParticipants = builder.maxParticipants;
		this.purchasingGroupAbilitation = builder.purchasingGroupAbilitation;
		this.quantity = builder.quantity;
		this.minQuantity = builder.minQuantity;
		this.uom = builder.uom;
		this.appearances = builder.appearances;
    }
    
    
    public static class Builder extends Resource.Builder<Builder> {
    	
    		//mandatory
        private String supplyType;
        private int minParticipants;
        private int maxParticipants;
        private String purchasingGroupAbilitation;
        private String quantity;
        
        private String uom;
        
        //optional
        private double minQuantity;
        private Set<String> appearances;
        
        public Builder(String supplyType, int minParticipants, int maxParticipants, String purchasingGroupAbilitation, String quantity, String uom) {
        	super();

    		this.supplyType = supplyType;
    		this.minParticipants = minParticipants;
    		this.maxParticipants = maxParticipants;
    		this.purchasingGroupAbilitation = purchasingGroupAbilitation;
    		this.quantity = quantity;
    		this.uom = uom;

        }
        
        public Builder setMinQuantity (double minQuantity) {
        	this.minQuantity = minQuantity;
        	return this;
        }
        
        public Builder setAppearance (Set<String> appearances) {
        	this.appearances = appearances;
        	return this;
        }

		@Override
		public ByProduct build() {
			return new ByProduct(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
    	
    }


	public String getSupplyType() {
		return supplyType;
	}


	public int getMinParticipants() {
		return minParticipants;
	}


	public int getMaxParticipants() {
		return maxParticipants;
	}


	public String getPurchasingGroupAbilitation() {
		return purchasingGroupAbilitation;
	}


	public String getQuantity() {
		return quantity;
	}


	public double getMinQuantity() {
		return minQuantity;
	}


	public String getUom() {
		return uom;
	}


	public Set<String> getAppearances() {
		return appearances;
	}
    
    

    }



