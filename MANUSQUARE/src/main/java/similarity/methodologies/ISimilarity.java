package similarity.methodologies;

import similarity.methodologies.parameters.SimilarityParameters;

public interface ISimilarity<P extends SimilarityParameters> {
    public double ComputeSimilaritySimpleGraph(P params);
}

