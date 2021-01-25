package validation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import json.InnovationManagementRequest;
import json.InnovationManagementRequest.InnovationManagerSector;
import json.InnovationManagementRequest.InnovationManagerSkill;
import ontology.OntologyOperations;

public class InnovationManagementValidator extends Validator {


	public static boolean validQuery (String inputJson, OWLOntology ontology) {

		boolean valid = true;

		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(ontology);
		Set<String> validInnovationManagementPhases = new HashSet<String>();
		Set<String> validInnovationManagementTypes = new HashSet<String>();
		Set<String> validSkills = new HashSet<String>();
		Set<String> validSectors = new HashSet<String>();
		InnovationManagementRequest imr = null;

		if (JSONValidator.isJSONValid(inputJson)) {
			imr = new Gson().fromJson(inputJson, InnovationManagementRequest.class);
		} else {
			try {
				imr = new Gson().fromJson(new FileReader(inputJson), InnovationManagementRequest.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		String validInnovationManagementPhase = null;
		String validInnovationManagementType = null;
		String validSkill = null;
		String validSector = null;

		Set<String> skills = new HashSet<String>();
		for (InnovationManagerSkill sk : imr.getInnovationManagerSkills()) {
			skills.add(sk.getSkill());
		}
		Set<String> sectors = new HashSet<String>();
		for (InnovationManagerSector sec : imr.getInnovationManagerSectors()) {
			sectors.add(sec.getSector());
		}

		//if either phases, types, skills or sectors are empty validation is false
		if (!imr.getProjectInnovationPhases().isEmpty() || !imr.getProjectInnovationTypes().isEmpty() || !skills.isEmpty() || !sectors.isEmpty()) {

			for (String ip : imr.getProjectInnovationPhases()) {
				try {
					validInnovationManagementPhase = QueryValidator.validateInnovationPhase(ip, ontology, allOntologyClasses);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (validInnovationManagementPhase != null) {
					validInnovationManagementPhases.add(validInnovationManagementPhase);
				}

			}

			for (String it : imr.getProjectInnovationTypes()) {
				try {
					validInnovationManagementType = QueryValidator.validateInnovationPhase(it, ontology, allOntologyClasses);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (validInnovationManagementType != null) {
					validInnovationManagementTypes.add(validInnovationManagementType);
				}

			}

			for (InnovationManagerSkill sk : imr.getInnovationManagerSkills()) {
				try {
					validSkill = QueryValidator.validateSkill(sk.getSkill(), ontology, allOntologyClasses);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (validSkill != null) {
					validSkills.add(validSkill);
				}

			}

			for (InnovationManagerSector sec : imr.getInnovationManagerSectors()) {
				try {
					validSector = QueryValidator.validateSector(sec.getSector(), ontology, allOntologyClasses);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (validSector != null) {
					validSectors.add(validSector);
				}

			}

			if (validInnovationManagementPhases.isEmpty() && validInnovationManagementTypes.isEmpty() && validSkills.isEmpty() && validSectors.isEmpty()) {
				valid = false;
			} else {
				valid = true;
			}

		} else {
			valid = false;
		}

		return valid;

	}


}
