package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.collections.EvictingCharQueue;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.bundles.string.StringBuilder2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import net.thevpc.nuts.NutsTextStyles;
import net.thevpc.nuts.NutsText;

public class StyledParserStep extends ParserStep {

    public static final IntPredicate EXIT_ON_CLOSE_ACCOLADES = ((cc) -> cc == '}' || cc == '#');
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
    private boolean wasSharp = false;
    private boolean atPresentEnded = false;
    private List<NutsTextStyle> atVals = new ArrayList<>();
    private NutsText atInvalid;
    private boolean parsedAt = false;
    private StyledParserStepCommandParser parseHelper = new StyledParserStepCommandParser();
    private EvictingCharQueue charQueue = new EvictingCharQueue(5);
    private DefaultNutsTextNodeParser.State state;

    public StyledParserStep(char c, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state) {
        start.append(c);
//        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
        this.state = state;
    }

    public StyledParserStep(String c, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state) {
        start.append(c);
//        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
        this.state = state;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State state) {
        charQueue.add(c);
        if (complete) {
            if (c == start.charAt(0)) {
                String e2 = end.append(c).readAll();
                complete = false;
                state.applyPush(new StyledParserStep(
                        e2, false, ws, state
                ));
            } else if (c == 'ø') {
                state.applyPop();
            } else {
                state.applyPopReject(c);
            }
            return;
        }
//        if (c == '\n' || c == '\r') {
//            state.applyPopReject(c);
//            return;
//        }
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
                    state.applyStart(c, /*spreadLines*/ true, false);
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
                    state.applyDropReplace(new PlainParserStep(startChar, lineStart, ws, state, null));
                    state.applyNextChar(c);
                } else {
                    state.applyStart(c, /*spreadLines*/ true, false);
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
                    state.applyPush(new PlainParserStep(s, /*spreadLines*/ true, false, ws, state, EXIT_ON_CLOSE_ACCOLADES));
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
                } else if (c == '#') {
                    if (styleMode == StyleMode.EMBEDDED) {
                        if (wasSharp) {
                            wasSharp = false;
                            state.applyPush(new StyledParserStep("##", lineStart, ws, state));
                        } else {
                            wasSharp = true;
                        }
                    } else {
                        end.append(c);
                    }
//                    state.applyPush(new StyledParserStep(c, spreadLines, false, ws));
                } else {
                    state.applyPush(new PlainParserStep(c, lineStart, ws, state, EXIT_ON_CLOSE_ACCOLADES));
                }
            } else if (end.charAt(0) == '}' && styleMode == StyleMode.EMBEDDED) {
                String y = end.readAll();
                appendChild(new PlainParserStep(y, /*spreadLines*/ true, false, ws, state, null));
            } else {
                String y = end.readAll();
                if (y.length() > 1) {
                    state.applyPush(new StyledParserStep(y, lineStart, ws, state));
                } else {
                    state.applyPush(new PlainParserStep(y, /*spreadLines*/ true, lineStart, ws, state,
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
    public NutsText toNode() {
        DefaultNutsTextManager factory0 = (DefaultNutsTextManager) ws.formats().text();
        String start = this.start.toString();
        String end = this.end.toString();
        List<NutsTextStyle> all = new ArrayList<>();
        if (wasSharp) {
            wasSharp = false;
            children.add(new PlainParserStep("#", false, false, ws, state, null));
        }
        if (styleMode == StyleMode.COLON) {
            if (!parsedAt) {
                parsedAt = true;
                NutsTextStyles parsedStyles = parseHelper.parse(atStr.toString());
                if (parsedStyles == null) {
                    atInvalid = ws.formats().text().forPlain(atStr.toString());
                } else {
                    for (NutsTextStyle parsedStyle : parsedStyles) {
                        atVals.add(parsedStyle);
                    }
                }
            }
        }
        if (styleMode == StyleMode.COLON) {
            all.addAll(atVals);
        } else {
            switch (start.charAt(0)) {
                case '#': {
                    all.add(NutsTextStyle.primary(start.length() - 1));
                    break;
                }
            }
        }

        NutsText child = null;
        if (children.size() == 1) {
            child = children.get(0).toNode();
        } else {
            List<NutsText> allChildren = new ArrayList<>();
            for (ParserStep a : children) {
                allChildren.add(a.toNode());
            }
            child = ws.formats().text().forList(allChildren.toArray(new NutsText[0]));
        }
        if (atInvalid != null) {
            child = ws.formats().text().forList(atInvalid, child);
        }
        if (all.isEmpty()) {
            all.add(NutsTextStyle.primary(1));
        }
        NutsTextStyles styles = NutsTextStyles.of(all.toArray(new NutsTextStyle[0]));
        child = factory0.createStyled(
                child, styles,
                isComplete());
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
        COLON, // ##:12:anything##
        EMBEDDED,     // ##:12:anything}##
        // ##:sh:anything##
    }

}
