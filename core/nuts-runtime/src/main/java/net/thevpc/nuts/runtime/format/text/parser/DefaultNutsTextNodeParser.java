/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.format.text.*;
import net.thevpc.nuts.runtime.format.text.parser.steps.AntiQuote3ParserStep;
import net.thevpc.nuts.runtime.format.text.parser.steps.ParserStep;
import net.thevpc.nuts.runtime.format.text.parser.steps.PlainParserStep;
import net.thevpc.nuts.runtime.format.text.parser.steps.RootParserStep;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thevpc
 */
public class DefaultNutsTextNodeParser extends AbstractNutsTextNodeParser {

    private static final Logger LOG = Logger.getLogger(DefaultNutsTextNodeParser.class.getName());

    private State state = new State();

    public DefaultNutsTextNodeParser(NutsWorkspace ws) {
        super(ws);
    }

//    public static NutsTextNode convert(FDocNode n) {
//        if (n != null) {
//            if (n instanceof FDocNode.Plain) {
//                FDocNode.Plain p = (FDocNode.Plain) n;
//                return new TextNodeEscaped(p.getValue());
//            } else if (n instanceof FDocNode.Escaped) {
//                FDocNode.Escaped p = (FDocNode.Escaped) n;
//                switch (p.getStart()) {
//                    case "\"":
//                    case "\"\"":
//                    case "\"\"\"":
//                    case "'":
//                    case "''":
//                    case "'''": {
//                        return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
//                    }
//                    case "``": {
//                        // this stays un-styled
//                        return new TextNodeUnStyled(p.getStart(), p.getValue(), new TextNodeEscaped(p.getValue()));
//                    }
//                    case "```": {
//                        //this is a comment ?
//                        return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
//                    }
//                    case "`": {
//                        //this might be a command !!
//                        String v = p.getValue().trim();
//                        switch (v) {
//                            case FPrintCommands.MOVE_LINE_START: {
//                                return new DefaultNutsTextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), TextFormats.MOVE_LINE_START);
//                            }
//                            case FPrintCommands.LATER_RESET_LINE: {
//                                return new DefaultNutsTextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), TextFormats.LATER_RESET_LINE);
//                            }
//                            case FPrintCommands.MOVE_UP: {
//                                return new DefaultNutsTextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), TextFormats.MOVE_UP);
//                            }
//                            default: {
//                                return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), TextFormats.FG_GREEN);
//                            }
//                        }
//                    }
//                }
//
//                return new TextNodeEscaped(p.getValue());
//            } else if (n instanceof FDocNode.List) {
//                FDocNode.List p = (FDocNode.List) n;
//                FDocNode[] children = p.getValues();
//                if (children.length == 1) {
//                    return convert(children[0]);
//                }
//                return convert(Arrays.asList(children));
//            } else if (n instanceof FDocNode.Typed) {
//                FDocNode.Typed p = (FDocNode.Typed) n;
//                switch (p.getStart()) {
//                    case "(":
//                    case "{":
//                    case "<": {
//                        return new TextNodeUnStyled(p.getStart(), p.getEnd(), convert(p.getNode()));
//                    }
//                    case "[": {
//                        if (p.getNode() instanceof FDocNode.Plain) {
//                            String s = ((FDocNode.Plain) p.getNode()).getValue();
//                            if (s.startsWith("#") && s.length() > 1 && s.indexOf('#', 1) < 0) {
//                                return new NutsTextNodeAnchor(
//                                        p.getStart() + "#",
//                                        p.getEnd(),
//                                        s.substring(1));
//                            }
//                        }
//                        return new TextNodeUnStyled(p.getStart(), p.getEnd(), convert(p.getNode()));
//                    }
//                    case "~~":
//                    case "~~~":
//                    case "~~~~":
//                    case "~~~~~":
//                    case "##":
//                    case "###":
//                    case "####":
//                    case "#####":
//                    case "######":
//                    case "#######":
//                    case "########":
//                    case "#########":
//                    case "##########":
//                    case "@@":
//                    case "@@@":
//                    case "@@@@":
//                    case "@@@@@":
//                    case "@@@@@@":
//                    case "@@@@@@@":
//                    case "@@@@@@@@":
//                    case "@@@@@@@@@":
//                    case "@@@@@@@@@@": {
//                        return new NutsTextNodeStyled(p.getStart(), p.getEnd(), createStyle(p.getStart()), convert(p.getNode()));
//                    }
//                }
//                NutsTextNode convert = convert(p.getNode());
//                return convert;
////                if (convert instanceof NutsTextNodePlain) {
////                    return new NutsTextNodePlain(((NutsTextNodePlain) convert).getValue());
////                } else {
////                    return new NutsTextNodePlain(convert.toString());
////                }
////                return new NutsTextNodePlain(String.valueOf(n.toString()));
//            } else if (n instanceof FDocNode.Title) {
//                FDocNode.Title p = (FDocNode.Title) n;
//                String sc = p.getStyleCode();
//                return new NutsTextNodeTitle(p.getStart(), createStyle(sc), convert(p.getNode()));
//            }
//
//        }
//        throw new UnsupportedOperationException("Unsupported type " + n.getClass().getSimpleName());
//    }

//    private static NutsTextNode wrap(NutsTextNode t, String prefix, String suffix, TextFormat format) {
//        if (t instanceof NutsTextNodePlain) {
//            NutsTextNodePlain y = new NutsTextNodePlain(
//                    prefix +
//                            ((NutsTextNodePlain) t).getValue()
//                            + suffix
//            );
//            if (format == null) {
//                return y;
//            }
//            return new NutsTextNodeStyled(prefix, suffix, format, y);
//        }
//        NutsTextNodeList y = new NutsTextNodeList(
//                new NutsTextNodePlain(prefix),
//                t,
//                new NutsTextNodePlain(suffix)
//        );
//        if (format == null) {
//            return y;
//        }
//        return new NutsTextNodeStyled(prefix, suffix, format, y);
//    }


//    private static NutsTextNode convert(List<FDocNode> n) {
//        if (n.size() == 1) {
//            return convert(n.get(0));
//        }
//        List<NutsTextNode> children = new ArrayList<>(n.size());
//        for (FDocNode node : n) {
//            children.add(convert(node));
//        }
//        return new NutsTextNodeList(children.toArray(new NutsTextNode[0]));
//    }


    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param str str
     * @return escaped text
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
                case '~':
                case '@':
                case '#':
                case 'ø':
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
//                case '(':
//                case '[':
//                case '{':
//                case '<':
//                case ')':
//                case ']':
//                case '}':
//                case '>':
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




