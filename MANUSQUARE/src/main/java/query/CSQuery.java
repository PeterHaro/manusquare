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
import edm.Process;
import json.RequestForQuotation;
import json.RequestForQuotation.ProjectAttributeKeys;
import json.RequestForQuotation.SupplierAttributeKeys;
import ontology.OntologyOperations;
import query.BPQuery.BPQueryBuilder;
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
	private Set<String> countries;

	private CSQuery(CSQueryBuilder builder) {
		this.processes = builder.processes;
		this.certifications = builder.certifications;
		this.supplierMaxDistance = builder.supplierMaxDistance;
		this.customerLocationInfo = builder.customerLocationInfo;
		this.languages = builder.languages;
		this.countries = builder.countries;

	}

	public static class CSQueryBuilder {

		private Set<Process> processes;

		private Set<Certification> certifications;
		private double supplierMaxDistance;
		private Map<String, String> customerLocationInfo;
		private Set<String> languages;
		private Set<String> countries;

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
		
		public CSQueryBuilder setCountries(Set<String> country) {
			this.countries = country;
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
	
	public Set<String> getCountry() {
		return countries;
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
		CSQuery query = null;
		Set<Process> processes = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		String processName = null;
		Set<String> processNames = new HashSet<String>();
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);
		Set<String> languages = new HashSet<String>();
		Set<String> countries = new HashSet<String>();

		RequestForQuotation rfq;

		if (JSONValidator.isJSONValid(filename)) {
			rfq = new Gson().fromJson(filename, RequestForQuotation.class);
		} else {
			rfq = new Gson().fromJson(new FileReader(filename), RequestForQuotation.class);
		}


		if (rfq.getProjectAttributes() == null || rfq.getProjectAttributes().isEmpty()) {

			query = null;
			System.err.println("Processes must be included - returning empty CSQuery!");

		} else {
			//need to validate the process names according to concepts in the ontology before we continue with the rest of the process
			for (ProjectAttributeKeys projectAttributes : rfq.getProjectAttributes()) {

				processName = QueryValidator.validateProcessName(projectAttributes.processName, onto, allOntologyClasses);

				if (processName != null)

					processNames.add(processName);
			}
		}

		if (processNames.isEmpty()) {

			query = null;
			System.err.println("Processes must be included - returning empty CSQuery!");

		} else {

			//get attribute and materials and map them to process
			for (String process : processNames) {

				Set<Attribute> attributeSet = new HashSet<Attribute>();
				Set<String> materialSet = new HashSet<String>();
				Set<String> equivalentProcesses = new HashSet<String>();
				String validatedProcessName = null;

				//get the materials and other attributes if they´re present
				for (ProjectAttributeKeys projectAttributes : rfq.getProjectAttributes()) {
					if (!projectAttributes.attributeKey.isEmpty()) {
						
						//Need to validate all process names
						validatedProcessName = QueryValidator.validateProcessName(projectAttributes.processName, onto, allOntologyClasses);

						if ((!projectAttributes.attributeKey.equalsIgnoreCase("AttributeMaterial")) && validatedProcessName.equals(process)) {
							
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
						} else if ((projectAttributes.attributeKey.equalsIgnoreCase("AttributeMaterial")) && validatedProcessName.equals(process)) { //get the materials

							materialSet.add(projectAttributes.attributeValue);
						}
					} else {
						System.out.println("There are no attributes in the JSON file!");
					}
				}

				//get equivalent process concepts to process (after we have ensured the process is included in the ontology)

				equivalentProcesses = OntologyOperations.getEquivalentClassesAsString(OntologyOperations.getClass(process, onto), onto);
								
				//if there are no equivalent processes in the ontology we just the process described by the consumer to the set of processes
				if (equivalentProcesses == null || equivalentProcesses.isEmpty()) {

					processes.add(new Process.Builder()
							.setName(process)
							.setMaterials(QueryValidator.validateMaterials(materialSet, onto, allOntologyClasses))
							.setAttributes(attributeSet)
							.build()
							);

				} else {//if there are equivalent processes in the ontology we add those to the set of processes together with the process included by the consumer

					for (String s : equivalentProcesses) {

						processes.add(new Process.Builder()
								.setName(process)
								.setMaterials(QueryValidator.validateMaterials(materialSet, onto, allOntologyClasses))
								.setAttributes(attributeSet)
								.setEquivalentProcesses(equivalentProcesses)
								.build()
								);

						processes.add(new Process.Builder()
								.setName(s)
								.setMaterials(QueryValidator.validateMaterials(materialSet, onto, allOntologyClasses))
								.setAttributes(attributeSet)
								.setEquivalentProcesses(updateEquivalenceSet(equivalentProcesses, s, process))
								.build()
								);

					}
				}

			}


			//add geographical information to consumer query
			double supplierMaxDistance = rfq.getSupplierMaxDistance();
			Map<String, String> customerInformation = rfq.getCustomer().customerInfo;

			//get certifications if they are specified by the consumer
			if (rfq.getSupplierAttributes() == null || rfq.getSupplierAttributes().isEmpty()) {
				//if no attributes nor certifications, we only add the processes to the ConsumerQuery object
				//assuming that supplierMaxDistance and customerInformation (name, location, coordinates) are always included and manager further in the process even if 0 or null.

				query = new CSQuery.CSQueryBuilder(processes).
						setSupplierMaxDistance(supplierMaxDistance).
						setCustomerLocationInfo(customerInformation).
						build();

			} else {

				for (SupplierAttributeKeys supplierAttributes : rfq.getSupplierAttributes()) {
					if (supplierAttributes.attributeKey.equalsIgnoreCase("certification")) {
						certifications.add(new Certification(supplierAttributes.attributeValue));
					}

					if (supplierAttributes.attributeKey.equalsIgnoreCase("Language")) {

						languages.add(supplierAttributes.attributeValue);
					}
					
					if (supplierAttributes.getAttributeKey().equalsIgnoreCase("Country")) {
						countries.add(supplierAttributes.getAttributeValue());
					}
				}

				//if both languages and countries
				if (!languages.isEmpty() && !countries.isEmpty()) {

					query = new CSQuery.CSQueryBuilder(processes)
							.setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses))
							.setSupplierMaxDistance(supplierMaxDistance)
							.setCustomerLocationInfo(customerInformation)
							.setLanguage(languages)
							.setCountries(countries)
							.build();
					
				// if only languages
				} else if (!languages.isEmpty() && countries.isEmpty()) {
					
					query = new CSQuery.CSQueryBuilder(processes)
							.setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses))
							.setSupplierMaxDistance(supplierMaxDistance)
							.setCustomerLocationInfo(customerInformation)
							.setLanguage(languages)
							.build();
					
				//if only countries
				} else if (!countries.isEmpty() && languages.isEmpty()) {
					
					query = new CSQuery.CSQueryBuilder(processes)
							.setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses))
							.setSupplierMaxDistance(supplierMaxDistance)
							.setCustomerLocationInfo(customerInformation)
							.setCountries(countries)
							.build();

				} else {
				//if not we omit languages and countries, and add only certifications from the supplier attributes
				query = new CSQuery.CSQueryBuilder(processes).
						setCertifications(QueryValidator.validateCertifications(certifications, onto, allOntologyClasses)).
						setSupplierMaxDistance(supplierMaxDistance).
						setCustomerLocationInfo(customerInformation).
						build();
				}

			}
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
		String filename = "./files/Davide_040221/Davide_CS_040221.json";
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
			System.out.println("Materials: " + p.getMaterials());
			System.out.println("Attributes: " + p.getAttributes());
			System.out.println("Equivalent processes: " + p.getEquivalentProcesses());

			if (p.getMaterials() != null) {
				System.out.println("Number of materials: " + p.getMaterials().size());
				for (String m : p.getMaterials()) {
					System.out.println("   Material: " + m);
				}
			}
			if (p.getAttributes() != null) {
			for (Attribute a : p.getAttributes()) {
				System.out.println("   Attribute: " + a.getKey());
			}
			}
		}

		if (query.getCertifications() != null && !query.getCertifications().isEmpty()) {
		for (Certification cert : query.getCertifications()) {
			System.out.println("Certification: " + cert.getId());
		}
		}

		System.out.println("Max supplier distance: " + query.getSupplierMaxDistance());

		System.out.println("Languages required: " + query.getLanguage());
		
		System.out.println("Countries required: " + query.getCountry());
	}

}