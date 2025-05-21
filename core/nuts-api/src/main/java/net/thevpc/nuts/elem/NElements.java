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
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.time.NProgressFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
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
     * parse url as a valid object of the given type
     *
     * @param url   source url
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(URL url, Class<T> clazz);

    /**
     * parse path as a valid object of the given type
     *
     * @param path  source path
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(NPath path, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @param clazz       target type
     * @param <T>         target type
     * @return new instance of the given class
     */
    <T> T parse(InputStream inputStream, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param string source as json string
     * @param clazz  target type
     * @param <T>    target type
     * @return new instance of the given class
     */
    <T> T parse(String string, Class<T> clazz);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(byte[] bytes, Class<T> clazz);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @param clazz  target type
     * @param <T>    target type
     * @return new instance of the given class
     */
    <T> T parse(Reader reader, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file  source url
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(Path file, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file  source URL
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(File file, Class<T> clazz);

    /**
     * parse url as a valid object of the given type
     *
     * @param url source url
     * @return element
     */
    NElement parse(URL url);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @return element
     */
    NElement parse(InputStream inputStream);

    /**
     * parse string as a valid NutsElement.
     * If the string is null, NutsElement.NULL is returned
     *
     * @param string source as json string
     * @return element
     */
    NElement parse(String string);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @return element
     */
    NElement parse(byte[] bytes);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @return element
     */
    NElement parse(Reader reader);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source url
     * @return element
     */
    NElement parse(Path file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NElement parse(File file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NElement parse(NPath file);

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

    //    NutsElementEntryBuilder forEntry();
    NPairElement ofPair(NElement key, NElement value);

    NPairElement ofPair(String key, NElement value);

    NPairElement ofPair(String key, Boolean value);

    NPairElement ofPair(String key, Byte value);

    NPairElement ofPair(String key, Short value);

    NPairElement ofPair(String key, Integer value);

    NPairElement ofPair(String key, Long value);

    NPairElement ofPair(String key, String value);

    NPairElement ofPair(String key, Double value);

    NPairElement ofPair(String key, Instant value);

    NPairElement ofPair(String key, LocalDate value);

    NPairElement ofPair(String key, LocalDateTime value);

    NPairElement ofPair(String key, LocalTime value);

    NPairElementBuilder ofPairBuilder(NElement key, NElement value);

    NPairElementBuilder ofPairBuilder();

    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    NObjectElementBuilder ofObjectBuilder();

    NObjectElementBuilder ofObjectBuilder(String name);

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    NArrayElementBuilder ofArrayBuilder();

    NArrayElementBuilder ofArrayBuilder(String name);

    NArrayElement ofEmptyArray();

    NObjectElement ofEmptyObject();

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
    //        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
    //    }
    NPrimitiveElement ofBoolean(String value);

    NPrimitiveElement ofBoolean(boolean value);

    NPrimitiveElement ofRegex(String value);

    NPrimitiveElement ofName(String value);

    NPrimitiveElement ofNameOrString(String value);

    NPrimitiveElement ofString(String str);

    NPrimitiveElement ofString(String str, NElementType stringLayout);

    NCustomElement ofCustom(Object object);

    NPrimitiveElement ofTrue();

    NPrimitiveElement ofFalse();

    NPrimitiveElement ofInstant(Instant instant);

    NPrimitiveElement ofLocalDate(LocalDate localDate);

    NPrimitiveElement ofLocalDateTime(LocalDateTime localDateTime);

    NPrimitiveElement ofLocalTime(LocalTime localTime);

    NPrimitiveElement ofFloat(Float value);

    NPrimitiveElement ofFloat(float value);

    NPrimitiveElement ofFloat(Float value, String suffix);

    NPrimitiveElement ofFloat(float value, String suffix);

    NPrimitiveElement ofByte(Byte value);

    NPrimitiveElement ofByte(byte value);

    NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofByte(Byte value, NNumberLayout layout);

    NPrimitiveElement ofByte(byte value, NNumberLayout layout);

    NPrimitiveElement ofByte(Byte value, String suffix);

    NPrimitiveElement ofByte(byte value, String suffix);

    NPrimitiveElement ofShort(Short value);

    NPrimitiveElement ofShort(short value);

    NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofShort(Short value, NNumberLayout layout);

    NPrimitiveElement ofShort(short value, NNumberLayout layout);

    NPrimitiveElement ofShort(Short value, String suffix);

    NPrimitiveElement ofShort(short value, String suffix);


    NPrimitiveElement ofInt(Integer value);

    NPrimitiveElement ofInt(int value);

    NPrimitiveElement ofInt(Integer value, String suffix);

    NPrimitiveElement ofInt(int value, String suffix);

    NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofInt(Integer value, NNumberLayout layout);

    NPrimitiveElement ofInt(int value, NNumberLayout layout);

    NPrimitiveElement ofLong(Long value);

    NPrimitiveElement ofLong(long value);

    NPrimitiveElement ofLong(Long value, String suffix);

    NPrimitiveElement ofLong(long value, String suffix);

    NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofLong(Long value, NNumberLayout layout);

    NPrimitiveElement ofLong(long value, NNumberLayout layout);


    NPrimitiveElement ofNull();

    NPrimitiveElement ofNumber(String value);

    NPrimitiveElement ofInstant(Date value);

    NPrimitiveElement ofInstant(String value);

    NPrimitiveElement ofChar(Character value);

    NPrimitiveElement ofDouble(Double value);

    NPrimitiveElement ofDouble(double value);

    NPrimitiveElement ofDouble(Double value, String suffix);

    NPrimitiveElement ofDouble(double value, String suffix);

    NPrimitiveElement ofDoubleComplex(double real);

    NPrimitiveElement ofDoubleComplex(double real, double imag);

    NPrimitiveElement ofFloatComplex(float real);

    NPrimitiveElement ofFloatComplex(float real, float imag);

    NPrimitiveElement ofBigComplex(BigDecimal real);

    NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag);


    NPrimitiveElement ofNumber(Number value);

    NPrimitiveElement ofBigDecimal(BigDecimal value);

    NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix);

    NPrimitiveElement ofBigInteger(BigInteger value);

    NPrimitiveElement ofBigInteger(BigInteger value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofBigInteger(BigInteger value, NNumberLayout layout);

    NPrimitiveElement ofBigInteger(BigInteger value, String suffix);

    Predicate<Class<?>> getIndestructibleObjects();

    NElements setIndestructibleFormat();

    NElements setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter);

    NIterableFormat iter(NPrintStream out);

    <T> NElements setMapper(Class<T> type, NElementMapper<T> mapper);

    boolean isLogProgress();

    NElements setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElements setTraceProgress(boolean traceProgress);

    NProgressFactory getProgressFactory();

    NElements setProgressFactory(NProgressFactory progressFactory);

    NUpletElementBuilder ofUpletBuilder();

    NUpletElementBuilder ofUpletBuilder(String name);

    NUpletElement ofEmptyUplet();

    NUpletElement ofUplet(NElement... items);

    NUpletElement ofUplet(String name, NElement... items);

    NMatrixElementBuilder ofMatrixBuilder();

    NArrayElement ofIntArray(int... items);

    NArrayElement ofIntArray(Integer... items);

    NArrayElement ofLongArray(long... items);

    NArrayElement ofLongArray(Long... items);

    NArrayElement ofNumberArray(Number... items);

    NArrayElement ofBooleanArray(boolean... items);

    NArrayElement ofBooleanArray(Boolean... items);

    NArrayElement ofArray(NElement... items);

    NArrayElement ofStringArray(String... items);

    NArrayElement ofDoubleArray(double... items);

    NArrayElement ofDoubleArray(Double... items);

    NObjectElement ofObject(NElement... items);

    NElementComments ofMultiLineComments(String... a);

    NElementComments ofSingleLineComments(String... a);

    NElementComments ofComments(NElementComment[] leading, NElementComment[] trailing);


    NElementComment ofMultiLineComment(String... a);

    NElementComment ofSingleLineComment(String... lines);

    NElement ofBinaryStream(NInputStreamProvider value);

    NElement ofCharStream(NReaderProvider value);

    NBinaryStreamElementBuilder ofBinaryStreamBuilder();

    NCharStreamElementBuilder ofCharStreamBuilder();

    NElementAnnotation ofAnnotation(String name, NElement... values);


}
