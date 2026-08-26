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

import net.thevpc.nuts.expr.NFixity;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NOperatorElementBuilder extends NElementBuilder {

    /**
     * Operators.
     *
     * @return operators result
     */
    List<NOperatorSymbol> operators();

    /**
     * Operands.
     *
     * @return operands result
     */
    List<NElement> operands();

    /**
     * Children.
     *
     * @return children result
     */
    List<NElement> children();

    /**
     * Operand.
     *
     * @param index index
     * @return operand result
     */
    NOptional<NElement> operand(int index);

    /**
     * First.
     *
     * @return first result
     */
    NOptional<NElement> first();

    /**
     * Second.
     *
     * @param value value
     * @return second result
     */
    NOperatorElementBuilder second(NElement value);

    /**
     * Second.
     *
     * @return second result
     */
    NOptional<NElement> second();

    /**
     * Third.
     *
     * @return third result
     */
    NOptional<NElement> third();

    /**
     * Operators.
     *
     * @param operators operators
     * @return operators result
     */
    NOperatorElementBuilder operators(NOperatorSymbol... operators);

    /**
     * Operands.
     *
     * @param operands operands
     * @return operands result
     */
    NOperatorElementBuilder operands(NElement... operands);

    /**
     * Clear operands.
     *
     * @return clear operands result
     */
    NOperatorElementBuilder clearOperands();

    /**
     * Clear operators.
     *
     * @return clear operators result
     */
    NOperatorElementBuilder clearOperators();

    /**
     * Adds the specified operands.
     *
     * @param operands operands
     * @return add operands result
     */
    NOperatorElementBuilder addOperands(NElement... operands);

    /**
     * Adds add.
     *
     * @param operands operands
     * @return add result
     */
    NOperatorElementBuilder add(NElement operands);

    /**
     * Adds the specified all.
     *
     * @param operandOrOperators operand or operators
     * @return add all result
     */
    NOperatorElementBuilder addAll(NElement... operandOrOperators);

    /**
     * Sets the children.
     *
     * @param operandOrOperators operand or operators
     * @return set children result
     */
    NOperatorElementBuilder setChildren(NElement... operandOrOperators);

    /**
     * Sets the children.
     *
     * @param operandOrOperators operand or operators
     * @return set children result
     */
    NOperatorElementBuilder setChildren(List<NElement> operandOrOperators);

    /**
     * Adds the specified operand.
     *
     * @param operand operand
     * @return add operand result
     */
    NOperatorElementBuilder addOperand(NElement operand);

    /**
     * Adds the specified operator.
     *
     * @param operator operator
     * @return add operator result
     */
    NOperatorElementBuilder addOperator(NOperatorSymbol operator);

    /**
     * Operator.
     *
     * @param value value
     * @return operator result
     */
    NOperatorElementBuilder operator(NOperatorSymbol value);

    /**
     * Fixity.
     *
     * @param operatorType operator type
     * @return fixity result
     */
    NOperatorElementBuilder fixity(NFixity operatorType);

    /**
     * Fixity.
     *
     * @return fixity result
     */
    NFixity fixity();

    /**
     * Operator.
     *
     * @return operator result
     */
    NOperatorSymbol operator();

    /**
     * Sets the operand.
     *
     * @param index index
     * @param operand operand
     * @return set operand result
     */
    NOperatorElementBuilder setOperand(int index, NElement operand);

    /**
     * First.
     *
     * @param value value
     * @return first result
     */
    NOperatorElementBuilder first(NElement value);

    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NOperatorElementBuilder doWith(Consumer<NOperatorElementBuilder> con);

    /**
     * Build.
     *
     * @return build result
     */
    NOperatorElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NOperatorElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NOperatorElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NOperatorElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NOperatorElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NOperatorElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NOperatorElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NOperatorElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NOperatorElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NOperatorElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NOperatorElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NOperatorElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NOperatorElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NOperatorElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NOperatorElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NOperatorElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NOperatorElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NOperatorElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NOperatorElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NOperatorElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NOperatorElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NOperatorElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NOperatorElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NOperatorElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NOperatorElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NOperatorElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NOperatorElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NOperatorElementBuilder metadata(NElementMetadata metadata);
}
