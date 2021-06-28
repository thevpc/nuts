package net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.*;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.DirectoryScanner;

import java.util.*;

public class Yaccer {

    private final Lexer lexer;
    private final LinkedList<JShellNode> buffer = new LinkedList<>();

    public Yaccer(Lexer lexer) {
        this.lexer = lexer;
    }


    public Iterable<JShellNode> nodes() {
        return () -> new Iterator<JShellNode>() {
            JShellNode n = null;

            @Override
            public boolean hasNext() {
                n = readNode();
                return n != null;
            }

            @Override
            public JShellNode next() {
                return n;
            }
        };
    }

    public JShellNode readNodeL0() {
        if (!buffer.isEmpty()) {
            return buffer.removeFirst();
        }
        Token u = getLexer().peekToken();
        if (u == null) {
            return null;
        }
        switch (u.type) {
            case "WHITE": {
                getLexer().skipWhites();
                return new WhiteNode(u);
            }
            case "NEWLINE": {
                u = getLexer().nextToken();
                return new NewlineNode(u);
            }
            case "#": {
                return readComments();
            }
            case "WORD":
            case "$WORD":
            case "\"":
            case "'":
            case "`":
            case "$(":
            case "$((":
            case "${":
            case "{":
            case "=":
            case ":":
            case "&&":
            case "&":
            case "|":
            case "||":
            case "<":
            case "<<":
            case ">":
            case ">>":
            case "&>":
            case "&>>":
            case "&<":
            case "&<<":
            case "*":
            case "?": {
                return new TokenNode(getLexer().nextToken());
            }
            case "(": {
                return readScriptPar();
            }
            default: {
                return new TokenNode(getLexer().nextToken());
            }
        }
    }

    private Lexer getLexer() {
        return lexer;
    }

    private JShellCommandNode readScriptL1() {
        Token u = getLexer().peekToken();
        if (u == null) {
            return null;
        }
        while (true) {
            Token not = getLexer().peekToken();
            if (not != null && (not.isNewline() || not.isEndCommand())) {
                getLexer().nextToken();
            } else {
                break;
            }
        }
        if (u.type.equals("!")) {
            Token not = getLexer().nextToken();
            JShellCommandNode next = readScriptL1();
            return new UnOpPrefix(not, next);
        }
        if (u.type.equals("(")) {
            return readScriptPar();
        }
        if (u.type.equals("#")) {
            Comments c = readComments();
            JShellCommandNode next = readScriptL1();
            return new CommentedNode(next, c);
        }
        JShellCommandNode a = readScriptLine();
        if (a == null) {
            return a;
        }
        u = getLexer().peekToken();
        if (u == null) {
            return a;
        }
        switch (u.type) {
            case "&&":
            case "||": {
                Token op = getLexer().nextToken();
                JShellNode b = readScriptLine();
                if (b == null) {
                    return new UnOpSuffix(a, op);
                }
                return new BinOp(a, op, b);
            }
        }
        return a;
    }

    public JShellCommandNode readScriptL2() {
        JShellCommandNode a = readScriptL1();
        if (a == null) {
            return null;
        }
        while (true) {
            Token u = getLexer().peekToken();
            if (u == null) {
                return a;
            }
            switch (u.type) {
                case "|": {
                    Token op = getLexer().nextToken();
                    JShellCommandNode b = readScriptL1();
                    if (b == null) {
                        return new UnOpSuffix(a, op);
                    } else {
                        a = new BinOp(a, op, b);
                    }
                    break;
                }
                default: {
                    return a;
                }
            }
        }
    }

    public JShellCommandNode readScriptL3() {
        JShellCommandNode a = readScriptL2();
        if (a == null) {
            return null;
        }
        while (true) {
            Token u = getLexer().peekToken();
            if (u == null) {
                return a;
            }
            switch (u.type) {
                case "&": {
                    Token op = getLexer().nextToken();
                    JShellCommandNode b = readScriptL2();
                    if (b == null) {
                        return new UnOpSuffix(a, op);
                    } else {
                        a = new BinOp(a, op, b);
                    }
                    break;
                }
                default: {
                    return a;
                }
            }
        }
    }

