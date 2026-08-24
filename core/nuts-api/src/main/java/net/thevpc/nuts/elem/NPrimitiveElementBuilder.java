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

import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NPrimitiveElementBuilder extends NElementBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NPrimitiveElementBuilder of() {
        return NElement.ofPrimitiveBuilder();
    }

    /**
     * Number layout.
     *
     * @return number layout result
     */
    NNumberLayout numberLayout();

    /**
     * Number layout.
     *
     * @param numberLayout number layout
     * @return number layout result
     */
    NPrimitiveElementBuilder numberLayout(NNumberLayout numberLayout);

    /**
     * Number suffix.
     *
     * @return number suffix result
     */
    String numberSuffix();

    /**
     * Image.
     *
     * @return image result
     */
    String image();

    /**
     * Number suffix.
     *
     * @param numberSuffix number suffix
     * @return number suffix result
     */
    NPrimitiveElementBuilder numberSuffix(String numberSuffix);


    /**
     * Value.
     *
     * @return value result
     */
    Object value();

    /**
     * Copy from.
     *
     * @param element element
     * @return copy from result
     */
    NPrimitiveElementBuilder copyFrom(NPrimitiveElement element);

    /**
     * Sets the value.
     *
     * @param value value
     * @return set value result
     */
    NPrimitiveElementBuilder setValue(Object value);

    /**
     * Value.
     *
     * @param value value
     * @return value result
     */
    NPrimitiveElementBuilder value(Object value);

    /**
     * Sets the block string.
     *
     * @param value value
     * @return set block string result
     */
    NPrimitiveElementBuilder setBlockString(NElementLine... value);

    /**
     * Sets the line string.
     *
     * @param value value
     * @return set line string result
     */
    NPrimitiveElementBuilder setLineString(NElementLine value);

    /**
     * Sets the instant.
     *
     * @param value value
     * @return set instant result
     */
    NPrimitiveElementBuilder setInstant(Instant value);

    /**
     * Sets the local date.
     *
     * @param value value
     * @return set local date result
     */
    NPrimitiveElementBuilder setLocalDate(LocalDate value);

    /**
     * Sets the local date time.
     *
     * @param value value
     * @return set local date time result
     */
    NPrimitiveElementBuilder setLocalDateTime(LocalDateTime value);

    /**
     * Sets the local time.
     *
     * @param value value
     * @return set local time result
     */
    NPrimitiveElementBuilder setLocalTime(LocalTime value);

    /**
     * Sets the string.
     *
     * @param value value
     * @return set string result
     */
    NPrimitiveElementBuilder setString(String value);

    /**
     * Sets the string.
     *
     * @param value value
     * @param stringLayout string layout
     * @return set string result
     */
    NPrimitiveElementBuilder setString(String value, NElementType stringLayout);

    /**
     * Sets the boolean.
     *
     * @param value value
     * @return set boolean result
     */
    NPrimitiveElementBuilder setBoolean(Boolean value);

    /**
     * Sets the boolean.
     *
     * @param value value
     * @return set boolean result
     */
    NPrimitiveElementBuilder setBoolean(boolean value);

    /**
     * Sets the single quoted string.
     *
     * @param value value
     * @return set single quoted string result
     */
    NPrimitiveElementBuilder setSingleQuotedString(String value);

    /**
     * Sets the double quoted string.
     *
     * @param value value
     * @return set double quoted string result
     */
    NPrimitiveElementBuilder setDoubleQuotedString(String value);

    /**
     * Sets the anti quoted string.
     *
     * @param value value
     * @return set anti quoted string result
     */
    NPrimitiveElementBuilder setAntiQuotedString(String value);

    /**
     * Sets the triple single quoted string.
     *
     * @param value value
     * @return set triple single quoted string result
     */
    NPrimitiveElementBuilder setTripleSingleQuotedString(String value);

    /**
     * Sets the triple double quoted string.
     *
     * @param value value
     * @return set triple double quoted string result
     */
    NPrimitiveElementBuilder setTripleDoubleQuotedString(String value);

    /**
     * Sets the triple anti quoted string.
     *
     * @param value value
     * @return set triple anti quoted string result
     */
    NPrimitiveElementBuilder setTripleAntiQuotedString(String value);

    /**
     * Sets the line string.
     *
     * @param value value
     * @return set line string result
     */
    NPrimitiveElementBuilder setLineString(String value);

    /**
     * Sets the block string.
     *
     * @param value value
     * @return set block string result
     */
    NPrimitiveElementBuilder setBlockString(String value);

    /**
     * Sets the int.
     *
     * @param value value
     * @return set int result
     */
    NPrimitiveElementBuilder setInt(Integer value);

    /**
     * Sets the long.
     *
     * @param value value
     * @return set long result
     */
    NPrimitiveElementBuilder setLong(Long value);

    /**
     * Sets the null.
     *
     * @return set null result
     */
    NPrimitiveElementBuilder setNull();

    /**
     * Sets the byte.
     *
     * @param value value
     * @return set byte result
     */
    NPrimitiveElementBuilder setByte(Byte value);

    /**
     * Sets the short.
     *
     * @param value value
     * @return set short result
     */
    NPrimitiveElementBuilder setShort(Short value);

    /**
     * Sets the char.
     *
     * @param value value
     * @return set char result
     */
    NPrimitiveElementBuilder setChar(char value);

    /**
     * Sets the char.
     *
     * @param value value
     * @return set char result
     */
    NPrimitiveElementBuilder setChar(Character value);

    /**
     * Sets the short.
     *
     * @param value value
     * @return set short result
     */
    NPrimitiveElementBuilder setShort(short value);

    /**
     * Sets the double.
     *
     * @param value value
     * @return set double result
     */
    NPrimitiveElementBuilder setDouble(double value);

    /**
     * Sets the float.
     *
     * @param value value
     * @return set float result
     */
    NPrimitiveElementBuilder setFloat(Float value);

    /**
     * Sets the double.
     *
     * @param value value
     * @return set double result
     */
    NPrimitiveElementBuilder setDouble(Double value);

    /**
     * Sets the big int.
     *
     * @param value value
     * @return set big int result
     */
    NPrimitiveElementBuilder setBigInt(BigInteger value);

    /**
     * Sets the big decimal.
     *
     * @param value value
     * @return set big decimal result
     */
    NPrimitiveElementBuilder setBigDecimal(BigDecimal value);

    /**
     * Sets the int.
     *
     * @param value value
     * @return set int result
     */
    NPrimitiveElementBuilder setInt(int value);

    /**
     * Sets the byte.
     *
     * @param value value
     * @return set byte result
     */
    NPrimitiveElementBuilder setByte(byte value);

    /**
     * Sets the double complex.
     *
     * @param value value
     * @return set double complex result
     */
    NPrimitiveElementBuilder setDoubleComplex(NDoubleComplex value);

    /**
     * Sets the float complex.
     *
     * @param value value
     * @return set float complex result
     */
    NPrimitiveElementBuilder setFloatComplex(NFloatComplex value);

    /**
     * Sets the big complex.
     *
     * @param value value
     * @return set big complex result
     */
    NPrimitiveElementBuilder setBigComplex(NBigComplex value);

    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NPrimitiveElementBuilder doWith(Consumer<NPrimitiveElementBuilder> con);

    /**
     * Build.
     *
     * @return build result
     */
    NPrimitiveElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NPrimitiveElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NPrimitiveElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NPrimitiveElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NPrimitiveElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NPrimitiveElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NPrimitiveElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NPrimitiveElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NPrimitiveElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NPrimitiveElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NPrimitiveElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NPrimitiveElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NPrimitiveElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NPrimitiveElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NPrimitiveElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NPrimitiveElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NPrimitiveElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NPrimitiveElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NPrimitiveElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NPrimitiveElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NPrimitiveElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NPrimitiveElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NPrimitiveElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NPrimitiveElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NPrimitiveElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NPrimitiveElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NPrimitiveElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NPrimitiveElementBuilder metadata(NElementMetadata metadata);
}
