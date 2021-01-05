package query;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

public class IMQuery {

	//mandatory attributes
	private List<String> innovationPhases;
	private List<String> innovationTypes;
	private List<String> skills;
	private List<String> sectors;

	//optional attributes
	private Set<Certification> certifications;
	private Set<String> languages;

	public Set<Certification> getCertifications() {
		return certifications;
	}

	public Set<String> getLanguages() {
		return languages;
	}

	public List<String> getInnovationPhases() {
		return innovationPhases;
	}

	public List<String> getInnovationTypes() {
		return innovationTypes;
	}

	public List<String> getSkills() {
		return skills;
	}

	public List<String> getSectors() {
		return sectors;
	}


	private IMQuery(IMQueryBuilder builder) {
		this.skills = builder.skills;
		this.innovationPhases = builder.innovationPhases;
		this.innovationTypes = builder.innovationTypes;
		this.sectors = builder.sectors;
		this.certifications = builder.certifications;
		this.languages = builder.languages;

	}

	public static class IMQueryBuilder {

		private Set<String> languages;
		private Set<Certification> certifications;
		private List<String> innovationPhases;
		private List<String> innovationTypes;
		private List<String> skills;
		private List<String> sectors;
		
		public IMQueryBuilder(List<String> skills, List<String> innovationPhases, List<String> innovationTypes, List<String> sectors) {		
			this.skills = skills;
			this.innovationPhases = innovationPhases;
			this.innovationTypes = innovationTypes;
			this.sectors = sectors;

		}

		public IMQueryBuilder setCertifications(Set<Certification> certifications) {
			this.certifications = certifications;
			return this;
		}

		public IMQueryBuilder setLanguage(Set<String> language) {
			this.languages = language;
			return this;
		}	

		public IMQuery build() {
			return new IMQuery(this);
		}
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
	public static IMQuery createQuery (String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {

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

		if (imr.projectInnovationPhases != null || !imr.projectInnovationPhases.isEmpty()) {			

			for (String ip : imr.projectInnovationPhases) {
				innovationManagementPhases.add(QueryValidator.validateInnovationPhase(ip, onto, allOntologyClasses));
			}
		}

		if (imr.projectInnovationTypes != null || !imr.projectInnovationTypes.isEmpty()) {

			for (String it : imr.projectInnovationTypes) {
				innovationManagementTypes.add(QueryValidator.validateInnovationType(it, onto, allOntologyClasses));
			}
		}

		if (imr.innovationManagerSkills != null || !imr.innovationManagerSkills.isEmpty()) {
			for (InnovationManagerSkill skill : imr.innovationManagerSkills) {
				innovationManagementSkills.add(QueryValidator.validateSkill(skill.skill, onto, allOntologyClasses));
			}
		}

		if (imr.innovationManagerSectors != null || !imr.innovationManagerSectors.isEmpty()) {
			for (InnovationManagerSector sector : imr.innovationManagerSectors) {
				innovationManagementSectors.add(QueryValidator.validateSector(sector.sector, onto, allOntologyClasses));
			}
		}

		IMQuery query = null;

		if (imr.innovationManagerAttributes != null || !imr.innovationManagerAttributes.isEmpty()) {

			for (InnovationManagerAttribute attributes :  imr.innovationManagerAttributes) {
				if (attributes.attributeKey.equalsIgnoreCase("certification")) {
					certifications.add(new Certification(attributes.attributeValue));
				}
				if (attributes.attributeKey.equalsIgnoreCase("language")) {
					languages.add(attributes.attributeValue);
				}

			}
		}

			if ((certifications != null || !certifications.isEmpty())
					&& (languages != null || !languages.isEmpty())) {

				query = new IMQuery.IMQueryBuilder(innovationManagementSkills, innovationManagementPhases, innovationManagementTypes, innovationManagementSectors).
						setCertifications(certifications).
						setLanguage(languages).
						build();

			} else if (languages == null || languages.isEmpty()) {
				
				query = new IMQuery.IMQueryBuilder(innovationManagementSkills, innovationManagementPhases, innovationManagementTypes, innovationManagementSectors).
						setCertifications(certifications).
						build();
			}

		return query;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/TESTING_INNOVATION_MANAGEMENT/Test_IM_8.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		IMQuery query = createQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);

		System.out.println("Innovation Phases: " + query.getInnovationPhases());
		System.out.println("Innovation Types: " + query.getInnovationTypes());
		System.out.println("Innovation Manager Skills: " + query.getSkills());
		System.out.println("Sectors: " + query.getSectors());
		System.out.println("Languages: " + query.getLanguages());
		for (Certification c : query.getCertifications()) {
			System.out.println("Certifications: " + c.getId());
		}

	}

}
