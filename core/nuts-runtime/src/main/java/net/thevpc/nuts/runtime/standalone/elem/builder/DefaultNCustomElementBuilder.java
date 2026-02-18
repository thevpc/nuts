package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNCustomElement;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNCustomElementBuilder extends AbstractNElementBuilder implements NCustomElementBuilder {
    private Object value;


    public DefaultNCustomElementBuilder() {
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
        return new DefaultNCustomElement(value, affixes(), diagnostics(),metadata());
    }

    @Override
    public NElementType type() {
        return NElementType.CUSTOM;
    }


    // ------------------------------------------

    @Override
    public NCustomElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NCustomElementBuilder) {
            NCustomElementBuilder b = (NCustomElementBuilder) other;
            this.value(b.value());
        }
        return this;
    }

    @Override
    public NCustomElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NCustomElement) {
            NCustomElement b = (NCustomElement) other;
            this.value(b.value());
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

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
    public NCustomElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NCustomElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NCustomElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NCustomElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }

    @Override
    public NCustomElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NCustomElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NCustomElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }

    @Override
    public NCustomElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NCustomElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NCustomElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NCustomElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NCustomElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NCustomElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NCustomElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NCustomElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
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
    public NCustomElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NCustomElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NCustomElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NCustomElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
