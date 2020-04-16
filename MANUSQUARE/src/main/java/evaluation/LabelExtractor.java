package evaluation;

import org.semanticweb.owlapi.model.*;

/** Simple visitor that grabs any labels on an entity. Author: Sean Bechhofer,
 * The University Of Manchester, Information Management Group, Date: 17-03-2007 */
@SuppressWarnings({ "unused", "javadoc" })
public class LabelExtractor implements OWLAnnotationObjectVisitor {
    String result;

    public LabelExtractor() {
        result = null;
    }

    @Override
    public void visit(OWLAnonymousIndividual individual) {}

    @Override
    public void visit(IRI iri) {}

    @Override
    public void visit(OWLLiteral literal) {}

    @Override
    public void visit(OWLAnnotation annotation) {
        /*
         * If it's a label, grab it as the result. Note that if there are
         * multiple labels, the last one will be used.
         */
        if (annotation.getProperty().isLabel()) {
            OWLLiteral c = (OWLLiteral) annotation.getValue();
            result = c.getLiteral();
        }
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {}

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {}

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {}

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {}

    public void visit(OWLAnnotationProperty property) {}

    public void visit(OWLAnnotationValue value) {}

    public String getResult() {
        return result;
    }
}