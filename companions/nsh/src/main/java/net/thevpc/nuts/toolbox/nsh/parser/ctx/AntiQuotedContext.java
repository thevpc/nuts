package net.thevpc.nuts.toolbox.nsh.parser.ctx;

import net.thevpc.nuts.toolbox.nsh.parser.AbstractContext;
import net.thevpc.nuts.toolbox.nsh.parser.JShellParser;
import net.thevpc.nuts.toolbox.nsh.parser.StrReader;
import net.thevpc.nuts.toolbox.nsh.parser.Token;

public class AntiQuotedContext extends AbstractContext {


    public AntiQuotedContext(JShellParser jshp) {
        super(jshp);
    }

    @Override
    public Token nextToken() {
        StrReader reader = this.reader.strReader();
        int r = reader.peekChar();
        if (r < 0) {
            return null;
        }
        char rc=(char)r;
        if (rc == '`') {
            reader.read();
            return null;
        }
        if (rc <= 32) {
            return this.reader.lexer().continueReadWhite();
        }
        if (reader.readString("$((")) {
            return this.reader.lexer().processContext("$((" ,new DollarPar2Context(this.reader));
        }
        if (reader.readString("((")) {
            return this.reader.lexer().processContext("((" ,new Par2Context(this.reader));
        }
        if (reader.readString("$(")) {
            return this.reader.lexer().processContext("$(" ,new DollarParContext(this.reader));
        }
        if (reader.readString("${")) {
            return this.reader.lexer().processContext("${" ,new DollarCurlBracketContext(this.reader));
        }
        if (rc == '$') {
            return this.reader.lexer().continueReadDollarWord();
        }
        for (String s : new String[]{
                "&>>",
                "&>>",
                "&1>>",
                "&2>>",
                "&1>",
                "&2>",
                "&<",
                "<<<",
                "<<",
                ">>>",
                ">>",
                ">=",
                "<=",
                "==>",
                "==",
                "=",
                "||",
                "|&",
                "@@",
                "@",
                "&&",
                "&",
                ";",
                ",",
                ":",
                "))",
                ")",
                "{",
                "}",
                "if",
                "else",
                "while",
                "break",
                "goto",
                "label",
        }) {
            if (reader.readString(s)) {
                return new Token(s, s,s);
            }
        }
        if (reader.isWordChar(rc)) {
            return this.reader.lexer().continueReadWord();
        }
        if (rc == '\"') {
            reader.read();
            return this.reader.lexer().processContext(String.valueOf(rc),new DoubleQuotedContext(this.reader));
        }
        if (rc == '\'') {
            reader.read();
            return this.reader.lexer().processContext(String.valueOf(rc),new SimpleQuotedContext(this.reader));
        }
        reader.read();
        return new Token(String.valueOf(rc), String.valueOf(rc), String.valueOf(rc));
    }
}
