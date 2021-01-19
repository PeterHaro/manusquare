package supplier;

import java.util.List;
import java.util.Objects;

import edm.Process;


public class CSSupplier extends Supplier {
	

	private List<Process> processes;
	
	
	private CSSupplier(Builder builder) {
		super(builder);
		
		this.processes = builder.processes;
	}
	
	public static class Builder extends Supplier.Builder<Builder> {
		private List<Process> processes;
		
		public Builder (List<Process> processes) {
			super();
			this.processes = processes;
		}

		@Override
		public CSSupplier build() {
			return new CSSupplier(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
		
		
	}

	public List<Process> getProcesses() {
		return processes;
	}
	
	@Override
    public boolean equals(Object o) {
        if (o instanceof CSSupplier && ((CSSupplier) o).getSupplierId().equals(this.getSupplierId())) {
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