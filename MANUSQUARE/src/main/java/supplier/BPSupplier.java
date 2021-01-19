package supplier;

import java.util.List;
import java.util.Objects;

import edm.ByProduct;


public class BPSupplier extends Supplier {
	

	private List<ByProduct> byProducts;
	
	
	private BPSupplier(Builder builder) {
		super(builder);
		
		this.byProducts = builder.byProducts;
	}
	
	
	public static class Builder extends Supplier.Builder<Builder> {
		private List<ByProduct> byProducts;
		
		public Builder(List<ByProduct> byProducts) {
			super();
			this.byProducts = byProducts;
		}

		@Override
		public BPSupplier build() {
			return new BPSupplier(this);
		}

		@Override
		protected Builder self() {
			return this;
		}
	}
	

	
    public List<ByProduct> getByProducts() {
		return byProducts;
	}


	@Override
    public boolean equals(Object o) {
        if (o instanceof BPSupplier && ((BPSupplier) o).getSupplierId().equals(this.getSupplierId())) {
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