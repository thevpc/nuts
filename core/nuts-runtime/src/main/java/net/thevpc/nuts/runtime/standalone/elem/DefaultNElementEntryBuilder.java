package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.NIllegalArgumentException;
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

public class DefaultNElementEntryBuilder implements NElementEntryBuilder {
    private NElement key;
    private NElement value;
    private final List<NElementAnnotation> annotations = new ArrayList<>();

    public DefaultNElementEntryBuilder() {
        key = NElements.of().ofNull();
        value = NElements.of().ofNull();
    }

    public DefaultNElementEntryBuilder(NElement key, NElement value) {
        this.key = key==null?NElements.of().ofNull():key;
        this.value = value==null?NElements.of().ofNull():value;
    }

    @Override
    public NElementEntryBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    public NElementEntryBuilder setValue(NElement value) {
        this.value = value == null ? NElements.of().ofNull() : value;
        return this;
    }

    public NElementEntryBuilder setKey(NElement key) {
        this.key = key == null ? NElements.of().ofNull() : key;
        return this;
    }


    @Override
    public NElementEntryBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NElementEntryBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NElementEntryBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NElementEntryBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public NElementEntry build() {
        return new DefaultNElementEntry(key, value, annotations.toArray(new NElementAnnotation[0]));
    }

    @Override
    public NElementType type() {
        return NElementType.ENTRY;
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
