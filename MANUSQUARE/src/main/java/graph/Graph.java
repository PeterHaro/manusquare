package graph;

import com.google.common.collect.Iterators;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

import ontology.OntologyOperations;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class Graph {

	public Graph() {}
	
	public static void addProcessesToGraph (MutableGraph<String> graph, List<String> supplierDefinedProcesses) {
		
		for (String s : supplierDefinedProcesses) {
						
			if (!graph.nodes().contains(s.toLowerCase())) {
				graph.putEdge(s.toLowerCase(), "mfgprocess");
			}
		}
	}
	
	public static void addMaterialsToGraph (MutableGraph<String> graph, List<String> supplierDefinedMaterials) {
		
		for (String s : supplierDefinedMaterials) {
			if (!graph.nodes().contains(s.toLowerCase())) {
				graph.putEdge(s.toLowerCase(), "materialtype");
			}
		}
	}
	
	public static void addInnovationTypesToGraph (MutableGraph<String> graph, List<String> supplierDefinedInnovationTypes) {
		
		for (String s : supplierDefinedInnovationTypes) {
			if (!graph.nodes().contains(s.toLowerCase())) {
				graph.putEdge(s.toLowerCase(), "innovationtype");
			}
		}
	}
	
	public static void addInnovationPhasesToGraph (MutableGraph<String> graph, List<String> supplierDefinedInnovationPhases) {
		
		for (String s : supplierDefinedInnovationPhases) {
			if (!graph.nodes().contains(s.toLowerCase())) {
				graph.putEdge(s.toLowerCase(), "innovationphase");
			}
		}
	}
	
	public static void addSkillsToGraph (MutableGraph<String> graph, List<String> supplierDefinedSkills) {
		
		for (String s : supplierDefinedSkills) {
			if (!graph.nodes().contains(s.toLowerCase())) {
				graph.putEdge(s.toLowerCase(), "capabilitytype");
			}
		}
	}
	
	public static void addSectorsToGraph (MutableGraph<String> graph, List<String> supplierDefinedSectors) {
		
		for (String s : supplierDefinedSectors) {
			if (!graph.nodes().contains(s.toLowerCase())) {
				graph.putEdge(s.toLowerCase(), "industry");
			}
		}
	}

	

	public static MutableGraph<String> createGraph (OWLOntology onto) {

		//get classes and their superclasses
		Map<String, String> superClassMap = OntologyOperations.getClassesAndSuperClassesUsingPellet(onto);

		//get individual classes from the superClassMap
		Set<String> classes = superClassMap.keySet();

		//create the graph
		MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(false).build();

		//create a node for thing
		String thingNode = "Thing";

		for (String s : classes) {
			String superClass = null;

			for (Entry<String, String> entry : superClassMap.entrySet()) {
				if (s.equals(entry.getKey())) {
					superClass = superClassMap.get(entry.getKey());
					//create an is-a relationship from the class to its superclass. If a class does not have any defined superclasses, create an is-relationship to thing
					if (superClass != null) {
						graph.putEdge(s.toLowerCase(), superClass.toLowerCase());
					} else {
						graph.putEdge(s.toLowerCase(), thingNode.toLowerCase());
					}
				}
			}
		}

		return graph;
	}
	
	public static boolean nodeInGraph(String node, MutableGraph graph) {

		if (graph.nodes().contains(node)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void printGraphNodes (MutableGraph<String> graph) {
		
		Set<String> graphNodes = graph.nodes();
		
		for (String s : graphNodes) {
			System.out.println(s);
		}
		
	}

	public static int getNodeDepth (String nodeName, MutableGraph<String> graph) {

		Iterator<String> iter = Traverser.forGraph(graph).breadthFirst(nodeName.toLowerCase()).iterator();

		Traverser.forGraph(graph).breadthFirst(nodeName.toLowerCase());

		return Iterators.size(iter);

	}

	public static String getLCS (String sourceNode, String targetNode, MutableGraph<String> graph) {
				
		//make all nodes lowercased
		sourceNode = sourceNode.toLowerCase();
		targetNode = targetNode.toLowerCase();

		//traverse the graph to get parents of sourceNode
		Iterator<String> iterSource = Traverser.forGraph(graph).breadthFirst(sourceNode).iterator();

		List<String> sourceNodeList = new LinkedList<String>();
		while (iterSource.hasNext()) {
			sourceNodeList.add(iterSource.next());
		}

		//remove the sourceNode from the list so that only parents remain
		//sourceNodeList.remove(sourceNode);

		//reverse the linked list to get the right order of generality of the parent nodes
		Collections.reverse(sourceNodeList);
		
		//traverse the graph to get parents of targetNode
		Iterator<String> iterTarget = Traverser.forGraph(graph).breadthFirst(targetNode).iterator();

		List<String> targetNodeList = new LinkedList<String>();
		while (iterTarget.hasNext()) {
			targetNodeList.add(iterTarget.next());
		}

		//remove the targetNode from the list so that only parents remain
		//targetNodeList.remove(targetNode);

		//reverse the linked list to get the right order of generality of the parent nodes
		Collections.reverse(targetNodeList);

		String lcs = null;

		for (String source : sourceNodeList) {
			for (String target : targetNodeList) {
				if (source.equals(target)) {
					lcs = source;
					break;
				}
			}
		}
		
		return lcs;

	}

	public static void printParents (String node, MutableGraph<String> graph) {

		Iterator<String> iter = Traverser.forGraph(graph).breadthFirst(node).iterator();

		while (iter.hasNext()) {
			System.out.print(" " + iter.next());
		}
	}

	public static Map<String, Integer> getOntologyHierarchy (OWLOntology onto, MutableGraph<String> graph) {

		Map<String, Integer> hierarchyMap = new LinkedHashMap<String, Integer>();
		Set<String> conceptsSet = OntologyOperations.getClassesAsString(onto);
		for (String s : conceptsSet) {
			if (!s.equalsIgnoreCase("Thing")) //we don´t need owl:Thing in the map
			hierarchyMap.put(s, getNodeDepth(s, graph)-2); //tweak with -2 to get the correct num edges to graph root
		}

		return hierarchyMap;


	}

	//test method
	public static void main(String[] args) throws OWLOntologyCreationException {

		File ontoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
//		Set<String> userDefinedConcepts = new HashSet<String>();
//		userDefinedConcepts.add("Chocolate");
		
		//create graph
		MutableGraph<String> graph = createGraph(onto);
		
//		System.out.println("Depth First (chocolate):");
//		Traverser.forGraph(graph).depthFirstPostOrder("chocolate").forEach(x->System.out.println(x));
//
//		List<String> userDefinedProcesses = new ArrayList<String>();
//		userDefinedProcesses.add("TestProcessAudun");
//		
//		addProcessesToGraph(graph, userDefinedProcesses);
//				
//		System.out.println("Depth First (testprocessaudun):");
//		Traverser.forGraph(graph).depthFirstPostOrder("testprocessaudun").forEach(x->System.out.println(x));
		
		System.out.println("Get ontology hierarchy");
		Map<String, Integer> hierarchy = getOntologyHierarchy(onto, graph);
		for (Entry<String, Integer> e : hierarchy.entrySet()) {
			System.out.println(e.getKey() + ": " + e.getValue());
		}
		

		

	}

} 