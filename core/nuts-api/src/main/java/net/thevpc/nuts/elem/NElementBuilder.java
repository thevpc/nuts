/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public interface NElementBuilder {
    NElementType type();

    boolean isCustomTree();

    List<NBoundAffix> affixes();

    List<NElementDiagnostic> diagnostics();

    NElement build();

    List<NElementComment> leadingComments();

    List<NElementComment> trailingComments();

    List<NElementComment> comments();

    List<NElementAnnotation> annotations();

    NElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NElementBuilder addAnnotation(NElementAnnotation annotation);

    NElementBuilder addAnnotation(String name, NElement... args);

    NElementBuilder addAffix(NBoundAffix affix);

    NElementBuilder addAffix(int index, NBoundAffix affix);

    NElementBuilder setAffix(int index, NBoundAffix affix);

    NElementBuilder addAffix(NAffix affix, NAffixAnchor anchor);

    NElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NElementBuilder removeAffix(int index);

    NElementBuilder removeAnnotation(NElementAnnotation annotation);

    NElementBuilder clearAnnotations();

    NElementBuilder clearAffixes();

    NElementBuilder addLeadingComment(NElementComment comment);

    NElementBuilder addLeadingComments(NElementComment... comments);

    NElementBuilder addTrailingComments(NElementComment... comments);

    NElementBuilder addTrailingComment(NElementComment comment);

    NElementBuilder clearComments();

    NElementBuilder addAffixes(List<NBoundAffix> affixes);
    NElementMetadata metadata();

    /// /////////////

    NElementBuilder addAffixSpace(String space, NAffixAnchor anchor);

    NElementBuilder addAffixNewLine(NNewLineMode newLineMode, NAffixAnchor anchor);

    NElementBuilder addAffixSeparator(String separator, NAffixAnchor anchor);

    NElementBuilder addAffixSpace(int index, String space, NAffixAnchor anchor);

    NElementBuilder addAffixNewLine(int index, NNewLineMode newLineMode, NAffixAnchor anchor);

    NElementBuilder addAffixSeparator(int index, String separator, NAffixAnchor anchor);

    NElementBuilder removeAffixIf(Predicate<NBoundAffix> affixPredicate);

    NElementBuilder copyFrom(NElementBuilder other);

    NElementBuilder copyFrom(NElement other);

    NElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NElementBuilder addDiagnostic(NElementDiagnostic error);

    NElementBuilder removeDiagnostic(NElementDiagnostic error);
    NElementBuilder metadata(NElementMetadata metadata);
}
