package sparqlresult;


public class CSSparqlResult extends SparqlResult {
	
	private String process;
	
	private CSSparqlResult(Builder builder) {
		super(builder);
		this.process = builder.process;
	}
	
	public static class Builder extends SparqlResult.Builder<Builder> {

		private String process;

		
		public Builder(String process) {
			super();
			this.process = process;

		}
		
		@Override
		public CSSparqlResult build() {
			
			return new CSSparqlResult(this);
		}


		@Override
		protected Builder self() {
			return this;
		}
		
	}

	public String getProcess() {
		return process;
	}


}
