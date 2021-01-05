package query;

import java.util.Map;
import java.util.Set;

import edm.Certification;

public class Query {
	
	protected Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> languages;
	private Set<String> country;
	
	public Query(Set<Certification> certifications, Set<String> languages) {
		this.certifications = certifications;
		this.languages = languages;
	}
	
	public Query() {}
	
	//generic and mandatory attributes
		//	certifications
		//	country
		//	customerlocationInfo
		//	language
		//	supplierMaxDistance

	//cs attributes
		//  processes
	
	//im attributes
		//	innovationTypes
		//	innovationPhases
		//	sectors
		//	skills
	
	//bp attributes
		//	byProducts
		//	maxNumberOfParticipants
		//	minNumberOfParticipants
		//	mode
		//	purchasingGroupAbilitation
	
}
