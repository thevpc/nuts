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

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NOperatorElementBuilder extends NElementBuilder {
    NOperatorElementBuilder doWith(Consumer<NOperatorElementBuilder> con);

    NOperatorElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NOperatorElementBuilder addAnnotation(String name, NElement... args);

    NOperatorElementBuilder addAnnotation(NElementAnnotation annotation);

    NOperatorElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NOperatorElementBuilder removeAnnotationAt(int index);

    NOperatorElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NOperatorElementBuilder copyFrom(NCustomElement element);

    NOperatorElementBuilder copyFrom(NOperatorElementBuilder element);


    NOperatorElementBuilder addLeadingComment(NElementCommentType type, String text);

    NOperatorElementBuilder addTrailingComment(NElementCommentType type, String text);

    NOperatorElementBuilder addLeadingComment(NElementComment comment);

    NOperatorElementBuilder addLeadingComments(NElementComment... comments);

    NOperatorElementBuilder addTrailingComment(NElementComment comment);

    NOperatorElementBuilder addTrailingComments(NElementComment... comments);

    NOperatorElementBuilder removeLeadingComment(NElementComment comment);

    NOperatorElementBuilder removeTrailingComment(NElementComment comment);

    NOperatorElementBuilder removeLeadingCommentAt(int index);

    NOperatorElementBuilder removeTrailingCommentAt(int index);

    NOperatorElementBuilder clearComments();

    NOperatorElementBuilder addComments(NElementComments comments);

    NOperatorElementBuilder first(NElement value);

    NElement first();

    NOperatorElementBuilder second(NElement value);

    NElement second();

    NOperatorElementBuilder operatorName(String value);

    String operatorName();

    NOperatorElementBuilder operatorType(NOperatorType value);

    NOperatorType operatorType();

    NOperatorElement build();
}
