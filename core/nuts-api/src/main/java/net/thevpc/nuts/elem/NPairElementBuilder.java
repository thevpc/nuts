package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Consumer;

public interface NPairElementBuilder extends NElementBuilder {

    NElement key();

    NElement value();

    NOptional<String> name();

    NPairElementBuilder key(String value);

    NPairElementBuilder key(NElement value);

    NPairElementBuilder value(NElement value);

    NPairElementBuilder doWith(Consumer<NPairElementBuilder> con);

    NPairElement build();

    /// ///////////////////////////////////////////////
    NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NPairElementBuilder addAnnotation(NElementAnnotation annotation);

    NPairElementBuilder addAnnotation(String name, NElement... args);

    NPairElementBuilder addAffix(int index, NBoundAffix affix);

    NPairElementBuilder setAffix(int index, NBoundAffix affix);
    NPairElementBuilder addAffix(NBoundAffix affix);

    NPairElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NPairElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NPairElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NPairElementBuilder removeAffix(int index);

    NPairElementBuilder removeAnnotation(NElementAnnotation annotation);

    NPairElementBuilder clearAnnotations();

    NPairElementBuilder clearAffixes();

    NPairElementBuilder addLeadingComment(NElementComment comment);

    NPairElementBuilder addLeadingComments(NElementComment... comments);

    NPairElementBuilder addTrailingComments(NElementComment... comments);

    NPairElementBuilder addTrailingComment(NElementComment comment);

    NPairElementBuilder clearComments();

    NPairElementBuilder copyFrom(NElementBuilder other);

    NPairElementBuilder copyFrom(NElement other);

    NPairElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NPairElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NPairElementBuilder addDiagnostic(NElementDiagnostic error);

    NPairElementBuilder removeDiagnostic(NElementDiagnostic error);

    NPairElementBuilder addAffixes(List<NBoundAffix> affixes);
}
