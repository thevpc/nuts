package net.vpc.toolbox.worky.fileprocessors.nodes;

import net.vpc.common.textsource.log.JTextSourceLog;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ExprNodeParser {
    StreamTokenizer st;
    JTextSourceLog log;
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
    public ExprNodeParser(String r, JTextSourceLog log) {
        this(new StringReader(r), log);
    }

    public ExprNodeParser(Reader r, JTextSourceLog log) {
        st = new StreamTokenizer(r);
        st.ordinaryChar('\n');
        this.log = log;
    }

    private ExprNode parseExpression() {
        ExprToken w = nextToken();
        if (w == null) {
            log.error("X000", "set statement", "missing expression", null);
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
                            log.error("X000", "function", "missing argument", null);
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
                            log.error("X000", "function", "missing ',' or ')'", null);
                        }
                    }
                    return new ExprNodeFunction(fctName, args.toArray(new ExprNode[0]));
                } else if (t != null && t.ttype == '=') {
                    String varName = (String) w.value;
                    ExprNode arg0 = parseExpression();
                    if (arg0 == null) {
                        return null;
                    }
                    return new ExprNodeFunction("set", new ExprNode[]{new ExprNodeVar(varName), arg0});
                } else {
                    pushBack(t);
                    return new ExprNodeVar((String) w.value);
                }
            }
            case StreamTokenizer.TT_NUMBER:
            case '\"':
            case '\'': {
                return new ExprNodeLiteral(w.value);
            }
            default: {
                log.error("X000", "expression", "invalid token " + w, null);
                return null;
            }
        }
    }

    private boolean parseColonOrNewLine(boolean required) {
        ExprToken w = nextToken();
        if (w != null && (w.ttype == ';' || w.ttype == '\n')) {
            while(true){
                w=peekToken();
                if (w != null && (w.ttype == ';' || w.ttype == '\n')) {
                    nextToken();
                }else{
                    break;
                }
            }
            return true;
        } else {
            if(required) {
                log.error("X000", "expression", "expected ';' or new line, encountered '" + w + "'", null);
            }
            if(w!=null) {
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
            log.error("X000", "set statement", "missing word", null);
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
                case '\"': {
                    return new ExprToken(t, st.sval);
                }
                default: {
                    //ignore
                    return new ExprToken(t, String.valueOf((char) ttype));
                }
            }
        }

    }
}
