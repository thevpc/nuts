/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
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

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NContentTypeFormat;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.time.NProgressFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.function.Predicate;

/**
 * Class responsible of manipulating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElements extends NContentTypeFormat {

    NElement normalize(NElement e);


    static boolean isValidElementNameChar(char c, boolean start) {
        if (start) {
            if (!Character.isJavaIdentifierStart(c)
                    && c != '.'
                    && c != ':'
                    && c != '@'
            ) {
                return false;
            }
        } else {
            if (!Character.isJavaIdentifierPart(c)
                    && c != '.'
                    && c != '-'
                    && c != ':'
                    && c != '@'
            ) {
                return false;
            }
        }
        return true;
    }

    static boolean isValidElementNameChar(char c, boolean start, NContentType contentType) {
        if (contentType == null) {
            return isValidElementNameChar(c, start);
        }
        switch (contentType) {
            case XML: {
                if (start) {
                    if (!Character.isJavaIdentifierStart(c)
                            && c != '.'
                            && c != ':'
                    ) {
                        return false;
                    }
                } else {
                    if (!Character.isJavaIdentifierPart(c)
                            && c != '.'
                            && c != '-'
                            && c != ':'
                    ) {
                        return false;
                    }
                }
            }
            case TSON: {
                if (start) {
                    if (!Character.isJavaIdentifierStart(c)
                            && c != '.'
                            && c != '@'
                    ) {
                        return false;
                    }
                } else {
                    if (!Character.isJavaIdentifierPart(c)
                            && c != '.'
                            && c != '-'
                            && c != '@'
                    ) {
                        return false;
                    }
                }
            }
            default: {
                return true;
            }
        }
    }

    static boolean isValidElementName(String name) {
        if (name == null) {
            return false;
        }
        if (name.isEmpty()) {
            return false;
        }
        char[] charArray = name.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)
                        && c != '.'
                        && c != '@'
                        && c != ':'
                ) {
                    return false;
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)
                        && c != '.'
                        && c != '-'
                        && c != '@'
                        && c != ':'
                ) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean isValidElementName(String name, NContentType contentType) {
        if (contentType == null) {
            return isValidElementName(name);
        }
        if (name == null) {
            return false;
        }
        if (name.isEmpty()) {
            return false;
        }
        char[] charArray = name.toCharArray();
        switch (contentType) {
            case XML: {
                //wont call isValidElementNameChar for performance
                for (int i = 0; i < charArray.length; i++) {
                    char c = charArray[i];
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)
                                && c != '.'
                                && c != ':'
                        ) {
                            return false;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)
                                && c != '.'
                                && c != '-'
                                && c != ':'
                        ) {
                            return false;
                        }
                    }
                }
                break;
            }
            case JSON: {
                //wont call isValidElementNameChar for performance
                for (int i = 0; i < charArray.length; i++) {
                    char c = charArray[i];
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)
                                && c != '.'
                                && c != '@'
                        ) {
                            return false;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)
                                && c != '.'
                                && c != '-'
                                && c != '@'
                        ) {
                            return false;
                        }
                    }
                }
                break;
            }
        }
        return true;
    }

    static NElements of(Object any) {
        return of().setValue(any);
    }

    static NElements of() {
        return NExtensions.of(NElements.class);
    }

    static NElements ofPlainJson(Object any) {
        return of().setValue(any).setNtf(false).json();
    }

    static NElements ofPlainProps(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.PROPS);
    }

    static NElements ofPlainXml(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.XML);
    }

    static NElements ofPlainTree(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.TREE);
    }

    static NElements ofPlain(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.PLAIN);
    }

    static NElements ofPlainTson(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.TSON);
    }

    static NElements ofPlainYaml(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.YAML);
    }

    static NElements ofPlainTable(Object any) {
        return of().setValue(any).setNtf(false).setContentType(NContentType.TABLE);
    }

    static NElements ofNtfJson(Object any) {
        return of().setValue(any).setNtf(true).json();
    }

    static NElements ofNtfProps(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.PROPS);
    }

    static NElements ofNtfXml(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.XML);
    }

    static NElements ofNtfTree(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.TREE);
    }

    static NElements ofNtfTson(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.TSON);
    }

    static NElements ofNtfYaml(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.YAML);
    }

    static NElements ofNtfTable(Object any) {
        return of().setValue(any).setNtf(true).setContentType(NContentType.TABLE);
    }

    static NElements ofPlainJson() {
        return of().setNtf(false).json();
    }

    static NElements ofPlainProps() {
        return of().setNtf(false).setContentType(NContentType.PROPS);
    }

    static NElements ofPlainXml() {
        return of().setNtf(false).setContentType(NContentType.XML);
    }

    static NElements ofPlainTree() {
        return of().setNtf(false).setContentType(NContentType.TREE);
    }

    static NElements ofPlain() {
        return of().setNtf(false).setContentType(NContentType.PLAIN);
    }

    static NElements ofPlainTson() {
        return of().setNtf(false).setContentType(NContentType.TSON);
    }

    static NElements ofPlainYaml() {
        return of().setNtf(false).setContentType(NContentType.YAML);
    }

    static NElements ofPlainTable() {
        return of().setNtf(false).setContentType(NContentType.TABLE);
    }

    static NElements ofNtfJson() {
        return of().setNtf(true).json();
    }

    static NElements ofNtfProps() {
        return of().setNtf(true).setContentType(NContentType.PROPS);
    }

    static NElements ofNtfXml() {
        return of().setNtf(true).setContentType(NContentType.XML);
    }

    static NElements ofNtfTree() {
        return of().setNtf(true).setContentType(NContentType.TREE);
    }

    static NElements ofNtfTson() {
        return of().setNtf(true).setContentType(NContentType.TSON);
    }

    static NElements ofNtfYaml() {
        return of().setNtf(true).setContentType(NContentType.YAML);
    }

    static NElements ofNtfTable() {
        return of().setNtf(true).setContentType(NContentType.TABLE);
    }

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NContentType getContentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NElements setContentType(NContentType contentType);

    NElements json();

    NElements yaml();

    NElements tson();

    NElements xml();

    /**
     * return current value to format.
     *
     * @return current value to format
     * @since 0.5.6
     */
    @Override
    Object getValue();

    /**
     * set current value to format.
     *
     * @param value value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    @Override
    NElements setValue(Object value);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NElements configure(boolean skipUnsupported, String... args);

    @Override
    NElements setNtf(boolean ntf);

    /**
     * compile pathExpression into a valid NutsElementPath that helps filtering
     * elements tree. JSONPath expressions refer to a JSON structure the same
     * way as XPath expression are used with XML documents. JSONPath expressions
     * can use the dot notation and/or bracket notations .store.book[0].title
     * The trailing root is not necessary : .store.book[0].title You can also
     * use bracket notation store['book'][0].title for input paths.
     *
     * @param pathExpression element path expression
     * @return Element Path filter
     */
    NElementPath compilePath(String pathExpression);

    /**
     * true is compact json flag is armed
     *
     * @return true is compact json flag is armed
     */
    boolean isCompact();

    /**
     * enable compact json
     *
     * @param compact true to enable compact mode
     * @return {@code this} instance
     */
    NElements setCompact(boolean compact);

    /**
    /**
     * convert element to the specified object if applicable or throw an
     * exception.
     *
     * @param <T> return type
     * @param any element to convert
     * @param to  class type
     * @return instance of type {@code T} converted from {@code element}
     */
    <T> T convert(Object any, Class<T> to);

    /**
     * destruct an object is to convert it to a simple object composed only of :
     * <ul>
     * <li>boxed primitives</li>
     * <li>simple objects like String,Date,Instant and Path</li>
     * <li>Map</li>
     * <li>Map.Entry</li>
     * <li>List</li>
     * </ul>
     *
     * @param any object
     * @return destructed object
     */
    Object destruct(Object any);

    NElement toElement(Object any);

