package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMapStrategy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNCustomElementBuilder extends AbstractNElementBuilder implements NCustomElementBuilder {
    private Object value;


    public DefaultNCustomElementBuilder() {
    }

    @Override
    public NCustomElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NCustomElementBuilder copyFrom(NElement other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NCustomElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        return (NCustomElementBuilder) super.copyFrom(other,strategy);
    }

    @Override
    public NCustomElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        return (NCustomElementBuilder) super.copyFrom(other,strategy);
    }

    @Override
    public boolean isCustomTree() {
        return true;
    }

    @Override
    public NCustomElementBuilder doWith(Consumer<NCustomElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    public Object value() {
        return value;
    }

    public NCustomElementBuilder value(Object value) {
        this.value = value;
        return this;
    }


    @Override
    public NCustomElement build() {
        return new DefaultNCustomElement(value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return NElementType.CUSTOM;
    }

    public NCustomElementBuilder copyFrom(NCustomElement element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
        }
        return this;
    }

    public NCustomElementBuilder copyFrom(NCustomElementBuilder element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NCustomElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NCustomElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NCustomElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NCustomElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NCustomElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NCustomElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NCustomElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NCustomElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NCustomElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NCustomElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NCustomElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NCustomElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NCustomElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NCustomElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NCustomElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NCustomElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NCustomElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NCustomElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

}