    public JShellCommandNode readScriptL4() {
        JShellCommandNode a = readScriptL3();
        if (a == null) {
            return null;
        }
        while (true) {
            Token u = getLexer().peekToken();
            if (u == null) {
                return a;
            }
            switch (u.type) {
                case ">":
                case ">>":
                case "<":
                case "<<":
                case "&>":
                case "&2>":
                case "&>>":
                case "&2>>": {
                    Token op = getLexer().nextToken();
                    JShellNode b = readScriptL3();
                    if (b == null) {
                        return new UnOpSuffix(a, op);
                    } else {
                        a = new BinOp(a, op, b);
                    }
                    break;
                }
                default: {
                    return a;
                }
            }
        }
    }

    public JShellCommandNode readScriptL5() {
        JShellCommandNode a = null;
        Token sep = null;
        while (true) {
            Token u = getLexer().peekToken();
            if (u == null) {
                return a;
            }
            switch (u.type) {
                case ";":
                case "NEWLINE": {
                    sep = getLexer().nextToken();
                    break;
                }
                default: {
                    JShellCommandNode b = readScriptL4();
                    if (b == null) {
                        return a;
                    }
                    if (a == null) {
                        a = b;
                    } else {
                        if (sep == null) {
                            sep = new Token("NEWLINE", "\n");
                        }
                        a = new BinOpCommand(a, sep, b);
                    }
                    sep = null;
                    break;
                }
            }
        }
    }

    public JShellNode readNodeL1() {
        JShellNode a = readNodeL0();
        if (a == null) {
            return a;
        }
        Token u = getLexer().peekToken();
        if (u == null) {
            return a;
        }
        switch (u.type) {
            case "&&":
            case "&": {
                Token op = getLexer().nextToken();
                JShellNode b = readNodeL0();
                if (b == null) {
                    return new UnOpSuffix(a, op);
                }
                return new BinOp(a, op, b);
            }
        }
        return a;
    }

    public JShellNode readNodeL2() {
        JShellNode a = readNodeL1();
        if (a == null) {
            return a;
        }
        Token u = getLexer().peekToken();
        if (u == null) {
            return a;
        }
        switch (u.type) {
            case "||":
            case "|": {
                Token op = getLexer().nextToken();
                JShellNode b = readNodeL1();
                if (b == null) {
                    return new UnOpSuffix(a, op);
                }
                return new BinOp(a, op, b);
            }
        }
        return a;
    }

    public JShellNode readNodeL3() {
        JShellNode a = readNodeL1();
        if (a == null) {
            return a;
        }
        Token u = getLexer().peekToken();
        if (u == null) {
            return a;
        }
        switch (u.type) {
            case ";": {
                Token op = getLexer().nextToken();
                JShellNode b = readNodeL1();
                if (b == null) {
                    return new UnOpSuffix(a, op);
                }
                return new BinOp(a, op, b);
            }
        }
        return a;
    }

    public JShellNode readNodeL4() {
        JShellNode a = readNodeL3();
        if (a == null) {
            return a;
        }
        Token u = getLexer().peekToken();
        if (u == null) {
            return a;
        }
        switch (u.type) {
            case ">":
            case "&>":
            case "<":
            case "&<": {
                Token op = getLexer().nextToken();
                JShellNode b = readNodeL3();
                if (b == null) {
                    return new UnOpSuffix(a, op);
                }
                return new BinOp(a, op, b);
            }
        }
        return a;
    }

    public JShellNode readNodeL5() {
        JShellNode a = readNodeL4();
        if (a == null) {
            return null;
        }

        Token u = getLexer().peekToken();
        if (u == null) {
            return a;
        }
        switch (u.type) {
            case ";": {
                Token op = getLexer().nextToken();
                JShellNode b = readNodeL4();
                if (b == null) {
                    return new UnOpSuffix(a, op);
                }
                return new BinOp(a, op, b);
            }
        }
        return a;
    }


    public JShellNode readNode() {
//        return readNodeL5();
        return readNodeL0();
    }

    public JShellCommandNode readScriptPar() {
        Token u = getLexer().peekToken();
        if (u == null) {
            return null;
        }
        if (u.type.equals("(")) {
            getLexer().nextToken();
            JShellNode n = readScript();
            u = getLexer().peekToken();
            if (u == null || u.type.equals(")")) {
                if (u != null) {
                    getLexer().nextToken();
                }
                return new Par(n);
            }
            return new Par(n);
        }
        return null;
    }

