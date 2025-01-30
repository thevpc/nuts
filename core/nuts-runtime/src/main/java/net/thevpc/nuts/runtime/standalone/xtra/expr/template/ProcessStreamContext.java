package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.io.NCharReader;
import net.thevpc.nuts.io.NullInputStream;
import net.thevpc.nuts.io.NullReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessStreamContext implements Cloneable{
    TagStreamProcessor tagStreamProcessor;
    TagTokenReader tr;
    TagNodeReader nr;
    NExprDeclarations context;
    Writer out;

    public ProcessStreamContext copy(){
        try {
            return (ProcessStreamContext) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public ProcessStreamContext(TagStreamProcessor tagStreamProcessor, InputStream source, NExprDeclarations context) {
        this.context = context;
        this.tagStreamProcessor = tagStreamProcessor;
        tr = new TagTokenReader(tagStreamProcessor.startTag, tagStreamProcessor.endTag, tagStreamProcessor.escape,
                new NCharReader(new InputStreamReader(source == null ? NullInputStream.INSTANCE : source)));
        nr = new TagNodeReader(tr, context);
    }

    public ProcessStreamContext(TagStreamProcessor tagStreamProcessor, Reader source, NExprDeclarations context) {
        this.context = context;
        this.tagStreamProcessor = tagStreamProcessor;
        tr = new TagTokenReader(tagStreamProcessor.startTag, tagStreamProcessor.endTag, tagStreamProcessor.escape,
                new NCharReader(source == null ? NullReader.INSTANCE : source));
        nr = new TagNodeReader(tr, context);
    }

    public TagStreamProcessor getStreamProcessor() {
        return tagStreamProcessor;
    }

    public TagNode next() throws IOException {
        List<TagNode> r = new ArrayList<>();
        while (true) {
            TagNode u = nr.next();
            if (u != null) {
                r.add(u);
            } else {
                break;
            }
        }
        return ListTagNode.of(r);
    }

    public void setVar(String varName, Object v) {
        context.getVar(varName).get().set(v, context);
    }

    public Object eval(String expr) {
        return context.parse(expr).get().eval(context).get();
    }

    public Object eval(NExprNode expr) {
        if (expr == null) {
            return null;
        }
        return expr.eval(context).get();
    }
}
