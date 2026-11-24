package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RubyCodeHighlighter extends AbstractCodeHighlighter {

    public RubyCodeHighlighter() {
        super("ruby");
        keywords.addAll(Arrays.asList(
                "BEGIN", "END", "alias", "and", "begin", "break", "case", "class", "def", "defined?",
                "do", "else", "elsif", "end", "ensure", "for", "if", "in", "module", "next", "not",
                "or", "redo", "rescue", "retry", "return", "self", "super", "then", "undef", "unless",
                "until", "when", "while", "yield", "require", "require_relative", "include", "extend",
                "attr_reader", "attr_writer", "attr_accessor", "private", "protected", "public",
                "lambda", "proc", "loop", "raise", "catch", "throw"
        ));
        types.addAll(Arrays.asList(
                "Array", "Bignum", "Binding", "Class", "Comparable", "Complex", "Dir", "Encoding",
                "Enumerator", "Exception", "Fiber", "File", "Float", "GC", "Hash", "Integer", "IO",
                "Kernel", "MatchData", "Method", "Module", "NilClass", "Numeric", "Object",
                "Proc", "Process", "Random", "Range", "Rational", "Regexp", "Set", "String",
                "Struct", "Symbol", "Thread", "Time", "TrueClass", "FalseClass"
        ));
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        nullLiterals.add("nil");
        lineComments.add("#");
        stringQuotes.add('\'');
        stringQuotes.add('`');
        interpolation = true;
        numberSuffixChars = "";
        operatorChars = "+-*/%=<>!&|^~?:.";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "ruby", "rb", "rake", "gemfile", "text/x-ruby", "application/x-ruby");
    }

    @Override
    protected boolean handleSpecial(StringReaderExt ar, List<NText> all) {
        char c = ar.peekChar();
        // @instance and @@class variables
        if (c == '@' && ar.hasNext(1) && (ar.peekChar(1) == '@' || isIdentifierStart(ar.peekChar(1)))) {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar());
            if (ar.hasNext() && ar.peekChar() == '@') {
                sb.append(ar.readChar());
            }
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.variable()));
            return true;
        }
        // $global and special globals ($!, $?, $0 ...)
        if (c == '$' && ar.hasNext(1)) {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar());
            if (isIdentifierStart(ar.peekChar())) {
                while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                    sb.append(ar.readChar());
                }
            } else {
                sb.append(ar.readChar());
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.variable()));
            return true;
        }
        // :symbol (but not ::)
        if (c == ':' && ar.hasNext(1) && ar.peekChar(1) != ':'
                && (isIdentifierStart(ar.peekChar(1)) || ar.peekChar(1) == '"')) {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar()); // :
            if (ar.peekChar() == '"') {
                sb.append(ar.readChar());
                while (ar.hasNext() && ar.peekChar() != '"') {
                    sb.append(ar.readChar());
                }
                if (ar.hasNext()) {
                    sb.append(ar.readChar());
                }
            } else {
                while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                    sb.append(ar.readChar());
                }
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
            return true;
        }
        return false;
    }

    @Override
    protected boolean isInterpolationStart(StringReaderExt ar) {
        return interpolation && ar.peekChar() == '#' && ar.hasNext(1) && ar.peekChar(1) == '{';
    }

    @Override
    protected List<NText> readInterpolation(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // #
        sb.append(ar.readChar()); // {
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
    }
}
