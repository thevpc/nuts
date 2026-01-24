package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.NBoundAffixList;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNBoundAffix;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementNewLine;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementSeparator;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementSpace;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractNElementBuilder implements NElementBuilder {
    private final NBoundAffixList affixes = new NBoundAffixList();
    private final List<NElementDiagnostic> diagnostics = new ArrayList<>();


    @Override
    public List<NBoundAffix> affixes() {
        return affixes.list();
    }

    @Override
    public NElementBuilder addDiagnostic(NElementDiagnostic error) {
        if (error != null) {
            this.diagnostics.add(error);
        }
        return this;
    }

    @Override
    public NElementBuilder removeDiagnostic(NElementDiagnostic error) {
        if (error != null) {
            this.diagnostics.remove(error);
        }
        return this;
    }

    @Override
    public List<NElementDiagnostic> diagnostics() {
        return new ArrayList<>(diagnostics);
    }

    @Override
    public boolean isCustomTree() {
        return build().isCustomTree();
    }

    public List<NElementComment> comments() {
        return affixes.comments();
    }

    @Override
    public NElementBuilder clearComments() {
        affixes.clearComments();
        return this;
    }

    @Override
    public List<NElementComment> trailingComments() {
        return affixes.trailingComments();
    }

    @Override
    public List<NElementComment> leadingComments() {
        return affixes.leadingComments();
    }

    @Override
    public NElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        this.affixes.addAnnotations(annotations);
        return this;
    }

    public NElementBuilder addAffixes(List<NBoundAffix> affixes) {
        this.affixes.addAffixes(affixes);
        return this;
    }

    public NElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        this.affixes.addAffixes(affixes, anchor);
        return this;
    }

    public NElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        this.affixes.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NElementBuilder addAnnotation(String name, NElement... args) {
        this.affixes.addAnnotation(name, args);
        return this;
    }

    @Override
    public NElementBuilder addAnnotation(NElementAnnotation annotation) {
        this.affixes.addAnnotation(annotation);
        return this;
    }

    @Override
    public NElementBuilder addAffix(int index, NBoundAffix affix) {
        this.affixes.addAffix(index, affix);
        return this;
    }

    @Override
    public NElementBuilder removeAnnotation(NElementAnnotation annotation) {
        this.affixes.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NElementBuilder removeAffix(int index) {
        this.affixes.removeAffix(index);
        return this;
    }

    @Override
    public NElementBuilder clearAnnotations() {
        affixes.clearAnnotations();
        return this;
    }

    @Override
    public List<NElementAnnotation> annotations() {
        return affixes.annotations();
    }


    @Override
    public NElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other, NAssignmentPolicy.ANY);
        return this;
    }

    @Override
    public NElementBuilder copyFrom(NElement other) {
        return copyFrom(other, NAssignmentPolicy.ANY);
    }

    @Override
    public NElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        this.diagnostics.addAll(other.diagnostics());
        this.affixes.addAffixes(other.affixes());
        return this;
    }

    @Override
    public NElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        this.diagnostics.addAll(other.diagnostics());
        this.affixes.addAffixes(other.affixes());
        return this;
    }


    @Override
    public NElementBuilder setAffix(int index, NBoundAffix affix) {
        this.affixes.setAffix(index, affix);
        return this;
    }

    @Override
    public NElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        this.affixes.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NElementBuilder addAffix(NBoundAffix affix) {
        this.affixes.addAffix(affix);
        return this;
    }

    @Override
    public NElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        this.affixes.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        this.affixes.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NElementBuilder addLeadingComment(NElementComment comment) {
        this.affixes.addLeadingComment(comment);
        return this;
    }

    @Override
    public NElementBuilder addLeadingComments(NElementComment... comments) {
        this.affixes.addLeadingComments(comments);
        return this;
    }

    @Override
    public NElementBuilder addTrailingComment(NElementComment comment) {
        this.affixes.addTrailingComment(comment);
        return this;
    }

    @Override
    public NElementBuilder addTrailingComments(NElementComment... comments) {
        this.affixes.addTrailingComments(comments);
        return this;
    }

    @Override
    public NElementBuilder addAffixSpace(String space, NAffixAnchor anchor) {
        NAssert.requireNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(space)) {
            addAffix(NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
        }
        return this;
    }

    @Override
    public NElementBuilder addAffixNewLine(NNewLineMode newLineMode, NAffixAnchor anchor) {
        NAssert.requireNonNull(anchor, "anchor");
        if (newLineMode != null) {
            addAffix(NBoundAffix.of(DefaultNElementNewLine.of(newLineMode), anchor));
        }
        return this;
    }

    @Override
    public NElementBuilder addAffixSeparator(String separator, NAffixAnchor anchor) {
        NAssert.requireNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(separator)) {
            addAffix(NBoundAffix.of(DefaultNElementSeparator.of(separator), anchor));
        }
        return this;
    }

    @Override
    public NElementBuilder addAffixSpace(int index, String space, NAffixAnchor anchor) {
        NAssert.requireNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(space)) {
            addAffix(index, NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
        }
        return this;
    }

    @Override
    public NElementBuilder addAffixNewLine(int index, NNewLineMode newLineMode, NAffixAnchor anchor) {
        NAssert.requireNonNull(anchor, "anchor");
        if (newLineMode != null) {
            addAffix(index, NBoundAffix.of(DefaultNElementNewLine.of(newLineMode), anchor));
        }
        return this;
    }

    @Override
    public NElementBuilder addAffixSeparator(int index, String separator, NAffixAnchor anchor) {
        NAssert.requireNonNull(anchor, "anchor");
        if (!NStringUtils.isEmpty(separator)) {
            addAffix(index, NBoundAffix.of(DefaultNElementSeparator.of(separator), anchor));
        }
        return this;
    }
}
