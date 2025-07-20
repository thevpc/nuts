/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NMapStrategy;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Consumer;

/**
 * Array element Builder is a mutable NutsArrayElement that helps
 * manipulating arrays.
 *
 * @author thevpc
 * @app.category Elements
 */
public interface NMatrixElementBuilder extends NElementBuilder {
    NMatrixElementBuilder doWith(Consumer<NMatrixElementBuilder> con);

    static NMatrixElementBuilder of() {
        return NElement.ofMatrixBuilder();
    }


    NMatrixElementBuilder copyFrom(NMatrixElement element);

    NMatrixElementBuilder copyFrom(NMatrixElementBuilder element);

    List<NArrayElement> rows();

    List<NArrayElement> columns();

    /**
     * element count
     *
     * @return element count
     */
    int rowsCount();

    int columnsCount();


    /**
     * element at index
     *
     * @param column row
     * @return element at index
     */
    NElement get(int column, int row);

    List<NElement> getRow(int row);

    List<NElement> getColumn(int column);


    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NArrayElement build();


    NMatrixElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NMatrixElementBuilder addAnnotation(String name, NElement... args);

    NMatrixElementBuilder addAnnotation(NElementAnnotation annotation);

    NMatrixElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NMatrixElementBuilder removeAnnotationAt(int index);
    NMatrixElementBuilder removeAnnotation(NElementAnnotation annotation);

    NMatrixElementBuilder clearAnnotations();

    NMatrixElementBuilder addParams(List<NElement> params);

    NMatrixElementBuilder addParam(NElement param);

    NMatrixElementBuilder addParamAt(int index, NElement param);

    NMatrixElementBuilder removeParamAt(int index);

    NMatrixElementBuilder clearParams();

    NOptional<List<NElement>> params();

    NOptional<String> name();

    NMatrixElementBuilder name(String name);

    boolean isParametrized();

    NMatrixElementBuilder setParametrized(boolean parametrized);

    NMatrixElementBuilder addLeadingComment(NElementCommentType type, String text);

    NMatrixElementBuilder addTrailingComment(NElementCommentType type, String text);

    NMatrixElementBuilder addLeadingComment(NElementComment comment);

    NMatrixElementBuilder addLeadingComments(NElementComment... comments);

    NMatrixElementBuilder addTrailingComment(NElementComment comment);

    NMatrixElementBuilder addTrailingComments(NElementComment... comments);

    NMatrixElementBuilder removeLeadingComment(NElementComment comment);

    NMatrixElementBuilder removeTrailingComment(NElementComment comment);

    NMatrixElementBuilder removeLeadingCommentAt(int index);

    NMatrixElementBuilder removeTrailingCommentAt(int index);

    NMatrixElementBuilder clearComments();

    NMatrixElementBuilder addComments(NElementComments comments);

    NMatrixElementBuilder copyFrom(NElementBuilder other);

    NMatrixElementBuilder copyFrom(NElement other);

    NMatrixElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy);

    NMatrixElementBuilder copyFrom(NElement other, NMapStrategy strategy);
}
