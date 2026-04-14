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

    NOperatorSymbolElementBuilder addAffixAt(int index, NBoundAffix affix);

    NOperatorSymbolElementBuilder setAffixAt(int index, NBoundAffix affix);

    NOperatorSymbolElementBuilder addAffix(NBoundAffix affix);

    NOperatorSymbolElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    NOperatorSymbolElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    NOperatorSymbolElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NOperatorSymbolElementBuilder removeAffix(int affix);

    NOperatorSymbolElementBuilder removeAnnotation(NElementAnnotation annotation);

    NOperatorSymbolElementBuilder clearAnnotations();

    NOperatorSymbolElementBuilder clearAffixes();

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

    NOperatorSymbolElementBuilder metadata(NElementMetadata metadata);
}
