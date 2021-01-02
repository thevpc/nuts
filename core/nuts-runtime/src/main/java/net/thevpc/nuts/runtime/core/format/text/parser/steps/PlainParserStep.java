package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.runtime.standalone.util.common.StringBuilder2;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class PlainParserStep extends ParserStep {

    char last = '\0';
    private boolean wasEscape;
    private boolean spreadLines;
    private boolean lineStart;
    private StringBuilder2 value = new StringBuilder2();
    private NutsWorkspace ws;
    private DefaultNutsTextNodeParser.State state;
    private IntPredicate exitCondition;

    public PlainParserStep(char c, boolean spreadLines, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state,IntPredicate exitCondition) {
        this.state = state;
        this.exitCondition = exitCondition;
        this.spreadLines = spreadLines;
        this.ws = ws;
        this.lineStart = state.isLineStart() ;
        if (c == '\\') {
            wasEscape = true;
        } else {
            value.append(c);
        }
        last = c;
        state.setLineStart(c=='\n');
    }

    public PlainParserStep(String s, boolean spreadLines, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state, IntPredicate exitCondition) {
        this.state = state;
        this.exitCondition = exitCondition;
        state.setLineStart(s.charAt(s.length()-1)=='\n');
        this.ws = ws;
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        value.append(s, 0, s.length()-1);
        char c=s.charAt(s.length()-1);
        if (c == '\\') {
            wasEscape = true;
        } else {
            value.append(c);
        }
        last = c;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p) {
        char oldLast = last;
        state.setLineStart(c=='\n');
        last = c;
        switch (c) {
            case '#':
//            case '@':
//            case '~':
//                case '$':
//                case '£':
//                case '§':
//                case '_':
//                case '¤':
//                case '^':
//                case '¨':
//                case '=':
//                case '*':
//                case '+':
            {
                if (wasEscape) {
                    wasEscape = false;
                    value.append(c);
                    last = '\0';
                    //p.applyContinue();
                    return;
                } else {
                    if(exitCondition!=null && exitCondition.test(c)){
                        p.applyPopReject(c);
                        return;
                    }else {
                        if (oldLast == c) {
                            value.readLast();
                            if (value.length() == 0) {
                                p.applyDropReplace(new StyledParserStep(c + "" + c, spreadLines, lineStart, ws));
                                return;
                            } else {
                                p.applyPopReplace(new StyledParserStep(c + "" + c, spreadLines, lineStart, ws));
                                return;
                            }
                        }
                        p.applyPopReject(c);
                        return;
                    }
                }
            }
            case 'ø': {
                if (wasEscape) {
                    wasEscape = false;
                    value.append(c);
                    last = '\0';
                    //p.applyContinue();
                    return;
                } else {
                    if(exitCondition!=null && exitCondition.test(c)){
                        p.applyPopReject(c);
                        return;
                    }else {
                        p.applyPop();
                        return;
                    }
                }
            }
            case '`':
//            case '"':
//            case '\'':
//                case '(':
//                case '[':
//                case '{':
//                case '<':
//                case ')':
//                case ']':
//                case '}':
//                case '>':
            {
                if (wasEscape) {
                    wasEscape = false;
                    value.append(c);
//                        p.applyContinue();
                    return;
                } else {
                    if(exitCondition!=null && exitCondition.test(c)){
                        p.applyPopReject(c);
                        return;
                    }else {
                        p.applyPopReject(c);
                        return;
                    }
                }
            }
            case '\n':
            case '\r': {
                if (spreadLines) {
                    if (wasEscape) {
                        wasEscape = false;
                    }
                    value.append(c);
                    p.applyPop();
                    if (!spreadLines) {
                        p.forceEnding();
                    }
                } else {
                    if (wasEscape) {
                        wasEscape = false;
                        value.append(c);
                        p.applyPop();
                        if (!spreadLines) {
                            p.forceEnding();
                        }
                    } else {
                        p.applyPopReject(c);
                    }
                }
                return;
            }
            case '\\': {
                if (wasEscape) {
                    wasEscape = false;
                    value.append(c);
                } else {
                    wasEscape = true;
                }
//                    p.appContinue();
                return;
            }
            default: {
                if (wasEscape) {
                    wasEscape = false;
                }
                if(exitCondition!=null && exitCondition.test(c)){
                    p.applyPopReject(c);
                }else {
                    value.append(c);
                }
                return;
//                    p.appContinue();
            }
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new UnsupportedOperationException("unsupported operation: appendChild");
    }

    @Override
    public NutsTextNode toNode() {
        return ws.formats().text().factory().plain(value.toString());
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop();
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        return value.toString();
//        StringBuilder sb = new StringBuilder("Plain(" + CoreStringUtils.dblQuote(value.toString()));
//        if (wasEscape) {
//            sb.append(",<ESCAPE>");
//        }
//        sb.append(isComplete() ? "" : ",incomplete");
//        sb.append(")");
//        return sb.toString();
    }

}
