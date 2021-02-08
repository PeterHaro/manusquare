package similarity.methodologies;

import graph.Graph;
import similarity.methodologies.parameters.WuPalmerParameters;

public class WuPalmerSimilarity implements ISimilarity<WuPalmerParameters> {

    
    @Override
    public double ComputeSimilaritySimpleGraph(WuPalmerParameters params) {
                
        String LCS = Graph.getLCS(params.sourceNode, params.targetNode, params.graph);
        
        int sourceNodeDepth = Graph.getNodeDepth(params.sourceNode, params.graph);
        int targetNodeDepth = Graph.getNodeDepth(params.targetNode, params.graph);
        int lcsNodeDepth = Graph.getNodeDepth(LCS, params.graph);

        if (params.sourceNode.equals(params.targetNode)) {
            return 1.0;
        } else {
            return (2 * (double) lcsNodeDepth) / ((double) sourceNodeDepth + (double) targetNodeDepth);
        }
        
    }
}
