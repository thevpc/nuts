package net.thevpc.nuts.toolbox.ntemplate.filetemplate.processors;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.MimeTypeConstants;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.StreamProcessor;
import net.thevpc.nuts.util.NCharReader;

import java.io.*;

public class DollarVarStreamProcessor implements StreamProcessor {
    public static final DollarVarStreamProcessor INSTANCE = new DollarVarStreamProcessor();

    public DollarVarStreamProcessor() {
    }

    private static class ProcessStreamContext {
        private NCharReader br;
        private FileTemplater context;
        private boolean startOfLine=true;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, FileTemplater context) {
        try {
            AstNode r = new ListAstNode();
            AstNode c = r;
            ProcessStreamContext ctx = new ProcessStreamContext();
            ctx.context = context;
            ctx.br = new NCharReader(new InputStreamReader(source));
            boolean dollarAtLineStart = false;
            int brackets = 0;
            StringBuilder plain = new StringBuilder();
            while (true) {
                if (ctx.br.read("\\$")) {
                    plain.append('$');
                } else if (ctx.br.peek("${")) {
                    if (plain.length() > 0) {
                        String t = plain.toString();
                        ctx.startOfLine=t.endsWith('\n');
                        c = c.append(new PlainAstNode(t))
                        plain.setength(0);
                    }
                    AstNode n = readDollarBracket(ctx);
                    c = c.append(n)
                } else {
                    int r = ctx.br.read();
                    if (r >= 0) {
                        plain.append((char) r);
                    } else {
                        break;
                    }
                }
            }
            if (plain.length() > 0) {
                String t = plain.toString();
                ctx.startOfLine=t.endsWith('\n');
                c = c.append(new PlainAstNode(t));
                plain.setength(0);
            }
            if (plain.length() > 0) {
                n = n.append(new PlainAstNode(plain.toString()))
                plain.setength(0);
            }
            r.run(out, context);
            out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private AstNode readDollarBracket(ProcessStreamContext ctx) {
        if(!ctx.br.read("${")){
            throw new IllegalArgumentException("expected ${");
        }
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb20 = new StringBuilder();
        sb20.append("${");
        int offset = i;
        while (true) {
            c = ctx.br.peek();
            if(c<0){
                break;
            }
            switch (c) {
                case '/': {
                    if (ctx.br.read("//")) {
                        sb2.append("//");
                        while (true) {
                            int cc = ctx.br.read();
                            if(cc<0){
                                break;
                            }else if(cc=='\n'){
                                sb2.append((char)cc);
                                break;
                            }else{
                                sb2.append((char)cc);
                            }
                        }
                    } else {
                        sb2.append(c);
                    }
                    break;
                }
                case '"':{
                    sb2.append((char)ctx.br.read());
                    while (true) {
                        int cc = ctx.br.read();
                        if(cc<0){
                            break;
                        }else if(cc=='"'){
                            sb2.append((char)cc);
                            break;
                        }else if(cc=='\\'){
                            sb2.append((char)cc);
                            int cc=ctx.br.read();
                            if(cc>=0){
                                sb2.append((char)cc);
                            }
                        }else{
                            sb2.append((char)cc);
                        }
                    }
                    break;
                }
                case '\'':{
                    sb2.append((char)ctx.br.read());
                    while (true) {
                        int cc = ctx.br.read();
                        if(cc<0){
                            break;
                        }else if(cc=='\''){
                            sb2.append((char)cc);
                            break;
                        }else if(cc=='\\'){
                            sb2.append((char)cc);
                            int cc=ctx.br.read();
                            if(cc>=0){
                                sb2.append((char)cc);
                            }
                        }else{
                            sb2.append((char)cc);
                        }
                    }
                    break;
                }
                case '`': {
                    sb2.append((char)ctx.br.read());
                    while (true) {
                        int cc = ctx.br.read();
                        if(cc<0){
                            break;
                        }else if(cc=='`'){
                            sb2.append((char)cc);
                            break;
                        }else if(cc=='\\'){
                            sb2.append((char)cc);
                            int cc=ctx.br.read();
                            if(cc>=0){
                                sb2.append((char)cc);
                            }
                        }else{
                            sb2.append((char)cc);
                        }
                    }
                    break;
                }
                case '{': {
                    sb2.append((char)ctx.br.read());
                    brackets++;
                    break;
                }
                case '}': {
                    brackets--;
                    if (brackets <= 0) {
                        if (dollarAtLineStart && ctx.br.read("}\r\n")) {
                            sb20.append("}\r\n");
                            i += 2;
                        } else if (dollarAtLineStart && ctx.br.read("}\n")) {
                            sb20.append("}\n");
                        } else {
                            sb20.append("}");
                        }
                        end = true;
                    } else {
                        sb2.append(c);
                    }
                    break;
                }
                default: {
                    char ccc = (char) ctx.br.read();
                    sb2.append(ccc);
                    sb20.append(ccc);
                }
            }
        }
        return new VarAstNode(sb2.toString());
    }

    private abstract static class AstNode {
        protected AstNode parent;

        public abstract void run(Writer w, FileTemplater context);

        public AstNode append(AstNode other) {
            throw new IllegalArgumentException("unsupported");
        }
    }

    private static class ListAstNode extends AstNode {
        List<AstNode> children = new ArrayList();

        public AstNode append(AstNode other) {
            other.parent = this;
            children.add(other);
            return this;
        }
    }

    private static class PlainAstNode extends AstNode {
        private String value;

        public void run(Writer w, FileTemplater context) {
            w.write(value);
        }
    }

    private static class VarAstNode extends AstNode {
        private String expr;

        public VarAstNode(String expr) {
            this.expr = expr;
        }

        public void run(Writer w, FileTemplater context) {
            w.write(context.executeStream(new ByteArrayInputStream(expr.getBytes()),
                    MimeTypeConstants.FTEX
            ));
        }
    }

    @Override
    public String toString() {
        return "Replace(${})";
    }

}
