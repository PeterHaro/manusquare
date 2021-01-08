package sparqlresult;

public class BPSparqlResult extends SparqlResult {
	
	//mandatory
	private String wsProfileId;
	private String byProductName;
	private String byProductSupplyType;	
	private String byProductMinParticipants;
	private String byProductMaxParticipants;
	private String purchasingGroupAbilitation;
	private String byProductQuantity;
	private String byProductMinQuantity;
	private String byProductUOM;
	
	//optional
	private String byProductDeadline;
	private String appearance;
	
	
	private BPSparqlResult(Builder builder) {
		super(builder);
		this.wsProfileId = builder.wsProfileId;
		this.byProductName = builder.byProductName;
		this.byProductSupplyType = builder.byProductSupplyType;
		this.byProductMinParticipants = builder.byProductMinParticipants;
		this.byProductMaxParticipants = builder.byProductMaxParticipants;
		this.purchasingGroupAbilitation = builder.purchasingGroupAbilitation;
		this.byProductQuantity = builder.byProductQuantity;
		this.byProductMinQuantity = builder.byProductMinQuantity;
		this.byProductUOM = builder.byProductUOM;
		this.byProductDeadline = builder.byProductDeadline;
		this.appearance = builder.appearance;
	}
	
	public static class Builder extends SparqlResult.Builder<Builder> {
		
		//mandatory
		private String wsProfileId;
		private String byProductName;
		private String byProductSupplyType;	
		private String byProductMinParticipants;
		private String byProductMaxParticipants;
		private String purchasingGroupAbilitation;
		private String byProductQuantity;
		private String byProductMinQuantity;
		private String byProductUOM;
		
		//optional
		private String byProductDeadline;
		private String appearance;
		
		public Builder(String wsProfileId, String byProductName, String byProductSupplyType, String byProductMinParticipants, String byProductMaxParticipants,
				String purchasingGroupAbilitation, String byProductQuantity, String byProductMinQuantity, String byProductUOM) {
			super();
			this.wsProfileId = wsProfileId;
			this.byProductName = byProductName;
			this.byProductSupplyType = byProductSupplyType;
			this.byProductMinParticipants = byProductMinParticipants;
			this.byProductMaxParticipants = byProductMaxParticipants;
			this.purchasingGroupAbilitation = purchasingGroupAbilitation;
			this.byProductQuantity = byProductQuantity;
			this.byProductMinQuantity = byProductMinQuantity;
			this.byProductUOM = byProductUOM;
		}
		
		public Builder setByProductDeadline(String byProductDeadline) {
			this.byProductDeadline = byProductDeadline;
			return this;
		}
		
		public Builder setAppearance(String appearance) {
			this.appearance = appearance;
			return this;
		}
		

		@Override
		public BPSparqlResult build() {
			return new BPSparqlResult(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
	
	}


	public String getWsProfileId() {
		return wsProfileId;
	}

	public String getAppearance() {
		return appearance;
	}

	public String getByProductName() {
		return byProductName;
	}

	public String getByProductSupplyType() {
		return byProductSupplyType;
	}

	public String getByProductDeadline() {
		return byProductDeadline;
	}

	public String getByProductMinParticipants() {
		return byProductMinParticipants;
	}

	public String getByProductMaxParticipants() {
		return byProductMaxParticipants;
	}

	public String getPurchasingGroupAbilitation() {
		return purchasingGroupAbilitation;
	}

	public String getByProductQuantity() {
		return byProductQuantity;
	}

	public String getByProductMinQuantity() {
		return byProductMinQuantity;
	}

	public String getByProductUOM() {
		return byProductUOM;
	}

	public String toString() {
		return "SupplierID: " + this.getSupplierId() + "\n" + "WSProfileID: " + this.getWsProfileId() + "\nBy-product name: " + this.getByProductName();
	}
	
	
}
