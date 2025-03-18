package net.thevpc.nuts.elem;

import java.util.List;

public interface NPairElementBuilder extends NElementBuilder {
    NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NPairElementBuilder addAnnotation(NElementAnnotation annotation);

    NPairElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NPairElementBuilder removeAnnotationAt(int index);

    NPairElementBuilder clearAnnotations();

    List<NElementAnnotation> getAnnotations();

    NPairElement build();

    NElement getKey();

    NElement getValue();

    NPairElementBuilder setKey(NElement value);

    NPairElementBuilder setValue(NElement value);

    NPairElementBuilder copyFrom(NPairElement other);

}
