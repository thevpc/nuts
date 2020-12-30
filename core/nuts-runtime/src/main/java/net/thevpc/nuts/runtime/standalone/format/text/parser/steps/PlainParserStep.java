package net.thevpc.nuts.runtime.standalone.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsTextNode;

public class PlainParserStep extends ParserStep {

    char last = '\0';
    private boolean wasEscape;
    private boolean spreadLines;
    private boolean lineStart;
    private StringBuilder value = new StringBuilder();
    private NutsWorkspace ws;
    private DefaultNutsTextNodeParser.State state;

    public PlainParserStep(char c, boolean spreadLines, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state) {
        this.state = state;
        this.spreadLines = spreadLines;
        this.ws = ws;
        this.lineStart = state.isWasNewLine();
        if (c == '\\') {
            wasEscape = true;
        } else {
            value.append(c);
        }
        last = c;
        state.setWasNewLine(c=='\n');
    }

    public PlainParserStep(String s, boolean spreadLines, boolean lineStart,NutsWorkspace ws, DefaultNutsTextNodeParser.State state) {
        this.state = state;
        state.setWasNewLine(s.charAt(s.length()-1)=='\n');
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
        state.setWasNewLine(c=='\n');
        last = c;
        switch (c) {
            case '#':
            case '@':
            case '~':
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
                    if (oldLast == c) {
                        value.deleteCharAt(value.length() - 1);
                        if (value.length() == 0) {
                            p.applyDropReplace(new StyledParserStep(c + "" + c, spreadLines, lineStart,ws));
                            return;
                        } else {
                            p.applyPopReplace(new StyledParserStep(c + "" + c, spreadLines, lineStart,ws));
                            return;
                        }
                    }
                    p.applyPopReject(c);
                    return;
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
                    p.applyPop();
                    return;
                }
            }
            case '`':
            case '"':
            case '\'':
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
                    p.applyPopReject(c);
                    return;
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
                value.append(c);
//                    p.appContinue();
                return;
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
