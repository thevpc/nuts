package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNFlatExprElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultNFlatExprElementBuilder extends AbstractNElementBuilder implements NFlatExprElementBuilder {
    private List<NElement> values = new ArrayList<>();

    public DefaultNFlatExprElementBuilder() {
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
    public NFlatExprElementBuilder set(int index, NOperatorSymbol op) {
        if (op != null) {
            if (index >= 0 && index < values.size()) {
                values.set(index, NElement.ofOpSymbol(op));
            }
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder set(int index, NElement element) {
        if (element != null) {
            if (index >= 0 && index < values.size()) {
                values.set(index, element);
            }
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder add(NOperatorSymbol op) {
        if (op != null) {
            values.add(NElement.ofOpSymbol(op));
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder add(NElement element) {
        if (element != null) {
            values.add(element);
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other, NAssignmentPolicy.ANY);
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElement other) {
        copyFrom(other, NAssignmentPolicy.ANY);
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NFlatExprElementBuilder) {
            NFlatExprElementBuilder from = (NFlatExprElementBuilder) other;
            this.values.addAll(from.children());
        }
        return this;
    }

    @Override
    public NFlatExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NFlatExprElement) {
            NFlatExprElement from = (NFlatExprElement) other;
            this.values.addAll(from.children());
        }
        return this;
    }


    @Override
    public NFlatExprElement build() {
        return new DefaultNFlatExprElement(values,
                annotations(), comments(), diagnostics()
        );
    }

    @Override
    public NElementType type() {
        return NElementType.FLAT_EXPR;
    }


    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NFlatExprElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NFlatExprElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NFlatExprElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NFlatExprElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NFlatExprElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    public NFlatExprElementBuilder copyFrom(NFlatExprElementBuilder element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            this.values.addAll(element.children());
        }
        return this;
    }


    @Override
    public NFlatExprElementBuilder doWith(Consumer<NFlatExprElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }
}
