/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.bundledlibs.fprint.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.FormattedPrintStreamParser;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.util.FormattedPrintStreamUtils;

/**
 *
 * @author vpc
 */
public class FormattedPrintStreamNodePartialParser implements FormattedPrintStreamParser {

    List<FDocNode> all = new ArrayList<FDocNode>();
    StringBuilder curr = new StringBuilder();
    Stack<ParseAction> statusStack = new Stack<>();

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

    public void forceEnding() {
        while (true) {
            ParseAction s = statusStack.peek();
            if (!(s instanceof AllParseAction)) {
                s.forceEnding();
                ParseAction tt = statusStack.pop();
                ParseAction parent = statusStack.peek();
                parent.appendChild(tt);
            } else {
                break;
            }
        }
    }

    static enum ConsumeResutType {
        POP,
        POP_REJECT,
        POP_REPLACE,
        DROP_REPLACE,
        PUSH,
        CONTINUE,
    }

    static class ConsumeResut {

        ConsumeResutType accepted;
        ParseAction replacement;
        char rejected;

        public ConsumeResut(ConsumeResutType accepted, ParseAction replacement, char rejected) {
            this.accepted = accepted;
            this.replacement = replacement;
            this.rejected = rejected;
        }

        public ConsumeResut(ConsumeResutType accepted, ParseAction replacement) {
            this.accepted = accepted;
            this.replacement = replacement;
            this.rejected = '\0';
        }

    }

    static abstract class ParseAction {

        abstract ConsumeResut consume(char c);

        abstract void appendChild(ParseAction tt);

        abstract FDocNode toFDocNode();

        abstract void forceEnding();
    }

    static class AllParseAction extends ParseAction {

        LinkedList<ParseAction> available = new LinkedList<>();

        public AllParseAction() {
        }

