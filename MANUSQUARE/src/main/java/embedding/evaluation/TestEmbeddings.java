package embedding.evaluation;

import similarity.SemanticSimilarity;

public class TestEmbeddings {
	
	public static void main(String[] args) {
		
		
		String concept1 = "viscose";
		String concept2 = "rayon";
		
		double sim = SemanticSimilarity.computeWESimilarity (concept1, concept2);
		
		System.out.println("Cosine similarity between " + concept1 + " and " + concept2 + ": " + sim);
	}

}
