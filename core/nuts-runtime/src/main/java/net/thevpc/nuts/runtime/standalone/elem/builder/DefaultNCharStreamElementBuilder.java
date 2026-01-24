package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNCharStreamElement;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNCharStreamElementBuilder extends AbstractNElementBuilder implements NCharStreamElementBuilder {
    private NReaderProvider value;
    private String blocIdentifier;


    public DefaultNCharStreamElementBuilder() {
    }

    @Override
    public NCharStreamElementBuilder doWith(Consumer<NCharStreamElementBuilder> con) {
        if (con != null) {
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
        return new DefaultNCharStreamElement(blocIdentifier, value, affixes(), diagnostics());
    }

    public String blocIdentifier() {
        return blocIdentifier;
    }

    public NCharStreamElementBuilder blocIdentifier(String blocIdentifier) {
        this.blocIdentifier = blocIdentifier;
        return this;
    }

    @Override
    public NElementType type() {
        return NElementType.CHAR_STREAM;
    }

    // ------------------------------------------

    @Override
    public NCharStreamElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NCharStreamElementBuilder) {
            NCharStreamElementBuilder b = (NCharStreamElementBuilder) other;
            blocIdentifier(b.blocIdentifier());
            value(b.value());
        }
        return this;
    }

    @Override
    public NCharStreamElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NCharStreamElement) {
            NCharStreamElement b = (NCharStreamElement) other;
            blocIdentifier(b.blocIdentifier());
            value(b.value());
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NCharStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    public NCharStreamElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeAffix(int index) {
        super.removeAffix(index);
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

    @Override
    public NCharStreamElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NCharStreamElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NCharStreamElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NCharStreamElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NCharStreamElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
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
    public NCharStreamElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NCharStreamElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NCharStreamElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }
}
