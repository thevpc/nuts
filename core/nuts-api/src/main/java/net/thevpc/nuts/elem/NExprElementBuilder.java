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
 */
public interface NExprElementBuilder extends NElementBuilder {

    List<NOperatorSymbol> symbols();

    List<NElement> operands();

    NOptional<NElement> operand(int index);

    NOptional<NElement> first();

    NExprElementBuilder second(NElement value);

    NOptional<NElement> second();

    NOptional<NElement> third();

    NExprElementBuilder symbols(NOperatorSymbol... operators);

    NExprElementBuilder operands(NElement... operands);

    NExprElementBuilder addOperands(NElement... operands);

    NExprElementBuilder addOperand(NElement operand);

    NExprElementBuilder addSymbol(NOperatorSymbol operator);

    NExprElementBuilder symbol(NOperatorSymbol value);

    NExprElementBuilder position(NOperatorPosition operatorType);

    NOperatorPosition position();

    NOperatorSymbol symbol();

    NExprElement build();

    /// /////////////

    NExprElementBuilder doWith(Consumer<NExprElementBuilder> con);

    NExprElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NExprElementBuilder addAnnotation(String name, NElement... args);

    NExprElementBuilder addAnnotation(NElementAnnotation annotation);

    NExprElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NExprElementBuilder removeAnnotationAt(int index);

    NExprElementBuilder removeAnnotation(NElementAnnotation annotation);

    NExprElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NExprElementBuilder copyFrom(NExprElementBuilder element);

    NExprElementBuilder addLeadingComment(NElementCommentType type, String text);

    NExprElementBuilder addTrailingComment(NElementCommentType type, String text);

    NExprElementBuilder addLeadingComment(NElementComment comment);

    NExprElementBuilder addLeadingComments(NElementComment... comments);

    NExprElementBuilder addTrailingComment(NElementComment comment);

    NExprElementBuilder addTrailingComments(NElementComment... comments);

    NExprElementBuilder removeLeadingComment(NElementComment comment);

    NExprElementBuilder removeTrailingComment(NElementComment comment);

    NExprElementBuilder removeLeadingCommentAt(int index);

    NExprElementBuilder removeTrailingCommentAt(int index);

    NExprElementBuilder clearComments();

    NExprElementBuilder addComments(NElementComments comments);

    NExprElementBuilder setOperand(int index, NElement operand);

    NExprElementBuilder first(NElement value);


    NExprElementBuilder copyFrom(NElementBuilder other);

    NExprElementBuilder copyFrom(NElement other);

    NExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);
}
