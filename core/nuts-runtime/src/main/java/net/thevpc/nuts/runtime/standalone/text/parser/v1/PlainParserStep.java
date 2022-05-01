package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTexts;
import net.thevpc.nuts.runtime.standalone.util.StringBuilder2;

import java.util.function.IntPredicate;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsText;

public class PlainParserStep extends ParserStep {

    char last = '\0';
    private StringBuilder escape;
//    private boolean spreadLines;
    private boolean lineStart;
    private StringBuilder2 value = new StringBuilder2();
    private NutsSession session;
    private DefaultNutsTextNodeParser.State state;
    private IntPredicate exitCondition;
    private boolean exitOnBrace;
//    private static int _COUNT=0;

    public PlainParserStep(char c, boolean lineStart, NutsSession session, DefaultNutsTextNodeParser.State state, IntPredicate exitCondition,boolean exitOnBrace) {
        this.state = state;
        this.exitCondition = exitCondition;
//        this.spreadLines = spreadLines;
        this.session = session;
        this.lineStart = state.isLineStart();
        if (c == '\\') {
            escape = new StringBuilder("\\");
        } else {
            value.append(c);
        }
        last = c;
        state.setLineStart(c == '\n');
        this.exitOnBrace = exitOnBrace;
//        _COUNT++;
//        System.err.println(" PlainParserStep "+c+" : "+value);
    }

    public PlainParserStep(String s, boolean spreadLines, boolean lineStart, NutsSession session, DefaultNutsTextNodeParser.State state, IntPredicate exitCondition,boolean preParsed,boolean exitOnBrace) {
        this.state = state;
        this.exitCondition = exitCondition;
        this.exitOnBrace = exitOnBrace;
//        this.spreadLines = spreadLines;
        this.session = session;
        this.lineStart = state.isLineStart();
        if(preParsed){
            value.append(s);
            last = value.last();
//            _COUNT++;
//            System.err.println(" PlainParserStep "+s+" : "+value);
            state.setLineStart(s.indexOf('\n')>=0);
        }else {
            char c=s.charAt(0);
            if (c == '\\') {
                escape = new StringBuilder("\\");
            } else {
                value.append(c);
            }
            last = c;
            state.setLineStart(c == '\n');
//            _COUNT++;
//            System.err.println(" PlainParserStep "+c+" : "+value);
            for (int i = 1; i < s.length(); i++) {
                char c2 = s.charAt(i);
                consume(c2, state, false);
            }
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
                        p.applyPopReplay(this, c);
                        return;
                    } else {
                        if (oldLast == c) {
                            value.readLast();
                            if (value.length() == 0) {
                                p.applyDropReplace(this, new StyledParserStep(c + "" + c, lineStart, session, state, exitOnBrace));
                                return;
                            } else {
                                p.applyPopReplace(this, new StyledParserStep(c + "" + c, lineStart, session, state, exitOnBrace));
                                return;
                            }
                        }
                        p.applyPopReplay(this, c);
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
                        p.applyPopReplay(this, c);
                        return;
                    } else {
                        p.applyPop(this);
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
                        p.applyPopReplay(this, c);
                        return;
                    } else {
                        p.applyPopReplay(this, c);
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
                    p.applyPop(this);
//                        if (!spreadLines) {
                    p.forceEnding();
//                        }
                } else {
                    p.applyPopReplay(this, c);
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
                if ((c=='}'&& exitOnBrace) || (exitCondition != null && exitCondition.test(c))) {
                    p.applyPopReplay(this, c);
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
        return NutsTexts.of(session).ofPlain(t);
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop(this);
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
