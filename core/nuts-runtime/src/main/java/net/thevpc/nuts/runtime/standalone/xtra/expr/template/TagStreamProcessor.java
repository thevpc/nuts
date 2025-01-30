package net.thevpc.nuts.runtime.standalone.xtra.expr.template;


import net.thevpc.nuts.expr.NExprCompiledTemplate;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.util.NBlankable;

import java.io.*;

/**
 * something
 * ${:if }
 * ${:else if }
 * ${:else}
 * ${:end}
 * ${:for}
 */
public class TagStreamProcessor implements NTemplateProcessor {
    public static final TagStreamProcessor DOLLAR = new TagStreamProcessor("${", "}");
    public static final TagStreamProcessor DOLLAR_BARACKET2 = new TagStreamProcessor("${{", "}}");
    public static final TagStreamProcessor BARACKET2 = new TagStreamProcessor("{{", "}}");
    public static final TagStreamProcessor LT_PERCENT = new TagStreamProcessor("<%", "%>");
    protected String startTag;
    protected String endTag;
    protected String escape;

    public TagStreamProcessor(String startTag, String endTag) {
        this.startTag = startTag;
        this.endTag = endTag;
        this.escape = "\\" + this.startTag;
    }

    public TagStreamProcessor(String startTag, String endTag, String escape) {
        if (NBlankable.isBlank(escape)) {
            escape = "\\";
        }
        this.startTag = startTag;
        this.endTag = endTag;
        this.escape = escape + this.startTag;
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

    public NExprCompiledTemplate compile(InputStream inputStream, NExprDeclarations context) {
        ProcessStreamContext ctx = new ProcessStreamContext(this, inputStream, context);
        TagNode n = null;
        try {
            n = ctx.next();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new MyNExprCompiledTemplate(ctx, n);
    }

    public NExprCompiledTemplate compile(Reader reader, NExprDeclarations context) {
        ProcessStreamContext ctx = new ProcessStreamContext(this, reader, context);
        TagNode n = null;
        try {
            n = ctx.next();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new MyNExprCompiledTemplate(ctx, n);
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NExprDeclarations context) {
        compile(source, context).run(target);
    }

    @Override
    public void processStream(Reader source, Writer target, NExprDeclarations context) {
        compile(source, context).run(target);
    }


    @Override
    public String toString() {
        return "Replace(" + startTag + "..." + endTag + ")";
    }

}
