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
public interface NOperatorElementBuilder extends NElementBuilder {

    List<NOperatorSymbol> operators();

    List<NElement> operands();

    List<NElement> children();

    NOptional<NElement> operand(int index);

    NOptional<NElement> first();

    NOperatorElementBuilder second(NElement value);

    NOptional<NElement> second();

    NOptional<NElement> third();

    NOperatorElementBuilder operators(NOperatorSymbol... operators);

    NOperatorElementBuilder operands(NElement... operands);

    NOperatorElementBuilder clearOperands();

    NOperatorElementBuilder clearOperators();

    NOperatorElementBuilder addOperands(NElement... operands);

    NOperatorElementBuilder add(NElement operands);

    NOperatorElementBuilder addAll(NElement... operandOrOperators);

    NOperatorElementBuilder setAll(NElement... operandOrOperators);

    NOperatorElementBuilder addOperand(NElement operand);

    NOperatorElementBuilder addOperator(NOperatorSymbol operator);

    NOperatorElementBuilder operator(NOperatorSymbol value);

    NOperatorElementBuilder position(NOperatorPosition operatorType);

    NOperatorPosition position();

    NOperatorSymbol operator();

    NOperatorElementBuilder setOperand(int index, NElement operand);

    NOperatorElementBuilder first(NElement value);

    NOperatorElementBuilder doWith(Consumer<NOperatorElementBuilder> con);

    NOperatorElement build();

    /// ///////////////////////////////////////////////
    NOperatorElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NOperatorElementBuilder addAnnotation(NElementAnnotation annotation);

    NOperatorElementBuilder addAnnotation(String name, NElement... args);

    NOperatorElementBuilder addAffix(int index, NBoundAffix affix);

    NOperatorElementBuilder setAffix(int index, NBoundAffix affix);

    NOperatorElementBuilder addAffix(NBoundAffix affix);

    NOperatorElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NOperatorElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NOperatorElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NOperatorElementBuilder removeAffix(int index);

    NOperatorElementBuilder removeAnnotation(NElementAnnotation annotation);

    NOperatorElementBuilder clearAnnotations();

    NOperatorElementBuilder clearAffixes();

    NOperatorElementBuilder addLeadingComment(NElementComment comment);

    NOperatorElementBuilder addLeadingComments(NElementComment... comments);

    NOperatorElementBuilder addTrailingComments(NElementComment... comments);

    NOperatorElementBuilder addTrailingComment(NElementComment comment);

    NOperatorElementBuilder clearComments();

    NOperatorElementBuilder copyFrom(NElementBuilder other);

    NOperatorElementBuilder copyFrom(NElement other);

    NOperatorElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NOperatorElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NOperatorElementBuilder addDiagnostic(NElementDiagnostic error);

    NOperatorElementBuilder removeDiagnostic(NElementDiagnostic error);

    NOperatorElementBuilder addAffixes(List<NBoundAffix> affixes);

    NOperatorElementBuilder metadata(NElementMetadata metadata);
}
