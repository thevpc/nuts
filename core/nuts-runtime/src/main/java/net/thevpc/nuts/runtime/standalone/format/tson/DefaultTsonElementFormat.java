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
package net.thevpc.nuts.runtime.standalone.format.tson;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomParser;
import net.thevpc.nuts.text.NContentType;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author thevpc
 */
public class DefaultTsonElementFormat implements NElementStreamFormat {


    public DefaultTsonElementFormat() {
    }

    public NElement parseElement(String string, NElementFactoryContext context, Object readerSource) {
        if (string == null) {
            string = "";
        }
        return parseElement(new StringReader(string), context, readerSource);
    }

    public void write(NPrintStream out, NElement data, NElementFormatter formatter) {
        DefaultTsonWriter d = new DefaultTsonWriter(out.asStringWriter());
        d.write(data.format(NContentType.TSON, formatter));
    }

    @Override
    public NElement normalize(NElement e) {
        return e;
    }


    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context, Object readerSource) {
        TsonCustomParser p = new TsonCustomParser(reader);
        return p.parseDocument();
    }

    @Override
    public void printElement(NElement value, NPrintStream out, NElementFormatter formatter, NElementFactoryContext context) {
        write(out, value, formatter);
    }
}

