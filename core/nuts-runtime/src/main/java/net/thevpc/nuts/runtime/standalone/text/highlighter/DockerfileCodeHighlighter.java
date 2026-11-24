package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DockerfileCodeHighlighter implements NCodeHighlighter {

    private static final Set<String> INSTRUCTIONS = new LinkedHashSet<>(Arrays.asList(
            "ADD", "ARG", "CMD", "COPY", "ENTRYPOINT", "ENV", "EXPOSE", "FROM", "HEALTHCHECK",
            "LABEL", "MAINTAINER", "ONBUILD", "RUN", "SHELL", "STOPSIGNAL", "USER", "VOLUME",
            "WORKDIR"
    ));

    @Override
    public String id() {
        return "dockerfile";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        return NCodeHighlighterHelper.score(context,
                "dockerfile", "docker", "containerfile",
                "text/x-dockerfile", "text/dockerfile", "application/x-dockerfile");
    }

    @Override
    public NText tokenToText(String text, String nodeType) {
        return NText.ofPlain(text);
    }

    @Override
    public NText stringToText(String text) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            StringBuilder line = new StringBuilder();
            while (ar.hasNext() && ar.peekChar() != '\n' && ar.peekChar() != '\r') {
                line.append(ar.readChar());
            }
            highlightLine(line.toString(), all);
            if (ar.hasNext()) {
                char c = ar.readChar();
                if (c == '\r' && ar.hasNext() && ar.peekChar() == '\n') {
                    all.add(NText.ofPlain("\r\n"));
                    ar.readChar();
                } else {
                    all.add(NText.ofPlain(String.valueOf(c)));
                }
            }
        }
        return NText.ofList(all.toArray(new NText[0]));
    }

    private void highlightLine(String line, List<NText> all) {
        int i = 0;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        if (i > 0) {
            all.add(NText.ofPlain(line.substring(0, i)));
        }
        if (i >= line.length()) {
            return;
        }
        if (line.charAt(i) == '#') {
            all.add(NText.ofStyled(line.substring(i), NTextStyle.comments()));
            return;
        }
        int start = i;
        while (i < line.length() && Character.isLetter(line.charAt(i))) {
            i++;
        }
        String word = line.substring(start, i);
        if (INSTRUCTIONS.contains(word.toUpperCase())) {
            all.add(NText.ofStyled(word, NTextStyle.keyword()));
        } else {
            all.add(NText.ofPlain(word));
        }
        highlightRest(line.substring(i), all);
    }

    private void highlightRest(String text, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
            } else if (c == '$') {
                all.addAll(readVariable(ar));
            } else if (c == '"' || c == '\'') {
                all.addAll(readString(ar, c));
            } else if (c == '\\' || c == '=' || c == ':') {
                all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
            } else {
                all.add(NText.ofPlain(String.valueOf(ar.readChar())));
            }
        }
    }

    private List<NText> readVariable(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        if (ar.hasNext() && ar.peekChar() == '{') {
            sb.append(ar.readChar());
            while (ar.hasNext() && ar.peekChar() != '}') {
                sb.append(ar.readChar());
            }
            if (ar.hasNext()) {
                sb.append(ar.readChar());
            }
        } else {
            while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_')) {
                sb.append(ar.readChar());
            }
        }
        return java.util.Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
    }

    private List<NText> readString(StringReaderExt ar, char quote) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                flush(sb, all);
                sb.append(ar.readChar());
                if (ar.hasNext()) {
                    sb.append(ar.readChar());
                }
                all.add(NText.ofStyled(sb.toString(), NTextStyle.separator()));
                sb.setLength(0);
            } else if (c == quote) {
                sb.append(ar.readChar());
                break;
            } else if (c == '$') {
                flush(sb, all);
                all.addAll(readVariable(ar));
            } else {
                sb.append(ar.readChar());
            }
        }
        flush(sb, all);
        return all;
    }

    private void flush(StringBuilder sb, List<NText> all) {
        if (sb.length() > 0) {
            all.add(NText.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
    }
}
