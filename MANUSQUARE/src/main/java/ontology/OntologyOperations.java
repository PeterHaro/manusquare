package ontology;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;

/**
 * @author audunvennesland Date:02.02.2017
 * @version 1.0
 */
public class OntologyOperations {


	/**
	 * An OWLOntologyManagermanages a set of ontologies. It is the main point
	 * for creating, loading and accessing ontologies.
	 */
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	//reasoners - need both because they present their results in different order
	static OWLReasonerFactory structuralReasonerFactory = new StructuralReasonerFactory();
	static OpenlletReasonerFactory openlletReasonerFactory = new OpenlletReasonerFactory();


	/**
	 * Default constructor
	 */
	public OntologyOperations() {

	}

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {

		File ontoFile = new File ("./files/ONTOLOGIES/updatedOntology.owl");
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
//		Map<String, String> classLabelMap = getClassLabels(onto);
//		
//		System.out.println("The classLabelMap contains " + classLabelMap.size() + " entries.");
//		
//		for (Entry<String, String> e : classLabelMap.entrySet()) {
//			System.out.println("Class: " + e.getKey() + ": " + e.getValue());
//		}

		
		Map<String, String> classesAndSuperClasses = getClassesAndSuperClassesUsingPellet (onto);
		
		BufferedWriter bfwriter = new BufferedWriter(new FileWriter("./files/TEST_OUTPUT/ontologyclasses.txt"));
		
		for (Entry<String, String> e : classesAndSuperClasses.entrySet()) {
			bfwriter.append("\nClass: " + e.getKey() + ", superclass: " + e.getValue());
		}

		bfwriter.close();

	}

	/**
	 * Constructs a map that holds classes as key and their label as value.
	 * @param onto 
	 * @return
	   Sep 22, 2020
	 */
	public static Map<String, String> getClassLabels (OWLOntology onto) {

		Map<String, String> classLabels = new HashMap<String, String>();

		for (OWLClass cls : onto.getClassesInSignature()) {
				for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(cls.getIRI())) {
					
					System.out.println("The annotation for " + cls + " is " + a.getProperty());
					
					if (a.getProperty() != null && a.getProperty().isLabel()) {
						//need to use a.getValue() instead of a.getAnnotation() to avoid including 'Annotation rdfs comment' that is included before the real definition.						
						classLabels.put(cls.getIRI().getFragment(), a.getValue().toString().substring(a.getValue().toString().indexOf("\"")+1, a.getValue().toString().lastIndexOf("\"")));
						
					}

			}

		}

		return classLabels;
	}



	/** Returns a set of OWL classes being equivalent to cls
	 *
	 * @param cls
	 * @param o
	 * @return
	Feb 10, 2020
	 */
	public static Set<String> getEquivalentClassesAsString(OWLClassExpression cls, OWLOntology o) {
		Set<String> result = new HashSet<String>();

		OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(o);

		Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(cls);

		Set<OWLClass> owlSet = null;

		if (cls.isAnonymous()) {
			owlSet = equivalentClasses.getEntities();
		} else {
			owlSet = equivalentClasses.getEntitiesMinus(cls.asOWLClass()); //get (equivalent) entities minus cls
		}

		for (OWLClass c : owlSet) {

			result.add(getFragmentHack(c.getIRI().toString()));
		}

		return result;

	}


	/**
	 * Returns a Map holding a class as key and its superclass as value. This version uses the Pellet reasoner, since the structural reasoner does not include all inferred superclasses of a class.
	 *
	 * @param o
	 *            the input OWL ontology from which classes and superclasses
	 *            should be derived
	 * @return classesAndSuperClasses a Map holding a class as key and its
	 *         superclass as value
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static Map<String, String> getClassesAndSuperClassesUsingPellet (OWLOntology o)  {

		OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(o);
		Set<OWLClass> cls = o.getClassesInSignature();
		Map<String, String> classesAndSuperClasses = new HashMap<String, String>();
		ArrayList<OWLClass> classList = new ArrayList<OWLClass>();

		for (OWLClass i : cls) {
			classList.add(i);
		}

		// Iterate through the arraylist and for each class get the subclasses
		// belonging to it
		// Transform from OWLClass to String to simplify further processing...
		for (int i = 0; i < classList.size(); i++) {
			OWLClass currentClass = classList.get(i);
			NodeSet<OWLClass> n = reasoner.getSuperClasses(currentClass, true);
			Set<OWLClass> s = n.getFlattened();
			for (OWLClass j : s) {
				classesAndSuperClasses.put(getFragmentHack(currentClass.getIRI().toString()), getFragmentHack(j.getIRI().toString()));
			}
		}

		return classesAndSuperClasses;

	}


	public static Set<String> getClassesAsString (OWLOntology onto) {
		Set<String> classesAsString = new HashSet<String>();
		for (OWLClass c : onto.getClassesInSignature()) {

			classesAsString.add(getFragmentHack(c.getIRI().toString()));
		}

		return classesAsString;
	}



	/**
	 * Helper method that retrieves ALL subclasses (fragments or proper name without URI) for an OWLClass (provided as parameter along with the OWLOntology which is needed for allowing the reasoner to get all subclasses for an OWLClass)
	 * @param onto the input OWLOntology
	 * @param inputClass the OWLClass for which subclasses will be retrieved
	 * @return Set<String> of subclasses for an OWLClass
	 */
	public static Set<String> getAllEntitySubclassesFragments (OWLOntology onto, OWLClass inputClass) {

		OWLReasoner reasoner = structuralReasonerFactory.createReasoner(onto);

		NodeSet<OWLClass> subclasses = reasoner.getSubClasses(inputClass, false);

		Set<String> subclsSet = new HashSet<String>();

		for (OWLClass cls : subclasses.getFlattened()) {
			if (!cls.isOWLNothing()) {
				subclsSet.add(getFragmentHack(cls.getIRI().toString()));
			}
		}

		return subclsSet;

	}


	/**
	 * Helper method that retrieves a set of ALL superclasses (their fragments or proper name without URI) for an OWLClass (provided as parameter along with the OWLOntology which is needed for allowing the reasoner to get all superclasses for an OWLClass)
	 * @param onto the input OWLOntology
	 * @param inputClass the OWLClass for which superclasses will be retrieved
	 * @return Set<String> of superclasses for an OWLClass
	 */
	public static List<String> getEntitySuperclassesFragmentsAsList (OWLOntology onto, OWLClass inputClass) {

		OWLReasoner reasoner = structuralReasonerFactory.createReasoner(onto);

		Set<OWLClass> superclasses = reasoner.getSuperClasses(inputClass, false).getFlattened();

		List<String> superclsList = new LinkedList<String>();

		for (OWLClass cls : superclasses) {
			if (!cls.isOWLNothing() && !cls.isOWLThing()) {
				superclsList.add(getFragmentHack(cls.getIRI().toString()));
			}
		}

		return superclsList;

	}


	/**
	 * Retrieves an OWLClass from its class name represented as a string
	 * @param className
	 * @param ontology
	 * @return
	 */
	public static OWLClass getClass(String className, OWLOntology ontology) {

		OWLClass relevantClass = null;

		Set<OWLClass> classes = ontology.getClassesInSignature();

		for (OWLClass cls : classes) {
			if (getFragmentHack(cls.getIRI().toString()).equals(className)) {
				relevantClass = cls;
				break;
			} else {
				relevantClass = null;
			}
		}

		return relevantClass;


	}

	private static String getFragmentHack (String fullIRI) {

		return fullIRI.substring(fullIRI.lastIndexOf("#") + 1);

	}



}