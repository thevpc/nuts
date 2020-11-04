/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util.fprint.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.fprint.FormattedPrintStreamParser;

/**
 * @author vpc
 */
public class FormattedPrintStreamNodePartialParser implements FormattedPrintStreamParser {

    private static final Logger LOG = Logger.getLogger(FormattedPrintStreamNodePartialParser.class.getName());

    Stack<ParseAction> statusStack = new Stack<>();
    private boolean lineMode = false;

    public FormattedPrintStreamNodePartialParser() {
        statusStack.push(new AllParseAction());
    }

    public FDocNode consumeFDocNode() {
        ParseAction s = root().available.poll();
        if (s == null) {
            return null;
        }
        return s.toFDocNode();
    }

    @Override
    public boolean isIncomplete() {
        ParseAction s = root().available.poll();
        if (s == null) {
            for (ParseAction parseAction : statusStack) {
                if (!parseAction.isComplete()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public TextNode consumeNode() {
        ParseAction s = root().available.poll();
        if (s == null) {
            while (!statusStack.isEmpty()) {
                ParseAction s2 = statusStack.peek();
                if (!(s2 instanceof AllParseAction)) {
                    if (s2 != null && s2.isComplete()) {
                        ParseAction tt = statusStack.pop();
                        ParseAction parent = statusStack.peek();
                        parent.appendChild(tt);
                    } else if (s2 == null) {
                        ParseAction tt = statusStack.pop();
                        //do nothing
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            s = root().available.poll();
        }
        return s == null ? null : FDocNodeHelper.convert(s.toFDocNode());
    }

    private AllParseAction root() {
        return (AllParseAction) statusStack.get(0);
    }

    public int size() {
        if (statusStack.isEmpty()) {
            return 0;
        }
        return statusStack.size() + root().available.size();
    }

    public boolean isEmpty() {
        return statusStack.isEmpty() || root().available.isEmpty();
    }

    @Override
    public boolean forceEnding() {
        boolean ok = false;
        while (true) {
            ParseAction s = statusStack.peek();
            if (!(s instanceof AllParseAction)) {
                if (s != null) {
                    if (s.isComplete()) {

                    } else {
                        s.forceEnding();
                    }
                }
                ParseAction tt = statusStack.pop();
                ParseAction parent = statusStack.peek();
                parent.appendChild(tt);
                ok = true;
            } else {
                break;
            }
        }
        return ok;
    }

    static abstract class ParseAction {

        abstract void consume(char c, FormattedPrintStreamNodePartialParser p);

        abstract void appendChild(ParseAction tt);

        abstract FDocNode toFDocNode();

        abstract void forceEnding();

        abstract boolean isComplete();

    }

    static class AllParseAction extends ParseAction {

        LinkedList<ParseAction> available = new LinkedList<>();

        public AllParseAction() {
        }

        @Override
        void consume(char c, FormattedPrintStreamNodePartialParser p) {
            p.applyStart(c);
        }

        @Override
        void appendChild(ParseAction tt) {
            available.add(tt);
        }

        @Override
        public String toString() {
            return "Root(" + available + ')';
        }

        @Override
        FDocNode toFDocNode() {
            List<FDocNode> all = new ArrayList<>();
            for (ParseAction a : available) {
                all.add(a.toFDocNode());
            }
            return new FDocNode.List(all.toArray(new FDocNode[0]));
        }

        @Override
        void forceEnding() {

        }

        public boolean isComplete() {
            return true;
        }
    }

    static class QuotedParseAction extends ParseAction {

        int status = 0;
        boolean escape = false;
        StringBuilder start = new StringBuilder();
        StringBuilder end = new StringBuilder();
        StringBuilder value = new StringBuilder();

        public QuotedParseAction(char c) {
            start.append(c);
        }

        public QuotedParseAction(String c) {
            start.append(c);
        }

        @Override
        void appendChild(ParseAction tt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void consume(char c, FormattedPrintStreamNodePartialParser p) {
            switch (status) {
                case 0: {
                    if (c == start.charAt(0)) {
                        if (start.length() < 1) {
                            start.append(c);
                        } else {
                            status = 2;
                            end.append(c);
                            p.applyPop();
                        }
                    } else {
                        switch (c) {
                            case '\\': {
                                escape = true;
                                break;
                            }
                            default: {
                                value.append(c);
                            }
                        }
                        status = 1;
                    }
//                    p.applyContinue();
                    return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
                }
                case 1: {
                    if (escape) {
                        escape = false;
                        value.append(c);
                    } else {
                        if (c == start.charAt(0)) {
                            status = 2;
                            end.append(c);
                            if (end.length() >= start.length()) {
                                p.applyPop();
                                return;//new ConsumeResut(ConsumeResutType.POP, null);
                            }
                        } else {
                            switch (c) {
                                case '\\': {
                                    escape = true;
                                    break;
                                }
                                default: {
                                    value.append(c);
                                }
                            }
                        }
                    }
                    return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
                }
                case 2: {
                    if (c == start.charAt(0)) {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            p.applyPop();
                            return;//new ConsumeResut(ConsumeResutType.POP, null);
                        } else {
                            return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
                        }
                    } else {
                        value.append(end);
                        end.delete(0, end.length());
                        switch (c) {
                            case '\\': {
                                escape = true;
                                break;
                            }
                            default: {
                                value.append(c);
                            }
                        }
                        status = 1;
                        return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
                    }
                }
            }
            throw new IllegalArgumentException("Unexpected");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Quoted(" + CoreStringUtils.dblQuote(start.toString()));
            sb.append(",");
            sb.append(CoreStringUtils.dblQuote(value.toString()));
            sb.append(",status=").append(status == 0 ? "EXPECT_START" : status == 1 ? "EXPECT_CONTENT" : status == 2 ? "EXPECT_END" : String.valueOf(status));
            sb.append(",end=");
            sb.append(end);
            if (escape) {
                sb.append(",<ESCAPED>");
            }
            sb.append(isComplete() ? "" : ",incomplete");
            return sb.append(")").toString();
        }

        public boolean isComplete() {
            return status == 2 && end.length() == start.length();
        }

        @Override
        void forceEnding() {
            while (end.length() < start.length()) {
                end.append(start.charAt(0));
            }
        }

        @Override
        FDocNode toFDocNode() {
            return new FDocNode.Escaped(start.toString(), end.toString(), value.toString());
        }
    }

    static class TypedParseAction extends ParseAction {

        boolean started = false;
        StringBuilder start = new StringBuilder();
        StringBuilder end = new StringBuilder();
        List<ParseAction> children = new ArrayList<>();

        public TypedParseAction(char c) {
            start.append(c);
        }

        public TypedParseAction(String c) {
            start.append(c);
        }

        @Override
        void appendChild(ParseAction tt) {
            children.add(tt);
        }

        @Override
        public void consume(char c, FormattedPrintStreamNodePartialParser p) {
            if (!started) {
                if (c == start.charAt(0)) {
                    if (start.length() < 4) {
                        start.append(c);
                    } else {
                        started = true;
                        p.applyStart(c);
                    }
                } else {
                    char endChar = endOf(start.charAt(0));
                    started = true;
                    if (c == endChar) {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            p.applyPop();
                        }
                    } else {
                        p.applyStart(c);
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
                            p.applyPop();
                        }
                    }
                } else {
                    if (end.length() == 0) {
                        p.applyStart(c);
                    } else {
                        String y = end.toString();
                        end.delete(0, end.length());
                        p.applyPush(new TypedParseAction(y));
                    }
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Typed(" + CoreStringUtils.dblQuote(start.toString()));
            if (!started) {
                sb.append(",<NEW>");
            }
            for (ParseAction parseAction : children) {
                sb.append(",");
                sb.append(parseAction.toString());
            }
            sb.append(",END(").append(CoreStringUtils.dblQuote(end.toString())).append(")");
            sb.append(isComplete() ? "" : ",incomplete");
            return sb.append(")").toString();
        }

        public boolean isComplete() {
            return started && end.length() == start.length();
        }

        @Override
        void forceEnding() {
            while (end.length() < start.length()) {
                end.append(endOf(start.charAt(0)));
            }
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

        @Override
        FDocNode toFDocNode() {
            List<FDocNode> all = new ArrayList<>();
            for (ParseAction a : children) {
                all.add(a.toFDocNode());
            }
            return new FDocNode.Typed(start.toString(), end.toString(), new FDocNode.List(all.toArray(new FDocNode[0])));
        }

    }

    static class PlainParseAction extends ParseAction {

        char last = '\0';
        private boolean wasEscape;
        private StringBuilder value = new StringBuilder();

        public PlainParseAction(char c) {
            if (c == '\\') {
                wasEscape = true;
            } else {
                value.append(c);
            }
            last = c;
        }

        @Override
        void appendChild(ParseAction tt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void consume(char c, FormattedPrintStreamNodePartialParser p) {
            char oldLast = last;
            last = c;
            switch (c) {
                case '$':
                case '£':
                case '§':
                case '_':
                case '~':
                case '¤':
                case '@':
                case '^':
                case '#':
                case '¨':
                case '=':
                case '*':
                case '+': {
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
                                p.applyDropReplace(new TypedParseAction(c + "" + c));
                                return;
                            } else {
                                p.applyPopReplace(new TypedParseAction(c + "" + c));
                                return;
                            }
                        }
                        p.applyPopReject(c);
                        return;
                    }
                }

                case '`':
                case '"':
                case '\'':
                case '%':
                case '(':
                case '[':
                case '{':
                case '<':
                case ')':
                case ']':
                case '}':
                case '>': {
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
                    if (wasEscape) {
                        wasEscape = false;
                    }
                    value.append(c);
                    p.applyPop();
                    if (p.lineMode) {
                        p.forceEnding();
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

        public boolean isComplete() {
            return true;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Plain(" + CoreStringUtils.dblQuote(value.toString()));
            if (wasEscape) {
                sb.append(",<ESCAPE>");
            }
            sb.append(isComplete() ? "" : ",incomplete");
            sb.append(")");
            return sb.toString();
        }

        @Override
        FDocNode toFDocNode() {
            return new FDocNode.Plain(value.toString());
        }

        @Override
        void forceEnding() {

        }

    }

    @Override
    public void take(String str) {
        char[] c = str.toCharArray();
        write(c, 0, c.length);
    }

    public void write(char[] str) {
        write(str, 0, str.length);
    }

    public void write(char[] s, int offset, int len) {
        for (int i = offset; i < len; i++) {
            write(s[i]);
        }
    }

    public void write(char s) {
        ParseAction st = statusStack.peek();
        st.consume(s, this);
    }

    private void applyPush(ParseAction r) {
        statusStack.push(r);
    }

    private void applyPop() {
        ParseAction tt = statusStack.pop();
        ParseAction parent = statusStack.peek();
        parent.appendChild(tt);
    }

    private void applyPopReplace(ParseAction r) {
        ParseAction tt = statusStack.pop();
        ParseAction parent = statusStack.peek();
        parent.appendChild(tt);
        statusStack.push(r);
    }

    private void applyDropReplace(ParseAction r) {
        ParseAction tt = statusStack.pop();
        //just drop
        statusStack.push(r);
    }

    private void applyPopReject(char rejected) {
        ParseAction tt = statusStack.pop();
        ParseAction parent = statusStack.peek();
        parent.appendChild(tt);
        parent.consume(rejected, this);
    }

//    private void debug() {
//        for (ParseAction status : statusStack) {
//            System.out.println(status);
//        }
//    }

    void applyStart(char c) {
        switch (c) {
            case '`':
            case '"':
            case '\'':
            case '%': {
                this.applyPush(new QuotedParseAction(c));
                break;
            }
            case '(':
            case '[':
            case '{':
            case '<': {
                this.applyPush(new TypedParseAction(c));
                break;
            }
            case '\n':
            case '\r': {
                this.applyPush(new PlainParseAction(c));
                applyPop();
                if (lineMode) {
                    forceEnding();
                }
                break;
            }
            default: {
                this.applyPush(new PlainParseAction(c));
            }
        }
    }

    @Override
    public String escapeText(String str) {
        return escapeText0(str);
    }

    public String filterText(String text) {
        return filterText0(text);
    }

    public static String filterText0(String text) {
        //create new instance not to alter current state
        FormattedPrintStreamNodePartialParser pp = new FormattedPrintStreamNodePartialParser();
        StringBuilder sb = new StringBuilder();
        try {
            pp.take(text);
            pp.forceEnding();
            FDocNode tn = null;
            while ((tn = pp.consumeFDocNode()) != null) {
                escape(tn, sb);
            }
            return sb.toString();
        } catch (Exception ex) {
            LOG.log(Level.FINEST, "Error parsing : \n" + text, ex);
            return text;
        }

    }

    private static void escape(FDocNode tn, StringBuilder sb) {
        if (tn instanceof FDocNode.Plain) {
            sb.append(((FDocNode.Plain) tn).getValue());
        } else if (tn instanceof FDocNode.List) {
            for (FDocNode fDocNode : ((FDocNode.List) tn).getValues()) {
                escape(fDocNode, sb);
            }
        } else if (tn instanceof FDocNode.Typed) {
            FDocNode.Typed rr = (FDocNode.Typed) tn;
            if (rr.getStart().length() == 1) {
                sb.append(rr.getStart());
                escape(rr.getNode(), sb);
                sb.append(rr.getEnd());
            } else {
                escape(rr.getNode(), sb);
            }
        } else if (tn instanceof FDocNode.Escaped) {
            FDocNode.Escaped rr = (FDocNode.Escaped) tn;
            sb.append(rr.getStart());
            sb.append(((FDocNode.Escaped) tn).getValue());
            sb.append(rr.getEnd());
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }

    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param str
     * @return
     */
    public static String escapeText0(String str) {
        if (str == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\"':
                case '\'':
                case '`':
                case '$':
                case '£':
                case '§':
                case '_':
                case '~':
                case '%':
                case '¤':
                case '@':
                case '^':
                case '#':
                case '¨':
                case '=':
                case '*':
                case '+':
                case '(':
                case '[':
                case '{':
                case '<':
                case ')':
                case ']':
                case '}':
                case '>':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "FormattedPrintStreamNodePartialParser{" + (isIncomplete() ? "incomplete" : isEmpty() ? "empty" : String.valueOf(size())) +
                (lineMode ? ",lineMode" : "") +
                "," + statusStack
                + "}";
    }


}
