package edm;

import java.util.Objects;

public class Skill {
	
	private String name;

	public Skill(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals (Object o) {
		if ( o instanceof Skill && ((Skill) o).getName().equals(this.name) ) {
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
