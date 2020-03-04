package query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
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
import embedding.vectoraggregation.VectorAggregationMethod;
import exceptions.NoProcessException;
import json.RequestForQuotation;
import json.RequestForQuotation.ProjectAttributeKeys;
import json.RequestForQuotation.SupplierAttributeKeys;
import owlprocessing.OntologyOperations;
import utilities.Cosine;
import utilities.StringUtilities;

public class ConsumerQuery {

	private Set<Process> processes;
	private Set<Certification> certifications;
	private double supplierMaxDistance;
	private Map<String, String> customerLocationInfo;
	
	private static final int NUM_VECTOR_DIMS = 300;
	private static final String EMBEDDING_FILE = "./files/EMBEDDINGS/manusquare_wikipedia_trained.txt";
	private static final VectorAggregationMethod VAM = VectorAggregationMethod.AVG;
	

	//if the consumer specifies both processes (incl. materials) and certifications.
	public ConsumerQuery(Set<Process> processes, Set<Certification> certifications) {
		super();
		this.processes = processes;
		this.certifications = certifications;
	}
	
	//14.02.2020: Added supplierMaxDistance and map holding location, lat, lon from RFQ JSON
	//if the consumer specifies both processes (incl. materials), certifications, and supplierMaxDistance.
	public ConsumerQuery(Set<Process> processes, Set<Certification> certifications, double supplierMaxDistance, Map<String, String> customerLocationInfo) {
		super();
		this.processes = processes;
		this.certifications = certifications;
		this.supplierMaxDistance = supplierMaxDistance;
		this.customerLocationInfo = customerLocationInfo;
	}

	//if only processes (incl. materials) are specified by the consumer
	public ConsumerQuery(Set<Process> processes) {
		super();
		this.processes = processes;
	}

