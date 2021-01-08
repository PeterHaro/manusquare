package sparqlresult;

public class IMSparqlResult extends SparqlResult {
	
	private String sector;
	private String skill;
	private String innovationType;
	private String innovationPhase;
	
	private IMSparqlResult(Builder builder) {
		super(builder);
		this.sector = builder.sector;
		this.skill = builder.skill;
		this.innovationType = builder.innovationType;
		this.innovationPhase = builder.innovationPhase;
	}
	
	public static class Builder extends SparqlResult.Builder<Builder> {
		
		private String sector;
		private String skill;
		private String innovationType;
		private String innovationPhase;
		
		public Builder(String sector, String skill, String innovationType, String innovationPhase) {
			super();
			this.sector = sector;
			this.skill = skill;
			this.innovationType = innovationType;
			this.innovationPhase = innovationPhase;

		}
		
		@Override
		public IMSparqlResult build() {
			
			return new IMSparqlResult(this);
		}


		@Override
		protected Builder self() {
			return this;
		}
		
	}
	

	public String getSector() {
		return sector;
	}

	public String getSkill() {
		return skill;
	}

	public String getInnovationType() {
		return innovationType;
	}


	public String getInnovationPhase() {
		return innovationPhase;
	}

}
