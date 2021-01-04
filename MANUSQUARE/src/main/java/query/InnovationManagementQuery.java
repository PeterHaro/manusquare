package query;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edm.Certification;
import json.InnovationManagementRequest;
import json.InnovationManagementRequest.InnovationManagerAttribute;
import json.InnovationManagementRequest.InnovationManagerSector;
import json.InnovationManagementRequest.InnovationManagerSkill;
import ontology.OntologyOperations;
import validation.JSONValidator;
import validation.QueryValidator;

public class InnovationManagementQuery {

	private String projectId;
	private static List<String> innovationPhases;
	private static List<String> innovationTypes;
	private static List<String> skills;
	private static List<String> sectors;
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> languages;


	public InnovationManagementQuery(String projectId, List<String> skills, List<String> innovationPhases, List<String> innovationTypes, List<String> sectors, Set<Certification> certifications, Set<String> languages) {
		super();
		this.projectId = projectId;
		InnovationManagementQuery.skills = skills;
		InnovationManagementQuery.innovationPhases = innovationPhases;
		InnovationManagementQuery.innovationTypes = innovationTypes;
		InnovationManagementQuery.sectors = sectors;
		this.certifications = certifications;
		this.languages = languages;
	}

	public InnovationManagementQuery() {
	}
	
	

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public Set<Certification> getCertifications() {
		return certifications;
	}


	public double getSupplierMaxDistance() {
		return supplierMaxDistance;
	}

	public Map<String, String> getCustomerLocationInfo() {
		return customerLocationInfo;
	}

	public Set<String> getLanguages() {
		return languages;
	}

	

	public void setLanguages(Set<String> languages) {
		this.languages = languages;
	}

	public List<String> getInnovationPhases() {
		return innovationPhases;
	}

	public void setInnovationPhases(List<String> projectInnovationPhases) {
		InnovationManagementQuery.innovationPhases = projectInnovationPhases;
	}

	public List<String> getInnovationTypes() {
		return innovationTypes;
	}

	public void setInnovationTypes(List<String> projectInnovationTypes) {
		InnovationManagementQuery.innovationTypes = projectInnovationTypes;
	}


	public static List<String> getSkills() {
		return skills;
	}

	public static void setSkills(List<String> innovationManagerSkills) {
		InnovationManagementQuery.skills = innovationManagerSkills;
	}
	

	public static List<String> getSectors() {
		return sectors;
	}

	public void setCertifications(Set<Certification> certifications) {
		this.certifications = certifications;
	}

	public static void setSectors(List<String> sectors) {
		InnovationManagementQuery.sectors = sectors;
	}

	/**
	 * Parses a json file and creates a InnovationManagerQuery object representing the input provided by a consumer in the RFI (Request for Innovation) establishment process.
	 *
	 * @param filename the path to the input json file.
	 * @param onto     the MANUSQUARE ontology.
	 * @return a InnovationManagerQuery object representing processes/materials/certifications requested by a consumer.
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws IOException
	 */
	public static InnovationManagementQuery createQuery (String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {

		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> languages = new HashSet<String>();
		List<String> innovationManagementPhases = new ArrayList<String>();
		List<String> innovationManagementTypes = new ArrayList<String>();
		List<String> innovationManagementSkills = new ArrayList<String>();
		List<String> innovationManagementSectors = new ArrayList<String>();
		
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		InnovationManagementRequest imr;

		if (JSONValidator.isJSONValid(filename)) {
			imr = new Gson().fromJson(filename, InnovationManagementRequest.class);
		} else {
			imr = new Gson().fromJson(new FileReader(filename), InnovationManagementRequest.class);
		}

		InnovationManagementQuery query = new InnovationManagementQuery();
		query.setProjectId(imr.getProjectId());

		if (imr.projectInnovationPhases != null || !imr.projectInnovationPhases.isEmpty()) {			
			
			for (String ip : imr.projectInnovationPhases) {
				innovationManagementPhases.add(QueryValidator.validateInnovationPhase(ip, onto, allOntologyClasses));
			}
			query.setInnovationPhases(innovationManagementPhases);
		}

		if (imr.projectInnovationTypes != null || !imr.projectInnovationTypes.isEmpty()) {

			for (String it : imr.projectInnovationTypes) {
				innovationManagementTypes.add(QueryValidator.validateInnovationType(it, onto, allOntologyClasses));
			}
			
			query.setInnovationTypes(innovationManagementTypes);
		}

		if (imr.innovationManagerSkills != null || !imr.innovationManagerSkills.isEmpty()) {
			for (InnovationManagerSkill skill : imr.innovationManagerSkills) {
				innovationManagementSkills.add(QueryValidator.validateSkill(skill.skill, onto, allOntologyClasses));
			}
			InnovationManagementQuery.setSkills(innovationManagementSkills);
		}
		
		if (imr.innovationManagerSectors != null || !imr.innovationManagerSectors.isEmpty()) {
			for (InnovationManagerSector sector : imr.innovationManagerSectors) {
				innovationManagementSectors.add(QueryValidator.validateSector(sector.sector, onto, allOntologyClasses));
			}
			InnovationManagementQuery.setSectors(innovationManagementSectors);
		}
		


		if (imr.innovationManagerAttributes != null || !imr.innovationManagerAttributes.isEmpty()) {

			for (InnovationManagerAttribute attributes :  imr.innovationManagerAttributes) {
				if (attributes.attributeKey.equalsIgnoreCase("certification")) {
					certifications.add(new Certification(attributes.attributeValue));
				}
				if (attributes.attributeKey.equalsIgnoreCase("language")) {
					languages.add(attributes.attributeValue);
				}

			}
			
			if (certifications != null || !certifications.isEmpty()) {
				query.setCertifications(certifications);
			}
			
			if (languages != null || !languages.isEmpty()) {
				query.setLanguages(languages);
			}

		}

		return query;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/SUPSI/Elias_100920.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		InnovationManagementQuery query = createQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);

		System.out.println("Innovation Phases: " + query.getInnovationPhases());
		System.out.println("Innovation Types: " + query.getInnovationTypes());
		System.out.println("Innovation Manager Skills: " + InnovationManagementQuery.getSkills());
		System.out.println("Sectors: " + InnovationManagementQuery.getSectors());
		System.out.println("Languages: " + query.getLanguages());
		for (Certification c : query.getCertifications()) {
		System.out.println("Certifications: " + c.getId());
		}

	}

}
