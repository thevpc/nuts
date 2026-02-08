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

import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NBinaryStreamElementBuilder extends NElementBuilder {

    NBinaryStreamElementBuilder value(NInputStreamProvider value);

    NInputStreamProvider value();

    String blocIdentifier();

    NBinaryStreamElementBuilder blocIdentifier(String blocIdentifier);

    NBinaryStreamElementBuilder doWith(Consumer<NBinaryStreamElementBuilder> con);

    NBinaryStreamElement build();

    /// ///////////////////////////////////////////////
    NBinaryStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NBinaryStreamElementBuilder addAnnotation(NElementAnnotation annotation);

    NBinaryStreamElementBuilder addAnnotation(String name, NElement... args);

    NBinaryStreamElementBuilder addAffix(int index, NBoundAffix affix);

    NBinaryStreamElementBuilder setAffix(int index, NBoundAffix affix);
    NBinaryStreamElementBuilder addAffix(NBoundAffix affix);

    NBinaryStreamElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NBinaryStreamElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NBinaryStreamElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NBinaryStreamElementBuilder removeAffix(int index);

    NBinaryStreamElementBuilder removeAnnotation(NElementAnnotation annotation);

    NBinaryStreamElementBuilder clearAnnotations();

    NBinaryStreamElementBuilder clearAffixes();

    NBinaryStreamElementBuilder addLeadingComment(NElementComment comment);

    NBinaryStreamElementBuilder addLeadingComments(NElementComment... comments);

    NBinaryStreamElementBuilder addTrailingComments(NElementComment... comments);

    NBinaryStreamElementBuilder addTrailingComment(NElementComment comment);

    NBinaryStreamElementBuilder clearComments();

    NBinaryStreamElementBuilder copyFrom(NElementBuilder other);

    NBinaryStreamElementBuilder copyFrom(NElement other);

    NBinaryStreamElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NBinaryStreamElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NBinaryStreamElementBuilder addDiagnostic(NElementDiagnostic error);

    NBinaryStreamElementBuilder removeDiagnostic(NElementDiagnostic error);

    NBinaryStreamElementBuilder addAffixes(List<NBoundAffix> affixes);

    NBinaryStreamElementBuilder metadata(NElementMetadata metadata);
}
