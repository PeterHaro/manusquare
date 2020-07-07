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
import json.InnovationManagementRequest.InnovationManagerSkill;
import validation.JSONValidation;

public class InnovationManagementQuery {

	private String projectId;
	private static List<String> projectInnovationPhases;
	private static List<String> projectInnovationTypes;
	private static List<String> innovationManagerSkills;
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> language;


	public InnovationManagementQuery(String projectId, Set<Certification> certifications, Set<String> language) {
		super();
		this.projectId = projectId;
		this.certifications = certifications;
		this.language = language;
	}

	public InnovationManagementQuery(String projectId, List<String> projectInnovationPhases, List<String> projectInnovationTypes, Set<Certification> certifications, Set<String> language) {
		super();
		this.projectId = projectId;
		this.projectInnovationPhases = projectInnovationPhases;
		this.projectInnovationTypes = projectInnovationTypes;
		this.certifications = certifications;
		this.language = language;
	}

	public InnovationManagementQuery(String projectId, List<String> innovationManagerSkills, List<String> projectInnovationPhases, List<String> projectInnovationTypes, Set<Certification> certifications, Set<String> language) {
		super();
		this.projectId = projectId;
		this.innovationManagerSkills = innovationManagerSkills;
		this.projectInnovationPhases = projectInnovationPhases;
		this.projectInnovationTypes = projectInnovationTypes;
		this.certifications = certifications;
		this.language = language;
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

	public void setCertifications(Set<Certification> certifications) {
		this.certifications = certifications;
	}

	public double getSupplierMaxDistance() {
		return supplierMaxDistance;
	}

	public void setSupplierMaxDistance(double supplierMaxDistance) {
		this.supplierMaxDistance = supplierMaxDistance;
	}


	public Map<String, String> getCustomerLocationInfo() {
		return customerLocationInfo;
	}

	public void setCustomerLocationInfo(Map<String, String> customerLocationInfo) {
		this.customerLocationInfo = customerLocationInfo;
	}

	public Set<String> getLanguage() {
		return language;
	}

	public void setLanguage(Set<String> language) {
		this.language = language;
	}


	public List<String> getProjectInnovationPhases() {
		return projectInnovationPhases;
	}

	public void setProjectInnovationPhases(List<String> projectInnovationPhases) {
		InnovationManagementQuery.projectInnovationPhases = projectInnovationPhases;
	}

	public List<String> getProjectInnovationTypes() {
		return projectInnovationTypes;
	}

	public void setProjectInnovationTypes(List<String> projectInnovationTypes) {
		InnovationManagementQuery.projectInnovationTypes = projectInnovationTypes;
	}


	public static List<String> getInnovationManagerSkills() {
		return innovationManagerSkills;
	}

	public static void setInnovationManagerSkills(List<String> innovationManagerSkills) {
		InnovationManagementQuery.innovationManagerSkills = innovationManagerSkills;
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
		List<String> innovationManagerSkills = new ArrayList<String>();

		InnovationManagementRequest imr;

		if (JSONValidation.isJSONValid(filename)) {
			imr = new Gson().fromJson(filename, InnovationManagementRequest.class);
		} else {
			imr = new Gson().fromJson(new FileReader(filename), InnovationManagementRequest.class);
		}

		InnovationManagementQuery query = new InnovationManagementQuery();
		query.setProjectId(imr.getProjectId());

		if (imr.projectInnovationPhases != null || !imr.projectInnovationPhases.isEmpty()) {			
			innovationManagementPhases.addAll(imr.projectInnovationPhases);
			query.setProjectInnovationPhases(innovationManagementPhases);
		}

		if (imr.projectInnovationTypes != null || !imr.projectInnovationTypes.isEmpty()) {
			innovationManagementTypes.addAll(imr.projectInnovationTypes);
			query.setProjectInnovationTypes(innovationManagementTypes);
		}

		if (imr.innovationManagerSkills != null || !imr.innovationManagerSkills.isEmpty()) {
			for (InnovationManagerSkill skill : imr.innovationManagerSkills) {
				innovationManagerSkills.add(skill.skill);
			}
			query.setInnovationManagerSkills(innovationManagerSkills);
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
				query.setLanguage(languages);
			}

		}

		return query;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/InnovationManagementJSON.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		InnovationManagementQuery query = createQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);

		System.out.println("Innovation Phases: " + query.getProjectInnovationPhases());
		System.out.println("Innovation Types: " + query.getProjectInnovationTypes());
		System.out.println("Innovation Manager Skills: " + InnovationManagementQuery.getInnovationManagerSkills());
		System.out.println("Languages: " + query.getLanguage());
		for (Certification c : query.getCertifications()) {
		System.out.println("Certifications: " + c.getId());
		}

	}

}
