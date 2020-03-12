package query;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import data.EmbeddingSingletonDataManager;
import edm.Attribute;
import edm.Certification;
import edm.Material;
import edm.Process;
import embedding.vectoraggregation.VectorAggregationMethod;
import exceptions.NoProcessException;
import json.RequestForQuotation;
import json.RequestForQuotation.ProjectAttributeKeys;
import json.RequestForQuotation.SupplierAttributeKeys;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import owlprocessing.OntologyOperations;
import utilities.Cosine;
import utilities.StringUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class ConsumerQuery {

	private Set<Process> processes;
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;


	//if the consumer specifies both processes (incl. materials) and certifications.
	public ConsumerQuery(Set<Process> processes, Set<Certification> certifications) {
		super();
		this.processes = processes;
		this.certifications = certifications;
	}


	//if the consumer specifies both processes (incl. materials), certifications, and supplierMaxDistance.
	public ConsumerQuery(Set<Process> processes, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo) {
		super();
		this.processes = processes;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
	}

	//if only processes (incl. materials) are specified by the consumer
	public ConsumerQuery(Set<Process> processes, double supplierMaxDistance, Map<String, String> customerLocationInfo) {
		super();

		this.processes = processes;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
	}

	public ConsumerQuery() {
	}

	public Set<Process> getProcesses() {
		return processes;
	}

	public Set<Attribute> getAttributes() {
		Set<Attribute> attributes = new HashSet<Attribute>();
		Set<Process> processes = getProcesses();
		for (Process p : processes) {
			attributes.addAll(p.getAttributes());
		}

		return attributes;

	}

	public void setProcesses(Set<Process> processes) {
		this.processes = processes;
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

	// I am so sorry for this. TODO: Hack warning
	public static boolean isJSONValid(String jsonInString) {
		Gson gson = new Gson();
		try {
			gson.fromJson(jsonInString, Object.class);
			return true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			return false;
		}
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
	public static ConsumerQuery createConsumerQuery(String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {
		Set<Process> processes = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> processNames = new HashSet<String>();
		Set<String> allOntologyClasses = OntologyOperations.getClassesAsString(onto);

		RequestForQuotation rfq;

		if (isJSONValid(filename)) {
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
				if (projectAttributes.attributeKey != null) {
					if (!projectAttributes.attributeKey.equals("material") && projectAttributes.processName.equals(process)) {
						attributeSet.add(new Attribute(projectAttributes.attributeKey, projectAttributes.attributeValue));
					} else if (projectAttributes.attributeKey.equals("material") && projectAttributes.processName.equals(process)) { //get the materials
						materialSet.add(new Material(projectAttributes.attributeValue));
					}
				} else {
					System.out.println("There are no attributes in the JSON file!");
				}
			}

			//10.02.2020: get equivalent process concepts to process (after we have ensured the process is included in the ontology)
			process = validateProcessName(process, onto, allOntologyClasses);
			equivalentProcesses = OntologyOperations.getEquivalentClassesAsString(OntologyOperations.getClass(process, onto), onto);

			if (equivalentProcesses.isEmpty() || equivalentProcesses == null) {

				processes.add(new Process(process, validateMaterials(materialSet, onto, allOntologyClasses), validateAttributeKeys(attributeSet, onto, allOntologyClasses)));

			} else {
				for (String s : equivalentProcesses) {

					processes.add(new Process(process, validateMaterials(materialSet, onto, allOntologyClasses), validateAttributeKeys(attributeSet, onto, allOntologyClasses), equivalentProcesses));
					processes.add(new Process(s, validateMaterials(materialSet, onto, allOntologyClasses), validateAttributeKeys(attributeSet, onto, allOntologyClasses), updateEquivalenceSet(equivalentProcesses, s, process)));
				}
			}

		}

		ConsumerQuery query = null;

		//add geographical information to consumer query
		double supplierMaxDistance = rfq.supplierMaxDistance;
		Map<String, String> customerInformation = rfq.customer.customerInfo;

		//get certifications if they are specified by the consumer
		if (rfq.supplierAttributes == null || rfq.supplierAttributes.isEmpty()) {
			//if no attributes nor certifications, we only add the processes to the ConsumerQuery object
			//assuming that supplierMaxDistance and customerInformation (name, location, coordinates) are always included
			query = new ConsumerQuery(processes, supplierMaxDistance, customerInformation);

		} else {
			for (SupplierAttributeKeys supplierAttributes : rfq.supplierAttributes) {
				if (supplierAttributes.attributeKey.equals("certification")) {
					certifications.add(new Certification(supplierAttributes.attributeValue));
				}
			}
			//if there are certifications specified we add those along with processes to the ConsumerQuery object
			query = new ConsumerQuery(processes, validateCertifications(certifications, onto, allOntologyClasses), supplierMaxDistance, customerInformation);
		}


		return query;
	}


	private static Set<String> updateEquivalenceSet(Set<String> equivalentProcesses, String toBeRemoved, String toBeAdded) {
		Set<String> updatedEquivalentProcesses = new HashSet<>(equivalentProcesses);
		updatedEquivalentProcesses.remove(toBeRemoved);
		updatedEquivalentProcesses.add(toBeAdded);

		return updatedEquivalentProcesses;
	}


	private static String validateProcessName(String processName, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {

		String validatedProcessName = null;

		if (!allOntologyClasses.contains(processName)) {
			validatedProcessName = getMostSimilarConcept(processName, QueryConceptType.PROCESS, onto, EmbeddingSingletonDataManager.VAM);
		} else {
			validatedProcessName = processName;
		}
		return validatedProcessName;
	}


	/**
	 * Checks if all materials specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialMaterials set of materials specified by the consumer in the RFQ process
	 * @return set of materials we´re sure exist as concepts in the ontology
	 * Nov 13, 2019
	 * @throws IOException
	 */
	private static Set<Material> validateMaterials(Set<Material> initialMaterials, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<Material> validatedMaterials = new HashSet<Material>();
		if (initialMaterials.isEmpty() || initialMaterials == null) {
			return null;
		} else {
			for (Material m : initialMaterials) {
				if (!allOntologyClasses.contains(m.getName())) { //if not, get the concept from the ontology with the highest similarity
					m.setName(getMostSimilarConcept(m.getName(), QueryConceptType.MATERIAL, onto, EmbeddingSingletonDataManager.VAM));
					validatedMaterials.add(m);
				} else {
					validatedMaterials.add(m);
				}
			}
			return validatedMaterials;
		}
	}

	/**
	 * Checks if all attribute keys specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialAttributes set of attribute keys specified by the consumer in the RFQ process
	 * @return set of attribute keys we´re sure exist as concepts in the ontology
	 * Jan 7, 2020
	 * @throws IOException
	 */
	private static Set<Attribute> validateAttributeKeys(Set<Attribute> initialAttributes, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<Attribute> validatedAttributes = new HashSet<Attribute>();
		if (initialAttributes.isEmpty() || initialAttributes == null) {
			return null;
		} else {

			for (Attribute a : initialAttributes) {
				if (!allOntologyClasses.contains(a.getKey())) { //if not, get the concept from the ontology with the highest similarity
					a.setKey(getMostSimilarConcept(a.getKey(), QueryConceptType.ATTRIBUTE, onto, EmbeddingSingletonDataManager.VAM));
					validatedAttributes.add(a);
				} else {
					validatedAttributes.add(a);
				}
			}

			return validatedAttributes;
		}

	}

	/**
	 * Checks if all certifications specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 *
	 * @param initialCertifications set of certifications specified by the consumer in the RFQ process
	 * @return set of certifications we´re sure exist as concepts in the ontology
	 * Nov 13, 2019
	 * @throws IOException
	 */
	private static Set<Certification> validateCertifications(Set<Certification> initialCertifications, OWLOntology onto, Set<String> allOntologyClasses) throws IOException {
		Set<Certification> validatedMaterials = new HashSet<Certification>();

		for (Certification c : initialCertifications) {
			if (!allOntologyClasses.contains(c.getId())) { //if not, get the concept from the ontology with the highest similarity
				System.out.println("The ontology does not contain " + c.getId());
				c.setId(getMostSimilarConcept(c.getId(), QueryConceptType.CERTIFICATION, onto, EmbeddingSingletonDataManager.VAM));
				validatedMaterials.add(c);
			} else {
				validatedMaterials.add(c);
			}
		}

		return validatedMaterials;

	}

	/**
	 * Retrieves the most semantically similar concept using embeddings created by Word2Vec
	 *
	 * @param consumerInput the input (e.g. requested process or material) from the consumer
	 * @param conceptType   the ontology class from which a relevant subset of concepts to be compared is retrieved (for processes this is 'MfgProcess', for materials this is 'MaterialType')
	 * @param onto          the ontology from which the most semantically similar concept is retrieved
	 * @return the ontology concept having the highest (semantic) similarity with the consumerInput
	 * @throws IOException Mar 3, 2020
	 */
	private static String getMostSimilarConcept(String consumerInput, QueryConceptType conceptType, OWLOntology onto, VectorAggregationMethod vectorAggregationMethod) throws IOException {
		EmbeddingSingletonDataManager embeddingManager = EmbeddingSingletonDataManager.getInstance();

		Set<String> classes = new HashSet<String>();
		//we only consider the classes below the stated ontology classes (e.g. MfgProcess for processes)
		if (conceptType == QueryConceptType.PROCESS) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MfgProcess", onto));
		} else if (conceptType == QueryConceptType.MATERIAL) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("MaterialType", onto));
		} else if (conceptType == QueryConceptType.CERTIFICATION) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("Certification", onto));
		} else if (conceptType == QueryConceptType.ATTRIBUTE) {
			classes = OntologyOperations.getAllEntitySubclassesFragments(onto, OntologyOperations.getClass("AttributeType", onto));
		}

		//if the consumerInput equals an ontology concept we return this
		if (containsIgnoreCase(classes, consumerInput)) {
			return preProcess(consumerInput);//TODO: Test this!
		} else {//if not, we do the semantic and syntactic matching process

			//basic pre-processing of the consumerInput
			String preProcessedConsumerProcess = preProcess(consumerInput);

			//create a vector map from the embeddings file for ontology concepts
			Map<String, double[]> vectorOntologyMap = embeddingManager.createOntologyVectorMap(classes, vectorAggregationMethod);
			double[] consumerInputVectors = embeddingManager.getLabelVector(preProcessedConsumerProcess, vectorAggregationMethod);
			String mostSemanticallySimilarConcept = null;
			//if there are no relevant embedding vectors for the consumer input, we do to the syntactic matching
			if (consumerInputVectors == null) {
				return getMostSimilarConceptSyntactically(preProcessedConsumerProcess, classes);
			} else {
				//check if vectormap/embeddings file contains consumerProcess as-is
				if (vectorOntologyMap.containsKey(preProcessedConsumerProcess.toLowerCase())) {
					mostSemanticallySimilarConcept = preProcessedConsumerProcess;
				} else { //if not, retrieve the most similar concept based on vector similarity
					mostSemanticallySimilarConcept = findMostSimilarVector(consumerInputVectors, vectorOntologyMap);
				}
				return findConceptName(mostSemanticallySimilarConcept, classes);

			}
		}

	}

	/**
	 * Returns an ontology concept name (with proper casing) from a word in the embeddings file
	 *
	 * @param concept    the word in the embeddings file for which the relevant ontology concept name is retrieved.
	 * @param allClasses set of ontology concept names from the ontology
	 * @return an ontology concept name with proper casing (camel-case)
	 * Mar 3, 2020
	 */
	private static String findConceptName(String concept, Set<String> allClasses) {
		String conceptName = null;
		for (String s : allClasses) {
			if (concept.equalsIgnoreCase(s)) {
				conceptName = s;
				break;//break process if a concept name is found
			} else {
				conceptName = null;
			}
		}
		return conceptName;
	}


	/**
	 * Uses cosine similarity to find the most similar word in the embeddings file by its vector representation
	 *
	 * @param consumerConceptVectors the vectors of the query inserted by the consumer
	 * @param vectorOntologyMap      a map holding the ontology concept name as key and its vector representation as value
	 * @return the ontology concept having the vector most similar to the vector of the consumer input (process or material)
	 * Mar 3, 2020
	 */
	public static String findMostSimilarVector(double[] consumerConceptVectors, Map<String, double[]> vectorOntologyMap) {
		double sim = 0;
		double localSim = 0;
		String mostSimilar = null;

		for (Entry<String, double[]> e : vectorOntologyMap.entrySet()) {
			localSim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			if (localSim > sim) {
				mostSimilar = e.getKey();
				sim = localSim;
			}
		}

		return mostSimilar;
	}


	/**
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 *
	 * @param input                   the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @return the best matching concept from the MANUSQUARE ontology
	 * Nov 13, 2019
	 */
	private static String getMostSimilarConceptSyntactically(String input, Set<String> ontologyClassesAsString) {

		Map<String, Double> similarityMap = new HashMap<String, Double>();
		String mostSimilarConcept = null;

		for (String s : ontologyClassesAsString) {

			similarityMap.put(s, new JaroWinklerSimilarity().apply(input, s));
		}

		mostSimilarConcept = getConceptWithHighestSim(similarityMap);


		return mostSimilarConcept;

	}

	/**
	 * Returns the concept (name) with the highest (similarity) score from a map of concepts
	 *
	 * @param similarityMap a map of concepts along with their similarity scores
	 * @return single concept (name) with highest similarity score
	 * Nov 13, 2019
	 */
	private static String getConceptWithHighestSim(Map<String, Double> similarityMap) {
		Map<String, Double> rankedResults = sortDescending(similarityMap);
		Map.Entry<String, Double> entry = rankedResults.entrySet().iterator().next();
		String conceptWithHighestSim = entry.getKey();
		return conceptWithHighestSim;
	}

	/**
	 * Sorts a map based on similarity scores (values in the map)
	 *
	 * @param map the input map to be sorted
	 * @return map with sorted values
	 * May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				if (compare == 0) return 1;
				else return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);

		sortedByValues.putAll(map);

		return sortedByValues;
	}

	private static String preProcess(String input) {

		input = new String(StringUtilities.capitaliseWord(input).replaceAll("\\s+|\\d+", ""));

		return input;
	}

	private static boolean containsIgnoreCase(Set<String> list, String soughtFor) {
		for (String current : list) {
			if (current.equalsIgnoreCase(soughtFor)) {
				return true;
			}
		}
		return false;
	}
	

	

	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {
		String filename = "./MANUSQUARE/files/rfq-attributes-custInfo.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));
		ConsumerQuery query = createConsumerQuery(filename, onto);
		System.out.println("Printing query:");

		for (Process p : query.getProcesses()) {
			System.out.println("Process: " + p.getName());
			System.out.println("Number of materials: " + p.getMaterials().size());
			for (Material m : p.getMaterials()) {
				System.out.println("   Material: " + m.getName());
			}
			for (Attribute a : p.getAttributes()) {
				System.out.println("   Attribute: " + a.getKey());
			}
		}

		for (Certification cert : query.getCertifications()) {
			System.out.println("Certification: " + cert.getId());
		}

		System.out.println("Max supplier distance: " + query.getSupplierMaxDistance());
	}

}
