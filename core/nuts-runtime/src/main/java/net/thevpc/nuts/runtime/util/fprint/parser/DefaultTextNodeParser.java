/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util.fprint.parser;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.fprint.AbstractTextNodeParser;
import net.thevpc.nuts.runtime.util.fprint.TextNodeVisitor;

/**
 * @author vpc
 */
public class DefaultTextNodeParser extends AbstractTextNodeParser {

    private static final Logger LOG = Logger.getLogger(DefaultTextNodeParser.class.getName());

    Stack<ParseAction> statusStack = new Stack<>();
    private boolean lineMode = false;

    public DefaultTextNodeParser() {
        statusStack.push(new AllParseAction(true));
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
        ParseAction s = root().available.peek();
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
    public long parseRemaining(TextNodeVisitor visitor) {
        return consumeNodes(true,visitor);
    }

    public TextNode consumeNode(TextNodeVisitor visitor) {
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
        if(s==null){
            return null;
        }
        TextNode n = FDocNodeHelper.convert(s.toFDocNode());
        if(visitor!=null) {
            visitor.visit(n);
        }
        return n;
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

        abstract void consume(char c, DefaultTextNodeParser p);

        abstract void appendChild(ParseAction tt);

        abstract FDocNode toFDocNode();

        abstract void forceEnding();

        abstract boolean isComplete();

    }

    static class AllParseAction extends ParseAction {
        boolean spreadLines;
        LinkedList<ParseAction> available = new LinkedList<>();

        public AllParseAction(boolean spreadLines) {
            this.spreadLines=spreadLines;
        }

        @Override
        void consume(char c, DefaultTextNodeParser p) {
            boolean lineStart=available.isEmpty();
            p.applyStart(c,spreadLines,lineStart);
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
            if(available.size()==1){
                return available.get(0).toFDocNode();
            }
            List<FDocNode> all = new ArrayList<>();
            boolean partial=false;
            for (ParseAction a : available) {
                if(!partial && !a.isComplete()){
                    partial=true;
                }
                all.add(a.toFDocNode());
            }
            return new FDocNode.List(all.toArray(new FDocNode[0]),partial);
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
        public void consume(char c, DefaultTextNodeParser p) {
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
            return new FDocNode.Escaped(start.toString(), end.toString(), value.toString(),!isComplete());
        }
    }

    static class TypedParseAction extends ParseAction {

        boolean spreadLines;
        boolean lineStart;
        boolean started = false;
        StringBuilder start = new StringBuilder();
        StringBuilder end = new StringBuilder();
        List<ParseAction> children = new ArrayList<>();

        public TypedParseAction(char c,boolean spreadLines,boolean lineStart) {
            start.append(c);
            this.spreadLines=spreadLines;
            this.lineStart = lineStart;
        }

        public TypedParseAction(String c,boolean spreadLines,boolean lineStart) {
            start.append(c);
            this.spreadLines=spreadLines;
            this.lineStart =lineStart;
        }

        @Override
        void appendChild(ParseAction tt) {
            children.add(tt);
        }

        @Override
        public void consume(char c, DefaultTextNodeParser p) {
            if(!spreadLines && (c=='\n' || c=='\r')){
                p.applyPopReject(c);
                return;
            }
            if (!started) {
                if (c == start.charAt(0)) {
                    if (start.length() < 4) {
                        start.append(c);
                    } else {
                        started = true;
                        p.applyStart(c,spreadLines,false);
                    }
                } else {
                    char endChar = endOf(start.charAt(0));
                    started = true;
                    if (c == endChar) {
                        end.append(c);
                        if (end.length() >= start.length()) {
                            p.applyPop();
                        }

                    }else if (lineStart && c == ')') {
                        //this is a title
                        p.applyDropReplace(new TitleParseAction(start.toString()+c));
                    } else {
                        p.applyStart(c,spreadLines,false);
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
                        p.applyStart(c,spreadLines,false);
                    } else {
                        String y = end.toString();
                        end.delete(0, end.length());
                        p.applyPush(new TypedParseAction(y,spreadLines, lineStart));
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
            if(children.size()==1){
                return new FDocNode.Typed(start.toString(), end.toString(), children.get(0).toFDocNode(),!children.get(0).isComplete());
            }
            List<FDocNode> all = new ArrayList<>();
            boolean partial=false;
            for (ParseAction a : children) {
                if(!partial && !a.isComplete()){
                    partial=true;
                }
                all.add(a.toFDocNode());
            }
            return new FDocNode.Typed(start.toString(), end.toString(), new FDocNode.List(all.toArray(new FDocNode[0]),partial),partial);
        }

    }

    static class TitleParseAction extends ParseAction {

        StringBuilder start = new StringBuilder();
        List<ParseAction> children = new ArrayList<>();

        public TitleParseAction(String c) {
            start.append(c);
        }

        @Override
        void appendChild(ParseAction tt) {
            children.add(tt);
        }

        @Override
        public void consume(char c, DefaultTextNodeParser p) {
            if(c==' ' && children.isEmpty()) {
                start.append(c);
            }else if(c=='\n' || c=='\r'){
                p.applyPopReject(c);
            } else {
                p.applyStart(c,true,false);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Title(" + CoreStringUtils.dblQuote(start.toString()));
            for (ParseAction parseAction : children) {
                sb.append(",");
                sb.append(parseAction.toString());
            }
            return sb.append(")").toString();
        }

        public boolean isComplete() {
            return true;
        }

        @Override
        void forceEnding() {
        }

        @Override
        FDocNode toFDocNode() {
            String s=start.toString();
            if(children.size()==1){
                return new FDocNode.Title(s, children.get(0).toFDocNode(),!children.get(0).isComplete());
            }
            List<FDocNode> all = new ArrayList<>();
            boolean partial=false;
            for (ParseAction a : children) {
                if(!partial && !a.isComplete()){
                    partial=true;
                }
                all.add(a.toFDocNode());
            }
            return new FDocNode.Title(s, new FDocNode.List(all.toArray(new FDocNode[0]),partial),partial);
        }

    }

    static class PlainParseAction extends ParseAction {

        char last = '\0';
        private boolean wasEscape;
        private boolean spreadLines;
        private boolean lineStart;
        private StringBuilder value = new StringBuilder();

        public PlainParseAction(char c,boolean spreadLines,boolean lineStart) {
            this.spreadLines=spreadLines;
            this.lineStart = lineStart;
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
        public void consume(char c, DefaultTextNodeParser p) {
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
                                p.applyDropReplace(new TypedParseAction(c + "" + c,spreadLines,lineStart));
                                return;
                            } else {
                                p.applyPopReplace(new TypedParseAction(c + "" + c,spreadLines,lineStart));
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
                    if(spreadLines) {
                        if (wasEscape) {
                            wasEscape = false;
                        }
                        value.append(c);
                        p.applyPop();
                        if (p.lineMode) {
                            p.forceEnding();
                        }
                    }else{
                        if (wasEscape) {
                            wasEscape = false;
                            value.append(c);
                            p.applyPop();
                            if (p.lineMode) {
                                p.forceEnding();
                            }
                        }else{
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
            return new FDocNode.Plain(value.toString(),!isComplete());
        }

        @Override
        void forceEnding() {

        }

    }

    @Override
    public long parseIncremental(byte[] buf, int off, int len, TextNodeVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        String raw = new String(buf, off, len);
        char[] c = raw.toCharArray();
        return parseIncremental(c,0,c.length,visitor);
    }

    @Override
    public long parseIncremental(char[] buf, int off, int len, TextNodeVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        write(buf, off, len);
        return consumeNodes(false, visitor);
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

    void applyStart(char c,boolean spreadLines,boolean lineStart) {
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
                this.applyPush(new TypedParseAction(c,spreadLines,false));
                break;
            }
            case '\n':
            case '\r': {
                this.applyPush(new PlainParseAction(c,spreadLines,true));
                applyPop();
                if (lineMode) {
                    forceEnding();
                }
                break;
            }
            default: {
                this.applyPush(new PlainParseAction(c,spreadLines,lineStart));
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
        DefaultTextNodeParser pp = new DefaultTextNodeParser();
        StringBuilder sb = new StringBuilder();
        try {
            byte[] bytes = text.getBytes();
            pp.parseIncremental(bytes,0,bytes.length,null);
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
     * @param str str
     * @return  escaped text
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
        return "DefaultTextNodeParser{" + (isIncomplete() ? "incomplete" : isEmpty() ? "empty" : String.valueOf(size())) +
                (lineMode ? ",lineMode" : "") +
                "," + statusStack
                + "}";
    }

    public long consumeNodes(boolean greedy, TextNodeVisitor visitor) {
        long count=0;
        while ((consumeNode(visitor)) != null) {
            count++;
        }
        if (greedy) {
            if (forceEnding()) {
                while ((consumeNode(visitor)) != null) {
                    count++;
                }
            }
        }
        return count;
    }


}
