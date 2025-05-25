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

import net.thevpc.nuts.util.NMapStrategy;

import java.util.List;

/**
 * @author thevpc
 */
public interface NElementBuilder {
    NElementType type();

    boolean isCustomTree();

    NElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NElementBuilder addAnnotation(NElementAnnotation annotation);

    NElementBuilder addAnnotation(String name, NElement... args);

    NElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NElementBuilder removeAnnotationAt(int index);

    NElementBuilder clearAnnotations();

    NElementBuilder addLeadingComment(NElementCommentType type, String text);

    NElementBuilder addTrailingComment(NElementCommentType type, String text);

    NElementBuilder addLeadingComment(NElementComment comment);

    NElementBuilder addLeadingComments(NElementComment... comments);

    NElementBuilder addTrailingComment(NElementComment comment);

    List<NElementComment> leadingComments();

    List<NElementComment> trailingComments();

    NElementBuilder addTrailingComments(NElementComment... comments);

    NElementBuilder removeLeadingComment(NElementComment comment);

    NElementBuilder removeTrailingComment(NElementComment comment);

    NElementBuilder removeLeadingCommentAt(int index);

    NElementBuilder removeTrailingCommentAt(int index);

    NElementBuilder clearComments();

    NElementBuilder addComments(NElementComments comments);

    NElementComments comments();

    List<NElementAnnotation> annotations();

    NElementBuilder copyFrom(NElementBuilder other);

    NElementBuilder copyFrom(NElement other);

    NElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy);

    NElementBuilder copyFrom(NElement other, NMapStrategy strategy);

    NElement build();
}
