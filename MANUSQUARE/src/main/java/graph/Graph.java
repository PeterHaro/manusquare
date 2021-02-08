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
	
	public static MutableGraph<String> createGraph (OWLOntology onto, List<String> userDefinedMaterials, List<String> userDefinedProcesses) {

		//get classes and their superclasses
		Map<String, String> superClassMap = OntologyOperations.getClassesAndSuperClassesUsingPellet(onto);
		
		for (Entry<String, String> e : superClassMap.entrySet()) {
			System.out.println(e);
		}
		
		Set<String> classes = new HashSet<String>();
		classes.addAll(superClassMap.keySet());
		
		//TODO: Adding user-defined materials and processes as sub-nodes to "resource"
		System.err.println("Graph: Adding " + userDefinedMaterials + " to classes");
		classes.addAll(userDefinedMaterials);
		classes.addAll(userDefinedProcesses);

		System.err.println("Creating the graph...");
		//create the graph
		MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
		System.err.println("Graph created...");
		//create a node for thing
		String thingNode = "Thing";

		for (String s : classes) {
			String superClass = null;

			for (Entry<String, String> entry : superClassMap.entrySet()) {
				if (s.equals(entry.getKey())) {
					superClass = superClassMap.get(entry.getKey());
					//create an is-a relationship from the class to its superclass. If a class does not have any defined superclasses, create an is-relationship to thing
					if (superClass != null) {
						//System.err.println("Adding edge " + s.toLowerCase() + " --- " + superClass.toLowerCase());
						graph.putEdge(s.toLowerCase(), superClass.toLowerCase());
					} else {
						//System.err.println("Adding edge " + s.toLowerCase() + " --- " + thingNode.toLowerCase());
						graph.putEdge(s.toLowerCase(), thingNode.toLowerCase());
					}
				} 
			}
			
			if (!superClassMap.containsKey(s)) {
				graph.putEdge(s.toLowerCase(), "resource");
			}
		}

		return graph;
	}
	
	public static MutableGraph<String> createGraph (OWLOntology onto, Set<String> userDefinedMaterials) {

		//get classes and their superclasses
		Map<String, String> superClassMap = OntologyOperations.getClassesAndSuperClassesUsingPellet(onto);
		
		for (Entry<String, String> e : superClassMap.entrySet()) {
			System.out.println(e);
		}
		
		Set<String> classes = new HashSet<String>();
		classes.addAll(superClassMap.keySet());
		
		System.err.println("Graph: Adding " + userDefinedMaterials + " to classes");
		classes.addAll(userDefinedMaterials);

		System.err.println("Creating the graph...");
		//create the graph
		MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
		System.err.println("Graph created...");
		//create a node for thing
		String thingNode = "Thing";

		for (String s : classes) {
			String superClass = null;

			for (Entry<String, String> entry : superClassMap.entrySet()) {
				if (s.equals(entry.getKey())) {
					superClass = superClassMap.get(entry.getKey());
					//create an is-a relationship from the class to its superclass. If a class does not have any defined superclasses, create an is-relationship to thing
					if (superClass != null) {
						//System.err.println("Adding edge " + s.toLowerCase() + " --- " + superClass.toLowerCase());
						graph.putEdge(s.toLowerCase(), superClass.toLowerCase());
					} else {
						//System.err.println("Adding edge " + s.toLowerCase() + " --- " + thingNode.toLowerCase());
						graph.putEdge(s.toLowerCase(), thingNode.toLowerCase());
					}
				} 
			}
			
			if (!superClassMap.containsKey(s)) {
				graph.putEdge(s.toLowerCase(), "resource");
			}
		}

		return graph;
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
		sourceNodeList.remove(sourceNode);

		//reverse the linked list to get the right order of generality of the parent nodes
		Collections.reverse(sourceNodeList);

		//traverse the graph to get parents of targetNode
		Iterator<String> iterTarget = Traverser.forGraph(graph).breadthFirst(targetNode).iterator();

		List<String> targetNodeList = new LinkedList<String>();
		while (iterTarget.hasNext()) {
			targetNodeList.add(iterTarget.next());
		}

		//remove the targetNode from the list so that only parents remain
		targetNodeList.remove(targetNode);

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
		
		Set<String> userDefinedConcepts = new HashSet<String>();
		userDefinedConcepts.add("Chocolate");
		userDefinedConcepts.add("lkjslgkjsdlkgjs");

		//create graph
		MutableGraph<String> graph = createGraph(onto, userDefinedConcepts);
		
		//print graph
		printGraphNodes(graph);
		
		System.out.println(graph.toString());

		//get the lcs of sourceNode and targetNode
//		String sourceNode = "VerticalMilling";
//		String targetNode = "Turning";
//		String lcs = getLCS(sourceNode, targetNode, graph);
//		System.out.println("\nThe lcs of " + sourceNode + " and " + targetNode + " is " + lcs);
//		System.out.println("\nThe depth of " + sourceNode + " is " + getNodeDepth(sourceNode, graph));
//
//		Map<String, Integer> hierarchyMap = getOntologyHierarchy(onto, graph);
//
//		System.out.println("\nPrinting hierarchyMap:");
//		for (Entry<String, Integer> e : hierarchyMap.entrySet()) {
//			System.out.println(e.getKey() + " : " + e.getValue());
//		}

	}

} 