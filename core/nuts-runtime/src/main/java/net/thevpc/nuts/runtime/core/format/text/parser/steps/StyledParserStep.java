package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextNodeFactory;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.standalone.util.common.StringBuilder2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;

public class StyledParserStep extends ParserStep {

    public static final IntPredicate EXIT_ON_CLOSE_ACCOLADES = ((cc) -> cc == '}' || cc == '#');
    boolean spreadLines;
    boolean lineStart;
    boolean started = false;
    boolean complete = false;
    StringBuilder start = new StringBuilder();
    StringBuilder atStr = new StringBuilder();
    StringBuilder2 end = new StringBuilder2();
    List<ParserStep> children = new ArrayList<>();
    int maxSize = 10;
    private NutsWorkspace ws;
    private StyleMode styleMode = StyleMode.SIMPLE;
    private boolean atPresentEnded = false;
    private List<NutsTextNodeStyle> atVals = new ArrayList<>();
    private NutsTextNode atInvalid;
    private boolean parsedAt = false;
    private StyledParserStepCommandParser parseHelper = new StyledParserStepCommandParser();

    public StyledParserStep(char c, boolean spreadLines, boolean lineStart, NutsWorkspace ws) {
        start.append(c);
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
    }

    public StyledParserStep(String c, boolean spreadLines, boolean lineStart, NutsWorkspace ws) {
        start.append(c);
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State state) {
        if (complete) {
            if (c == start.charAt(0)) {
                ;
                String e2 = end.append(c).readAll();
                complete = false;
                state.applyPush(new StyledParserStep(
                        e2, spreadLines, false, ws
                ));
            } else if (c == 'ø') {
                state.applyPop();
            } else {
                state.applyPopReject(c);
            }
            return;
        }
        if (!spreadLines && (c == '\n' || c == '\r')) {
            state.applyPopReject(c);
            return;
        }
        if (c == 'ø') {
            if (!started) {
                started = true;
                state.applyPush(new DispatchAction(false, false));
            } else {
                state.applyPop();
            }
            return;
        }
        if (!started) {
            if (c == start.charAt(0)) {
                if (start.length() <= maxSize) {
                    start.append(c);
                } else {
                    started = true;
                    state.applyStart(c, spreadLines, false);
                }
            } else {
                char startChar = start.charAt(0);
                char endChar = endOf(startChar);
                started = true;
                if (c == endChar) {
                    end.append(c);
                    if (end.length() >= start.length()) {
                        state.applyPop();
                    }

                } else if (lineStart && startChar == '#' && c == ')') {
                    //this is a title
                    state.applyDropReplace(new TitleParserStep(start.toString() + c, ws));
                } else if (startChar == '#' && start.length() == 2 && c == ':') {
                    styleMode = StyleMode.COLON;
                    atStr.append(c);
                    //this is a title ##:
                } else if (startChar == '#' && start.length() == 2 && c == '{') {
                    styleMode = StyleMode.EMBEDDED;
                    atStr.append(c);
                    //this is a title ##:
                } else if (start.length() == 1 && c != startChar) {
                    state.applyDropReplace(new PlainParserStep(startChar,spreadLines, lineStart, ws, state, null));
                    state.applyNextChar(c);
                } else {
                    state.applyStart(c, spreadLines, false);
                }
            }
        } else {
            if ((styleMode == StyleMode.COLON || styleMode == StyleMode.EMBEDDED) && !atPresentEnded) {
                if (parseHelper.isCommandPart(c)) {
                    atStr.append(c);
                } else if (parseHelper.isCommandEnd(c)) {
                    atStr.append(c);
                    atPresentEnded = true;
                } else {
                    //rollback
                    String s = atStr.toString() + c;
                    atStr.setLength(0);
                    styleMode = StyleMode.SIMPLE;
                    state.applyPush(new PlainParserStep(s, spreadLines, false, ws, state, EXIT_ON_CLOSE_ACCOLADES));
                    atPresentEnded = false;
                }
            } else if ((styleMode == StyleMode.SIMPLE || styleMode == StyleMode.COLON) && c == endOf(start.charAt(0))) {
                if (end.length() >= start.length()) {
                    state.applyPopReject(c);
                } else {
                    end.append(c);
                    if (end.length() >= start.length()) {
                        complete = true;
                    }
                }
            } else if ((styleMode == StyleMode.EMBEDDED) && end.length() > 0 && end.charAt(0) == '}' && c == endOf(start.charAt(0))) {
                if (end.length() > start.length()) {
                    state.applyPopReject(c);
                } else {
                    end.append(c);
                    if (end.length() > start.length()) {
                        complete = true;
                    }
                }
            } else if (end.isEmpty()) {
                if (styleMode == StyleMode.EMBEDDED && c == '}') {
                    end.append(c);
                }else if (styleMode != StyleMode.EMBEDDED && c == '#') {
                    end.append(c);
                }else if (styleMode == StyleMode.EMBEDDED && c == '#') {
                    state.applyPush(new StyledParserStep(c, spreadLines, false, ws));
                } else {
                    state.applyPush(new PlainParserStep(c,spreadLines, lineStart, ws, state, EXIT_ON_CLOSE_ACCOLADES));
                }
            } else if (end.charAt(0) == '}' && styleMode == StyleMode.EMBEDDED) {
                String y = end.readAll();
                appendChild(new PlainParserStep(y, spreadLines, false, ws, state, null));
            } else {
                String y = end.readAll();
                if (y.length() > 1) {
                    state.applyPush(new StyledParserStep(y, spreadLines, lineStart, ws));
                } else {
                    state.applyPush(new PlainParserStep(y, spreadLines, lineStart, ws, state,
                            styleMode == StyleMode.EMBEDDED ? EXIT_ON_CLOSE_ACCOLADES : null
                    ));
                }
            }
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        children.add(tt);
    }

    @Override
    public NutsTextNode toNode() {
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        String start = this.start.toString();
        String end = this.end.toString();
        List<NutsTextNodeStyle> all = new ArrayList<>();
        if (styleMode == StyleMode.COLON) {
            if (!parsedAt) {
                parsedAt = true;
                NutsTextNodeStyle[] parsedStyles = parseHelper.parse(atStr.toString());
                if (parsedStyles == null) {
                    atInvalid = ws.formats().text().factory().plain(atStr.toString());
                } else {
                    atVals.addAll(Arrays.asList(parsedStyles));
                }
            }
        }
        if (styleMode == StyleMode.COLON) {
            all.addAll(atVals);
        } else {
            switch (start.charAt(0)) {
                case '#': {
                    all.add(NutsTextNodeStyle.primary(start.length() - 1));
                    break;
                }
            }
        }

        NutsTextNode child = null;
        if (children.size() == 1) {
            child = children.get(0).toNode();
        } else {
            List<NutsTextNode> allChildren = new ArrayList<>();
            for (ParserStep a : children) {
                allChildren.add(a.toNode());
            }
            child = ws.formats().text().factory().list(allChildren.toArray(new NutsTextNode[0]));
        }
        if (atInvalid != null) {
            child = ws.formats().text().factory().list(atInvalid, child);
        }
        if (all.isEmpty()) {
            all.add(NutsTextNodeStyle.primary(1));
        }
        for (NutsTextNodeStyle s : all) {
            child = factory0.createStyled(
                    child, s,
                    isComplete());
        }
        return child;
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        if (!isComplete()) {
            while (end.length() < start.length()) {
                end.append(endOf(start.charAt(0)));
            }
        }
        p.applyPop();
    }

    public boolean isComplete() {
        return started && end.length() == start.length();
    }

    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder("Typed(" + CoreStringUtils.dblQuote(start.toString()));
//        if (!started) {
//            sb.append(",<NEW>");
//        }
//        for (ParserStep parserStep : children) {
//            sb.append(",");
//            sb.append(parserStep.toString());
//        }
//        sb.append(",END(").append(CoreStringUtils.dblQuote(end.toString())).append(")");
//        sb.append(isComplete() ? "" : ",incomplete");
//        return sb.append(")").toString();
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        for (ParserStep parserStep : children) {
            sb.append(parserStep.toString());
        }
        sb.append(end);
        return sb.toString();
    }

    public char endOf(char c) {
        switch (c) {
            case '<':
                return '>';
            case '(':
                return ')';
            case '[':
                return ']';
            case '{':
                return '}';
        }
        return c;
    }

    enum StyleMode {
        SIMPLE, // ##anything##
        COLON,     // ##:12:anything##
        EMBEDDED,     // ##:12:anything}##
        // ##:sh:anything##
    }

}
