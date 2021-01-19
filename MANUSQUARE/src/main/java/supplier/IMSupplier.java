package supplier;

import java.util.List;
import java.util.Objects;


public class IMSupplier extends Supplier {
	

	private List<String> skills;
	private List<String> innovationPhases;
	private List<String> innovationTypes;
	private List<String> sectors;
	
	private IMSupplier(Builder builder) {
		super(builder);
		
		this.skills = builder.skills;
		this.innovationPhases = builder.innovationPhases;
		this.innovationTypes = builder.innovationTypes;
		this.sectors = builder.sectors;
	}
	
	public static class Builder extends Supplier.Builder<Builder> {
		
		private List<String> skills;
		private List<String> innovationPhases;
		private List<String> innovationTypes;
		private List<String> sectors;
		
	
		public Builder() {}
		
		public Builder setSkills (List<String> skills) {
			this.skills = skills;
			return this;
		}
		
		public Builder setInnovationPhases (List<String> innovationPhases) {
			this.innovationPhases = innovationPhases;
			return this;
		}
		
		public Builder setInnovationTypes (List<String> innovationTypes) {
			this.innovationTypes = innovationTypes;
			return this;
		}
		
		public Builder setSectors (List<String> sectors) {
			this.sectors = sectors;
			return this;
		}

		@Override
		public IMSupplier build() {
			return new IMSupplier(this);
		}

		@Override
		protected Builder self() {
			return this;
		}

	}

	public List<String> getSkills() {
		return skills;
	}

	public List<String> getInnovationPhases() {
		return innovationPhases;
	}

	public List<String> getInnovationTypes() {
		return innovationTypes;
	}

	public List<String> getSectors() {
		return sectors;
	}

	@Override
    public boolean equals(Object o) {
        if (o instanceof IMSupplier && ((IMSupplier) o).getSupplierId().equals(this.getSupplierId())) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.getSupplierId());
    }

	

}
