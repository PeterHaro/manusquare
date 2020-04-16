package similarity.SimilarityMethodologies.SimilarityParameters;

import com.google.common.graph.MutableGraph;
import org.semanticweb.owlapi.model.OWLOntology;
import similarity.SimilarityMethods;

//TODO: CONVERT TO BUILDERS (if more params
public class SimilarityParametersFactory {
    public static SimilarityParameters CreateSimpleGraphParameters(SimilarityMethods methodology, String sourceNode, String targetNode, OWLOntology ontology, MutableGraph<String> graph) {
        switch(methodology) {
            case WU_PALMER:
                return new WuPalmerParameters(sourceNode, targetNode, graph);
//            case RESNIK:
//                    return new ResnikSimilarityParameters(sourceNode, targetNode, label, ontology);
//            case LIN:
//                return new LinSimilarityParameters(sourceNode, targetNode, label, ontology);
//            case JIANG_CONRATH:
//                return new JiangConrathSimilarityParameters(sourceNode, targetNode, label, ontology);
            default:
                throw new UnsupportedOperationException("Invalid methodology for creating SimpleGraph Parameters:: " + methodology);
        }
    }
}
