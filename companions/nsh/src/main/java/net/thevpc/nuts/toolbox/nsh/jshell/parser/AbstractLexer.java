package net.thevpc.nuts.toolbox.nsh.jshell.parser;

public abstract class AbstractLexer implements Lexer {
    @Override
    public boolean skipWhites() {
        boolean some=false;
        while (true) {
            Token t = peekToken();
            if (t == null) {
                return some;
            }
            if (!t.type.equals("WHITE")) {
                return some;
            }
            some=true;
            nextToken();
        }
    }

}
