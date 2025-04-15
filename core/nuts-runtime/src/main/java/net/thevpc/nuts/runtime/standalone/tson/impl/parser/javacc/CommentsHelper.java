package net.thevpc.nuts.runtime.standalone.tson.impl.parser.javacc;

import net.thevpc.nuts.runtime.standalone.tson.Tson;
import net.thevpc.nuts.runtime.standalone.tson.TsonComment;
import net.thevpc.nuts.runtime.standalone.tson.TsonParserVisitor;
import net.thevpc.nuts.runtime.standalone.tson.TsonStreamParserConfig;

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
