/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

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
 */
public interface NutsXmlFormat extends NutsFormat {

    /**
     * true if compact xml generated.
     * @return true if compact xml generated.
     */
    boolean isCompact();

    /**
     * set compact xml generated mode.
     * @return {@code this} instance
     */
    NutsXmlFormat compact();

    /**
     * if true compact xml generated. if false, use more versatile/formatted output.
     * @param compact compact flag
     * @return {@code this} instance
     */
    NutsXmlFormat compact(boolean compact);

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
    NutsXmlFormat session(NutsSession session);

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
