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

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NCustomElementBuilder extends NElementBuilder {
    NCustomElementBuilder value(Object value);

    Object value();

    NCustomElementBuilder doWith(Consumer<NCustomElementBuilder> con);

    NCustomElement build();

    /// ///////////////////////////////////////////////
    NCustomElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NCustomElementBuilder addAnnotation(NElementAnnotation annotation);

    NCustomElementBuilder addAnnotation(String name, NElement... args);

    NCustomElementBuilder addAffix(int index, NBoundAffix affix);

    NCustomElementBuilder setAffix(int index, NBoundAffix affix);

    NCustomElementBuilder addAffix(NBoundAffix affix);

    NCustomElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NCustomElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NCustomElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NCustomElementBuilder removeAffix(int index);

    NCustomElementBuilder removeAnnotation(NElementAnnotation annotation);

    NCustomElementBuilder clearAnnotations();

    NCustomElementBuilder clearAffixes();

    NCustomElementBuilder addLeadingComment(NElementComment comment);

    NCustomElementBuilder addLeadingComments(NElementComment... comments);

    NCustomElementBuilder addTrailingComments(NElementComment... comments);

    NCustomElementBuilder addTrailingComment(NElementComment comment);

    NCustomElementBuilder clearComments();

    NCustomElementBuilder copyFrom(NElementBuilder other);

    NCustomElementBuilder copyFrom(NElement other);

    NCustomElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NCustomElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NCustomElementBuilder addDiagnostic(NElementDiagnostic error);

    NCustomElementBuilder removeDiagnostic(NElementDiagnostic error);

    NCustomElementBuilder addAffixes(List<NBoundAffix> affixes);

}
