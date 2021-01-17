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

import edm.Attribute;
import json.ByProductSharingRequest;
import json.ByProductSharingRequest.ByProductElement;
import ontology.OntologyOperations;
import query.BPQuery;

public class ByProductValidator extends Validator {

	public static boolean validQuery (String inputJson, OWLOntology ontology) {
		
		boolean valid = true;
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(ontology);
		Set<String> validByProductNames = new HashSet<String>();
		Set<String> validMaterials = new HashSet<String>();
		
		ByProductSharingRequest bpsr = null;;

		if (JSONValidator.isJSONValid(inputJson)) {
			bpsr = new Gson().fromJson(inputJson, ByProductSharingRequest.class);
		} else {
			try {
				bpsr = new Gson().fromJson(new FileReader(inputJson), ByProductSharingRequest.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		//if neither byProductName or AttributeMaterial is provided the query is invalid
		String byProductName = null;
		String validByProductName = null;
		
		for (ByProductElement element : bpsr.getByProductElements()) {
			byProductName = element.getByProductName();
			try {
				validByProductName = QueryValidator.validateMaterialName(byProductName, ontology, allOntologyClasses);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (validByProductName != null) {
				validByProductNames.add(validByProductName);
			}
			
			
			Set<Attribute> attributeSet = BPQuery.normaliseAttributes(element.getByProductAttributes());
			
			String validMaterialName = null;
			for (Attribute bp : attributeSet) {
				if (bp.getKey().equals("AttributeMaterial")) {
					try {
						validMaterialName = QueryValidator.validateMaterialName(bp.getValue(), ontology, allOntologyClasses);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (validMaterialName != null) {
						validMaterials.add(bp.getValue());
					}
				}
			}
			
		}
		
		if (validByProductNames.isEmpty() && validMaterials.isEmpty()) {
			valid = false;
		} else {
			valid = true;
		}
		
		
		return valid;
		
	}
}
