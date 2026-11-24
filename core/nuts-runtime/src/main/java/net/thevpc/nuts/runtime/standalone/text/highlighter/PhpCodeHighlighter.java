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

public class PhpCodeHighlighter extends AbstractCodeHighlighter {

    public PhpCodeHighlighter() {
        super("php");
        keywords.addAll(Arrays.asList(
                "abstract", "and", "array", "as", "break", "callable", "case", "catch", "class", "clone",
                "const", "continue", "declare", "default", "do", "echo", "else", "elseif", "empty",
                "enddeclare", "endfor", "endforeach", "endif", "endswitch", "endwhile", "enum", "eval",
                "exit", "extends", "final", "finally", "fn", "for", "foreach", "function", "global",
                "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof",
                "interface", "isset", "list", "match", "namespace", "new", "or", "print", "private",
                "protected", "public", "readonly", "require", "require_once", "return", "static",
                "switch", "throw", "trait", "try", "unset", "use", "var", "while", "xor", "yield", "from"
        ));
        types.addAll(Arrays.asList(
                "int", "float", "string", "bool", "array", "object", "mixed", "void", "never", "iterable",
                "self", "parent", "callable", "resource", "null", "false", "true", "static"
        ));
        booleanLiterals.addAll(Arrays.asList("true", "false"));
        nullLiterals.add("null");
        lineComments.add("//");
        lineComments.add("#");
        blockCommentOpen = "/*";
        blockCommentClose = "*/";
        stringQuotes.add('\'');
        numberSuffixChars = "";
        operatorChars = "+-*/%=<>!&|^~?:.@";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "php", "php3", "php4", "php5", "php7", "php8", "phtml",
                "text/x-php", "application/x-php", "text/php");
    }

    @Override
    protected boolean handleSpecial(StringReaderExt ar, List<NText> all) {
        // PHP open/close tags
        if (startsWith(ar, "<?php")) {
            all.add(NText.ofStyled(ar.nextChars(5), NTextStyle.annotation()));
            return true;
        }
        if (startsWith(ar, "<?=")) {
            all.add(NText.ofStyled(ar.nextChars(3), NTextStyle.annotation()));
            return true;
        }
        if (startsWith(ar, "<?")) {
            all.add(NText.ofStyled(ar.nextChars(2), NTextStyle.annotation()));
            return true;
        }
        if (startsWith(ar, "?>")) {
            all.add(NText.ofStyled(ar.nextChars(2), NTextStyle.annotation()));
            return true;
        }
        // PHP 8 attribute #[...] must win over the # line comment
        if (startsWith(ar, "#[")) {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar());
            sb.append(ar.readChar());
            int depth = 1;
            while (ar.hasNext() && depth > 0) {
                char c = ar.readChar();
                sb.append(c);
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                }
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
            return true;
        }
        // variables: $name
        if (ar.peekChar() == '$' && ar.hasNext(1) && isIdentifierStart(ar.peekChar(1))) {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar());
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            all.add(NText.ofStyled(sb.toString(), NTextStyle.variable()));
            return true;
        }
        // heredoc / nowdoc
        if (isHeredocStart(ar)) {
            List<NText> heredoc = readHeredoc(ar);
            if (heredoc != null) {
                all.addAll(heredoc);
                return true;
            }
        }
        return false;
    }

    private boolean isHeredocStart(StringReaderExt ar) {
        if (!startsWith(ar, "<<<")) {
            return false;
        }
        int i = 3;
        char quote = 0;
        if (ar.hasNext(i) && (ar.peekChar(i) == '\'' || ar.peekChar(i) == '"')) {
            quote = ar.peekChar(i);
            i++;
        }
        int start = i;
        while (ar.hasNext(i) && isIdentifierPart(ar.peekChar(i))) {
            i++;
        }
        if (i == start) {
            return false;
        }
        if (quote != 0) {
            if (!ar.hasNext(i) || ar.peekChar(i) != quote) {
                return false;
            }
            i++;
        }
        return ar.hasNext(i) && (ar.peekChar(i) == '\n' || ar.peekChar(i) == '\r');
    }

    @Override
    protected List<NText> readString(StringReaderExt ar) {
        if (ar.peekChar() == '\'') {
            boolean old = interpolation;
            interpolation = false;
            try {
                return super.readString(ar);
            } finally {
                interpolation = old;
            }
        }
        return super.readString(ar);
    }

    @Override
    protected boolean isInterpolationStart(StringReaderExt ar) {
        if (!interpolation) {
            return false;
        }
        char c = ar.peekChar();
        if (c == '$' && ar.hasNext(1) && (ar.peekChar(1) == '{' || isIdentifierStart(ar.peekChar(1)))) {
            return true;
        }
        return c == '{' && ar.hasNext(1) && ar.peekChar(1) == '$';
    }

    @Override
    protected List<NText> readInterpolation(StringReaderExt ar) {
        if (ar.peekChar() == '{') {
            StringBuilder sb = new StringBuilder();
            sb.append(ar.readChar()); // {
            sb.append(ar.readChar()); // $
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            // consume array/property access up to the closing brace
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
        return super.readInterpolation(ar);
    }

    private List<NText> readHeredoc(StringReaderExt ar) {
        // <<<LABEL or <<<'LABEL' or <<<"LABEL"
        StringBuilder header = new StringBuilder();
        header.append(ar.nextChars(3));
        char quote = 0;
        if (ar.hasNext() && (ar.peekChar() == '\'' || ar.peekChar() == '"')) {
            quote = ar.readChar();
            header.append(quote);
        }
        StringBuilder label = new StringBuilder();
        while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
            label.append(ar.readChar());
        }
        header.append(label);
        if (quote != 0 && ar.hasNext() && ar.peekChar() == quote) {
            header.append(ar.readChar());
        }
        if (label.length() == 0 || !ar.hasNext() || (ar.peekChar() != '\n' && ar.peekChar() != '\r')) {
            return null;
        }
        // consume newline into the header token
        if (ar.hasNext()) {
            char c = ar.readChar();
            header.append(c);
            if (c == '\r' && ar.hasNext() && ar.peekChar() == '\n') {
                header.append(ar.readChar());
            }
        }
        List<NText> all = new ArrayList<>();
        all.add(NText.ofStyled(header.toString(), NTextStyle.annotation()));
        StringBuilder body = new StringBuilder();
        String closing = label.toString();
        while (ar.hasNext()) {
            StringBuilder line = new StringBuilder();
            while (ar.hasNext() && ar.peekChar() != '\n' && ar.peekChar() != '\r') {
                line.append(ar.readChar());
            }
            if (ar.hasNext()) {
                char c = ar.readChar();
                line.append(c);
                if (c == '\r' && ar.hasNext() && ar.peekChar() == '\n') {
                    line.append(ar.readChar());
                }
            }
            body.append(line);
            String t = line.toString().trim();
            if (t.equals(closing) || t.equals(closing + ";")) {
                break;
            }
        }
        all.add(NText.ofStyled(body.toString(), NTextStyle.string()));
        return all;
    }
}
