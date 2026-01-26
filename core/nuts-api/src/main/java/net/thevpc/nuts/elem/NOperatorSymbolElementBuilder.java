package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;

public interface NOperatorSymbolElementBuilder extends NElementBuilder {

    NOperatorSymbol symbol();
    NOperatorSymbolElementBuilder symbol(NOperatorSymbol symbol);

    NOperatorSymbolElement build();


    /// ///////////////////////////////////////////////
    NOperatorSymbolElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NOperatorSymbolElementBuilder addAnnotation(NElementAnnotation annotation);

    NOperatorSymbolElementBuilder addAnnotation(String name, NElement... args);

    NOperatorSymbolElementBuilder addAffix(int index, NBoundAffix affix);

    NOperatorSymbolElementBuilder setAffix(int index, NBoundAffix affix);

    NOperatorSymbolElementBuilder addAffix(NBoundAffix affix);

    NOperatorSymbolElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NOperatorSymbolElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NOperatorSymbolElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NOperatorSymbolElementBuilder removeAffix(int index);

    NOperatorSymbolElementBuilder removeAnnotation(NElementAnnotation annotation);

    NOperatorSymbolElementBuilder clearAnnotations();

    NOperatorSymbolElementBuilder addLeadingComment(NElementComment comment);

    NOperatorSymbolElementBuilder addLeadingComments(NElementComment... comments);

    NOperatorSymbolElementBuilder addTrailingComments(NElementComment... comments);

    NOperatorSymbolElementBuilder addTrailingComment(NElementComment comment);

    NOperatorSymbolElementBuilder clearComments();

    NOperatorSymbolElementBuilder copyFrom(NElementBuilder other);

    NOperatorSymbolElementBuilder copyFrom(NElement other);

    NOperatorSymbolElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NOperatorSymbolElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NOperatorSymbolElementBuilder addDiagnostic(NElementDiagnostic error);

    NOperatorSymbolElementBuilder removeDiagnostic(NElementDiagnostic error);

    NOperatorSymbolElementBuilder addAffixes(List<NBoundAffix> affixes);
}
