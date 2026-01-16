package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNExprElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultNExprElementBuilder extends AbstractNElementBuilder implements NExprElementBuilder {
    private List<NElement> values = new ArrayList<>();

    public DefaultNExprElementBuilder() {
    }

    @Override
    public List<NElement> children() {
        return new ArrayList<>(values);
    }


    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("index %s", index));
    }

    @Override
    public NExprElementBuilder setOp(int index, NOperatorSymbol op) {
        if (op != null) {
            if (index >= 0 && index < values.size()) {
                values.set(index, NElement.ofOpSymbol(op));
            }
        }
        return this;
    }

    @Override
    public NExprElementBuilder setElement(int index, NElement element) {
        if (element != null) {
            if (index >= 0 && index < values.size()) {
                values.set(index, element);
            }
        }
        return this;
    }

    @Override
    public NExprElementBuilder addOp(NOperatorSymbol op) {
        if (op != null) {
            values.add(NElement.ofOpSymbol(op));
        }
        return this;
    }

    @Override
    public NExprElementBuilder addElement(NElement element) {
        if (element != null) {
            values.add(element);
        }
        return this;
    }

    @Override
    public NExprElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other, NAssignmentPolicy.ANY);
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElement other) {
        copyFrom(other, NAssignmentPolicy.ANY);
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NExprElementBuilder) {
            NExprElementBuilder from = (NExprElementBuilder) other;
            this.values.addAll(from.children());
        }
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NExprElement) {
            NExprElement from = (NExprElement) other;
            this.values.addAll(from.children());
        }
        return this;
    }


    @Override
    public NExprElement build() {
        return new DefaultNExprElement(values,
                annotations().toArray(new NElementAnnotation[0]), comments()
        );
    }

    @Override
    public NElementType type() {
        return NElementType.EXPR;
    }


    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NExprElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NExprElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NExprElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NExprElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NExprElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NExprElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NExprElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NExprElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NExprElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NExprElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NExprElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NExprElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NExprElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NExprElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NExprElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NExprElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NExprElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NExprElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    public NExprElementBuilder copyFrom(NExprElementBuilder element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            this.values.addAll(element.children());
        }
        return this;
    }


    @Override
    public NExprElementBuilder doWith(Consumer<NExprElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }
}
