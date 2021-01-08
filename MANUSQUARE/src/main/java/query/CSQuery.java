package query;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edm.Attribute;
import edm.Certification;
import edm.Material;
import edm.Process;
import exceptions.NoProcessException;
import json.RequestForQuotation;
import json.RequestForQuotation.ProjectAttributeKeys;
import json.RequestForQuotation.SupplierAttributeKeys;
import ontology.OntologyOperations;
import utilities.StringUtilities;
import validation.JSONValidator;
import validation.QueryValidator;
import validation.UnitOfMeasurementConverter;

public class CSQuery {

	//mandatory attributes
	private Set<Process> processes;
	
	//optional attributes
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	private Set<String> languages;

	private CSQuery(CSQueryBuilder builder) {
		this.processes = builder.processes;
		this.certifications = builder.certifications;
		this.supplierMaxDistance = builder.supplierMaxDistance;
		this.customerLocationInfo = builder.customerLocationInfo;
		this.languages = builder.languages;

	}
	
	public static class CSQueryBuilder {
		
		private Set<Process> processes;
		
		private Set<Certification> certifications;
		private double supplierMaxDistance;
		private Map<String, String> customerLocationInfo;
		private Set<String> languages;
		
		public CSQueryBuilder(Set<Process> processes) {
			this.processes = processes;

		}
		
		public CSQueryBuilder setCertifications(Set<Certification> certifications) {
			this.certifications = certifications;
			return this;
		}
		
		public CSQueryBuilder setSupplierMaxDistance(double supplierMaxDistance) {
			this.supplierMaxDistance = supplierMaxDistance;
			return this;
		}
		
		public CSQueryBuilder setCustomerLocationInfo(Map<String, String> customerLocationInfo) {
			this.customerLocationInfo = customerLocationInfo;
			return this;
		}
		
		public CSQueryBuilder setLanguage(Set<String> language) {
			this.languages = language;
			return this;
		}

		
		public CSQuery build() {
			return new CSQuery(this);
		}
		
	}
	
