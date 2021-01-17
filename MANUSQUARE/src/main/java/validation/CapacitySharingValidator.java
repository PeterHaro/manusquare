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

import json.RequestForQuotation;
import json.RequestForQuotation.ProjectAttributeKeys;
import ontology.OntologyOperations;

public class CapacitySharingValidator extends Validator {
	
	//if processes are not valid matching should return empty results
	public static boolean validQuery (String inputJson, OWLOntology ontology) {
		
		boolean valid = true;
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(ontology);
		String processName = null;
		Set<String> processNames = new HashSet<String>();
		
		RequestForQuotation rfq = null;

		if (JSONValidator.isJSONValid(inputJson)) {
			rfq = new Gson().fromJson(inputJson, RequestForQuotation.class);
		} else {
			try {
				rfq = new Gson().fromJson(new FileReader(inputJson), RequestForQuotation.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if (rfq.getProjectAttributes() == null || rfq.getProjectAttributes().isEmpty()) {

			valid = false;
			System.err.println("Processes must be included - returning empty CSQuery!");

		} else {
			//need to validate the process names according to concepts in the ontology before we continue with the rest of the process
			for (ProjectAttributeKeys projectAttributes : rfq.getProjectAttributes()) {

				try {
					processName = QueryValidator.validateProcessName(projectAttributes.processName, ontology, allOntologyClasses);
				} catch (IOException e) {

					e.printStackTrace();
				}

				if (processName != null)

					processNames.add(processName);
			}
		}

		if (processNames.isEmpty()) {
			valid = false;
		}
		
		
		return valid;
		
	}

}
