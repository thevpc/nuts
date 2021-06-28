package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExprNodeParser {

    public static final int STR_INTERP = -100;

    StreamTokenizer st;
    FileTemplater context;
    private List<ExprToken> tokens = new ArrayList<>();

//    public static void main(String[] args) {
//        ExprNodeParser e=new ExprNodeParser("a\na",new JTextSourceLogImpl("test"));
//        while(true){
//            ExprToken t = e.nextToken();
//            if(t!=null){
//                System.out.println(t);
//            }
//        }
//    }
    public ExprNodeParser(String r, FileTemplater context) {
        this(new StringReader(r), context);
    }

    public ExprNodeParser(Reader r, FileTemplater context) {
        st = new StreamTokenizer(r);
        st.ordinaryChar('\n');
        st.quoteChar('`');
        st.commentChar('#');
        this.context = context;
    }

    public FileTemplater getContext() {
        return context;
    }

    private ExprNode parseExpression() {
        ExprToken w = nextToken();
        if (w == null) {
            context.getLog().error("set statement", "missing expression");
            return null;
        }
        switch (w.ttype) {
            case StreamTokenizer.TT_WORD: {
                ExprToken t = nextToken();
                if (t != null && t.ttype == '(') {
                    String fctName = (String) w.value;
                    List<ExprNode> args = new ArrayList<>();
                    ExprNode arg0 = parseExpression();
                    if (arg0 == null) {
                        return null;
                    }
                    args.add(arg0);
                    while (true) {
                        ExprToken nt = peekToken();
                        if (nt == null) {
                            context.getLog().error("function", "missing argument for "+fctName+"("+args.stream().map(x->x.toString()).collect(Collectors.joining(","))+")");
                            nextToken();//skip!
                        } else if (nt.ttype == ',') {
                            nextToken();//consume
                            arg0 = parseExpression();
                            if (arg0 == null) {
                                return null;
                            }
                            args.add(arg0);
                        } else if (nt.ttype == ')') {
                            nextToken();//consume
                            break;
                        } else {
                            context.getLog().error("function", "missing ',' or ')'");
                        }
                    }
                    return new ExprNodeFunction(fctName, args.toArray(new ExprNode[0]));
                } else if (t != null && t.ttype == '=') {
                    String varName = (String) w.value;
                    ExprNode arg0 = parseExpression();
                    if (arg0 == null) {
                        return null;
                    }
                    return new ExprNodeFunction("set", new ExprNode[]{new ExprNodeLiteral("\"", varName), arg0});
                } else {
                    pushBack(t);
                    return new ExprNodeVar((String) w.value);
                }
            }
            case StreamTokenizer.TT_NUMBER: {
                return new ExprNodeLiteral("number", w.value);
            }
            case '\"':
            case '\'':
            case '`': {
                return new ExprNodeLiteral(String.valueOf((char) w.ttype), w.value);
            }
            case STR_INTERP: {
                return new ExprNodeLiteral("$\"", w.value);
            }
            default: {
                context.getLog().error("expression", "invalid token " + w);
                return null;
            }
        }
    }

    private boolean parseColonOrNewLine(boolean required) {
        ExprToken w = nextToken();
        if (w != null && (w.ttype == ';' || w.ttype == '\n')) {
            while (true) {
                w = peekToken();
                if (w != null && (w.ttype == ';' || w.ttype == '\n')) {
                    nextToken();
                } else {
                    break;
                }
            }
            return true;
        } else {
            if (required) {
                context.getLog().error("expression", "expected ';' or new line, encountered '" + w + "'");
            }
            if (w != null) {
                pushBack(w);
            }
            return false;
        }
    }

    private String parseWord() {
        ExprToken w = nextToken();
        if (w != null && w.ttype == StreamTokenizer.TT_WORD) {
            return (String) w.value;
        } else {
            context.getLog().error("set statement", "missing word");
            readUntilNextStatement();
            return null;
        }
    }

    private boolean moreTokens() {
        return peekToken() != null;
    }

    public ExprNode parseDocument() {
        if (moreTokens()) {
            parseColonOrNewLine(false);
            List<ExprNode> exprs = new ArrayList<>();
            ExprNode e = parseExpression();
            if (e == null) {
                return null;
            }
            exprs.add(e);
            while (true) {
                if (!moreTokens()) {
                    break;
                }
                if (!parseColonOrNewLine(true)) {
                    return null;
                }
                if (!moreTokens()) {
                    break;
                }
                e = parseExpression();
                if (e == null) {
                    return null;
                }
                exprs.add(e);
            }
            return new ExprNodeFunction(";", exprs.toArray(new ExprNode[0]));
        } else {
            return new ExprNodeFunction(";", new ExprNode[0]);
        }
    }

    private void readUntilNextStatement() {
        while (true) {
            int t = 0;
            try {
                t = st.nextToken();
            } catch (IOException e) {
                return;
            }
            if (t == ';') {
                return;
            }
        }
    }

    private void pushBack(ExprToken st) {
        if (st != null) {
            tokens.add(0, st);
        }
    }

    private ExprToken peekToken() {
        if (!tokens.isEmpty()) {
            return tokens.get(0);
        }
        ExprToken t = nextToken();
        if (t != null) {
            pushBack(t);
        }
        return t;
    }

    private ExprToken nextToken() {
        if (!tokens.isEmpty()) {
            return tokens.remove(0);
        }
        while (true) {
            int ttype = -1;
            int t = 0;
            try {
                t = st.nextToken();
            } catch (IOException e) {
                return null;
            }
            switch (t) {
                case StreamTokenizer.TT_EOF: {
                    return null;
                }
                case StreamTokenizer.TT_NUMBER: {
                    return new ExprToken(t, st.nval);
                }
                case StreamTokenizer.TT_WORD:
                case '\'':
                case '\"':
                case '`': {
                    return new ExprToken(t, st.sval);
                }
                case '$': {
                    ExprToken old = new ExprToken(t, String.valueOf((char) t));
                    ExprToken nt = nextToken();
                    if (nt == null) {
                        return old;
                    }
                    if (nt.ttype == '\"') {
                        return new ExprToken(STR_INTERP, st.sval);
                    }
                    pushBack(nt);
                    return old;
                }

                default: {
                    //ignore
                    return new ExprToken(t, String.valueOf((char) t));
                }
            }
        }

    }

}
