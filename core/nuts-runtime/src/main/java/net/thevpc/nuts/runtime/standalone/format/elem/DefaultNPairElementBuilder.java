package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNPairElementBuilder extends AbstractNElementBuilder implements NPairElementBuilder {
    private NElement key;
    private NElement value;

    public DefaultNPairElementBuilder() {
        key = NElements.of().ofNull();
        value = NElements.of().ofNull();
    }

    @Override
    public NPairElementBuilder doWith(Consumer<NPairElementBuilder> con) {
        if(con!=null){
            con.accept(this);
        }
        return this;
    }

    public DefaultNPairElementBuilder(NElement key, NElement value) {
        this.key = key == null ? NElements.of().ofNull() : key;
        this.value = value == null ? NElements.of().ofNull() : value;
    }


    public NPairElementBuilder value(NElement value) {
        this.value = value == null ? NElements.of().ofNull() : value;
        return this;
    }

    public NPairElementBuilder copyFrom(NPairElement element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            key(element.key());
            value(element.value());
        }
        return this;
    }

    public NPairElementBuilder copyFrom(NPairElementBuilder element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            key(element.key());
            value(element.value());
        }
        return this;
    }

    public NPairElementBuilder key(NElement key) {
        this.key = key == null ? NElements.of().ofNull() : key;
        return this;
    }

    @Override
    public NPairElementBuilder key(String key) {
        this.key = key == null ? NElements.of().ofNull() : NElements.of().ofNameOrString(key);
        return this;
    }

    @Override
    public NPairElement build() {
        return new DefaultNPairElement(key, value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return NElementType.PAIR;
    }

    @Override
    public NElement value() {
        return value;
    }

    @Override
    public NElement key() {
        return key;
    }


    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NPairElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NPairElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NPairElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NPairElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NPairElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NPairElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NPairElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NPairElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NPairElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NPairElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

}