    public JShellCommandNode readCommandL1() {
        getLexer().skipWhites();
        Token t = getLexer().peekToken();
        if (t == null) {
            return null;
        }
        JShellCommandNode line = readScriptLine();
        if (line == null) {
            return null;
        }
        boolean loop = true;
        while (loop) {
            loop = false;
            t = getLexer().peekToken();
            if (t != null) {
                switch (t.type) {
                    case "<":
                    case ">":
                    case "<<":
                    case ">>":
                    case "&<":
                    case "&>":
                    case "&<<":
                    case "&>>": {
                        getLexer().nextToken();
                        JShellCommandNode next = readScriptLine();
                        if (next == null) {
                            line = new SuffixOpCommand(line, t);
                        } else {
                            line = new BinOpCommand(line, t, next);
                            loop = true;
                        }
                        break;
                    }
                }
            }
        }
        return line;
    }

    public JShellCommandNode readCommandL2() {
        JShellCommandNode line = readCommandL1();
        if (line == null) {
            return null;
        }
        boolean loop = true;
        while (loop) {
            loop = false;
            Token t = getLexer().peekToken();
            if (t != null) {
                switch (t.type) {
                    case "|": {
                        getLexer().nextToken();
                        JShellCommandNode next = readCommandL1();
                        if (next == null) {
                            line = new SuffixOpCommand(line, t);
                        } else {
                            line = new BinOpCommand(line, t, next);
                            loop = true;
                        }
                    }
                }
            }
        }
        return line;
    }

    public JShellCommandNode readCommandL3() {
        JShellCommandNode line = readCommandL2();
        if (line == null) {
            return null;
        }
        boolean loop = true;
        while (loop) {
            loop = false;
            Token t = getLexer().peekToken();
            if (t != null) {
                switch (t.type) {
                    case "&&":
                    case "||": {
                        getLexer().nextToken();
                        JShellCommandNode next = readCommandL2();
                        if (next == null) {
                            line = new SuffixOpCommand(line, t);
                        } else {
                            line = new BinOpCommand(line, t, next);
                            loop = true;
                        }
                    }
                }
            }
        }
        return line;
    }

    private String getArgumentsLineFirstArgToken(JShellCommandNode line) {
        if (line != null) {
            Argument arg1 = ((ArgumentsLine) line).args.get(0);
            if (arg1.nodes.size() == 1 && arg1.nodes.get(0) instanceof TokenNode) {
                Token token = ((TokenNode) arg1.nodes.get(0)).token;
                if (token.type.equals("WORD")) {
                    return token.value.toString();
                }
            }
        }
        return "";
    }

    public ArgumentsLine readScriptLine() {
        List<Argument> a = new ArrayList<>();
        while (true) {
            Token t = getLexer().peekToken();
            if (t == null) {
                break;
            }
            boolean exit = false;
            switch (t.type) {
                case "NEWLINE":
                case ";":
                case "&":
                case "&&":
                case "<<":
                case ">>":
                case "&<":
                case "&>":
                case "&<<":
                case "&>>":
                case "|":
                case "||": {
                    exit = true;
                    break;
                }
            }
            if (exit) {
                break;
            }
            if (t.type.equals("WHITE")) {
                getLexer().nextToken();
                //ignore
            } else {
                Argument aa = readArgument();
                if (aa != null) {
                    a.add(aa);
                } else {
                    throw new IllegalArgumentException("Unexpected " + aa);
                }
            }
        }
        if (a.isEmpty()) {
            return null;
        }
        return new ArgumentsLine(a);
    }

    public JShellCommandNode readScript() {
        return readScriptL5();
    }

    public Comments readComments() {
        List<Token> ok = new ArrayList<>();
        while (true) {
            Token t = getLexer().peekToken();
            if (t == null) {
                break;
            }
            if (t.type.equals("#")) {
                getLexer().nextToken();
                ok.add(t);
            } else {
                break;
            }
        }
        if (ok.isEmpty()) {
            return null;
        }
        return new Comments(ok);
    }

    public Argument readArgument() {
        List<JShellNode> a = new ArrayList<>();
        while (true) {
            Token t = getLexer().peekToken();
            if (t == null || t.type.equals("NEWLINE") || t.type.equals(";")) {
                break;
            }
            if (t.type.equals("#")) {
                if (!a.isEmpty()) {
                    break;
                }
            }
            JShellNode n = readNode();
            if (n == null) {
                break;
            }
            if (n instanceof WhiteNode) {
                break;
            }
            a.add(n);
        }
        if (a.isEmpty()) {
            return null;
        }
        return new Argument(a);
//        List<Token> ok = new ArrayList<>();
//        boolean loop = true;
//        while (loop) {
//            Token t = jShellParser2.lexer().peedToken();
//            if (t == null) {
//                break;
//            }
//            switch (t.type) {
//                case "WORD":
//                case "$WORD":
//                case "$(":
//                case "$((":
//                case "${":
//                case "(":
//                case "{":
//                case "=":
//                case ":":
//                case "\"":
//                case "'":
//                case "`": {
//                    jShellParser2.lexer().nextToken();
//                    ok.add(t);
//                    break;
//                }
//                default: {
//                    loop = false;
//                    break;
//                }
//            }
//        }
//        if (ok.isEmpty()) {
//            return null;
//        }
//        return new Argument(ok);
    }