    public void write(char[] str) {
        write(str, 0, str.length);
    }

    public void write(char[] s, int offset, int len) {
        for (int i = offset; i < len; i++) {
            write(s[i]);
        }
    }

    public void write(char s) {
        state().onNewChar(s);
    }


    public State state() {
        return state;
    }

    @Override
    public String escapeText(String str) {
        return escapeText0(str);
    }

    public String filterText(String text) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NutsTextNodeWriterStringer s = new NutsTextNodeWriterStringer(out);
            s.writeNode(this.parse(new StringReader(text)), new NutsTextNodeWriteConfiguration().setFiltered(true));
            s.flush();
            return out.toString();
        } catch (Exception ex) {
            LOG.log(Level.FINEST, "error parsing : \n" + text, ex);
            return text;
        }
    }

    @Override
    public long parseIncremental(byte[] buf, int off, int len, NutsTextNodeVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        String raw = new String(buf, off, len);
        char[] c = raw.toCharArray();
        return parseIncremental(c, 0, c.length, visitor);
    }

    @Override
    public long parseIncremental(char[] buf, int off, int len, NutsTextNodeVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        write(buf, off, len);
        return state().consumeNodes(false, visitor);
    }

    @Override
    public long parseRemaining(NutsTextNodeVisitor visitor) {
        return state().consumeNodes(true, visitor);
    }

    @Override
    public boolean isIncomplete() {
        return state().isIncomplete();
    }

    @Override
    public String toString() {
        return "DefaultNutsTextNodeParser{" + state() + "}";
    }


    public class State {
        Stack<ParserStep> statusStack = new Stack<>();
        private boolean lineMode = false;
        private boolean wasNewLine = false;

        public State() {
            statusStack.push(new RootParserStep(true,ws));
        }

        public boolean isWasNewLine() {
            return wasNewLine;
        }

        public State setWasNewLine(boolean wasNewLine) {
            this.wasNewLine = wasNewLine;
            return this;
        }

        public void applyPush(ParserStep r) {
            statusStack.push(r);
        }

        private void onNewChar(char c) {
            ParserStep st = statusStack.peek();
            st.consume(c, state());
        }

        public ParserStep applyDrop() {
            return statusStack.pop();
        }

        public long consumeNodes(boolean greedy, NutsTextNodeVisitor visitor) {
            long count = 0;
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

        public ParserStep applyPop() {
            ParserStep tt = statusStack.pop();
            ParserStep parent = statusStack.peek();
            parent.appendChild(tt);
            return parent;
        }

        public void applyPopReplace(ParserStep r) {
            applyPop();
            statusStack.push(r);
        }

        public void applyDropReplace(ParserStep r) {
            ParserStep tt = statusStack.pop();
            //just drop
            statusStack.push(r);
        }

        public void applyPopReject(char rejected) {
            applyPop().consume(rejected, this);
        }

        public void applyStart(char c, boolean spreadLines, boolean lineStart) {
            switch (c) {
                case '`':{
                    this.applyPush(new AntiQuote3ParserStep(c, spreadLines,ws));
                    break;
                }
                case 'ø':{
                    //ignore...
                    break;
                }
                case '\n':
                case '\r': {
                    this.applyPush(new PlainParserStep(c, spreadLines, true,ws,state()));
                    applyPop();
                    if (lineMode) {
                        forceEnding();
                    }
                    break;
                }
                default: {
                    this.applyPush(new PlainParserStep(c, spreadLines, lineStart,ws,state()));
                }
            }
        }

        public boolean isIncomplete() {
            ParserStep s = root().peek();
            if (s == null) {
                for (ParserStep parserStep : statusStack) {
                    if (!parserStep.isComplete()) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }


        private RootParserStep root() {
            return (RootParserStep) statusStack.get(0);
        }

        public int size() {
            if (statusStack.isEmpty()) {
                return 0;
            }
            return statusStack.size() + root().size();
        }

        public boolean isEmpty() {
            return statusStack.isEmpty() || root().isEmpty();
        }

        public NutsTextNode consumeFDocNode() {
            ParserStep s = root().poll();
            if (s == null) {
                return null;
            }
            return s.toNode();
        }

        public NutsTextNode consumeNode(NutsTextNodeVisitor visitor) {
            ParserStep s = root().poll();
            if (s == null) {
                while (!statusStack.isEmpty()) {
                    ParserStep s2 = statusStack.peek();
                    if (!(s2 instanceof RootParserStep)) {
                        if (s2 != null && s2.isComplete()) {
                            ParserStep tt = statusStack.pop();
                            ParserStep parent = statusStack.peek();
                            parent.appendChild(tt);
                        } else if (s2 == null) {
                            ParserStep tt = statusStack.pop();
                            //do nothing
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                s = root().poll();
            }
            if (s == null) {
                return null;
            }
            NutsTextNode n = s.toNode();
            if (visitor != null) {
                visitor.visit(n);
            }
            return n;
        }


        public boolean forceEnding() {
            boolean ok = false;
            while (true) {
                ParserStep s = statusStack.peek();
                if (!(s instanceof RootParserStep)) {
                    if (s != null) {
                        s.end(this);
                    }
                    ok = true;
                } else {
                    break;
                }
            }
            return ok;
        }

        @Override
        public String toString() {
            return "State{" + (isIncomplete() ? "incomplete" : isEmpty() ? "empty" : String.valueOf(size())) +
                    (lineMode ? ",lineMode" : "") +
                    "," + statusStack
                    + "}";
        }
    }

//    private void debug() {
//        for (ParserStep status : statusStack) {
//            System.out.println(status);
//        }
//    }


}
