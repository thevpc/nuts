package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNBinaryStreamElement;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNBinaryStreamElementBuilder extends AbstractNElementBuilder implements NBinaryStreamElementBuilder {
    private NInputStreamProvider value;
    private String blocIdentifier;


    public DefaultNBinaryStreamElementBuilder() {
    }

    public String blocIdentifier() {
        return blocIdentifier;
    }

    public NBinaryStreamElementBuilder blocIdentifier(String blocIdentifier) {
        this.blocIdentifier = blocIdentifier;
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder doWith(Consumer<NBinaryStreamElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }


    public NInputStreamProvider value() {
        return value;
    }

    public NBinaryStreamElementBuilder value(NInputStreamProvider value) {
        this.value = value;
        return this;
    }


    @Override
    public NBinaryStreamElement build() {
        return new DefaultNBinaryStreamElement(value, blocIdentifier, affixes(), diagnostics());
    }

    @Override
    public NElementType type() {
        return NElementType.BINARY_STREAM;
    }


    // ------------------------------------------

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NBinaryStreamElementBuilder) {
            NBinaryStreamElementBuilder b = (NBinaryStreamElementBuilder) other;
            blocIdentifier(b.blocIdentifier());
            value(b.value());
        }
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NBinaryStreamElement) {
            NBinaryStreamElement b = (NBinaryStreamElement) other;
            blocIdentifier(b.blocIdentifier());
            value(b.value());
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NBinaryStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    public NBinaryStreamElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeAffix(int index) {
        super.removeAffix(index);
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

    @Override
    public NBinaryStreamElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NBinaryStreamElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
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
    public NBinaryStreamElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NBinaryStreamElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

}
