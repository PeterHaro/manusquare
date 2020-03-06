package embedding.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import embedding.vectoraggregation.VectorAggregationMethod;
import owlprocessing.OntologyOperations;
import query.ConsumerQuery;
import utilities.Cosine;

public class EmbeddingEvaluation {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {

		File ontoFile = new File("./files/ONTOLOGIES/updatedOntology.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		String embeddingsFile = "./files/EMBEDDINGS/manusquare_wikipedia_trained_NN_VBG.txt";
		Set<String> cls = OntologyOperations.getClassesAsString(onto);		

		Map<String, double[]> ontologyVectorMap = ConsumerQuery.createOntologyVectorMap(cls, embeddingsFile, VectorAggregationMethod.SUM);
		Map<String, double[]> vectorMap = ConsumerQuery.createVectorMap(embeddingsFile);

		double minThreshold = 0.8;
		double maxThreshold = 0.9;

		
		//print vectorMap
		String vectorMapPath = "./files/EMBEDDINGS/vectorMap_NN_VBG.txt";
		printVectorMap(vectorMap, vectorMapPath);
		
		//find relations
		//findRelations(vectorMap, ontologyVectorMap, minThreshold, maxThreshold);
		
		//print duplicates in VM and OVM
		String duplicatesPath = "./files/EMBEDDINGS/duplicates_NN_VBG.txt";
		printDuplicates(vectorMap, ontologyVectorMap, duplicatesPath);

	}

	public static void printVectorMap (Map<String, double[]> vectorMap, String path) throws IOException {

		FileWriter fw = new FileWriter(path);
		PrintWriter pw = new PrintWriter(fw);

		for (Entry<String, double[]> e : vectorMap.entrySet() ) {
			pw.println(e.getKey());
		}

		fw.close();
		pw.close();
	}

	public static void findRelations (Map<String, double[]> vectorMap, Map<String, double[]> ontologyVectorMap, double minThreshold, double maxThreshold) {

		Set<Relation> scores = new HashSet<Relation>();
		double sim = 0;
		for (Entry<String, double[]> vm : vectorMap.entrySet()) {
			for (Entry<String, double[]> ovm : ontologyVectorMap.entrySet()) {
				sim = Cosine.cosineSimilarity(vm.getValue(), ovm.getValue());
				if (sim > minThreshold && sim < maxThreshold)
					scores.add(new Relation(vm.getKey(), ovm.getKey(), sim));
			}
		}

		System.out.println("There are " + scores.size() + " relations having a similarity above " + minThreshold + " and below " + maxThreshold);

		for (Relation rel : scores) {
			System.out.println(rel.toString());
		}

	}

	public static void printDuplicates (Map<String, double[]> vectorMap, Map<String, double[]> ontologyVectorMap, String filePath) throws IOException {

		FileWriter fw = new FileWriter(filePath);
		PrintWriter pw = new PrintWriter(fw);

		for (Entry<String, double[]> vm : vectorMap.entrySet()) {
			for (Entry<String, double[]> ovm : ontologyVectorMap.entrySet()) {
				if (vm.getKey().equals(ovm.getKey())) {
					pw.println(vm.getKey());
				}
			}
		}

		fw.close();
		pw.close();

	}

}
