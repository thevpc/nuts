package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.NDefaultEmptyElement;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNEmptyElementBuilder extends AbstractNElementBuilder implements NEmptyElementBuilder {
    @Override
    public NEmptyElementBuilder doWith(Consumer<NEmptyElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    @Override
    public NEmptyElement build() {
        return new NDefaultEmptyElement(
                affixes()
                , diagnostics(),metadata()
        );
    }

    @Override
    public NElementType type() {
        return NElementType.EMPTY;
    }


    // ------------------------------------------

    @Override
    public NEmptyElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NEmptyElementBuilder) {
            NEmptyElementBuilder b = (NEmptyElementBuilder) other;
        }
        return this;
    }

    @Override
    public NEmptyElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NEmptyElement) {
            NEmptyElement b = (NEmptyElement) other;
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NEmptyElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NEmptyElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NEmptyElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NEmptyElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }

    @Override
    public NEmptyElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NEmptyElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }


    public NEmptyElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NEmptyElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NEmptyElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NEmptyElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NEmptyElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NEmptyElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NEmptyElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NEmptyElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NEmptyElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NEmptyElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NEmptyElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NEmptyElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NEmptyElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NEmptyElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
