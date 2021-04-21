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
package net.thevpc.nuts.runtime.core.format.xml;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import javax.xml.transform.stream.StreamResult;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryContext;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementStreamFormat;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author vpc
 */
public class DefaultXmlNutsElementStreamFormat implements NutsElementStreamFormat {

    public DefaultXmlNutsElementStreamFormat() {
    }

    @Override
    public NutsElement parseElement(Reader reader, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        Document doc = null;
        try {
            doc = NutsXmlUtils.createDocumentBuilder(false, session).parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new NutsIOException(session, new IOException(ex));
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return context.objectToElement(doc, Document.class);
    }

    @Override
    public void printElement(NutsElement value, PrintStream out, boolean compact, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        Document doc = (Document) context.elementToObject(value, Document.class);
        if (context.getWorkspace().term().setSession(session).isFormatted(out)) {
            ByteArrayPrintStream bos = new ByteArrayPrintStream();
            NutsXmlUtils.writeDocument(doc, new StreamResult(bos), compact, true, session);
            out.print(context.getWorkspace().formats().text().forCode("xml", bos.toString()));
        } else {
            NutsXmlUtils.writeDocument(doc, new StreamResult(out), compact, true, session);
        }
    }

}