	public Set<Process> getProcesses() {
		return processes;
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

	public Set<String> getLanguage() {
		return languages;
	}
	
	public Set<Attribute> getAttributes() {
		Set<Attribute> attributes = new HashSet<Attribute>();
		Set<Process> processes = getProcesses();
		for (Process p : processes) {
			attributes.addAll(p.getAttributes());
		}

		return attributes;
	}


	/**
	 * Parses a json file and creates a ConsumerQuery object representing the input provided by a consumer in the RFQ establishment process.
	 *
	 * @param filename the path to the input json file.
	 * @param onto     the MANUSQUARE ontology.
	 * @return a ConsumerQuery object representing processes/materials/certifications requested by a consumer.
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws IOException
	 */
	public static CSQuery createConsumerQuery(String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {
		Set<Process> processes = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> processNames = new HashSet<String>();
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);
		Set<String> languages = new HashSet<String>();

		RequestForQuotation rfq;

		if (JSONValidator.isJSONValid(filename)) {
			rfq = new Gson().fromJson(filename, RequestForQuotation.class);
		} else {
			rfq = new Gson().fromJson(new FileReader(filename), RequestForQuotation.class);
		}

		if (rfq.projectAttributes == null || rfq.projectAttributes.isEmpty()) {
			throw new NoProcessException("Processes must be included!");
		} else {
			for (ProjectAttributeKeys projectAttributes : rfq.projectAttributes) {
				processNames.add(projectAttributes.processName);
			}
		}

		//get attribute and materials and map them to process
		for (String process : processNames) {
			Set<Attribute> attributeSet = new HashSet<Attribute>();
			Set<Material> materialSet = new HashSet<Material>();
			Set<String> equivalentProcesses = new HashSet<String>();

			//get the materials and other attributes if they´re present
			for (ProjectAttributeKeys projectAttributes : rfq.projectAttributes) {
				if (!projectAttributes.attributeKey.isEmpty()) {
					if ((!projectAttributes.attributeKey.equalsIgnoreCase("material") && !projectAttributes.attributeKey.equalsIgnoreCase("attributeMaterial")) && projectAttributes.processName.equals(process)) {
						//check if uom is included in JSON
						if (projectAttributes.unitOfMeasure != null) {
							if(StringUtilities.isValidNumber(projectAttributes.attributeValue)) {
								attributeSet.add(new Attribute(projectAttributes.attributeKey, UnitOfMeasurementConverter.convertUnitOfMeasurement(projectAttributes.attributeValue, projectAttributes.unitOfMeasure), projectAttributes.unitOfMeasure));
							} else {
								attributeSet.add(new Attribute(projectAttributes.attributeKey, projectAttributes.attributeValue, projectAttributes.unitOfMeasure));
							}
						} else {
							attributeSet.add(new Attribute(projectAttributes.attributeKey, projectAttributes.attributeValue, projectAttributes.unitOfMeasure));
						}
					} else if ((projectAttributes.attributeKey.equalsIgnoreCase("material") || projectAttributes.attributeKey.equalsIgnoreCase("attributeMaterial")) && projectAttributes.processName.equals(process)) { //get the materials
						materialSet.add(new Material(projectAttributes.attributeValue));
					}
				} else {
					System.out.println("There are no attributes in the JSON file!");
				}
			}

			//10.02.2020: get equivalent process concepts to process (after we have ensured the process is included in the ontology)
			process = QueryValidator.validateProcessName(process, onto, allOntologyClasses);
			equivalentProcesses = OntologyOperations.getEquivalentClassesAsString(OntologyOperations.getClass(process, onto), onto);

			//if there are no equivalent processes in the ontology we just the process described by the consumer to the set of processes
			if (equivalentProcesses == null || equivalentProcesses.isEmpty()) {

				processes.add(new Process(process, QueryValidator.validateMaterials(materialSet, onto, allOntologyClasses), QueryValidator.validateAttributeKeys(attributeSet, onto, allOntologyClasses)));

			} else {//if there are equivalent processes in the ontology we add those to the set of processes together with the process included by the consumer
				for (String s : equivalentProcesses) {

					processes.add(new Process(process, QueryValidator.validateMaterials(materialSet, onto, allOntologyClasses), QueryValidator.validateAttributeKeys(attributeSet, onto, allOntologyClasses), equivalentProcesses));
					processes.add(new Process(s, QueryValidator.validateMaterials(materialSet, onto, allOntologyClasses), QueryValidator.validateAttributeKeys(attributeSet, onto, allOntologyClasses), updateEquivalenceSet(equivalentProcesses, s, process)));
				}
			}

		}

		CSQuery query = null;

		//add geographical information to consumer query
		double supplierMaxDistance = rfq.supplierMaxDistance;
		Map<String, String> customerInformation = rfq.customer.customerInfo;

		//get certifications if they are specified by the consumer
		if (rfq.supplierAttributes == null || rfq.supplierAttributes.isEmpty()) {
			//if no attributes nor certifications, we only add the processes to the ConsumerQuery object
			//assuming that supplierMaxDistance and customerInformation (name, location, coordinates) are always included
			
					query = new CSQuery.CSQueryBuilder(processes).
					setSupplierMaxDistance(supplierMaxDistance).
					setCustomerLocationInfo(customerInformation).
					build();

		} else {
			for (SupplierAttributeKeys supplierAttributes : rfq.supplierAttributes) {
				if (supplierAttributes.attributeKey.equalsIgnoreCase("certification")) {
					certifications.add(new Certification(supplierAttributes.attributeValue));
				}
				
				if (supplierAttributes.attributeKey.equalsIgnoreCase("Language")) {
					languages.add(supplierAttributes.attributeValue);
				}
			}
			
			if (languages != null) {
				
				query = new CSQuery.CSQueryBuilder(processes).
						setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
						setSupplierMaxDistance(supplierMaxDistance).
						setCustomerLocationInfo(customerInformation).
						setLanguage(languages).
						build();
				
			} 
			//if not we omit languages, and add only certifications from the supplier attributes
			query = new CSQuery.CSQueryBuilder(processes).
					setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
					setSupplierMaxDistance(supplierMaxDistance).
					setCustomerLocationInfo(customerInformation).
					build();

		}

		return query;
	}

	/**
	 * Removes and adds a process to a set of processes
	 *
	 * @param equivalentProcesses the set of processes
	 * @param toBeRemoved         the process to be removed from the set of processes
	 * @param toBeAdded           the process to be added to the set of processes
	 * @return set of processes
	 * Mar 27, 2020
	 */
	private static Set<String> updateEquivalenceSet(Set<String> equivalentProcesses, String toBeRemoved, String toBeAdded) {
		Set<String> updatedEquivalentProcesses = new HashSet<>(equivalentProcesses);
		updatedEquivalentProcesses.remove(toBeRemoved);
		updatedEquivalentProcesses.add(toBeAdded);

		return updatedEquivalentProcesses;
	}


	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./files/Test1.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		CSQuery query = createConsumerQuery(filename, onto);
		System.out.println("Printing query from JSON file: " + filename);
		
		System.out.println("Printing processes: ");
		for (Process p : query.getProcesses()) {
			System.out.println(p.getName());
		}

		for (Process p : query.getProcesses()) {
			System.out.println("Process: " + p.getName());
			
			if (p.getMaterials() != null) {
			System.out.println("Number of materials: " + p.getMaterials().size());
			for (Material m : p.getMaterials()) {
				System.out.println("   Material: " + m.getName());
			}
			}
			for (Attribute a : p.getAttributes()) {
				System.out.println("   Attribute: " + a.getKey());
			}
		}

		for (Certification cert : query.getCertifications()) {
			System.out.println("Certification: " + cert.getId());
		}

		System.out.println("Max supplier distance: " + query.getSupplierMaxDistance());
		
		System.out.println("Languages required: " + query.getLanguage());
	}

}