    public static String evalTokenString(Token token, JShellFileContext context) {
        switch (token.type) {
            case "WORD": {
                return token.value.toString();
            }
            case "$WORD": {
                String s = (String) token.value;
                switch (s) {
                    case "0": {
                        return DirectoryScanner.escape(context.getServiceName());
                    }
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                    case "8":
                    case "9": {
                        return DirectoryScanner.escape(context.getArg(Integer.parseInt(s) - 1));
                    }
                    case "?": {
                        return DirectoryScanner.escape(String.valueOf(context.getArgsCount()));
                    }
                    default: {
                        String y = context.vars().get(s,"");
                        return DirectoryScanner.escape(y);
                    }
                }
            }
            case "`":
            case "$(": {
                List<Token> subTokens = new ArrayList<>((Collection<? extends Token>) token.value);
                if (subTokens.isEmpty()) {
                    return "";
                }
                Yaccer yy2 = new Yaccer(new PreloadedLexer(subTokens));
                JShellCommandNode subCommand = yy2.readScript();
                if (subCommand == null) {
                    //all are comments perhaps!
                    return "";
                }
                return DirectoryScanner.escape(context.getShell().getEvaluator().evalCommandAndReturnString(subCommand, context));
            }
            case "\"": {
                List<Token> s = (List<Token>) token.value;
                StringBuilder sb = new StringBuilder();
                for (Token token2 : s) {
                    sb.append(evalTokenString(token2, context));
                }
                return sb.toString();
            }
            case "'": {
                if (token.value instanceof String) {
                    return DirectoryScanner.escape((String) token.value);
                }
                StringBuilder sb = new StringBuilder();
                for (Token t : ((List<Token>) token.value)) {
                    sb.append(DirectoryScanner.escape(evalTokenString(t, context)));
                }
                return sb.toString();
            }
            case "STR": {
                return (String) token.value;
            }
            case "${": {
                StringBuilder sb = new StringBuilder();
                //TODO fix me, should implement ${...} expressions
                List<Token> values = (List<Token>) token.value;
                if(values.isEmpty()){
                    throw new IllegalArgumentException("bad substitution");
                }
                String varVal="";
                Token t = values.get(0);
                if(t.isWord()) {
                    String y = context.vars().get(evalTokenString(t,context),null);
                    if (y != null) {
                        varVal = y;
                    }
                }else{
                    throw new IllegalArgumentException("bad substitution");
                }
                sb.append(DirectoryScanner.escape(varVal));
                return sb.toString();
            }
            default: {
                return (String) token.value;
            }
        }
    }


    public static String evalNodeString(JShellNode node, JShellFileContext context) {
        if (node instanceof Comments) {
            return "";
        } else if (node instanceof TokenNode) {
            return ((TokenNode) node).evalString(context);
        }
        throw new RuntimeException("Error");
    }

    public class WhiteNode implements JShellNode {
        Token token;

        public WhiteNode(Token token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return String.valueOf(token);
        }
    }

    public class NewlineNode implements JShellNode {
        Token token;

        public NewlineNode(Token token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return String.valueOf(token);
        }
    }

    public static class TokenNode implements JShellNode {
        Token token;

        public TokenNode(Token token) {
            this.token = token;
        }

        public String evalString(JShellFileContext context) {
            return evalTokenString(token, context);
        }

        @Override
        public String toString() {
            return String.valueOf(token);
        }
    }

    public static class BinOpCommand implements JShellCommandNode {
        JShellCommandNode left;
        Token op;
        JShellCommandNode right;

        public BinOpCommand(JShellCommandNode left, Token op, JShellCommandNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public void eval(final JShellFileContext context) {
            String cmd = op.type.equals("NEWLINE") ? ";" : String.valueOf(op.value);
            context.getShell().getEvaluator().evalBinaryOperation(cmd, left, right, context);
        }

        @Override
        public String toString() {
            return "(" +
                    left +
                    " " + op.value +
                    right +
                    ')';
        }
    }

