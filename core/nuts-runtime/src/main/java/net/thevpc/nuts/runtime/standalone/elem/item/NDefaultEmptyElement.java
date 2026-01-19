package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNEmptyElementBuilder;

import java.util.List;

public class NDefaultEmptyElement extends AbstractNElement implements NEmptyElement {
    public NDefaultEmptyElement(List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(NElementType.EMPTY, annotations, comments, diagnostics);
    }

    @Override
    public String toString(boolean compact) {
        return "<empty>";
    }

    @Override
    public NEmptyElementBuilder builder() {
        return (NEmptyElementBuilder)
                new DefaultNEmptyElementBuilder()
                        .addComments(comments())
                        .addAnnotations(annotations())
                ;
    }
}
