package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.bundles.string.StringBuilder2;

import java.util.function.IntPredicate;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsText;

public class PlainParserStep extends ParserStep {

    char last = '\0';
    private StringBuilder escape;
//    private boolean spreadLines;
    private boolean lineStart;
    private StringBuilder2 value = new StringBuilder2();
    private NutsWorkspace ws;
    private DefaultNutsTextNodeParser.State state;
    private IntPredicate exitCondition;

    public PlainParserStep(char c, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state, IntPredicate exitCondition) {
        this.state = state;
        this.exitCondition = exitCondition;
//        this.spreadLines = spreadLines;
        this.ws = ws;
        this.lineStart = state.isLineStart();
        if (c == '\\') {
            escape = new StringBuilder("\\");
        } else {
            value.append(c);
        }
        last = c;
        state.setLineStart(c == '\n');
    }

    public PlainParserStep(String s, boolean spreadLines, boolean lineStart, NutsWorkspace ws, DefaultNutsTextNodeParser.State state, IntPredicate exitCondition) {
        this(s.charAt(0),lineStart, ws,state,exitCondition);
        for (int i = 1; i < s.length(); i++) {
            char c=s.charAt(i);
            consume(c,state, false);
        }
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p, boolean wasNewLine) {
        char oldLast = last;
        state.setLineStart(c == '\n');
        last = c;
        switch (c) {
            case '#': {
                if (escape != null) {
                    if (!escape.toString().equals("\\")) {
                        value.append(escape);
                    }
                    escape = null;
                    value.append(c);
                    last = '\0';
                    //p.applyContinue();
                    return;
                } else {
                    if (exitCondition != null && exitCondition.test(c)) {
                        p.applyPopReject(c);
                        return;
                    } else {
                        if (oldLast == c) {
                            value.readLast();
                            if (value.length() == 0) {
                                p.applyDropReplace(new StyledParserStep(c + "" + c, lineStart, ws, state));
                                return;
                            } else {
                                p.applyPopReplace(new StyledParserStep(c + "" + c, lineStart, ws, state));
                                return;
                            }
                        }
                        p.applyPopReject(c);
                        return;
                    }
                }
            }
            case NutsConstants.Ntf.SILENT: {
                if (escape != null) {
                    if (!escape.toString().equals("\\")) {
                        value.append(escape);
                    }
                    escape = null;
                    value.append(c);
                    last = '\0';
                    //p.applyContinue();
                    return;
                } else {
                    if (exitCondition != null && exitCondition.test(c)) {
                        p.applyPopReject(c);
                        return;
                    } else {
                        p.applyPop();
                        return;
                    }
                }
            }
            case '`': //            case '"':
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
                if (escape != null) {
                    if (!escape.toString().equals("\\")) {
                        value.append(escape);
                    }
                    escape = null;
                    value.append(c);
//                        p.applyContinue();
                    return;
                } else {
                    if (exitCondition != null && exitCondition.test(c)) {
                        p.applyPopReject(c);
                        return;
                    } else {
                        p.applyPopReject(c);
                        return;
                    }
                }
            }
            case '\n':
            case '\r': {
//                if (spreadLines) {
//                    if (wasEscape) {
//                        wasEscape = false;
//                    }
//                    value.append(c);
//                    p.applyPop();
//                    if (!spreadLines) {
//                        p.forceEnding();
//                    }
//                } else {
                if (escape != null) {
                    if (!escape.toString().equals("\\")) {
                        value.append(escape);
                    }
                    escape = null;
                    value.append(c);
                    p.applyPop();
//                        if (!spreadLines) {
                    p.forceEnding();
//                        }
                } else {
                    p.applyPopReject(c);
                }
//                }
                return;
            }
            case '\\': {
                if (escape != null) {
                    if (escape.toString().equals("\\")) {
                        value.append(c);
                    } else {
                        value.append(escape);
                        value.append(c);
                    }
                    escape = null;
                } else {
                    escape = new StringBuilder("\\");
                }
                return;
            }
            default: {
                if (escape != null) {
                    if ((escape.length() == 1 && c == 'u')
                            || ((escape.length() >= 2 && escape.length() <= 5) && ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))) { // \
                        escape.append(c);
                        if (escape.length() == 6) {

                            value.append(escape);

                            int cval = 0;
                            for (int i = 0; i < 4; i++) {
                                char aChar = escape.charAt(i + 2);
                                switch (aChar) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        cval = (cval << 4) + aChar - '0';
                                        break;
                                    case 'a':
                                    case 'b':
                                    case 'c':
                                    case 'd':
                                    case 'e':
                                    case 'f':
                                        cval = (cval << 4) + 10 + aChar - 'a';
                                        break;
                                    case 'A':
                                    case 'B':
                                    case 'C':
                                    case 'D':
                                    case 'E':
                                    case 'F':
                                        cval = (cval << 4) + 10 + aChar - 'A';
                                        break;
                                }
                            }
                            char cc = (char) cval;
                            escape = null;
                            consume(cc, p, false);
                            return;
                        }
                    } else {
//                        if (escape.toString().equals("\\") && (c == '{' || c == '}')) {
//                            value.append(c);
//                        } else {
                            value.append(escape);
                            value.append(c);
//                        }
//                        value.append(escape);
                        escape = null;
//                        value.append(c);
                    }
                    return;
                }
                if (exitCondition != null && exitCondition.test(c)) {
                    p.applyPopReject(c);
                } else {
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
    public NutsText toText() {
        String t = value.toString();
//        String q = NutsTextNodeWriterStringer.removeEscapes(t);
        return ws.text().forPlain(t);
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