    public class SuffixOpCommand implements JShellCommandNode {
        JShellCommandNode a;
        Token op;

        public SuffixOpCommand(JShellCommandNode a, Token op) {
            this.a = a;
            this.op = op;
        }

        @Override
        public void eval(JShellFileContext context) {
            switch (op.type) {
                case "&": {
                    context.getShell().getEvaluator().evalSuffixAndOperation(a, context);
                    break;
                }
            }
            throw new IllegalArgumentException("Unsupported yet");
        }
    }

    public class CondBloc {
        JShellCommandNode cond;
        JShellCommandNode block;

        public CondBloc(JShellCommandNode cond, JShellCommandNode block) {
            this.cond = cond;
            this.block = block;
        }

        public boolean eval(JShellFileContext context) {
//        System.out.println("+ IF " + conditionNode);
            boolean trueCond = false;
            if (cond != null) {
                try {
                    context.getShell().uniformException(new JShellNodeUnsafeRunnable(cond, context));
                    trueCond = true;
                } catch (JShellUniformException ex) {
                    if (ex.isQuit()) {
                        ex.throwQuit();
                    }
                    trueCond = false;
                }
                if (trueCond) {
                    if (block != null) {
                        block.eval(context);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public class IfCommand implements JShellCommandNode {
        CondBloc _if;
        JShellCommandNode _then;
        List<CondBloc> _elif = new ArrayList<>();
        JShellCommandNode _else;

        @Override
        public void eval(JShellFileContext context) {
//        System.out.println("+ IF " + conditionNode);
            if (_if.eval(context)) {
                return;
            }
            for (CondBloc condBloc : _elif) {
                if (condBloc.eval(context)) {
                    return;
                }
            }
            if (_else != null) {
                _else.eval(context);
            }
        }

    }

    public class WhileCommand implements JShellCommandNode {
        CondBloc _while;
        JShellCommandNode _do;
        JShellCommandNode _done;

        @Override
        public void eval(JShellFileContext context) {
            while (true) {
                if (!_while.eval(context)) {
                    break;
                }
            }
        }
    }

    public static class ArgumentsLine implements JShellCommandLineNode {
        List<Argument> args;

        public ArgumentsLine(List<Argument> args) {
            this.args = args;
        }

        public List<Argument> getArgs() {
            return args;
        }

        @Override
        public Iterator<JShellArgumentNode> iterator() {
            return (Iterator) args.iterator();
        }

        @Override
        public void eval(JShellFileContext context) {
            JShell shell = context.getShell();
            ArrayList<String> cmds = new ArrayList<String>();
            Map<String, String> usingItems = new LinkedHashMap<>();
            List<Argument> args2 = new ArrayList<>(args);
            boolean source = false;
            if (args2.size() > 0) {
                Argument arg = args2.get(0);
                List<JShellNode> anodes = arg.nodes;
                if (anodes.size() == 1
                        && anodes.get(0) instanceof TokenNode && ((TokenNode) anodes.get(0)).token.type.equals(".")
                ) {
                    source = true;
                    args2.remove(0);
                }
            }
            if (!source) {
                while (args2.size() > 0) {
                    Argument arg = args2.get(0);
                    List<JShellNode> anodes = arg.nodes;
                    if (anodes.size() >= 2
                            && anodes.get(0) instanceof TokenNode && ((TokenNode) anodes.get(0)).token.type.equals("WORD")
                            && anodes.get(1) instanceof TokenNode && ((TokenNode) anodes.get(1)).token.type.equals("=")
                    ) {
                        String varName = ((TokenNode) anodes.get(0)).evalString(context);
                        String[] varValues = (anodes.size() > 2) ? new Argument(anodes.subList(2, anodes.size())).evalString(context) : new String[]{""};
                        usingItems.put(varName, String.join(" ", varValues));
                        args2.remove(0);
                    } else {
                        break;
                    }
                }
            }
            for (Argument arg : args2) {
                cmds.addAll(Arrays.asList(arg.evalString(context)));
            }
            if (source) {
                cmds.add(0, "source");
                shell.executePreparedCommand(cmds.toArray(new String[0]), true, true, true, context);
            } else {
                if (cmds.isEmpty() || (cmds.size() == 1 && cmds.get(0).isEmpty())) {
                    if (!usingItems.isEmpty()) {
                        context.vars().set((Map) usingItems);
                    }
                } else {
                    if (!usingItems.isEmpty()) {
                        context = shell.createNewContext(context);
                        context.vars().set(context.vars());
                        context.vars().set((Map) usingItems);
                    }
                    shell.executePreparedCommand(cmds.toArray(new String[0]), true, true, true, context);
                }
            }
        }

        @Override
        public String toString() {
            return "ArgumentsLine{" +
                    args +
                    '}';
        }
    }

    public class Par implements JShellCommandNode {
        JShellNode element;

        public Par(JShellNode element) {
            this.element = element;
        }

        @Override
        public void eval(JShellFileContext context) {
            ((JShellCommandNode) element).eval(context);
        }
    }

    public class UnOpSuffix implements JShellCommandNode {
        JShellNode a;
        Token op;

        public UnOpSuffix(JShellNode a, Token op) {
            this.a = a;
            this.op = op;
        }

        @Override
        public String toString() {
            return a +
                    " " + op;
        }

        @Override
        public void eval(JShellFileContext context) {
            throw new IllegalArgumentException("Not yet");
        }
    }

    public class CommentedNode implements JShellCommandNode {
        JShellNode a;
        List<Comments> comments = new ArrayList<>();

        public CommentedNode(JShellNode a, Comments comments) {
            if (a instanceof CommentedNode) {
                this.a = ((CommentedNode) a).a;
                this.comments.add(comments);
                this.comments.addAll(((CommentedNode) a).comments);
            } else {
                this.a = a;
                this.comments.add(comments);
            }
        }

        @Override
        public void eval(JShellFileContext context) {
            if (a != null) {
                ((JShellCommandNode) a).eval(context);
            }
        }
    }

    public class UnOpPrefix implements JShellCommandNode {
        JShellNode a;
        Token op;

        public UnOpPrefix(Token op, JShellNode a) {
            this.a = a;
            this.op = op;
        }

        @Override
        public String toString() {
            return op + " " + a;
        }

        @Override
        public void eval(JShellFileContext context) {
            throw new IllegalArgumentException("Not yet");
        }
    }

    public class BinOp implements JShellCommandNode {
        JShellNode a;
        Token op;
        JShellNode b;

        public BinOp(JShellNode a, Token op, JShellNode b) {
            this.a = a;
            this.op = op;
            this.b = b;
        }

        @Override
        public String toString() {
            return a +
                    " " + op +
                    " " + b;
        }

        @Override
        public void eval(JShellFileContext context) {
            throw new IllegalArgumentException("Not yet");
        }
    }

    public static class Argument implements JShellArgumentNode {
        List<JShellNode> nodes;

        public Argument(List<JShellNode> nodes) {
            this.nodes = nodes;
        }

        @Override
        public String toString() {
            if (nodes.size() == 1) {
                return nodes.get(0).toString();
            }
            return nodes.toString();
        }

        public String[] evalString(JShellFileContext context) {
            StringBuilder sb = new StringBuilder();
            for (JShellNode node : nodes) {
                sb.append(evalNodeString(node, context));
            }
            String value = sb.toString();
            boolean wasAntiSlash = false;
            boolean applyWildCard = false;
            StringBuilder sb2 = new StringBuilder();
            for (char c : value.toCharArray()) {
                if (wasAntiSlash) {
                    wasAntiSlash = false;
                    sb2.append(c);
                } else {
                    switch (c) {
                        case '\\': {
                            wasAntiSlash = true;
                            break;
                        }
                        case '*':
                        case '?': {
                            sb2.append(c);
                            applyWildCard = true;
                            break;
                        }
                        default: {
                            sb2.append(c);
                            break;
                        }
                    }
                }
            }
            if (applyWildCard) {
                DirectoryScanner d = new DirectoryScanner(
                        DirectoryScanner.PATH_FILE_SYSTEM.isAbsolute(value) ? value :
                                DirectoryScanner.PATH_FILE_SYSTEM.resolve(context.getCwd(), value)
                );
                String[] r = d.toArray();
                if(r.length>0){
                    return r;
                }
            }
            return new String[]{sb2.toString()};
        }
    }

    public class Comments implements JShellNode {
        List<Token> tokens;

        public Comments(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public String toString() {
            return "Comments{" +
                    tokens +
                    '}';
        }
    }

//    public void pushBack(JShellNode n){
//        buffer.addFirst(n);
//    }
}
