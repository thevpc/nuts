package net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser;

public interface Lexer {
    boolean skipWhites();

    Token peekToken();

    Token nextToken();
}
