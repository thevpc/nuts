package net.thevpc.nuts.runtime.standalone.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.format.text.DefaultNutsTextNodeFactory;
import net.thevpc.nuts.runtime.standalone.format.text.parser.*;
import net.thevpc.nuts.NutsTextNode;

import java.util.ArrayList;
import java.util.List;

public class StyledParserStep extends ParserStep {

    boolean spreadLines;
    boolean lineStart;
    boolean started = false;
    boolean complete = false;
    StringBuilder start = new StringBuilder();
    StringBuilder end = new StringBuilder();
    List<ParserStep> children = new ArrayList<>();
    int maxSize = 10;
    private NutsWorkspace ws;

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
                end.append(c);
                String e2 = end.toString();
                end.delete(0, e2.length());
                complete = false;
                state.applyPush(new StyledParserStep(
                        e2, spreadLines, false,ws
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

                } else if (lineStart && startChar=='#' && c == ')') {
                    //this is a title
                    state.applyDropReplace(new TitleParserStep(start.toString() + c,ws));
                } else {
                    state.applyStart(c, spreadLines, false);
                }
            }
        } else {
            char endChar = endOf(start.charAt(0));
            if (c == endChar) {
                if (end.length() >= start.length()) {
                    state.applyPopReject(c);
                } else {
                    end.append(c);
                    if (end.length() >= start.length()) {
                        complete = true;
                    }
                }
            } else {
                if (end.length() == 0) {
                    state.applyStart(c, spreadLines, false);
                } else {
                    String y = end.toString();
                    end.delete(0, end.length());
                    if(y.length()>1) {
                        state.applyPush(new StyledParserStep(y, spreadLines, lineStart, ws));
                    }else{
                        state.applyPush(new PlainParserStep(y, spreadLines, lineStart, ws,state));
                    }
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
        if (children.size() == 1) {
            return factory0.createStyled(
                    start.toString(), end.toString(),
                    children.get(0).toNode(), null,
                    isComplete());
        }
        List<NutsTextNode> all = new ArrayList<>();
        for (ParserStep a : children) {
            all.add(a.toNode());
        }
        return factory0.createStyled(start.toString(), end.toString(),
                ws.formats().text().factory().list(all.toArray(new NutsTextNode[0])),
                null,
                isComplete());
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        if(!isComplete()) {
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

}
