package net.thevpc.nuts.toolbox.nsh.parser;

public interface Lexer {
    boolean skipWhites();

    Token peekToken();

    Token nextToken();
}
