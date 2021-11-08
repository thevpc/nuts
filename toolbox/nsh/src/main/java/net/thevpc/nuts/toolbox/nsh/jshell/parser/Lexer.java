package net.thevpc.nuts.toolbox.nsh.jshell.parser;

public interface Lexer {
    boolean skipWhites();

    Token peekToken();

    Token nextToken();
}
