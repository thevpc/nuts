/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;

/**
 * Class responsible of manipulating {@link NutsElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @since 0.5.5
 * @category Format
 */
public interface NutsElementFormat extends NutsObjectFormat {

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NutsContentType getContentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NutsElementFormat setContentType(NutsContentType contentType);

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
    NutsElementFormat setValue(Object value);

    /**
     * set current session.
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsElementFormat setSession(NutsSession session);

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
    NutsElementPath compilePath(String pathExpression);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsElementFormat configure(boolean skipUnsupported, String... args);

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
    NutsElementFormat setCompact(boolean compact);

    /**
     * parse url as a valid object of the given type
     *
     * @param url source url
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(URL url, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(InputStream inputStream, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param string source as json string
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(String string, Class<T> clazz);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(byte[] bytes, Class<T> clazz);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(Reader reader, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source url
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(Path file, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @param clazz target type
     * @param <T> target type
     * @return new instance of the given class
     */
    <T> T parse(File file, Class<T> clazz);

    /**
     * parse url as a valid object of the given type
     *
     * @param url source url
     * @return element
     */
    NutsElement parse(URL url);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @return element
     */
    NutsElement parse(InputStream inputStream);

    /**
     * parse string as a valid NutsElement.
     * If the string is null, NutsElement.NULL is returned
     *
     * @param string source as json string
     * @return element
     */
    NutsElement parse(String string);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @return element
     */
    NutsElement parse(byte[] bytes);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @return element
     */
    NutsElement parse(Reader reader);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source url
     * @return element
     */
    NutsElement parse(Path file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NutsElement parse(File file);

    /**
     * convert element to the specified object if applicable or throw an
     * exception.
     *
     * @param <T> return type
     * @param any element to convert
     * @param to class type
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

    NutsElement toElement(Object any);

//    NutsElementEntryBuilder forEntry();
    NutsElementEntry forEntry(NutsElement key, NutsElement value);

//    /**
//     * create object element builder (mutable)
//     *
//     * @return primitive builder
//     */
//    NutsPrimitiveElementBuilder forPrimitive();
    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    NutsObjectElementBuilder forObject();

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    NutsArrayElementBuilder forArray();

    @Override
    public NutsElementFormat setNtf(boolean ntf);

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
    //        return str == null ? DefaultNutsPrimitiveElementBuilder.NULL : new DefaultNutsPrimitiveElement(NutsElementType.NUTS_STRING, str);
    //    }
    NutsPrimitiveElement forBoolean(String value);

    NutsPrimitiveElement forBoolean(boolean value);

    NutsPrimitiveElement forString(String str);

    NutsPrimitiveElement forTrue();

    NutsPrimitiveElement forFalse();

    NutsPrimitiveElement forInstant(Instant instant);

    NutsPrimitiveElement forFloat(Float value);

    NutsPrimitiveElement forInt(Integer value);

    NutsPrimitiveElement forLong(Long value);

    NutsPrimitiveElement forNull();

    NutsPrimitiveElement forNumber(String value);

    NutsPrimitiveElement forInstant(Date value);

    NutsPrimitiveElement forInstant(String value);

    NutsPrimitiveElement forByte(Byte value);

    NutsPrimitiveElement forDouble(Double value);

    NutsPrimitiveElement forFloat(Short value);

    NutsPrimitiveElement forNumber(Number value);

    Predicate<Type> getDestructTypeFilter();

    NutsElementFormat setDestructTypeFilter(Predicate<Type> destructTypeFilter);

    NutsIterableFormat iter(NutsPrintStream out);

    void setMapper(Class type, NutsElementMapper mapper);
}
