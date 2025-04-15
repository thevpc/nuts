package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.javacc;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonParserFactory;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonStreamParser;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonStreamParserConfig;

import java.io.Reader;

public class JavaccTsonParserFactory implements TsonParserFactory {
    @Override
    public String id() {
        return "javacc";
    }

    @Override
    public TsonStreamParser fromReader(Reader reader, String parser, Object source) {
        TsonStreamParserImpl p = new TsonStreamParserImpl(reader);
        p.source(source);
        return new TsonStreamParser() {
            @Override
            public Object source() {
                return source;
            }

            @Override
            public void setConfig(TsonStreamParserConfig config) {
                p.setConfig(config);
            }

            @Override
            public void parseElement() {
                try{
                    p.parseElement();
                }catch (ParseException e){
                    throw JavaccHelper.createTsonParseException(e,source);
                }
            }

            @Override
            public void parseDocument()  {
                try{
                    p.parseDocument();
                }catch (ParseException e){
                    throw JavaccHelper.createTsonParseException(e,source);
                }
            }
        };
    }
}
