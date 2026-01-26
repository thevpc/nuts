package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorSymbolElement;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;

public class DefaultNOperatorSymbolElementBuilder extends AbstractNElementBuilder implements NOperatorSymbolElementBuilder {
    private NOperatorSymbol symbol;
    public DefaultNOperatorSymbolElementBuilder() {
    }

    public DefaultNOperatorSymbolElementBuilder(NOperatorSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public NOperatorSymbolElementBuilder symbol(NOperatorSymbol symbol) {
        this.symbol=symbol;
        return this;
    }

    @Override
    public NOperatorSymbolElement build() {
        return new DefaultNOperatorSymbolElement(symbol,affixes(), diagnostics());
    }

    @Override
    public NElementType type() {
        return NElementType.OPERATOR_SYMBOL;
    }

    public NOperatorSymbol symbol() {
        return symbol;
    }
    // ------------------------------------------

    @Override
    public DefaultNOperatorSymbolElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NOperatorSymbolElementBuilder) {
            NOperatorSymbolElementBuilder b = (NOperatorSymbolElementBuilder) other;
            this.symbol=b.symbol();
        }
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NOperatorSymbolElement) {
            NOperatorSymbolElement b = (NOperatorSymbolElement) other;
            this.symbol=b.symbol();
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public DefaultNOperatorSymbolElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public DefaultNOperatorSymbolElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public DefaultNOperatorSymbolElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public DefaultNOperatorSymbolElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }
}
