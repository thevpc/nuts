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

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NBinaryStreamElementBuilder extends NElementBuilder {
    NBinaryStreamElementBuilder doWith(Consumer<NBinaryStreamElementBuilder> con);
    NBinaryStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NBinaryStreamElementBuilder addAnnotation(NElementAnnotation annotation);

    NBinaryStreamElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NBinaryStreamElementBuilder removeAnnotationAt(int index);

    NBinaryStreamElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NBinaryStreamElementBuilder copyFrom(NBinaryStreamElement element) ;

    NBinaryStreamElementBuilder copyFrom(NBinaryStreamElementBuilder element) ;

    NBinaryStreamElementBuilder addLeadingComment(NElementCommentType type, String text);
    NBinaryStreamElementBuilder addTrailingComment(NElementCommentType type, String text);
    NBinaryStreamElementBuilder addLeadingComment(NElementComment comment);
    NBinaryStreamElementBuilder addLeadingComments(NElementComment... comments);
    NBinaryStreamElementBuilder addTrailingComment(NElementComment comment);
    NBinaryStreamElementBuilder addTrailingComments(NElementComment... comments);
    NBinaryStreamElementBuilder removeLeadingComment(NElementComment comment);
    NBinaryStreamElementBuilder removeTrailingComment(NElementComment comment);
    NBinaryStreamElementBuilder removeLeadingCommentAt(int index);
    NBinaryStreamElementBuilder removeTrailingCommentAt(int index);
    NBinaryStreamElementBuilder clearComments();
    NBinaryStreamElementBuilder addComments(NElementComments comments);

    NBinaryStreamElementBuilder value(NInputStreamProvider value);
    NInputStreamProvider value();
    NBinaryStreamElement build();
}
