package net.thevpc.nuts.reserved.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class NReservedVersionIntervalParser {

    final int NEXT = 1;
    final int NEXT_COMMA = 2;
    final int EXPECT_V1 = 3;
    final int EXPECT_V_COMMA = 4;
    final int EXPECT_V2 = 5;
    final int EXPECT_CLOSE = 6;
    int t;
    int state = NEXT;
    int open = -1;
    int close = -1;
    String v1 = null;
    String v2 = null;
    List<NVersionInterval> dd = new ArrayList<>();

    public NReservedVersionIntervalParser() {
    }

    void reset() {
        open = -1;
        close = -1;
        v1 = null;
        v2 = null;
    }

    void addNextValue(String sval) {
        if (sval.endsWith("*")) {
            String min = sval.substring(0, sval.length() - 1);
            if (min.equals("")) {
                dd.add(new DefaultNVersionInterval(false, false, min, null));
            } else {
                String max = NVersion.of(min).inc(-1).getValue();
                dd.add(new DefaultNVersionInterval(true, false, min, max));
            }
        } else {
            dd.add(new DefaultNVersionInterval(true, true, sval, sval));
        }
    }

    void addNextInterval() {
        boolean inclusiveLowerBoundary = open == '[' && (v1 != null);
        boolean inclusiveUpperBoundary = close == ']' && (v2 != null);
        dd.add(new DefaultNVersionInterval(inclusiveLowerBoundary, inclusiveUpperBoundary, v1, v2));
        reset();
    }

    public NOptional<List<NVersionInterval>> parse(String version) {
        StreamTokenizer st = new StreamTokenizer(new StringReader(version));
        st.resetSyntax();
        st.whitespaceChars(0, 32);
        for (int i = 33; i < 256; i++) {
            switch ((char) i) {
                case '(':
                case ')':
                case ',':
                case '[':
                case ']': {
                    break;
                }
                default: {
                    st.wordChars(i, i);
                }
            }
        }
        try {
            while ((t = st.nextToken()) != StreamTokenizer.TT_EOF) {
                switch (state) {
                    case NEXT: {
                        switch (t) {
                            case StreamTokenizer.TT_WORD: {
                                addNextValue(st.sval);
                                state = NEXT_COMMA;
                                break;
                            }
                            case '[':
                            case ']':
                            case '(': {
                                open = t;
                                state = EXPECT_V1;
                                break;
                            }
                            case ',': {
                                //just ignore
                                break;
                            }
                            default: {
                                return NOptional.ofError(() -> NMsg.ofC("unexpected  %s", ((char) t)));
                            }
                        }
                        break;
                    }
                    case NEXT_COMMA: {
                        switch (t) {
                            case ',': {
                                state = NEXT;
                                break;
                            }
                            default: {
                                return NOptional.ofError(() -> NMsg.ofC("expected ',' found %s", ((char) t)));
                            }
                        }
                        break;
                    }
                    case EXPECT_V1: {
                        switch (t) {
                            case StreamTokenizer.TT_WORD: {
                                v1 = st.sval;
                                state = EXPECT_V_COMMA;
                                break;
                            }
                            case ',': {
                                state = EXPECT_V2;
                                break;
                            }
                            default: {
                                return NOptional.ofError(() -> NMsg.ofC("unexpected %s", ((char) t)));
                            }
                        }
                        break;

                    }
                    case EXPECT_V_COMMA: {
                        switch (t) {
                            case ',': {
                                state = EXPECT_V2;
                                break;
                            }
                            case ']': {
                                close = t;
                                v2 = v1;
                                addNextInterval();
                                state = NEXT_COMMA;
                                break;
                            }
                            case '[':
                            case ')': {
                                close = t;
                                v2 = v1; //the same?
                                addNextInterval();
                                state = NEXT_COMMA;
                                break;
                            }
                            default: {
                                return NOptional.ofError(() -> NMsg.ofC("unexpected %s", ((char) t)));
                            }
                        }
                        break;
                    }
                    case EXPECT_V2: {
                        switch (t) {
                            case StreamTokenizer.TT_WORD: {
                                v2 = st.sval;
                                state = EXPECT_CLOSE;
                                break;
                            }
                            case '[':
                            case ']':
                            case ')': {
                                close = t;
                                addNextInterval();
                                state = NEXT_COMMA;
                                break;
                            }
                            default: {
                                return NOptional.ofError(() -> NMsg.ofC("unexpected %s", ((char) t)));
                            }
                        }
                        break;
                    }
                    case EXPECT_CLOSE: {
                        switch (t) {
                            case '[':
                            case ']':
                            case ')': {
                                close = t;
                                addNextInterval();
                                state = NEXT_COMMA;
                                break;
                            }
                            default: {
                                return NOptional.ofError(() -> NMsg.ofC("unexpected %s", ((char) t)));
                            }
                        }
                        break;
                    }
                    default: {
                        return NOptional.ofError(() -> NMsg.ofC("unsupported state %s", state));
                    }
                }
            }
            if (state != NEXT_COMMA && state != NEXT) {
                return NOptional.ofError(() -> NMsg.ofC("invalid state %s", state));
            }
        } catch (IOException ex) {
            return NOptional.ofError(() -> NMsg.ofC("parse version failed: %s : ", version, ex));
        }
        return NOptional.ofNamed(dd, "version");
    }
}
