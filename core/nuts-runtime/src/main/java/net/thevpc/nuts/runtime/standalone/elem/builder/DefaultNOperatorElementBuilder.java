package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorElementBinary;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorElementUnary;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DefaultNOperatorElementBuilder extends AbstractNElementBuilder implements NOperatorElementBuilder {
    private NOperatorSymbol symbol;
    private NOperatorPosition position;

    private NElement first;

    private NElement second;

    public NOperatorElementBuilder symbol(NOperatorSymbol operator) {
        this.symbol = operator;
        return this;
    }

    @Override
    public NOperatorElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NOperatorPosition position() {
        return position;
    }

    public NOperatorElementBuilder position(NOperatorPosition operatorType) {
        this.position = operatorType;
        return this;
    }

    public NOperatorElementBuilder first(NElement first) {
        this.first = first;
        return this;
    }

    public NOperatorElementBuilder second(NElement second) {
        this.second = second;
        return this;
    }

    @Override
    public NElementType type() {
        return null;
    }

    @Override
    public NOperatorSymbol symbol() {
        return symbol;
    }

    @Override
    public NOptional<NElement> first() {
        return NOptional.ofNamed(first, NMsg.ofC("first operand of %s", symbol()));
    }

    @Override
    public NOptional<NElement> second() {
        return NOptional.ofNamed(second, NMsg.ofC("second operand of %s", symbol()));
    }


    public String toString() {
        return toString(false);
    }

    public String toString(boolean compact) {
        return build().toString(compact);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        DefaultNOperatorElementBuilder that = (DefaultNOperatorElementBuilder) object;
        return symbol == that.symbol && Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), symbol, first, second);
    }

    @Override
    public NOperatorElement build() {
        NAssert.requireNonNull(symbol, "operator");
        NAssert.requireNonNull(first, "first");
        if (position == null) {
            if (second == null) {
                position = NOperatorPosition.PREFIX;
            } else {
                position = NOperatorPosition.INFIX;
            }
        }
        if (position == NOperatorPosition.INFIX && second == null) {
            NAssert.requireNonNull(second, "second");
        }
        if (second == null) {
            return new DefaultNOperatorElementUnary(symbol, position, first, annotations().toArray(new NElementAnnotation[0]), comments());
        }
        return new DefaultNOperatorElementBinary(symbol, position, first, second, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NOperatorElementBuilder copyFrom(NOperatorElementBuilder element) {
        if (element != null) {
            super.copyFrom(element);
            symbol = element.symbol();
            position = element.position();
            first = element.first().orNull();
            second = element.second().orNull();
        }
        return this;
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElementBuilder other) {
        if (other instanceof NOperatorElementBuilder) {
            return copyFrom((NOperatorElementBuilder) other);
        }
        return (NOperatorElementBuilder) super.copyFrom(other);
    }

    ///
    @Override
    public NOperatorElementBuilder doWith(Consumer<NOperatorElementBuilder> con) {
        con.accept(this);
        return this;
    }

    @Override
    public NOperatorElementBuilder addLeadingComment(NElementCommentType type, String text) {
        return (NOperatorElementBuilder) super.addLeadingComment(type, text);
    }

    @Override
    public NOperatorElementBuilder addTrailingComment(NElementCommentType type, String text) {
        return (NOperatorElementBuilder) super.addTrailingComment(type, text);
    }

    @Override
    public NOperatorElementBuilder addLeadingComment(NElementComment comment) {
        return (NOperatorElementBuilder) super.addLeadingComment(comment);
    }

    @Override
    public NOperatorElementBuilder addLeadingComments(NElementComment... comments) {
        return (NOperatorElementBuilder) super.addLeadingComments(comments);
    }

    @Override
    public NOperatorElementBuilder addTrailingComment(NElementComment comment) {
        return (NOperatorElementBuilder) super.addTrailingComment(comment);
    }

    @Override
    public NOperatorElementBuilder addTrailingComments(NElementComment... comments) {
        return (NOperatorElementBuilder) super.addTrailingComments(comments);
    }

    @Override
    public NOperatorElementBuilder clearComments() {
        return (NOperatorElementBuilder) super.clearComments();
    }

    @Override
    public NOperatorElementBuilder removeTrailingCommentAt(int index) {
        return (NOperatorElementBuilder) super.removeTrailingCommentAt(index);
    }

    @Override
    public NOperatorElementBuilder removeLeadingCommentAt(int index) {
        return (NOperatorElementBuilder) super.removeLeadingCommentAt(index);
    }

    @Override
    public NOperatorElementBuilder removeTrailingComment(NElementComment comment) {
        return (NOperatorElementBuilder) super.removeTrailingComment(comment);
    }

    @Override
    public NOperatorElementBuilder removeLeadingComment(NElementComment comment) {
        return (NOperatorElementBuilder) super.removeLeadingComment(comment);
    }

    @Override
    public NOperatorElementBuilder addComments(NElementComments comments) {
        return (NOperatorElementBuilder) super.addComments(comments);
    }

    @Override
    public NOperatorElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        return (NOperatorElementBuilder) super.addAnnotations(annotations);
    }

    @Override
    public NOperatorElementBuilder addAnnotation(String name, NElement... args) {
        return (NOperatorElementBuilder) super.addAnnotation(name, args);
    }

    @Override
    public NOperatorElementBuilder addAnnotation(NElementAnnotation annotation) {
        return (NOperatorElementBuilder) super.addAnnotation(annotation);
    }

    @Override
    public NOperatorElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        return (NOperatorElementBuilder) super.addAnnotationAt(index, annotation);
    }

    @Override
    public NOperatorElementBuilder removeAnnotationAt(int index) {
        return (NOperatorElementBuilder) super.removeAnnotationAt(index);
    }

    @Override
    public NOperatorElementBuilder clearAnnotations() {
        return (NOperatorElementBuilder) super.clearAnnotations();
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        return (NOperatorElementBuilder) super.copyFrom(other, assignmentPolicy);
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        return (NOperatorElementBuilder) super.copyFrom(other, assignmentPolicy);
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElement other) {
        return (NOperatorElementBuilder) super.copyFrom(other);
    }
}
