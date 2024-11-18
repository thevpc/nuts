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
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.io.IOException;
import java.io.Reader;
import javax.xml.transform.stream.StreamResult;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.text.NTexts;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author thevpc
 */
public class DefaultXmlNElementStreamFormat implements NElementStreamFormat {

    public DefaultXmlNElementStreamFormat() {
    }

    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context) {
        NSession session = context.getSession();
        Document doc = null;
        try {
            doc = XmlUtils.createDocumentBuilder(false).parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new NIOException(new IOException(ex));
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return context.objectToElement(doc, Document.class);
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        NSession session = context.getSession();
        Document doc = (Document) context.elementToObject(value, Document.class);
        if (out.isNtf()) {
            NPrintStream bos = NMemoryPrintStream.of();
            XmlUtils.writeDocument(doc, new StreamResult(bos.asPrintStream()), compact, true);
            out.print(NTexts.of().ofCode("xml", bos.toString()));
        } else {
            XmlUtils.writeDocument(doc, new StreamResult(out.asPrintStream()), compact, true);
        }
    }

}