        @Override
        ConsumeResut consume(char c) {
            return new ConsumeResut(ConsumeResutType.PUSH, consumeStart(c));
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
        public ConsumeResut consume(char c) {
            switch (status) {
                case 0: {
                    if (c == start.charAt(0)) {
                        if (start.length() < 4) {
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
                    return new ConsumeResut(ConsumeResutType.CONTINUE, null);
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
                                return new ConsumeResut(ConsumeResutType.POP, null);
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
                    return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                }
                case 2: {
                    if (c == start.charAt(0)) {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            return new ConsumeResut(ConsumeResutType.POP, null);
                        } else {
                            return new ConsumeResut(ConsumeResutType.CONTINUE, null);
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
                        return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                    }
                }
            }
            throw new IllegalArgumentException("Unexpected");
        }

        @Override
        public String toString() {
            return "Quoted{" + "value=" + value + ", start=" + start + ", end=" + end + '}';
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
        public ConsumeResut consume(char c) {
            if (!started) {
                if (c == start.charAt(0)) {
                    if (start.length() < 4) {
                        start.append(c);
                        return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                    } else {
                        started = true;
                        return new ConsumeResut(ConsumeResutType.PUSH, consumeStart(c));
                    }
                } else {
                    char endChar = endOf(start.charAt(0));
                    started = true;
                    if (c == endChar) {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            return new ConsumeResut(ConsumeResutType.POP, null);
                        } else {
                            return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                        }
                    } else {
                        return new ConsumeResut(ConsumeResutType.PUSH, consumeStart(c));
                    }
                }
            } else {
                char endChar = endOf(start.charAt(0));
                if (c == endChar) {
                    if (end.length() >= start.length()) {
                        return new ConsumeResut(ConsumeResutType.POP_REJECT, null, c);
                    } else {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            return new ConsumeResut(ConsumeResutType.POP, null);
                        } else {
                            return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                        }
                    }
                } else {
                    if (end.length() == 0) {
                        return new ConsumeResut(ConsumeResutType.PUSH, consumeStart(c));
                    } else {
                        String y = end.toString();
                        end.delete(0, end.length());
                        return new ConsumeResut(ConsumeResutType.PUSH, new TypedParseAction(y));
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "CMD_STATUS{" + "started=" + started + ", start=" + start + ", end=" + end + '}';
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
        public ConsumeResut consume(char c) {
            char oldLast = last;
            last = c;
            switch (c) {
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
                case '+': {
                    if (wasEscape) {
                        wasEscape = false;
                        value.append(c);
                        return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                    } else {
                        if (oldLast == c) {
                            value.deleteCharAt(value.length() - 1);
                            if (value.length() == 0) {
                                return new ConsumeResut(ConsumeResutType.DROP_REPLACE, new TypedParseAction(c + "" + c));
                            } else {
                                return new ConsumeResut(ConsumeResutType.POP_REPLACE, new TypedParseAction(c + "" + c));
                            }
                        }
                        return new ConsumeResut(ConsumeResutType.POP_REJECT, null, c);
                    }
                }

                case '`':
                case '"':
                case '\'':
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
                        return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                    } else {
                        return new ConsumeResut(ConsumeResutType.POP_REJECT, null, c);
                    }
                }
                case '\n': {
                    value.append(c);
                    return new ConsumeResut(ConsumeResutType.POP, null);
                }
                case '\r': {
                    value.append(c);
                    return new ConsumeResut(ConsumeResutType.POP, null);
                }
                case '\\': {
                    if (wasEscape) {
                        wasEscape = false;
                        value.append(c);
                    } else {
                        wasEscape = true;
                    }
                    return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                }
                default: {
                    value.append(c);
                    return new ConsumeResut(ConsumeResutType.CONTINUE, null);
                }
            }
        }

        @Override
        public String toString() {
            return "CHAR_STATUS{" + "wasEscape=" + wasEscape + ", value=" + value + '}';
        }

        @Override
        FDocNode toFDocNode() {
            return new FDocNode.Plain(value.toString());
        }

        @Override
        void forceEnding() {

        }

    }

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
        applyConsumeResut(st.consume(s));
    }

    private void applyConsumeResut(ConsumeResut r) {
        switch (r.accepted) {
            case CONTINUE: {
                //DO NOTHGING
                break;
            }
            case PUSH: {
                //DO NOTHGING
                statusStack.push(r.replacement);
                break;
            }
            case POP: {
                ParseAction tt = statusStack.pop();
                ParseAction parent = statusStack.peek();
                parent.appendChild(tt);
                break;
            }
            case POP_REPLACE: {
                ParseAction tt = statusStack.pop();
                ParseAction parent = statusStack.peek();
                parent.appendChild(tt);

                statusStack.push(r.replacement);
                break;
            }
            case DROP_REPLACE: {
                ParseAction tt = statusStack.pop();
                //just drop

                statusStack.push(r.replacement);
                break;
            }
            case POP_REJECT: {
                ParseAction tt = statusStack.pop();
                ParseAction parent = statusStack.peek();
                parent.appendChild(tt);

                applyConsumeResut(parent.consume(r.rejected));
                break;
            }
        }
    }

    private void debug() {
        for (ParseAction status : statusStack) {
            System.out.println(status);
        }
    }

    static ParseAction consumeStart(char c) {
        switch (c) {
            case '`':
            case '"':
            case '\'': {
                return new QuotedParseAction(c);
            }
//            case '$':
//            case '£':
//            case '§':
//            case '_':
//            case '~':
//            case '%':
//            case '¤':
//            case '@':
//            case '^':
//            case '#':
//            case '¨':
//            case '=':
//            case '*':
//            case '+':
            case '(':
            case '[':
            case '{':
            case '<': {
                return new TypedParseAction(c);
            }
            default: {
                return new PlainParseAction(c);
            }
        }
    }

    @Override
    public String escapeText(String str) {
        return FormattedPrintStreamUtils.escapeText(str);
    }
}
