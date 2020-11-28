package net.thevpc.nuts.runtime.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.text.parser.*;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.util.ArrayList;
import java.util.List;

public class TypedParserStep extends ParserStep {

    boolean spreadLines;
    boolean lineStart;
    boolean started = false;
    boolean complete = false;
    StringBuilder start = new StringBuilder();
    StringBuilder end = new StringBuilder();
    List<ParserStep> children = new ArrayList<>();
    int maxSize = 10;
    private NutsWorkspace ws;

    public TypedParserStep(char c, boolean spreadLines, boolean lineStart,NutsWorkspace ws) {
        start.append(c);
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
    }

    public TypedParserStep(String c, boolean spreadLines, boolean lineStart,NutsWorkspace ws) {
        start.append(c);
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.ws = ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p) {
        if (complete) {
            if (c == start.charAt(0)) {
                end.append(c);
                String e2 = end.toString();
                end.delete(0, e2.length());
                complete = false;
                p.applyPush(new TypedParserStep(
                        e2, spreadLines, false,ws
                ));
            } else {
                p.applyPopReject(c);
            }
            return;
        }
        if (!spreadLines && (c == '\n' || c == '\r')) {
            p.applyPopReject(c);
            return;
        }
        if (c == 'ø') {
            if (!started) {
                started = true;
                p.applyPush(new DispatchAction(false, false));
            } else {
                p.applyPop();
            }
            return;
        }
        if (!started) {
            if (c == start.charAt(0)) {
                if (start.length() <= maxSize) {
                    start.append(c);
                } else {
                    started = true;
                    p.applyStart(c, spreadLines, false);
                }
            } else {
                char startChar = start.charAt(0);
                char endChar = endOf(startChar);
                started = true;
                if (c == endChar) {
                    end.append(c);
                    if (end.length() >= start.length()) {
                        p.applyPop();
                    }

                } else if (lineStart && startChar!='(' && c == ')') {
                    //this is a title
                    p.applyDropReplace(new TitleParserStep(start.toString() + c,ws));
                } else {
                    p.applyStart(c, spreadLines, false);
                }
            }
        } else {
            char endChar = endOf(start.charAt(0));
            if (c == endChar) {
                if (end.length() >= start.length()) {
                    p.applyPopReject(c);
                } else {
                    end.append(c);
                    if (end.length() >= start.length()) {
                        complete = true;
                    }
                }
            } else {
                if (end.length() == 0) {
                    p.applyStart(c, spreadLines, false);
                } else {
                    String y = end.toString();
                    end.delete(0, end.length());
                    p.applyPush(new TypedParserStep(y, spreadLines, lineStart,ws));
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
        if (children.size() == 1) {
            return new DefaultNutsTextNodeStyled(
                    start.toString(), end.toString(),
                    DefaultNutsTextNodeParser.createStyle(start.toString()),
                    children.get(0).toNode(), isComplete());
        }
        List<NutsTextNode> all = new ArrayList<>();
        for (ParserStep a : children) {
            all.add(a.toNode());
        }
        return new DefaultNutsTextNodeStyled(start.toString(), end.toString(),
                DefaultNutsTextNodeParser.createStyle(start.toString()),
                ws.formats().text().factory().list(all.toArray(new NutsTextNode[0])), isComplete());
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
        StringBuilder sb = new StringBuilder("Typed(" + CoreStringUtils.dblQuote(start.toString()));
        if (!started) {
            sb.append(",<NEW>");
        }
        for (ParserStep parserStep : children) {
            sb.append(",");
            sb.append(parserStep.toString());
        }
        sb.append(",END(").append(CoreStringUtils.dblQuote(end.toString())).append(")");
        sb.append(isComplete() ? "" : ",incomplete");
        return sb.append(")").toString();
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
