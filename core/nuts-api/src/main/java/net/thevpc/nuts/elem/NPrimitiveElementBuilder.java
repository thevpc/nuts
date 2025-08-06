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
import net.thevpc.nuts.util.NMapStrategy;

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
    NPrimitiveElementBuilder doWith(Consumer<NPrimitiveElementBuilder> con);

    NNumberLayout numberLayout();

    NPrimitiveElementBuilder numberLayout(NNumberLayout numberLayout);

    String numberSuffix();

    NPrimitiveElementBuilder numberSuffix(String numberSuffix);

    NPrimitiveElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NPrimitiveElementBuilder addAnnotation(String name, NElement... args);

    NPrimitiveElementBuilder addAnnotation(NElementAnnotation annotation);

    NPrimitiveElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NPrimitiveElementBuilder removeAnnotationAt(int index);

    NPrimitiveElementBuilder removeAnnotation(NElementAnnotation annotation);

    NPrimitiveElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NPrimitiveElement build();

    Object value();

    NPrimitiveElementBuilder copyFrom(NPrimitiveElement element);

    NPrimitiveElementBuilder setValue(Object value);

    NPrimitiveElementBuilder value(Object value);

    NPrimitiveElementBuilder setInstant(Instant value);

    NPrimitiveElementBuilder setLocalDate(LocalDate value);

    NPrimitiveElementBuilder setLocalDateTime(LocalDateTime value);

    NPrimitiveElementBuilder setLocalTime(LocalTime value);

    NPrimitiveElementBuilder setString(String value);

    NPrimitiveElementBuilder setString(String value, NElementType stringLayout);

    NPrimitiveElementBuilder setBoolean(Boolean value);

    NPrimitiveElementBuilder setBoolean(boolean value);

    NPrimitiveElementBuilder setSingleQuotedString(String value);

    NPrimitiveElementBuilder setDoubleQuotedString(String value);

    NPrimitiveElementBuilder setAntiQuotedString(String value);

    NPrimitiveElementBuilder setTripleSingleQuotedString(String value);

    NPrimitiveElementBuilder setTripleDoubleQuotedString(String value);

    NPrimitiveElementBuilder setTripleAntiQuotedString(String value);

    NPrimitiveElementBuilder setLineString(String value);

    NPrimitiveElementBuilder setInt(Integer value);

    NPrimitiveElementBuilder setLong(Long value);

    NPrimitiveElementBuilder setNull();

    NPrimitiveElementBuilder setByte(Byte value);

    NPrimitiveElementBuilder setShort(Short value);

    NPrimitiveElementBuilder setChar(char value);

    NPrimitiveElementBuilder setChar(Character value);

    NPrimitiveElementBuilder setShort(short value);

    NPrimitiveElementBuilder setDouble(double value);

    NPrimitiveElementBuilder setFloat(Float value);

    NPrimitiveElementBuilder setDouble(Double value);

    NPrimitiveElementBuilder setBigInt(BigInteger value);

    NPrimitiveElementBuilder setBigDecimal(BigDecimal value);

    NPrimitiveElementBuilder setInt(int value);

    NPrimitiveElementBuilder setByte(byte value);

    NPrimitiveElementBuilder setDoubleComplex(NDoubleComplex value);

    NPrimitiveElementBuilder setFloatComplex(NFloatComplex value);

    NPrimitiveElementBuilder setBigComplex(NBigComplex value);

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

    NPrimitiveElementBuilder copyFrom(NElementBuilder other);

    NPrimitiveElementBuilder copyFrom(NElement other);

    NPrimitiveElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy);

    NPrimitiveElementBuilder copyFrom(NElement other, NMapStrategy strategy);
}
