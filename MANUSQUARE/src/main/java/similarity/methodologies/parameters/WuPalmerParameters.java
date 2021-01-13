package similarity.methodologies.parameters;

import com.google.common.graph.MutableGraph;

public class WuPalmerParameters extends SimilarityParameters {
    public String sourceNode;
    public String targetNode;
    public MutableGraph<String> graph;

    public WuPalmerParameters(String sourceNode, String targetNode, MutableGraph<String> graph) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.graph = graph;
    }
    
}
