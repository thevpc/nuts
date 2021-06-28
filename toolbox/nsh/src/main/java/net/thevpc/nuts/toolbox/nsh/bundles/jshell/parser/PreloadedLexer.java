package net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser;

import java.util.Collection;
import java.util.LinkedList;

public class PreloadedLexer extends AbstractLexer {
    private LinkedList<Token> tokens=new LinkedList<>();

    public PreloadedLexer(Collection<Token> tokens) {
        this.tokens = new LinkedList<>(tokens);
    }

    @Override
    public Token peekToken() {
        return tokens.peekFirst();
    }

    @Override
    public Token nextToken() {
        return tokens.removeFirst();
    }
}
