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

import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 * @since 0.8.9
 */
public interface NFlatExprElementBuilder extends NElementBuilder {
    static NFlatExprElementBuilder of() {
        return NElement.ofFlatExprBuilder();
    }

    NOptional<NElement> get(int index);

    NFlatExprElementBuilder set(int index, NOperatorSymbol op);

    NFlatExprElementBuilder set(int index, NElement element);

    NFlatExprElementBuilder add(NOperatorSymbol op);

    NFlatExprElementBuilder add(NElement element);

    NFlatExprElementBuilder setAt(int index, NElement element);

    NFlatExprElementBuilder setAt(int index, NOperatorSymbol element);

    List<NElement> children();

    NFlatExprElementBuilder doWith(Consumer<NFlatExprElementBuilder> con);

    int size();

    NFlatExprElement build();

    /// ///////////////////////////////////////////////
    NFlatExprElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NFlatExprElementBuilder addAnnotation(NElementAnnotation annotation);

    NFlatExprElementBuilder addAnnotation(String name, NElement... args);

    NFlatExprElementBuilder addAffix(int index, NBoundAffix affix);

    NFlatExprElementBuilder setAffix(int index, NBoundAffix affix);

    NFlatExprElementBuilder addAffix(NBoundAffix affix);

    NFlatExprElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NFlatExprElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NFlatExprElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NFlatExprElementBuilder removeAffix(int index);

    NFlatExprElementBuilder removeAnnotation(NElementAnnotation annotation);

    NFlatExprElementBuilder clearAnnotations();

    NFlatExprElementBuilder clearAffixes();

    NFlatExprElementBuilder addLeadingComment(NElementComment comment);

    NFlatExprElementBuilder addLeadingComments(NElementComment... comments);

    NFlatExprElementBuilder addTrailingComments(NElementComment... comments);

    NFlatExprElementBuilder addTrailingComment(NElementComment comment);

    NFlatExprElementBuilder clearComments();

    NFlatExprElementBuilder copyFrom(NElementBuilder other);

    NFlatExprElementBuilder copyFrom(NElement other);

    NFlatExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NFlatExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NFlatExprElementBuilder addDiagnostic(NElementDiagnostic error);

    NFlatExprElementBuilder removeDiagnostic(NElementDiagnostic error);

    NFlatExprElementBuilder addAffixes(List<NBoundAffix> affixes);

}
