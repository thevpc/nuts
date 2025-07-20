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
import net.thevpc.nuts.util.NMapStrategy;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NCharStreamElementBuilder extends NElementBuilder {
    NCharStreamElementBuilder doWith(Consumer<NCharStreamElementBuilder> con);
    NCharStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NCharStreamElementBuilder addAnnotation(String name,NElement ...args);
    NCharStreamElementBuilder addAnnotation(NElementAnnotation annotation);

    NCharStreamElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NCharStreamElementBuilder removeAnnotationAt(int index);
    NCharStreamElementBuilder removeAnnotation(NElementAnnotation annotation);

    NCharStreamElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NCharStreamElementBuilder copyFrom(NCharStreamElement element);

    NCharStreamElementBuilder copyFrom(NCharStreamElementBuilder element);

    NCharStreamElementBuilder addLeadingComment(NElementCommentType type, String text);

    NCharStreamElementBuilder addTrailingComment(NElementCommentType type, String text);

    NCharStreamElementBuilder addLeadingComment(NElementComment comment);

    NCharStreamElementBuilder addLeadingComments(NElementComment... comments);

    NCharStreamElementBuilder addTrailingComment(NElementComment comment);

    NCharStreamElementBuilder addTrailingComments(NElementComment... comments);

    NCharStreamElementBuilder removeLeadingComment(NElementComment comment);

    NCharStreamElementBuilder removeTrailingComment(NElementComment comment);

    NCharStreamElementBuilder removeLeadingCommentAt(int index);

    NCharStreamElementBuilder removeTrailingCommentAt(int index);

    NCharStreamElementBuilder clearComments();

    NCharStreamElementBuilder addComments(NElementComments comments);

    NCharStreamElementBuilder value(NReaderProvider value);

    NReaderProvider value();

    NCharStreamElement build();


    NCharStreamElementBuilder copyFrom(NElementBuilder other);

    NCharStreamElementBuilder copyFrom(NElement other);
    NCharStreamElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy);

    NCharStreamElementBuilder copyFrom(NElement other, NMapStrategy strategy);
}