	public ConsumerQuery() {}

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
		} catch(com.google.gson.JsonSyntaxException ex) {
			return false;
		}
	}

	/**
	 * Parses a json file and creates a ConsumerQuery object representing the input provided by a consumer in the RFQ establishment process.
	 * @param filename the path to the input json file.
	 * @param onto the MANUSQUARE ontology.
	 * @return a ConsumerQuery object representing processes/materials/certifications requested by a consumer.
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws IOException 
	 */
	public static ConsumerQuery createConsumerQuery (String filename, OWLOntology onto) throws JsonSyntaxException, JsonIOException, IOException {
		Set<Process> processes = new HashSet<>();
		Set<Certification> certifications = new HashSet<Certification>();
		Set<String> processNames = new HashSet<String>();
		
		RequestForQuotation rfq;
		
		if(isJSONValid(filename)) {
			rfq = new Gson().fromJson(filename, RequestForQuotation.class);
		} else {
			rfq = new Gson().fromJson(new FileReader(filename), RequestForQuotation.class);
		}
		

		if (rfq.projectAttributes == null || rfq.projectAttributes.isEmpty()) {
			throw new NoProcessException ("Processes must be included!");
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
			
			for (ProjectAttributeKeys projectAttributes : rfq.projectAttributes) {
				//get the attributes
				if (!projectAttributes.attributeKey.equals("material") && projectAttributes.processName.equals(process)) {
					attributeSet.add(new Attribute(projectAttributes.attributeKey, projectAttributes.attributeValue));
				} else if (projectAttributes.attributeKey.equals("material") && projectAttributes.processName.equals(process)) { //get the materials
					materialSet.add(new Material(projectAttributes.attributeValue));
				}
			}

			//FIXME: Don´t trust this process of validating process names, but will look at it later.
			//10.02.2020: get equivalent process concepts to process (after we have ensured the process is included in the ontology)
			process = validateProcessName(process, onto);
			equivalentProcesses = OntologyOperations.getEquivalentClassesAsString(OntologyOperations.getClass(process, onto), onto);
			
			if (equivalentProcesses.isEmpty() || equivalentProcesses == null) {
							
			processes.add(new Process(process, validateMaterials(materialSet, onto), validateAttributeKeys(attributeSet, onto)));
			
			} else {
				for (String s : equivalentProcesses) {
					
					processes.add(new Process(process, validateMaterials(materialSet, onto), validateAttributeKeys(attributeSet, onto), equivalentProcesses));
					processes.add(new Process (s, validateMaterials(materialSet, onto), validateAttributeKeys(attributeSet, onto), updateEquivalenceSet(equivalentProcesses, s, process)));
				}
			}
			
		}
		
		ConsumerQuery query = null;
		
		//add geographical information to consumer query
		double supplierMaxDistance = rfq.supplierMaxDistance;
		Map<String, String> customerLocation = rfq.customer.customerInfo;

		//get certifications if they are specified by the consumer
		if (rfq.supplierAttributes == null || rfq.supplierAttributes.isEmpty()) {
			//if no certifications, we only add the processes to the ConsumerQuery object
			query = new ConsumerQuery(processes);

		} else {
			for (SupplierAttributeKeys supplierAttributes : rfq.supplierAttributes) {
				if (supplierAttributes.attributeKey.equals("certification")) {
					certifications.add(new Certification(supplierAttributes.attributeValue));
				}
			}
			//if there are certifications specified we add those along with processes to the ConsumerQuery object
			query = new ConsumerQuery(processes, validateCertifications(certifications, onto), supplierMaxDistance, customerLocation);
		}
		
		return query;
	}
	

	//FIXME: Not very elegant, but seems to work.
	private static Set<String> updateEquivalenceSet (Set<String> equivalentProcesses, String toBeRemoved, String toBeAdded) {
		Set<String> updatedEquivalentProcesses = new HashSet<>(equivalentProcesses);	
		updatedEquivalentProcesses.remove(toBeRemoved);		
		updatedEquivalentProcesses.add(toBeAdded);
		
		return updatedEquivalentProcesses;
	}


	
	private static String validateProcessName (String processName, OWLOntology onto) throws IOException {
		//FIXME: Do we really need this with the new getMostSimilarConcept method?
		Set<String> ontologyClassesAsString = OntologyOperations.getClassesAsString(onto);
		String validatedProcessName = null;

		if (!ontologyClassesAsString.contains(processName)) {
			validatedProcessName = getMostSimilarConcept(processName, QueryConceptType.PROCESS, onto, EMBEDDING_FILE, VAM);
		} else {
			validatedProcessName = processName;
		}
				
		return validatedProcessName;
		
	}
	

	/**
	 * Checks if all materials specified by the consumer actually exist as concepts in the ontology. If they´re not, find the closest matching concept.
	 * @param initialMaterials set of materials specified by the consumer in the RFQ process
	 * @param ontology the MANUSQUARE ontology
	 * @return set of materials we´re sure exist as concepts in the ontology
	   Nov 13, 2019
	 * @throws IOException 
	 */
	private static Set<Material> validateMaterials (Set<Material> initialMaterials, OWLOntology onto) throws IOException {
		Set<Material> validatedMaterials = new HashSet<Material>();
		
		if (initialMaterials.isEmpty() || initialMaterials == null) {
			return null;
		} else {

		//FIXME: Do we really need this with the new getMostSimilarConcept method?
		Set<String> ontologyClassesAsString = OntologyOperations.getClassesAsString(onto);

		for (Material m : initialMaterials) {
			if (!ontologyClassesAsString.contains(m.getName())) { //if not, get the concept from the ontology with the highest similarity
				m.setName(getMostSimilarConcept(m.getName(), QueryConceptType.MATERIAL, onto, EMBEDDING_FILE, VAM));
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
	 * @param initialAttributes set of attribute keys specified by the consumer in the RFQ process
	 * @param ontology the MANUSQUARE ontology
	 * @return set of attribute keys we´re sure exist as concepts in the ontology
	   Jan 7, 2020
	 * @throws IOException 
	 */
	private static Set<Attribute> validateAttributeKeys (Set<Attribute> initialAttributes, OWLOntology onto) throws IOException {
		Set<Attribute> validatedAttributes = new HashSet<Attribute>();
		
		if (initialAttributes.isEmpty() || initialAttributes == null) {
			return null;
		} else {

		//FIXME: Do we really need this with the new getMostSimilarConcept method?
		Set<String> ontologyClassesAsString = OntologyOperations.getClassesAsString(onto);

		for (Attribute a : initialAttributes) {
			if (!ontologyClassesAsString.contains(a.getKey())) { //if not, get the concept from the ontology with the highest similarity
				a.setKey(getMostSimilarConcept(a.getKey(), QueryConceptType.ATTRIBUTE, onto, EMBEDDING_FILE, VAM));
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
	 * @param initialCertifications set of certifications specified by the consumer in the RFQ process
	 * @param ontology the MANUSQUARE ontology
	 * @return set of certifications we´re sure exist as concepts in the ontology
	   Nov 13, 2019
	 * @throws IOException 
	 */
	private static Set<Certification> validateCertifications (Set<Certification> initialCertifications, OWLOntology onto) throws IOException {
		Set<Certification> validatedMaterials = new HashSet<Certification>();

		//FIXME: Do we really need this with the new getMostSimilarConcept method?
		Set<String> ontologyClassesAsString = OntologyOperations.getClassesAsString(onto);

		for (Certification c : initialCertifications) {
			if (!ontologyClassesAsString.contains(c.getId())) { //if not, get the concept from the ontology with the highest similarity
				System.out.println("The ontology does not contain " + c.getId());
				c.setId(getMostSimilarConcept(c.getId(), QueryConceptType.CERTIFICATION, onto, EMBEDDING_FILE, VAM));
				validatedMaterials.add(c);
			} else {
				validatedMaterials.add(c);
			}
		}

		return validatedMaterials;

	}
	
	/**
	 * Retrieves the most semantically similar concept using embeddings created by Word2Vec
	 * @param consumerInput the input (e.g. requested process or material) from the consumer
	 * @param conceptType the ontology class from which a relevant subset of concepts to be compared is retrieved (for processes this is 'MfgProcess', for materials this is 'MaterialType')
	 * @param onto the ontology from which the most semantically similar concept is retrieved
	 * @param embeddingsFile a file holding word-to-embedding vectors
	 * @param numVectorDims number of dimensions of the vectors in the embeddingsFile
	 * @return the ontology concept having the highest (semantic) similarity with the consumerInput
	 * @throws IOException
	   Mar 3, 2020
	 */
	private static String getMostSimilarConcept(String consumerInput, QueryConceptType conceptType, OWLOntology onto, String embeddingsFile, VectorAggregationMethod vectorAggregationMethod) throws IOException {

		//basic pre-processing of the consumerInput
		String preProcessedConsumerProcess = preProcess(consumerInput);
		
		//create a vector map for the entire embeddingsfile
		Map<String, double[]> vectorMap = createVectorMap (embeddingsFile);

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

		//create a vector map from the embeddings file for ontology concepts
		Map<String, double[]> vectorOntologyMap = createOntologyVectorMap(classes, embeddingsFile, vectorAggregationMethod);

		double[] consumerInputVectors = getLabelVector(preProcessedConsumerProcess, vectorMap, vectorAggregationMethod);

		String mostSemanticallySimilarConcept = null;

		//if there are no relevant embedding vectors for the consumer input
		if (consumerInputVectors == null) {

			System.err.println(preProcessedConsumerProcess + " does not have any embedding vectors, so using a syntactic matching approach instead...");
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

	/**
	 * Returns an ontology concept name (with proper casing) from a word in the embeddings file
	 * @param concept the word in the embeddings file for which the relevant ontology concept name is retrieved.
	 * @param allClasses set of ontology concept names from the ontology
	 * @return an ontology concept name with proper casing (camel-case)
	   Mar 3, 2020
	 */
	private static String findConceptName (String concept, Set<String> allClasses) {
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
	 * @param consumerProcessVectors the vectors of the query inserted by the consumer
	 * @param vectorOntologyMap a map holding the ontology concept name as key and its vector representation as value
	 * @return the ontology concept having the vector most similar to the vector of the consumer input (process or material)
	   Mar 3, 2020
	 */
	public static String findMostSimilarVector (double[] consumerConceptVectors, Map<String, double[]> vectorOntologyMap) {
		double sim = 0;
		double localSim = 0;
		String mostSimilar = null;

		for (Entry<String,double[]> e : vectorOntologyMap.entrySet()) {
			localSim = Cosine.cosineSimilarity(consumerConceptVectors, e.getValue());
			if (localSim > sim) {
				mostSimilar = e.getKey();
				sim = localSim;
			}
		}

		return mostSimilar;

	}

	/**
	 * Creates a map holding a class as key along with an array of vectors as value.
	 * @param onto an OWL ontology
	 * @param vectorFile a file holding terms and corresponding embedding vectors.
	 * @return a Map<String, double[]) representing classes and corresponding embedding vectors.
	 * @throws IOException
	   Jul 15, 2019
	 */
	public static Map<String, double[]> createOntologyVectorMap (Set<String> ontologyClasses, String vectorFile, VectorAggregationMethod vectorAggregationMethod) throws IOException {

		Map<String, double[]> vectors = new HashMap<String, double[]>();

		//create the vector map from the source vector file
		Map<String, double[]> vectorMap = createVectorMap (vectorFile);
		double[] labelVector = new double[NUM_VECTOR_DIMS];

		for (String s : ontologyClasses) {

			//if the embeddings file contains the whole concept name as-is
			if (vectorMap.containsKey(s.toLowerCase())) {

				labelVector = getLabelVector(s, vectorMap, vectorAggregationMethod);
				vectors.put(s.toLowerCase(), labelVector);

				//check if any of the compound parts are included in the embeddings file
			} else if (StringUtilities.isCompoundWord(s)) {

				String[] compounds = StringUtilities.getCompoundParts(s);
				List<double[]> compoundsWithVectors = new ArrayList<double[]>();
				for (int i = 0; i < compounds.length; i++) {
					if (vectorMap.containsKey(compounds[i].toLowerCase())) {
						double[] vectorArray = vectorMap.get(compounds[i].toLowerCase());		
						compoundsWithVectors.add(vectorArray);
					}
				}

				vectors.put(s.toLowerCase(), getAVGVectors(compoundsWithVectors, NUM_VECTOR_DIMS));

			}

		}

		return vectors;

	}


	/**
	 * Takes a file of words and corresponding vectors and creates a Map where the word in each line is key and the vectors are values (as ArrayList<Double>)
	 * @param vectorFile A file holding a word and corresponding vectors on each line
	 * @return A Map<String, ArrayList<Double>> where the key is a word and the value is a list of corresponding vectors
	 * @throws FileNotFoundException
	 */
	public static Map<String, double[]> createVectorMap (String vectorFile) throws FileNotFoundException {

		Map<String, double[]> vectorMap = new HashMap<String, double[]>();

		Scanner sc = new Scanner(new File(vectorFile));

		//read the file holding the vectors and extract the concept word (first word in each line) as key and the vectors as ArrayList<Double> as value in a Map
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			String[] strings = line.split(" ");

			//get the word, not the vectors
			String word1 = strings[0];

			//initialize an array that includes only the vector entries
			double[] labelVectorArray = new double[strings.length-1];

			for (int i = 1; i < strings.length; i++) {

				labelVectorArray[i-1] = Double.valueOf(strings[i]);
			}

			//put the word and associated vectors in the vectormap
			vectorMap.put(word1, labelVectorArray);

		}
		sc.close();

		return vectorMap;
	}

	/**
	 * Checks if the vectorMap contains the label of an OWL class as key and if so the vectors of the label are returned. 
	 * @param cls An input OWL class
	 * @param vectorMap The Map holding words and corresponding vectors
	 * @return a set of vectors (as a string) associated with the label
	 */
	public static double[] getLabelVector(String label, Map<String, double[]> vectorMap, VectorAggregationMethod vectorAggregationMethod) {

		List<double[]> aggregatedLabelVectors = new ArrayList<double[]>();

		double[] labelVectorArray = new double[NUM_VECTOR_DIMS];
		double[] localVectorArray = new double[NUM_VECTOR_DIMS];

		//if the class name is not a compound, turn it into lowercase, 
		if (!StringUtilities.isCompoundWord(label)) {

			String lcLabel = label.toLowerCase();

			//if the class name is in the vectormap, get its vectors
			if (vectorMap.containsKey(lcLabel)) {
				labelVectorArray = vectorMap.get(lcLabel);

			} else {

				labelVectorArray = null;
			}

			//if the class name is a compound, split the compounds, and if the vectormap contains ANY of the compounds, extract the vectors from 
			//the compound parts and average them in order to return the vector for the compound class name
			//TODO: The compound head should probably have more impact on the score than the compound modifiers
		} else if (StringUtilities.isCompoundWord(label)) {

			//get the compound parts and check if any of them are in the vector file			
			String[] compounds = StringUtilities.getCompoundParts(label);

			for (int i = 0; i < compounds.length; i++) {

				if (vectorMap.containsKey(compounds[i].toLowerCase())) {

					localVectorArray = vectorMap.get(compounds[i].toLowerCase());

					aggregatedLabelVectors.add(localVectorArray);


				} else {

					labelVectorArray = null;
				}
			}

			//average or sum all vector arraylists
			if (vectorAggregationMethod == VectorAggregationMethod.AVG) {

				labelVectorArray = getAVGVectors(aggregatedLabelVectors, NUM_VECTOR_DIMS);

			} else if (vectorAggregationMethod == VectorAggregationMethod.SUM) {

				labelVectorArray = getSummedVectors(aggregatedLabelVectors, NUM_VECTOR_DIMS);
			}
		}

		return labelVectorArray;
	}

	/**
	 * Receives a list of vectors and averages each vector component
	 * @param a_input list of vectors
	 * @param numVectorDims number of vector dimensions (e.g. 300)
	 * @return an averaged vector
	   Mar 3, 2020
	 */
	private static double[] getAVGVectors(List<double[]> a_input, int numVectorDims) {

		double[] avg = new double[numVectorDims];
		double[] temp = new double[numVectorDims];

		for (double[] singleVector : a_input) {
			for (int i = 0; i < temp.length; i++) {
				temp[i] += singleVector[i];
			}
		}

		for (int i = 0; i < temp.length; i++) {
			avg[i] = temp[i] / (double) a_input.size();
		}

		return avg;
	}

	/**
	 * Receives a list of vectors and sums each vector component
	 * @param a_input list of vectors
	 * @param numVectorDims number of vector dimensions (e.g. 300)
	 * @return a summed vector
	   Mar 3, 2020
	 */
	private static double[] getSummedVectors(List<double[]> a_input, int numVectorDims) {

		double[] sum = new double[numVectorDims];

		for (double[] singleVector : a_input) {
			for (int i = 0; i < sum.length; i++) {
				sum[i] += singleVector[i];
			}
		}

		return sum;
	}

	/**
	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
	 * @param input the input process/material/certification specified by the consumer
	 * @param ontologyClassesAsString set of ontology concepts represented as strings
	 * @param method the similarity method applied
	 * @return the best matching concept from the MANUSQUARE ontology
	   Nov 13, 2019
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
	 * @param similarityMap a map of concepts along with their similarity scores
	 * @return single concept (name) with highest similarity score
	   Nov 13, 2019
	 */
	private static String getConceptWithHighestSim (Map<String, Double> similarityMap) {

		Map<String, Double> rankedResults = sortDescending(similarityMap);

		Map.Entry<String,Double> entry = rankedResults.entrySet().iterator().next();
		String conceptWithHighestSim = entry.getKey();


		return conceptWithHighestSim;

	}

	/** 
	 * Sorts a map based on similarity scores (values in the map)
	 * @param map the input map to be sorted
	 * @return map with sorted values
	   May 16, 2019
	 */
	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
		Comparator<K> valueComparator =  new Comparator<K>() {
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

	private static String preProcess (String input) {

		input = new String(StringUtilities.capitaliseWord(input).replaceAll("\\s+", ""));

		return input;
	}
	
//	
//
//	/**
//	 * Uses (string) similarity techniques to find most similar ontology concept to a consumer-specified process/material/certification
//	 * @param input the input process/material/certification specified by the consumer
//	 * @param ontologyClassesAsString set of ontology concepts represented as strings
//	 * @param method the similarity method applied
//	 * @return the best matching concept from the MANUSQUARE ontology
//	   Nov 13, 2019
//	 */
//	private static String getMostSimilarConcept(String input, Set<String> ontologyClassesAsString) {
//
//		Map<String, Double> similarityMap = new HashMap<String, Double>();
//		String mostSimilarConcept = null;
//
//		//remove whitespace in input string
//		input.replaceAll("\\s+","");
//
//		//jaroWinkler distance
//
//			for (String s : ontologyClassesAsString) {
//				//new JaroWinklerSimilarity().apply(s1, s2)
//				similarityMap.put(s, new JaroWinklerSimilarity().apply(input, s));
//				System.out.println("Test: Putting " + s + " with a Jaro Winkler score of " + new JaroWinklerSimilarity().apply(input, s) + " into the similarityMap");
//			}
//
//			mostSimilarConcept = getConceptWithHighestSim(similarityMap);
//	
//
//		return mostSimilarConcept;
//
//	}
//
//	/**
//	 * Returns the concept (name) with the highest (similarity) score from a map of concepts
//	 * @param similarityMap a map of concepts along with their similarity scores
//	 * @return single concept (name) with highest similarity score
//	   Nov 13, 2019
//	 */
//	private static String getConceptWithHighestSim (Map<String, Double> similarityMap) {
//
//		Map<String, Double> rankedResults = sortDescending(similarityMap);
//
//		Map.Entry<String,Double> entry = rankedResults.entrySet().iterator().next();
//		String conceptWithHighestSim = entry.getKey();
//
//
//		return conceptWithHighestSim;
//
//	}
//
//	/** 
//	 * Sorts a map based on similarity scores (values in the map)
//	 * @param map the input map to be sorted
//	 * @return map with sorted values
//	   May 16, 2019
//	 */
//	private static <K, V extends Comparable<V>> Map<K, V> sortDescending(final Map<K, V> map) {
//		Comparator<K> valueComparator =  new Comparator<K>() {
//			public int compare(K k1, K k2) {
//				int compare = map.get(k2).compareTo(map.get(k1));
//				if (compare == 0) return 1;
//				else return compare;
//			}
//		};
//		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
//
//		sortedByValues.putAll(map);
//
//		return sortedByValues;
//	}

	//test method
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, OWLOntologyCreationException, IOException {

		String filename = "./MANUSQUARE/files/rfq-attributes-custInfo.json";
		String ontology = "./files/ONTOLOGIES/updatedOntology.owl";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(new File(ontology));

		ConsumerQuery query = createConsumerQuery (filename, onto);

		System.out.println("Printing query:");

		for (Process p : query.getProcesses()) {
			System.out.println("Process: " + p.getName());
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
