package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.util.NMapStrategy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNCharStreamElementBuilder extends AbstractNElementBuilder implements NCharStreamElementBuilder {
    private NReaderProvider value;


    public DefaultNCharStreamElementBuilder() {
    }


    @Override
    public NCharStreamElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NCharStreamElementBuilder copyFrom(NElement other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NCharStreamElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        return (NCharStreamElementBuilder) super.copyFrom(other,strategy);
    }

    @Override
    public NCharStreamElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        return (NCharStreamElementBuilder) super.copyFrom(other,strategy);
    }

    @Override
    public NCharStreamElementBuilder doWith(Consumer<NCharStreamElementBuilder> con) {
        if(con!=null){
            con.accept(this);
        }
        return this;
    }

    public NReaderProvider value() {
        return value;
    }

    public NCharStreamElementBuilder value(NReaderProvider value) {
        this.value = value;
        return this;
    }


    @Override
    public NCharStreamElement build() {
        return new DefaultNCharStreamElement(value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return NElementType.CHAR_STREAM;
    }

    public NCharStreamElementBuilder copyFrom(NCharStreamElement element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
        }
        return this;
    }

    public NCharStreamElementBuilder copyFrom(NCharStreamElementBuilder element) {
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
    public NCharStreamElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name,args);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NCharStreamElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NCharStreamElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

}
