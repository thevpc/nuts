package net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellCommandNode;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellNode;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser.ctx.DefaultContext;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JShellParser {
    private StrReader strReader = new StrReader();
    private DefaultLexer lexer = new DefaultLexer(this);
    private Yaccer yaccer = new Yaccer(this.lexer);

    public JShellParser(Reader reader) {
        strReader.reader = reader;
        lexer.ctx.push(new DefaultContext(this));
    }

    public static JShellParser fromString(String s) {
        return new JShellParser(new StringReader(s == null ? "" : s));
    }

    public static JShellParser fromInputStream(InputStream s) {
        return new JShellParser(s==null?new StringReader("") : new InputStreamReader(s));
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


    public JShellNode parse() {
        return yaccer().readScript();
    }

    public static JShellCommandNode createCommandNode(String[] args) {
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
