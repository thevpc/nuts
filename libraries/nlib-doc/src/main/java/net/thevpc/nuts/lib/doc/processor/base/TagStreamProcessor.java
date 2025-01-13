package net.thevpc.nuts.lib.doc.processor.base;

import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.processor.NDocStreamProcessor;

import java.io.*;

/**
 * something
 * ${:if }
 * ${:else if }
 * ${:else}
 * ${:end}
 * ${:for}
 */
public class TagStreamProcessor implements NDocStreamProcessor {
    String startTag;
    String endTag;
    String escape;
    String exprLang;

    public TagStreamProcessor(String startTag, String endTag, String exprLang) {
        this.startTag = startTag;
        this.endTag = endTag;
        this.escape = "\\" + this.startTag;
        this.exprLang = exprLang;
    }

    static boolean startsWithWord(String ss, String a) {
        if (ss.startsWith(a)) {
            if (ss.equals(a)) {
                return true;
            }
            char c = ss.charAt(a.length());
            if (
                    (c >= 'a' && c <= 'z')
                            || (c >= 'A' && c <= 'Z')
                            || (c >= '0' && c <= '9')
                            || (c == '_')
            ) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {
        try {
            ProcessStreamContext ctx = new ProcessStreamContext(this,source, context);
            ctx.out = new BufferedWriter(new OutputStreamWriter(target));
            TagNode n = ctx.next();
            if(n!=null) {
                n.run(ctx);
            }
            ctx.out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    @Override
    public String toString() {
        return "Replace(" + startTag + "..." + endTag + " as " + exprLang + ")";
    }

}
