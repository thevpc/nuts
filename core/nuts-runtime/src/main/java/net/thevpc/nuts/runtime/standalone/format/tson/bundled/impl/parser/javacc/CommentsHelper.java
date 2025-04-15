package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.javacc;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.Tson;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonComment;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonParserVisitor;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonStreamParserConfig;

public class CommentsHelper {
    private static ThreadLocal<Data> CURR=new ThreadLocal<>();
    private static class Data{
        private TsonStreamParserConfig config;
        private TsonParserVisitor visitor;
        private Object source;

        public Data(TsonStreamParserConfig config, TsonParserVisitor visitor, Object source) {
            this.config = config;
            this.visitor = visitor;
            this.source = source;
        }
    }

    public static void init(TsonStreamParserConfig config,TsonParserVisitor visitor,Object source){
        CURR.set(new Data(config, visitor, source));
    }

    public static void onComments(String image){
        Data c = CURR.get();
        if(!c.config.isSkipComments()) {
            TsonComment rc=(!c.config.isRawComments())
                    ? Tson.parseComments(image)
                    :TsonComment.ofMultiLine(image);
            c.visitor.visitComments(rc);
        }
    }
}
