/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.NutsTextWriteConfiguration;
import net.thevpc.nuts.runtime.core.format.text.AbstractNutsTextNodeParser;
import net.thevpc.nuts.runtime.core.format.text.NutsTextNodeWriterStringer;
import net.thevpc.nuts.runtime.core.format.text.parser.steps.*;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsText;
import net.thevpc.nuts.NutsTextVisitor;

/**
 * @author thevpc
 */
public class DefaultNutsTextNodeParser extends AbstractNutsTextNodeParser {

    private static final Logger LOG = Logger.getLogger(DefaultNutsTextNodeParser.class.getName());

    private State state = new State();

    public DefaultNutsTextNodeParser(NutsSession session) {
        super(session);
    }

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
//                case '\"':
//                case '\'':
                case '`':
//                case '~':
//                case '@':
                case '#':
                case NutsConstants.Ntf.SILENT:
//                case '$':
//                case '£':
//                case '§':
//                case '_':
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
//    public static NutsText convert(FDocNode n) {
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
//                        return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), AnsiEscapeCommands.FG_GREEN);
//                    }
//                    case "``": {
//                        // this stays un-styled
//                        return new TextNodeUnStyled(p.getStart(), p.getValue(), new TextNodeEscaped(p.getValue()));
//                    }
//                    case "```": {
//                        //this is a comment ?
//                        return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), AnsiEscapeCommands.FG_GREEN);
//                    }
//                    case "`": {
//                        //this might be a command !!
//                        String v = p.getValue().trim();
//                        switch (v) {
//                            case FPrintCommands.MOVE_LINE_START: {
//                                return new DefaultNutsTextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), AnsiEscapeCommands.MOVE_LINE_START);
//                            }
//                            case FPrintCommands.LATER_RESET_LINE: {
//                                return new DefaultNutsTextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), AnsiEscapeCommands.LATER_RESET_LINE);
//                            }
//                            case FPrintCommands.MOVE_UP: {
//                                return new DefaultNutsTextNodeCommand(p.getStart(), p.getEnd(), p.getValue(), AnsiEscapeCommands.MOVE_UP);
//                            }
//                            default: {
//                                return wrap(new TextNodeEscaped(p.getValue()), p.getStart(), p.getEnd(), AnsiEscapeCommands.FG_GREEN);
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
//                                return new NutsTextAnchor(
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
//                        return new NutsTextStyled(p.getStart(), p.getEnd(), createStyle(p.getStart()), convert(p.getNode()));
//                    }
//                }
//                NutsText convert = convert(p.getNode());
//                return convert;
////                if (convert instanceof NutsTextPlain) {
////                    return new NutsTextPlain(((NutsTextPlain) convert).getValue());
////                } else {
////                    return new NutsTextPlain(convert.toString());
////                }
////                return new NutsTextPlain(String.valueOf(n.toString()));
//            } else if (n instanceof FDocNode.Title) {
//                FDocNode.Title p = (FDocNode.Title) n;
//                String sc = p.getStyleCode();
//                return new NutsTextTitle(p.getStart(), createStyle(sc), convert(p.getNode()));
//            }
//
//        }
//        throw new UnsupportedOperationException("Unsupported type " + n.getClass().getSimpleName());
//    }

//    private static NutsText wrap(NutsText t, String prefix, String suffix, AnsiEscapeCommand format) {
//        if (t instanceof NutsTextPlain) {
//            NutsTextPlain y = new NutsTextPlain(
//                    prefix +
//                            ((NutsTextPlain) t).getValue()
//                            + suffix
//            );
//            if (format == null) {
//                return y;
//            }
//            return new NutsTextStyled(prefix, suffix, format, y);
//        }
//        NutsTextList y = new NutsTextList(
//                new NutsTextPlain(prefix),
//                t,
//                new NutsTextPlain(suffix)
//        );
//        if (format == null) {
//            return y;
//        }
//        return new NutsTextStyled(prefix, suffix, format, y);
//    }
//    private static NutsText convert(List<FDocNode> n) {
//        if (n.size() == 1) {
//            return convert(n.get(0));
//        }
//        List<NutsTextNode> children = new ArrayList<>(n.size());
//        for (FDocNode node : n) {
//            children.add(convert(node));
//        }
//        return new NutsTextList(children.toArray(new NutsText[0]));
//    }
    public void reset() {
        state.reset();
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
            NutsTextNodeWriterStringer s = new NutsTextNodeWriterStringer(out, getSession());
            s.writeNode(this.parse(new StringReader(text)), new NutsTextWriteConfiguration().setFiltered(true));
            s.flush();
            return out.toString();
        } catch (Exception ex) {
            LOG.log(Level.FINEST, "error parsing : \n" + text, ex);
            return text;
        }
    }

    @Override
    public long parseIncremental(byte[] buf, int off, int len, NutsTextVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        String raw = new String(buf, off, len);
        char[] c = raw.toCharArray();
        return parseIncremental(c, 0, c.length, visitor);
    }

    @Override
    public long parseIncremental(char[] buf, int off, int len, NutsTextVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        write(buf, off, len);
        return state().consumeNodes(false, visitor);
    }

    @Override
    public long parseRemaining(NutsTextVisitor visitor) {
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
        private Stack<ParserStep> statusStack = new Stack<>();
        private boolean lineMode = false;
        private boolean lineStart = true;

        public State() {
            statusStack.push(new RootParserStep(true, getSession()));
        }

        public boolean isLineStart() {
            return lineStart;
        }

        public State setLineStart(boolean wasNewLine) {
            this.lineStart = wasNewLine;
            return this;
        }

        public boolean isSpreadLine() {
            return !lineStart;
        }

        public void applyPush(ParserStep r) {
            statusStack.push(r);
        }

        private void onNewChar(char c) {
            ParserStep st = statusStack.peek();
            boolean wasNewLine=false;
            if(st instanceof RootParserStep){
                wasNewLine=((RootParserStep) st).isEmpty();
            }
            st.consume(c, this, wasNewLine);
        }

        public ParserStep applyDrop() {
            return statusStack.pop();
        }

        public long consumeNodes(boolean greedy, NutsTextVisitor visitor) {
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

        public void applyAppendSibling(ParserStep r) {
            int len = statusStack.size();
            ParserStep parent = statusStack.elementAt(len - 2);
            parent.appendChild(r);
        }

        public void applyPopReplace(ParserStep r) {
            applyPop();
            statusStack.push(r);
        }

        public void applyDropReplacePreParsedPlain(String text,boolean exitOnBrace) {
            applyDropReplace(new PlainParserStep(text, lineStart, false, session, state, null,true,exitOnBrace));
        }

        public void applyDropReplace(ParserStep r) {
            ParserStep tt = statusStack.pop();
            //just drop
            statusStack.push(r);
        }

        public void applyNextChar(char c) {
            onNewChar(c);
        }

        public void applyPopReplay(char rejected) {
            ParserStep tt = statusStack.peek();
            ParserStep p = applyPop();
            boolean wasNewLine=
                    (tt instanceof NewLineParserStep)
                    ;
            p.consume(rejected, this, wasNewLine);
        }

        public void applyPush(String c, boolean spreadLines, boolean lineStart,boolean exitOnBrace) {
            if (c.length() > 0) {
                applyPush(c.charAt(0), spreadLines, lineStart, exitOnBrace);
                for (int i = 1; i < c.length(); i++) {
                    onNewChar(c.charAt(i));
                }
            }
        }

        public void applyPush(char c, boolean spreadLines, boolean lineStart, boolean exitOnBrace) {
            switch (c) {
                case '`': {
                    this.applyPush(new AntiQuote3ParserStep(c, spreadLines, getSession(),exitOnBrace));
                    break;
                }
                case '#': {
                    this.applyPush(new StyledParserStep(c, lineStart, getSession(), state(),exitOnBrace));
                    break;
                }
                case NutsConstants.Ntf.SILENT: {
                    //ignore...
                    break;
                }
                case '\n':
                case '\r': {
                    this.applyPush(new NewLineParserStep(c, getSession()));
                    if (lineMode) {
                        forceEnding();
                    }
                    break;
                }
                default: {
                    State state = state();
//                    state.setLineStart(lineStart);
                    this.applyPush(new PlainParserStep(c, lineStart, getSession(), state, null,exitOnBrace));
                }
            }
        }

        public boolean isIncomplete() {
            if (root().isEmpty()) {
                return false;
            }
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

        public NutsText consumeFDocNode() {
            ParserStep s = root().poll();
            if (s == null) {
                return null;
            }
            return s.toText();
        }

        public NutsText consumeNode(NutsTextVisitor visitor) {
//            JOptionPane.showMessageDialog(null,"consumeNode "+this);
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
            NutsText n = s.toText();
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
            return "State{" + (isIncomplete() ? "incomplete" : isEmpty() ? "empty" : String.valueOf(size()))
                    + (lineMode ? ",lineMode" : "")
                    + "," + statusStack
                    + "}";
        }

        public void reset() {
            statusStack.clear();
            lineMode = false;
            lineStart = true;
            statusStack.push(new RootParserStep(true, getSession()));
        }
    }

}
