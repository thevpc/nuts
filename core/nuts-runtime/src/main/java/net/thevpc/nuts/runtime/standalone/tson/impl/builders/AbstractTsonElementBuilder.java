package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTsonElementBuilder<T extends TsonElementBuilder> implements TsonElementBuilder {
    private TsonComments comments;
    private final List<TsonAnnotation> annotations = new ArrayList<>();

    @Override
    public TsonComments comments() {
        return comments;
    }

    @Override
    public List<TsonAnnotation> annotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public T setComments(TsonComments comments) {
        if (comments != null && comments.isEmpty()) {
            comments = null;
        }
        this.comments = comments;
        return (T) this;
    }

    @Override
    public T setAnnotations(TsonAnnotation[] annotations) {
        this.annotations.clear();
        addAnnotations(annotations);
        return (T) this;
    }

    @Override
    public T addAnnotations(TsonAnnotation... annotations) {
        for (TsonAnnotation annotation : annotations) {
            addAnnotation(annotation);
        }
        return (T) this;
    }

    @Override
    public T addAnnotations(Collection<TsonAnnotation> annotations) {
        if (annotations != null) {
            this.annotations.addAll(annotations);
        }
        return (T) this;
    }


    @Override
    public T addAnnotation(TsonAnnotation annotation) {
        if (annotation != null) {
            this.annotations.add(annotation);
        }
        return (T) this;
    }

    @Override
    public T addAnnotation(String name, TsonElementBase... elements) {
        return addAnnotation(Tson.ofAnnotation(name, elements));
    }

    @Override
    public T removeAnnotationAt(int index) {
        this.annotations.remove(index);
        return (T) this;
    }

    @Override
    public T annotation(String name, TsonElementBase... elements) {
        return addAnnotation(name, elements);
    }

    @Override
    public T comments(TsonComments comments) {
        return setComments(comments);
    }

    @Override
    public String toString() {
        return build().toString();
    }

    @Override
    public String toString(boolean compact) {
        return build().toString(compact);
    }

    @Override
    public String toString(TsonFormat format) {
        return build().toString(format);
    }

    @Override
    public T clearAnnotations() {
        annotations.clear();
        return (T) this;
    }

    @Override
    public TsonArrayBuilder toArray() {
        return (TsonArrayBuilder) this;
    }

    @Override
    public TsonObjectBuilder toObject() {
        return (TsonObjectBuilder) this;
    }

    @Override
    public TsonUpletBuilder toUplet() {
        return (TsonUpletBuilder) this;
    }

    @Override
    public TsonElementBuilder anchor(String name) {
        return addAnnotation(null, Tson.ofName(name));
    }

}
