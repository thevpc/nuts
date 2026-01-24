package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

public interface NEmptyElementBuilder extends NElementBuilder {
    NEmptyElementBuilder doWith(Consumer<NEmptyElementBuilder> con);

    NEmptyElement build();

    /// ///////////////////////////////////////////////
    NEmptyElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NEmptyElementBuilder addAnnotation(NElementAnnotation annotation);

    NEmptyElementBuilder addAnnotation(String name, NElement... args);

    NEmptyElementBuilder addAffix(int index, NBoundAffix affix);

    NEmptyElementBuilder setAffix(int index, NBoundAffix affix);
    NEmptyElementBuilder addAffix(NBoundAffix affix);

    NEmptyElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NEmptyElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NEmptyElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NEmptyElementBuilder removeAffix(int index);

    NEmptyElementBuilder removeAnnotation(NElementAnnotation annotation);

    NEmptyElementBuilder clearAnnotations();

    NEmptyElementBuilder addLeadingComment(NElementComment comment);

    NEmptyElementBuilder addLeadingComments(NElementComment... comments);

    NEmptyElementBuilder addTrailingComments(NElementComment... comments);

    NEmptyElementBuilder addTrailingComment(NElementComment comment);

    NEmptyElementBuilder clearComments();

    NEmptyElementBuilder copyFrom(NElementBuilder other);

    NEmptyElementBuilder copyFrom(NElement other);

    NEmptyElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NEmptyElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NEmptyElementBuilder addDiagnostic(NElementDiagnostic error);

    NEmptyElementBuilder removeDiagnostic(NElementDiagnostic error);

    NEmptyElementBuilder addAffixes(List<NBoundAffix> affixes);
}
