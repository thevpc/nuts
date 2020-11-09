/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Xml Format Helper class
 * @author vpc
 * @since 0.5.5
 * @category Format
 */
public interface NutsXmlFormat extends NutsFormat {

    /**
     * true if compact xml generated.
     * @return true if compact xml generated.
     */
    boolean isCompact();

    /**
     * if true compact xml generated. if false, sue more versatile/formatted output.
     * @param compact compact flag
     * @return {@code this} instance
     */
    NutsXmlFormat setCompact(boolean compact);

    /**
     * convert {@code value} to an xml document.
     * @param value value to convert
     * @return converted object
     */
    Document toXmlDocument(Object value);

    /**
     * convert {@code value} to a valid root element to add to the given {@code xmlDocument}.
     * if the document is null, a new one will be created.
     * @param value value to convert
     * @param xmlDocument target document
     * @return converted object
     */
    Element toXmlElement(Object value, Document xmlDocument);

    /**
     * convert {@code xmlElement} to a valid instance of type {@code clazz}
     * @param xmlElement xmlElement to convert
     * @param clazz target class
     * @param <T> class type
     * @return converted object
     */
    <T> T fromXmlElement(Element xmlElement, Class<T> clazz);

    /**
     * parse url content as xml to the given class
     * @param url url to parse
     * @param clazz target class
     * @param <T> class type
     * @return parsed instance
     */
    <T> T parse(URL url, Class<T> clazz);

    /**
     * parse inputStream as xml to the given class
     * @param inputStream inputStream to parse
     * @param clazz target class
     * @param <T> class type
     * @return parsed instance
     */
    <T> T parse(InputStream inputStream, Class<T> clazz);

    /**
     * parse bytes as xml to the given class
     * @param bytes bytes to parse
     * @param clazz target class
     * @param <T> class type
     * @return parsed instance
     */
    <T> T parse(byte[] bytes, Class<T> clazz);

    /**
     * Parse Xml Content as given class type.
     *
     * @param <T> class type to parse to
     * @param reader input content
     * @param clazz type to parse to
     * @return instance of type parsed to
     */
    <T> T parse(Reader reader, Class<T> clazz);

    /**
     * Parse Xml Content as given class type.
     *
     * @param <T> class type to parse to
     * @param path input content
     * @param clazz type to parse to
     * @return instance of type parsed to
     */
    <T> T parse(Path path, Class<T> clazz);

    /**
     * Parse Xml Content as given class type.
     *
     * @param <T> class type to parse to
     * @param file input content
     * @param clazz type to parse to
     * @return instance of type parsed to
     */
    <T> T parse(File file, Class<T> clazz);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsXmlFormat setSession(NutsSession session);

    /**
     * return value to format
     * @return value to format
     */
    Object getValue();

    /**
     * set value to format
     * @param value value to format
     * @return value to format
     */
    NutsXmlFormat value(Object value);

    /**
     * set value to format
     * @param value value to format
     * @return value to format
     */
    NutsXmlFormat setValue(Object value);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsXmlFormat configure(boolean skipUnsupported, String... args);

}
