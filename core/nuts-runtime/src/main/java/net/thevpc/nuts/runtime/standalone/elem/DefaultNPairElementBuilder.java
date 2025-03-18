package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultNPairElementBuilder implements NPairElementBuilder {
    private NElement key;
    private NElement value;
    private final List<NElementAnnotation> annotations = new ArrayList<>();

    public DefaultNPairElementBuilder() {
        key = NElements.of().ofNull();
        value = NElements.of().ofNull();
    }

    public DefaultNPairElementBuilder(NElement key, NElement value) {
        this.key = key == null ? NElements.of().ofNull() : key;
        this.value = value == null ? NElements.of().ofNull() : value;
    }

    @Override
    public NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    public NPairElementBuilder setValue(NElement value) {
        this.value = value == null ? NElements.of().ofNull() : value;
        return this;
    }

    public NPairElementBuilder copyFrom(NPairElement other) {
        if (other != null) {
            setKey(other.key());
            setValue(other.value());
        }
        return this;
    }

    public NPairElementBuilder setKey(NElement key) {
        this.key = key == null ? NElements.of().ofNull() : key;
        return this;
    }


    @Override
    public NPairElementBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NPairElementBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NPairElementBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public NPairElement build() {
        return new DefaultNPairElement(key, value, annotations.toArray(new NElementAnnotation[0]));
    }

    @Override
    public NElementType type() {
        return NElementType.PAIR;
    }

    @Override
    public NElement getValue() {
        return value;
    }

    @Override
    public NElement getKey() {
        return key;
    }


}
