package net.thevpc.nuts.toolbox.nsh.parser;

import net.thevpc.nuts.toolbox.nsh.nodes.NShellCommandNode;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellNode;
import net.thevpc.nuts.toolbox.nsh.parser.ctx.DefaultContext;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NShellParser {
    private StrReader strReader = new StrReader();
    private DefaultLexer lexer = new DefaultLexer(this);
    private Yaccer yaccer = new Yaccer(this.lexer);

    public NShellParser(Reader reader) {
        strReader.reader = reader;
        lexer.ctx.push(new DefaultContext(this));
    }

    public static NShellParser fromString(String s) {
        return new NShellParser(new StringReader(s == null ? "" : s));
    }

    public static NShellParser fromInputStream(InputStream s) {
        return new NShellParser(s==null?new StringReader("") : new InputStreamReader(s));
    }

    public StrReader strReader() {
        return strReader;
    }

    public DefaultLexer lexer() {
        return lexer;
    }

    public Yaccer yaccer() {
        return yaccer;
    }


    public NShellNode parse() {
        return yaccer().readScript();
    }

    public static NShellCommandNode createCommandNode(String[] args) {
        List<Yaccer.Argument> args2 = new ArrayList<>();
        for (String arg : args) {
            args2.add(new Yaccer.Argument(
                    Arrays.asList(
                            new Yaccer.TokenNode(
                                    new Token(
                                            "WORD",
                                            arg,
                                            arg
                                    )
                            )
                    )
            ));
        }
        return new Yaccer.ArgumentsLine(args2);
    }
}