//    /**
//     * create object element builder (mutable)
//     *
//     * @return primitive builder
//     */
//    NutsPrimitiveElementBuilder forPrimitive();

    <T> T fromElement(NElement o, Class<T> to);


    Predicate<Class<?>> getIndestructibleObjects();

    NElements setIndestructibleFormat();

    NElements setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter);

    NIterableFormat iter(NPrintStream out);


    boolean isLogProgress();

    NElements setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElements setTraceProgress(boolean traceProgress);

    NProgressFactory getProgressFactory();

    NElements setProgressFactory(NProgressFactory progressFactory);

/// ///////////////////////////////////////////////////////////////////////////////////

    static NPairElement ofPair(NElement key, NElement value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, NElement value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Boolean value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Byte value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Short value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Integer value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Long value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, String value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Double value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, Instant value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, LocalDate value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, LocalDateTime value){return NElementFactory.of().ofPair(key,value);}

    static NPairElement ofPair(String key, LocalTime value){return NElementFactory.of().ofPair(key,value);}

    static NPairElementBuilder ofPairBuilder(NElement key, NElement value){return NElementFactory.of().ofPairBuilder(key,value);}

    static NPairElementBuilder ofPairBuilder(){return NElementFactory.of().ofPairBuilder();}

    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    static NObjectElementBuilder ofObjectBuilder(){return NElementFactory.of().ofObjectBuilder();}

    static NObjectElementBuilder ofObjectBuilder(String name){return NElementFactory.of().ofObjectBuilder(name);}

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    static NArrayElementBuilder ofArrayBuilder(){return NElementFactory.of().ofArrayBuilder();}

    static NArrayElementBuilder ofArrayBuilder(String name){return NElementFactory.of().ofArrayBuilder(name);}

    static NArrayElement ofArray(){return NElementFactory.of().ofArray();}

    static NObjectElement ofObject(){return NElementFactory.of().ofObject();}

    static NPrimitiveElement ofBoolean(String value){return NElementFactory.of().ofBoolean(value);}

    static NPrimitiveElement ofBoolean(boolean value){return NElementFactory.of().ofBoolean(value);}

    static NPrimitiveElement ofRegex(String value){return NElementFactory.of().ofRegex(value);}

    static NPrimitiveElement ofName(String value){return NElementFactory.of().ofName(value);}

    static NPrimitiveElement ofNameOrString(String value){return NElementFactory.of().ofNameOrString(value);}

    static NPrimitiveElement ofString(String value){return NElementFactory.of().ofString(value);}

    static NPrimitiveElement ofString(String value, NElementType stringLayout){return NElementFactory.of().ofString(value,stringLayout);}

    static NCustomElement ofCustom(Object value){return NElementFactory.of().ofCustom(value);}

    static NPrimitiveElement ofTrue(){return NElementFactory.of().ofTrue();}

    static NPrimitiveElement ofFalse(){return NElementFactory.of().ofFalse();}

    static NPrimitiveElement ofInstant(Instant value){return NElementFactory.of().ofInstant(value);}

    static NPrimitiveElement ofLocalDate(LocalDate value){return NElementFactory.of().ofLocalDate(value);}

    static NPrimitiveElement ofLocalDateTime(LocalDateTime value){return NElementFactory.of().ofLocalDateTime(value);}

    static NPrimitiveElement ofLocalTime(LocalTime value){return NElementFactory.of().ofLocalTime(value);}

    static NPrimitiveElement ofFloat(Float value){return NElementFactory.of().ofFloat(value);}

    static NPrimitiveElement ofFloat(float value){return NElementFactory.of().ofFloat(value);}

    static NPrimitiveElement ofFloat(Float value, String suffix){return NElementFactory.of().ofFloat(value,suffix);}

    static NPrimitiveElement ofFloat(float value, String suffix){return NElementFactory.of().ofFloat(value,suffix);}

    static NPrimitiveElement ofByte(Byte value){return NElementFactory.of().ofByte(value);}

    static NPrimitiveElement ofByte(byte value){return NElementFactory.of().ofByte(value);}

    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix){return NElementFactory.of().ofByte(value,layout,suffix);}

    static NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix){return NElementFactory.of().ofByte(value,layout,suffix);}

    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout){return NElementFactory.of().ofByte(value,layout);}

    static NPrimitiveElement ofByte(byte value, NNumberLayout layout){return NElementFactory.of().ofByte(value,layout);}

    static NPrimitiveElement ofByte(Byte value, String suffix){return NElementFactory.of().ofByte(value,suffix);}

    static NPrimitiveElement ofByte(byte value, String suffix){return NElementFactory.of().ofByte(value,suffix);}

    static NPrimitiveElement ofShort(Short value){return NElementFactory.of().ofShort(value);}

    static NPrimitiveElement ofShort(short value){return NElementFactory.of().ofShort(value);}

    static NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix){return NElementFactory.of().ofShort(value,layout,suffix);}

    static NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix){return NElementFactory.of().ofShort(value,layout,suffix);}

    static NPrimitiveElement ofShort(Short value, NNumberLayout layout){return NElementFactory.of().ofShort(value,layout);}

    static NPrimitiveElement ofShort(short value, NNumberLayout layout){return NElementFactory.of().ofShort(value,layout);}

    static NPrimitiveElement ofShort(Short value, String suffix){return NElementFactory.of().ofShort(value,suffix);}

    static NPrimitiveElement ofShort(short value, String suffix){return NElementFactory.of().ofShort(value,suffix);}

    static NPrimitiveElement ofInt(Integer value){return NElementFactory.of().ofInt(value);}

    static NPrimitiveElement ofInt(int value){return NElementFactory.of().ofInt(value);}

    static NPrimitiveElement ofInt(Integer value, String suffix){return NElementFactory.of().ofInt(value,suffix);}

    static NPrimitiveElement ofInt(int value, String suffix){return NElementFactory.of().ofInt(value,suffix);}

    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix){return NElementFactory.of().ofInt(value,layout,suffix);}

    static NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix){return NElementFactory.of().ofInt(value,layout,suffix);}

    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout){return NElementFactory.of().ofInt(value,layout);}

    static NPrimitiveElement ofInt(int value, NNumberLayout layout){return NElementFactory.of().ofInt(value,layout);}

    static NPrimitiveElement ofLong(Long value){return NElementFactory.of().ofLong(value);}

    static NPrimitiveElement ofLong(long value){return NElementFactory.of().ofLong(value);}

    static NPrimitiveElement ofLong(Long value, String suffix){return NElementFactory.of().ofLong(value,suffix);}

    static NPrimitiveElement ofLong(long value, String suffix){return NElementFactory.of().ofLong(value,suffix);}

    static NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix){return NElementFactory.of().ofLong(value,layout,suffix);}

    static NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix){return NElementFactory.of().ofLong(value,layout,suffix);}

    static NPrimitiveElement ofLong(Long value, NNumberLayout layout){return NElementFactory.of().ofLong(value,layout);}

    static NPrimitiveElement ofLong(long value, NNumberLayout layout){return NElementFactory.of().ofLong(value,layout);}

    static NPrimitiveElement ofNull(){return NElementFactory.of().ofNull();}

    static NPrimitiveElement ofNumber(String value){return NElementFactory.of().ofNumber(value);}

    static NPrimitiveElement ofInstant(Date value){return NElementFactory.of().ofInstant(value);}

    static NPrimitiveElement ofInstant(String value){return NElementFactory.of().ofInstant(value);}

    static NPrimitiveElement ofChar(Character value){return NElementFactory.of().ofChar(value);}

    static NPrimitiveElement ofDouble(Double value){return NElementFactory.of().ofDouble(value);}

    static NPrimitiveElement ofDouble(double value){return NElementFactory.of().ofDouble(value);}

    static NPrimitiveElement ofDouble(Double value, String suffix){return NElementFactory.of().ofDouble(value,suffix);}

    static NPrimitiveElement ofDouble(double value, String suffix){return NElementFactory.of().ofDouble(value,suffix);}

    static NPrimitiveElement ofDoubleComplex(double real){return NElementFactory.of().ofDoubleComplex(real);}

    static NPrimitiveElement ofDoubleComplex(double real, double imag){return NElementFactory.of().ofDoubleComplex(real,imag);}

    static NPrimitiveElement ofFloatComplex(float real){return NElementFactory.of().ofFloatComplex(real);}

    static NPrimitiveElement ofFloatComplex(float real, float imag){return NElementFactory.of().ofFloatComplex(real,imag);}

    static NPrimitiveElement ofBigComplex(BigDecimal real){return NElementFactory.of().ofBigComplex(real);}

    static NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag){return NElementFactory.of().ofBigComplex(real,imag);}


    static NPrimitiveElement ofNumber(Number value){return NElementFactory.of().ofNumber(value);}

    static NPrimitiveElement ofBigDecimal(BigDecimal value){return NElementFactory.of().ofBigDecimal(value);}

    static NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix){return NElementFactory.of().ofBigDecimal(value,suffix);}

    static NPrimitiveElement ofBigInteger(BigInteger value){return NElementFactory.of().ofBigInteger(value);}

    static NPrimitiveElement ofBigInteger(BigInteger value, NNumberLayout layout, String suffix){return NElementFactory.of().ofBigInteger(value,layout,suffix);}

    static NPrimitiveElement ofBigInteger(BigInteger value, NNumberLayout layout){return NElementFactory.of().ofBigInteger(value,layout);}

    static NPrimitiveElement ofBigInteger(BigInteger value, String suffix){return NElementFactory.of().ofBigInteger(value,suffix);}


    static NUpletElementBuilder ofUpletBuilder(){return NElementFactory.of().ofUpletBuilder();}

    static NUpletElementBuilder ofUpletBuilder(String name){return NElementFactory.of().ofUpletBuilder(name);}

    static NUpletElement ofUplet(){return NElementFactory.of().ofUplet();}

    static NUpletElement ofUplet(NElement... items){return NElementFactory.of().ofUplet(items);}

    static NUpletElement ofUplet(String name, NElement... items){return NElementFactory.of().ofUplet(name,items);}

    static NUpletElement ofNamedUplet(String name, NElement... items){return NElementFactory.of().ofNamedUplet(name,items);}

    static NMatrixElementBuilder ofMatrixBuilder(){return NElementFactory.of().ofMatrixBuilder();}

    static NArrayElement ofIntArray(int... items){return NElementFactory.of().ofIntArray(items);}

    static NArrayElement ofIntArray(Integer... items){return NElementFactory.of().ofIntArray(items);}

    static NArrayElement ofLongArray(long... items){return NElementFactory.of().ofLongArray(items);}

    static NArrayElement ofLongArray(Long... items){return NElementFactory.of().ofLongArray(items);}

    static NArrayElement ofNumberArray(Number... items){return NElementFactory.of().ofNumberArray(items);}

    static NArrayElement ofBooleanArray(boolean... items){return NElementFactory.of().ofBooleanArray(items);}

    static NArrayElement ofBooleanArray(Boolean... items){return NElementFactory.of().ofBooleanArray(items);}

    static NArrayElement ofArray(NElement... items){return NElementFactory.of().ofArray(items);}

    static NArrayElement ofArray(String name, NElement... items){return NElementFactory.of().ofArray(name,items);}

    static NArrayElement ofNamedArray(String name, NElement... items){return NElementFactory.of().ofNamedArray(name,items);}

    static NArrayElement ofNamedParametrizedArray(String name, NElement[] params, NElement... items){return NElementFactory.of().ofNamedParametrizedArray(name,items);}

    static NArrayElement ofArray(String name, NElement[] params, NElement... items){return NElementFactory.of().ofArray(name,params,items);}

    static NArrayElement ofParametrizedArray(NElement[] params, NElement... items){return NElementFactory.of().ofParametrizedArray(params,items);}

    static NArrayElement ofParametrizedArray(NElement... params){return NElementFactory.of().ofParametrizedArray(params);}

    static NArrayElement ofParametrizedArray(String name, NElement[] params, NElement... items){return NElementFactory.of().ofParametrizedArray(name,params,items);}

    static NArrayElement ofParametrizedArray(String name, NElement... params){return NElementFactory.of().ofParametrizedArray(name,params);}

    static NArrayElement ofStringArray(String... items){return NElementFactory.of().ofStringArray(items);}

    static NArrayElement ofDoubleArray(double... items){return NElementFactory.of().ofDoubleArray(items);}

    static NArrayElement ofDoubleArray(Double... items){return NElementFactory.of().ofDoubleArray(items);}

    static NObjectElement ofObject(NElement... items){return NElementFactory.of().ofObject(items);}

    static NObjectElement ofObject(String name, NElement... items){return NElementFactory.of().ofObject(name,items);}

    static NObjectElement ofNamedObject(String name, NElement... items){return NElementFactory.of().ofNamedObject(name,items);}

    static NObjectElement ofNamedParametrizedObject(String name, NElement[] params, NElement... items){return NElementFactory.of().ofNamedParametrizedObject(name,params,items);}

    static NObjectElement ofParametrizedObject(NElement[] params, NElement... items){return NElementFactory.of().ofParametrizedObject(params,items);}

    static NObjectElement ofParametrizedObject(NElement... params){return NElementFactory.of().ofParametrizedObject(params);}

    static NObjectElement ofParametrizedObject(String name, NElement[] params, NElement... items){return NElementFactory.of().ofParametrizedObject(name,params,items);}

    static NObjectElement ofObject(String name, NElement[] params, NElement... items){return NElementFactory.of().ofObject(name,params,items);}

    static NObjectElement ofParametrizedObject(String name, NElement... params){return NElementFactory.of().ofParametrizedObject(name,params);}

    static NElementComments ofMultiLineComments(String... comments){return NElementFactory.of().ofMultiLineComments();}

    static NElementComments ofSingleLineComments(String... comments){return NElementFactory.of().ofSingleLineComments(comments);}

    static NElementComments ofComments(NElementComment[] leading, NElementComment[] trailing){return NElementFactory.of().ofComments(leading,trailing);}

    static NElementComment ofMultiLineComment(String... comments){return NElementFactory.of().ofMultiLineComment(comments);}

    static NElementComment ofSingleLineComment(String... lines){return NElementFactory.of().ofSingleLineComment(lines);}

    static NElement ofBinaryStream(NInputStreamProvider value){return NElementFactory.of().ofBinaryStream(value);}

    static NElement ofCharStream(NReaderProvider value){return NElementFactory.of().ofCharStream(value);}

    static NBinaryStreamElementBuilder ofBinaryStreamBuilder(){return NElementFactory.of().ofBinaryStreamBuilder();}

    static NCharStreamElementBuilder ofCharStreamBuilder(){return NElementFactory.of().ofCharStreamBuilder();}

    static NElementAnnotation ofAnnotation(String name, NElement... values){return NElementFactory.of().ofAnnotation(name, values);}

    static NPrimitiveElementBuilder ofPrimitiveBuilder(){return NElementFactory.of().ofPrimitiveBuilder();}
}
