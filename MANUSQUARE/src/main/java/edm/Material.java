package edm;

import java.util.Objects;

public class Material {
	
	private String name;

	public Material(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
//	@Override
//	public boolean equals (Object object) {
//		boolean result = false;
//		if (object == null || object.getClass() != getClass()) {
//			result = false;
//		} else {
//			Material material = (Material) object;
//			if (this.name == material.getName()) {
//				result = true;
//			}
//		}
//		return result;
//	}
	
	@Override
	public boolean equals (Object o) {
		if ( o instanceof Material && ((Material) o).getName().equals(this.name) ) {
			return true;
		} else {
			return false;
		}
	}
	
	  @Override
	    public int hashCode() {
	        return Objects.hash(name);
	    }
	
	

}
