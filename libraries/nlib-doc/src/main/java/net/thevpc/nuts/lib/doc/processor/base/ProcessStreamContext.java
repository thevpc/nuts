package net.thevpc.nuts.lib.doc.processor.base;

import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.util.NCharReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessStreamContext {
    final TagStreamProcessor tagStreamProcessor;
    TagTokenReader tr;
    TagNodeReader nr;
    NDocContext context;
    Writer out;
    InputStream source;

    public ProcessStreamContext(TagStreamProcessor tagStreamProcessor, InputStream source, NDocContext context) {
        this.context = context;
        this.tagStreamProcessor = tagStreamProcessor;
        tr = new TagTokenReader(tagStreamProcessor.startTag, tagStreamProcessor.endTag, tagStreamProcessor.escape, tagStreamProcessor.exprLang,
                new NCharReader(new InputStreamReader(source)));
        nr =new TagNodeReader(tr,tagStreamProcessor.exprLang);
    }

    public TagNode next() throws IOException {
        List<TagNode> r = new ArrayList<>();
        while (true) {
            TagNode u = nr.next();
            if (u != null) {
                r.add(u);
            }else {
                break;
            }
        }
        return ListAstNode.of(r);
    }

}
