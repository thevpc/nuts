package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultNCustomElementBuilder implements NCustomElementBuilder {
    private Object value;
    private final List<NElementAnnotation> annotations = new ArrayList<>();

    private transient NWorkspace workspace;

    public DefaultNCustomElementBuilder(NWorkspace workspace) {
        this.workspace = workspace;
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
        return new DefaultNCustomElement(value, annotations.toArray(new NElementAnnotation[0]), workspace);
    }

    @Override
    public NElementType type() {
        return NElementType.CUSTOM;
    }

}
