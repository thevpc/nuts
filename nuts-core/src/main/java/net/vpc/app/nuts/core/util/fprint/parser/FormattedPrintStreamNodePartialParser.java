/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.fprint.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import net.vpc.app.nuts.core.util.fprint.FormattedPrintStreamParser;
import net.vpc.app.nuts.core.util.fprint.util.FormattedPrintStreamUtils;

/**
 *
 * @author vpc
 */
public class FormattedPrintStreamNodePartialParser implements FormattedPrintStreamParser {

    List<FDocNode> all = new ArrayList<>();
    StringBuilder curr = new StringBuilder();
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
    public TextNode consumeNode() {
        ParseAction s = root().available.poll();
        if (s == null) {
            return null;
        }
        return FDocNodeHelper.convert(s.toFDocNode());
    }

    private AllParseAction root() {
        return (AllParseAction) statusStack.get(0);
    }

    public boolean isEmpty() {
        return statusStack.isEmpty() || root().available.isEmpty();
    }

    @Override
    public void forceEnding() {
        while (true) {
            ParseAction s = statusStack.peek();
            if (!(s instanceof AllParseAction)) {
                if (s != null) {
                    s.forceEnding();
                }
                ParseAction tt = statusStack.pop();
                ParseAction parent = statusStack.peek();
                parent.appendChild(tt);
            } else {
                break;
            }
        }
    }

    static abstract class ParseAction {

        abstract void consume(char c, FormattedPrintStreamNodePartialParser p);

        abstract void appendChild(ParseAction tt);

        abstract FDocNode toFDocNode();

        abstract void forceEnding();
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
            return "START_STATUS{" + "available=" + available + '}';
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
            StringBuilder sb = new StringBuilder("<Q>" + start);
            sb.append(value);
            sb.append("<").append(status).append(">");
            sb.append(end);
            if (escape) {
                sb.append("<ESCAPED>");
            }
            return sb.toString();
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
            StringBuilder sb = new StringBuilder("<T>" + start);
            if (!started) {
                sb.append("<NEW>");
            }
            for (ParseAction parseAction : children) {
                sb.append(parseAction.toString());
            }
            sb.append(end);
            return sb.toString();
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
                    value.append(c);
//                    p.appContinue();
                    return;
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("<P>" + value);
            if (wasEscape) {
                sb.append("<EScCAPE>");
            }
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

    private void debug() {
        for (ParseAction status : statusStack) {
            System.out.println(status);
        }
    }

    void applyStart(char c) {
        switch (c) {
            case '`':
            case '"':
            case '\'': 
            case '%': 
            {
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
        return FormattedPrintStreamUtils.escapeText(str);
    }
}
