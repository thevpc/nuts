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

/**
 * @author thevpc
 */
public interface NPrimitiveElementBuilder extends NElementBuilder {
    public NNumberLayout numberLayout();

    public NPrimitiveElementBuilder numberLayout(NNumberLayout numberLayout);

    public String numberSuffix();

    public NPrimitiveElementBuilder numberSuffix(String numberSuffix);

    NPrimitiveElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NPrimitiveElementBuilder addAnnotation(NElementAnnotation annotation);

    NPrimitiveElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NPrimitiveElementBuilder removeAnnotationAt(int index);

    NPrimitiveElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NPrimitiveElement build();

    Object value();

    NPrimitiveElementBuilder copyFrom(NPrimitiveElement element);

    NPrimitiveElementBuilder copyFrom(NPrimitiveElementBuilder element);


    NPrimitiveElementBuilder value(Object value);

    NPrimitiveElementBuilder setInt(int value);

    NPrimitiveElementBuilder setByte(byte value);

    NPrimitiveElementBuilder setDoubleComplex(NDComplex value);

    NPrimitiveElementBuilder setFloatComplex(NFComplex value);

    NPrimitiveElementBuilder setBigComplex(NBComplex value);

    NPrimitiveElementBuilder addLeadingComment(NElementCommentType type, String text);

    NPrimitiveElementBuilder addTrailingComment(NElementCommentType type, String text);

    NPrimitiveElementBuilder addLeadingComment(NElementComment comment);

    NPrimitiveElementBuilder addLeadingComments(NElementComment... comments);

    NPrimitiveElementBuilder addTrailingComment(NElementComment comment);

    NPrimitiveElementBuilder addTrailingComments(NElementComment... comments);

    NPrimitiveElementBuilder removeLeadingComment(NElementComment comment);

    NPrimitiveElementBuilder removeTrailingComment(NElementComment comment);

    NPrimitiveElementBuilder removeLeadingCommentAt(int index);

    NPrimitiveElementBuilder removeTrailingCommentAt(int index);

    NPrimitiveElementBuilder clearComments();

    NPrimitiveElementBuilder addComments(NElementComments comments);

}
