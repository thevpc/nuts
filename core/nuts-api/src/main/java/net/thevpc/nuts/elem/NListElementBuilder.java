package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

public interface NListElementBuilder extends NElementBuilder {
    int depth();

    NListElementBuilder addItem(NListItemElement item);

    NListItemElement get(int index);

    List<NListItemElement> items();

    NListElementBuilder setItemAt(int index, NListItemElement other);

    int size();

    NListElementBuilder doWith(Consumer<NListElementBuilder> con);

    NListElement build();


    /// ///////////////////////////////////////////////
    NListElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NListElementBuilder addAnnotation(NElementAnnotation annotation);

    NListElementBuilder addAnnotation(String name, NElement... args);

    NListElementBuilder addAffixAt(int index, NBoundAffix affix);

    NListElementBuilder setAffixAt(int index, NBoundAffix affix);

    NListElementBuilder setAffixes(List<NBoundAffix> affixes);

    NListElementBuilder addAffix(NBoundAffix affix);

    NListElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    NListElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    NListElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NListElementBuilder removeAffix(int affix);

    NListElementBuilder removeAnnotation(NElementAnnotation annotation);

    NListElementBuilder clearAnnotations();

    NListElementBuilder clearAffixes();

    NListElementBuilder addLeadingComment(NElementComment comment);

    NListElementBuilder addLeadingComments(NElementComment... comments);

    NListElementBuilder addTrailingComments(NElementComment... comments);

    NListElementBuilder addTrailingComment(NElementComment comment);

    NListElementBuilder clearComments();

    NListElementBuilder copyFrom(NElementBuilder other);

    NListElementBuilder copyFrom(NElement other);

    NListElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NListElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NListElementBuilder addDiagnostic(NElementDiagnostic error);

    NListElementBuilder removeDiagnostic(NElementDiagnostic error);

    NListElementBuilder addAffixes(List<NBoundAffix> affixes);

    NListElementBuilder metadata(NElementMetadata metadata);
}
