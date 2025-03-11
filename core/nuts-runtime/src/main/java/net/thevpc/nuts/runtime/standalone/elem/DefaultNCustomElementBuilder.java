package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultNCustomElementBuilder implements NCustomElementBuilder {
    private Object value;
    private final List<NElementAnnotation> annotations = new ArrayList<>();


    public DefaultNCustomElementBuilder() {
    }

    @Override
    public NCustomElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    public Object getValue() {
        return value;
    }

    public NCustomElementBuilder setValue(Object value) {
        this.value = value;
        return this;
    }


    @Override
    public NCustomElementBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NCustomElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NCustomElementBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NCustomElementBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public NCustomElement build() {
        return new DefaultNCustomElement(value, annotations.toArray(new NElementAnnotation[0]));
    }

    @Override
    public NElementType type() {
        return NElementType.CUSTOM;
    }

}
