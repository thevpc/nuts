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
import net.thevpc.nuts.runtime.standalone.format.tson.format.DefaultTsonFormatConfig;
import net.thevpc.nuts.runtime.standalone.format.tson.format.TsonFormatImplBuilder;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomParser;
import net.thevpc.nuts.util.NUtils;

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

    public void write(NPrintStream out, NElement data, boolean compact) {
        TsonFormatImplBuilder ts = new TsonFormatImplBuilder();
        DefaultTsonFormatConfig c = new DefaultTsonFormatConfig();
        c.setCompact(compact);
        ts.setConfig(c);
        out.print(ts.build().format(data));
    }

    @Override
    public NElement normalize(NElement e) {
        return e;
    }


    @Override
    public NElement parseElement(Reader reader, NElementFactoryContext context, Object readerSource) {
        TsonCustomParser p=new TsonCustomParser(reader);
        return p.parseDocument();
//        TsonStreamParserConfig config = new TsonStreamParserConfig();
//        ElementBuilderTsonParserVisitor r = new ElementBuilderTsonParserVisitor();
//        TsonStreamParser source = fromReader(reader, readerSource);
//        config.setVisitor(r);
//        source.setConfig(config);
//        try {
//            source.parseDocument();
//        } catch (Exception e) {
//            throw new TsonParseException(e, NUtils.firstNonNull(source.source(),readerSource));
//        }
//        NElement e = r.getDocument();
//        return e;
    }

    @Override
    public void printElement(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context) {
        write(out, value, compact);
    }


//    public TsonStreamParser fromReader(Reader reader, Object source) {
//        TsonStreamParserImpl p = new TsonStreamParserImpl(reader);
//        p.source(source);
//        return new TsonStreamParser() {
//            @Override
//            public Object source() {
//                return source;
//            }
//
//            @Override
//            public void setConfig(TsonStreamParserConfig config) {
//                p.setConfig(config);
//            }
//
//            @Override
//            public void parseElement() {
//                try {
//                    p.parseElement();
//                } catch (TokenMgrError e) {
//                    throw JavaccHelper.createTsonParseException(e, source);
//                } catch (ParseException e) {
//                    throw JavaccHelper.createTsonParseException(e, source);
//                }
//            }
//
//            @Override
//            public void parseDocument() {
//                try {
//                    p.parseDocument();
//                } catch (TokenMgrError e) {
//                    throw JavaccHelper.createTsonParseException(e, source);
//                } catch (ParseException e) {
//                    throw JavaccHelper.createTsonParseException(e, source);
//                }
//            }
//        };
//    }
}
