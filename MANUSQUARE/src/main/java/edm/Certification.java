package edm;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import utilities.StringUtilities;

public class Certification {
	
	private String id;
	private String qualification;
	private String authority;
	private String hasCertification;
	
	public Certification(String id, String qualification, String authority, String hasCertification) {
		super();
		this.id = id;
		this.qualification = qualification;
		this.authority = authority;
		this.hasCertification = hasCertification;
	}
	
	public Certification(String id) {
		super();
		this.id = id;
	}
	
	public Certification() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getHasCertification() {
		return hasCertification;
	}

	public void setHasCertification(String hasCertification) {
		this.hasCertification = hasCertification;
	}
	
	@Override
	public boolean equals (Object o) {
		if ( o instanceof Certification && ((Certification) o).getId().equals(this.id) ) {
			return true;
		} else {
			return false;
		}
	}
	
	  @Override
	    public int hashCode() {
	        return Objects.hash(id);
	    }
		

}
