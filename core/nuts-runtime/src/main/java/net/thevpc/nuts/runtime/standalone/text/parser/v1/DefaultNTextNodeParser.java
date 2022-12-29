/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.AbstractNTextNodeParser;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextVisitor;

import java.util.Objects;
import java.util.Stack;

/**
 * @author thevpc
 */
public class DefaultNTextNodeParser extends AbstractNTextNodeParser {

    private State state = new State();

    public DefaultNTextNodeParser(NSession session) {
        super(session);
    }


    public void reset() {
        state.reset();
    }

    @Override
    public void offer(String c) {
        offer(c.toCharArray());
    }

    public void offer(char[] str) {
        offer(str, 0, str.length);
    }

    public void offer(char[] s, int offset, int len) {
        for (int i = offset; i < len; i++) {
            offer(s[i]);
        }
    }

    public void offer(char s) {
        state().onNewChar(s);
    }

    public State state() {
        return state;
    }


    @Override
    public long parseIncremental(char[] buf, int off, int len, NTextVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        offer(buf, off, len);
        return state().consumeNodes(false, visitor);
    }

    @Override
    public long parseRemaining(NTextVisitor visitor) {
        return state().consumeNodes(true, visitor);
    }

    @Override
    public boolean isIncomplete() {
        return state().isIncomplete();
    }

    @Override
    public String toString() {
        return "NTextNodeParser{" + state() + "}";
    }

    @Override
    public NText read() {
        return state().consumeNode(null);
    }


    @Override
    public NText readFully() {
        return state().consumeNodeGreedy(null);
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
            _push(r);
        }

        private void onNewChar(char c) {
            ParserStep st = statusStack.peek();
            boolean wasNewLine = false;
            if (st instanceof RootParserStep) {
                wasNewLine = ((RootParserStep) st).isEmpty();
            }
            st.consume(c, this, wasNewLine);
        }

        public ParserStep applyDrop(ParserStep me) {
            return _pop(me);
        }

        public NText consumeNodeGreedy(NTextVisitor visitor) {
            NText n = consumeNode(visitor);
            if (n != null) {
                return n;
            }
            forceEnding();
            return consumeNode(visitor);
        }

        public long consumeNodes(boolean greedy, NTextVisitor visitor) {
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


        public synchronized ParserStep applyPop(ParserStep me) {
            if (statusStack.size() < 2) {
                System.err.println("problem");
            }
            ParserStep tt = _pop(me);
            ParserStep parent = statusStack.peek();
            parent.appendChild(tt);
            return parent;
        }

        public void applyAppendSibling(ParserStep r) {
            int len = statusStack.size();
            ParserStep parent = statusStack.elementAt(len - 2);
            parent.appendChild(r);
        }

        public void applyPopReplace(ParserStep me, ParserStep r) {
            applyPop(me);
            _push(r);
        }

        public void applyDropReplacePreParsedPlain(ParserStep me, String text, boolean exitOnBrace) {
            applyDropReplace(me, new PlainParserStep(text, lineStart, false, session, state, null, true, exitOnBrace));
        }

        public void applyDropReplace(ParserStep me, ParserStep r) {
            ParserStep tt = _pop(me);
            //just drop
            _push(r);
        }

        private void _push(ParserStep r) {
            if (statusStack.isEmpty()) {
                System.err.println("problem");
            } else if (statusStack.peek() instanceof PlainParserStep && r instanceof PlainParserStep) {
                System.err.println("problem");
            }
            statusStack.push(r);
        }

        private ParserStep _pop(ParserStep me) {
            if (statusStack.size() < 2) {
                System.err.println("problem");
            }
            ParserStep r = statusStack.peek();
            if (!Objects.equals(r, me)) {
                System.err.println("problem");
            }
            return statusStack.pop();
        }

        public void applyNextChar(char c) {
            onNewChar(c);
        }

        public void applyPopReplay(ParserStep me, char rejected) {
            ParserStep tt = statusStack.peek();
            ParserStep p = applyPop(me);
            boolean wasNewLine =
                    (tt instanceof NewLineParserStep);
            p.consume(rejected, this, wasNewLine);
        }

        public void applyPush(String c, boolean spreadLines, boolean lineStart, boolean exitOnBrace) {
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
                    this.applyPush(new AntiQuote3ParserStep(c, spreadLines, getSession(), exitOnBrace));
                    break;
                }
                case '#': {
                    this.applyPush(new StyledParserStep(c, lineStart, getSession(), state(), exitOnBrace));
                    break;
                }
                case NConstants.Ntf.SILENT: {
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
                    this.applyPush(new PlainParserStep(c, lineStart, getSession(), state, null, exitOnBrace));
                }
            }
        }

        public boolean isIncomplete() {
            RootParserStep root = root();
            if (root == null) {
                return false;
            }
            if (root.isEmpty()) {
                return false;
            }
            ParserStep s = root.peek();
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
            if (statusStack.isEmpty()) {
                return null;
            }
            return (RootParserStep) statusStack.get(0);
        }

        public int size() {
            RootParserStep root = root();
            return statusStack.size() + (root == null ? 0 : root.size());
        }

        public boolean isEmpty() {
            RootParserStep root = root();
            return statusStack.isEmpty() || root == null || root.isEmpty();
        }

        public NText consumeFDocNode() {
            RootParserStep root = root();
            if (root == null) {
                return null;
            }
            ParserStep s = root.poll();
            if (s == null) {
                return null;
            }
            return s.toText();
        }

        public NText consumeNode(NTextVisitor visitor) {
//            JOptionPane.showMessageDialog(null,"consumeNode "+this);
            RootParserStep root = root();
            if (root == null) {
                return null;
            }
            ParserStep s = root.poll();
            if (s == null) {
                while (!statusStack.isEmpty()) {
                    ParserStep s2 = statusStack.peek();
                    if (!(s2 instanceof RootParserStep)) {
                        if (s2 != null && s2.isComplete()) {
                            ParserStep tt = _pop(s2);
                            ParserStep parent = statusStack.peek();
                            parent.appendChild(tt);
                        } else if (s2 == null) {
                            ParserStep tt = _pop(s2);
                            //do nothing
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                root = root();
                s = root == null ? null : root.poll();
            }
            if (s == null) {
                return null;
            }
            NText n = s.toText();
            if (visitor != null) {
                visitor.visit(n);
            }
            return n;
        }


        public synchronized boolean forceEnding() {
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

        public synchronized void reset() {
            statusStack.clear();
            lineMode = false;
            lineStart = true;
            statusStack.push(new RootParserStep(true, getSession()));
        }
    }

}
