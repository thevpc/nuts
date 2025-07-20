package net.thevpc.nuts.runtime.standalone.format.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.runtime.standalone.format.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.format.elem.item.DefaultNBinaryStreamElement;
import net.thevpc.nuts.util.NMapStrategy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNBinaryStreamElementBuilder extends AbstractNElementBuilder implements NBinaryStreamElementBuilder {
    private NInputStreamProvider value;


    public DefaultNBinaryStreamElementBuilder() {
    }
    @Override
    public NBinaryStreamElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }
    @Override
    public NBinaryStreamElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElement other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        return (NBinaryStreamElementBuilder) super.copyFrom(other,strategy);
    }

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        return (NBinaryStreamElementBuilder) super.copyFrom(other,strategy);
    }

    @Override
    public NBinaryStreamElementBuilder doWith(Consumer<NBinaryStreamElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    @Override
    public NInputStreamProvider getValue() {
        return value;
    }

    public NInputStreamProvider value() {
        return value;
    }

    public NBinaryStreamElementBuilder setValue(NInputStreamProvider value) {
        return value(value);
    }

    public NBinaryStreamElementBuilder value(NInputStreamProvider value) {
        this.value = value;
        return this;
    }


    @Override
    public NBinaryStreamElement build() {
        return new DefaultNBinaryStreamElement(value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return NElementType.BINARY_STREAM;
    }

    public NBinaryStreamElementBuilder copyFrom(NBinaryStreamElement element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
        }
        return this;
    }

    public NBinaryStreamElementBuilder copyFrom(NBinaryStreamElementBuilder element) {
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
    public NBinaryStreamElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }


    @Override
    public NBinaryStreamElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

}
