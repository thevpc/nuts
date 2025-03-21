package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;

import java.util.List;

public class DefaultNBinaryStreamElementBuilder extends AbstractNElementBuilder implements NBinaryStreamElementBuilder {
    private NInputStreamProvider value;


    public DefaultNBinaryStreamElementBuilder() {
    }


    public NInputStreamProvider value() {
        return value;
    }

    public NBinaryStreamElementBuilder setValue(NInputStreamProvider value) {
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
