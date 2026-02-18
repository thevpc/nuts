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

import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NCharStreamElementBuilder extends NElementBuilder {

    String blocIdentifier();

    NCharStreamElementBuilder blocIdentifier(String blockIdentifier);

    NCharStreamElementBuilder value(NReaderProvider value);

    NReaderProvider value();

    NCharStreamElementBuilder doWith(Consumer<NCharStreamElementBuilder> con);

    NCharStreamElement build();


    /// ///////////////////////////////////////////////
    NCharStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NCharStreamElementBuilder addAnnotation(NElementAnnotation annotation);

    NCharStreamElementBuilder addAnnotation(String name, NElement... args);

    NCharStreamElementBuilder addAffix(int index, NBoundAffix affix);

    NCharStreamElementBuilder setAffix(int index, NBoundAffix affix);
    NCharStreamElementBuilder addAffix(NBoundAffix affix);

    NCharStreamElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NCharStreamElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NCharStreamElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NCharStreamElementBuilder removeAffix(int index);

    NCharStreamElementBuilder removeAnnotation(NElementAnnotation annotation);

    NCharStreamElementBuilder clearAnnotations();

    NCharStreamElementBuilder clearAffixes();

    NCharStreamElementBuilder addLeadingComment(NElementComment comment);

    NCharStreamElementBuilder addLeadingComments(NElementComment... comments);

    NCharStreamElementBuilder addTrailingComments(NElementComment... comments);

    NCharStreamElementBuilder addTrailingComment(NElementComment comment);

    NCharStreamElementBuilder clearComments();

    NCharStreamElementBuilder copyFrom(NElementBuilder other);

    NCharStreamElementBuilder copyFrom(NElement other);

    NCharStreamElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NCharStreamElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NCharStreamElementBuilder addDiagnostic(NElementDiagnostic error);

    NCharStreamElementBuilder removeDiagnostic(NElementDiagnostic error);

    NCharStreamElementBuilder addAffixes(List<NBoundAffix> affixes);

    NCharStreamElementBuilder metadata(NElementMetadata metadata);
}
