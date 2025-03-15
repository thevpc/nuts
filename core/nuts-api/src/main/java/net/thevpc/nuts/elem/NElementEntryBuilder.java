package net.thevpc.nuts.elem;

import java.util.List;

public interface NElementEntryBuilder extends NElementBuilder {
    NElementEntryBuilder addAnnotations(List<NElementAnnotation> annotations);

    NElementEntryBuilder addAnnotation(NElementAnnotation annotation);

    NElementEntryBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NElementEntryBuilder removeAnnotationAt(int index);

    NElementEntryBuilder clearAnnotations();

    List<NElementAnnotation> getAnnotations();

    NElementEntry build();

    NElement getKey();

    NElement getValue();

    NElementEntryBuilder setKey(NElement value);

    NElementEntryBuilder setValue(NElement value);

}
