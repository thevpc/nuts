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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.vpc.app.nuts.core.format.CanonicalBuilder;
import net.vpc.app.nuts.core.format.ObjectOutputFormatWriterHelper;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import org.w3c.dom.Document;

/**
 *
 * @author vpc
 */
public class NutsXmlUtils {

    public static void print(String name, Object object, Writer out, boolean compact, CanonicalBuilder canonicalBuilder) {
        print(name, object, (Object) out, compact, canonicalBuilder);
    }

    public static void print(String name, Object object, PrintStream out, boolean compact, CanonicalBuilder canonicalBuilder) {
        print(name, object, (Object) out, compact, canonicalBuilder);
    }

    private static void print(String name, Object object, Object out, boolean compact, CanonicalBuilder canonicalBuilder) {
        try {
            if (canonicalBuilder != null) {
                object = canonicalBuilder.toCanonical(object);
            }
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            String rootName = name;
            document.appendChild(ObjectOutputFormatWriterHelper.createElement(CoreStringUtils.isBlank(rootName) ? "root" : rootName, object, document));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = null;
            if (out instanceof PrintStream) {
                streamResult = new StreamResult((PrintStream) out);
            } else {
                streamResult = new StreamResult((Writer) out);
            }
            if (!compact) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.STANDALONE, "false");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            transformer.transform(domSource, streamResult);
            if (out instanceof PrintStream) {
                ((PrintStream) out).flush();
            } else {
                ((Writer) out).flush();
            }

        } catch (ParserConfigurationException | TransformerException ex) {
            throw new UncheckedIOException(new IOException(ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
