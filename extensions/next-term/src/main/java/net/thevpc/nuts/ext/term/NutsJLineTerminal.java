/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.ext.term;

import java.awt.Color;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.*;
import org.jline.reader.*;
import org.jline.terminal.Cursor;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jline.reader.impl.LineReaderImpl;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Created by vpc on 2/20/17.
 */
@NutsComponentScope(NutsComponentScopeType.PROTOTYPE)
public class NutsJLineTerminal extends NutsSystemTerminalBaseImpl {

    private static final Logger LOG = Logger.getLogger(NutsJLineTerminal.class.getName());
    private Terminal terminal;
    private LineReader reader;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private InputStream in;
    private NutsCommandAutoCompleteResolver autoCompleteResolver;
    private NutsCommandHistory commandHistory;
    private String commandHighlighter;

    public NutsJLineTerminal() {
    }

    private AttributedString toAttributedString(NutsText n, NutsTextStyles styles, NutsSession session) {
        switch (n.getType()) {
            case PLAIN: {
                styles = NutsTexts.of(session).getTheme().toBasicStyles(styles, session);
                NutsTextPlain p = (NutsTextPlain) n;
                if (styles.isPlain()) {
                    return new AttributedString(p.getText());
                } else {
                    AttributedStyle s = AttributedStyle.DEFAULT;
                    for (int i = 0; i < styles.size(); i++) {
                        NutsTextStyle ii = styles.get(i);
                        switch (ii.getType()) {
                            case BACK_COLOR: {
                                s = s.background(ii.getVariant());
                                break;
                            }
                            case BACK_TRUE_COLOR: {
                                Color c = new Color(ii.getVariant());
                                s = s.background(c.getRed(), c.getGreen(), c.getBlue());
                                break;
                            }
                            case FORE_COLOR: {
                                s = s.foreground(ii.getVariant());
                                break;
                            }
                            case FORE_TRUE_COLOR: {
                                Color c = new Color(ii.getVariant());
                                s = s.foreground(c.getRed(), c.getGreen(), c.getBlue());
                                break;
                            }
                            case BLINK: {
                                s = s.blink();
                                break;
                            }
                            case UNDERLINED: {
                                s = s.underline();
                                break;
                            }
                            case STRIKED: {
                                s = s.crossedOut();
                                break;
                            }
                            case ITALIC: {
                                s = s.italic();
                                break;
                            }
                            case BOLD: {
                                s = s.bold();
                                break;
                            }
                        }
                    }
                    return new AttributedString(p.getText(), s);
                }
            }
            case ANCHOR: {
                return new AttributedString("");
            }
            case COMMAND: {
                return new AttributedString("");
            }
            case CODE: {
                NutsTextCode p = (NutsTextCode) n;
                NutsText nn = p.highlight(session);
                return toAttributedString(nn, NutsTextStyles.PLAIN, session);
            }
            case TITLE: {
                NutsTextTitle p = (NutsTextTitle) n;
                return toAttributedString(p.getChild(), NutsTextStyles.PLAIN, session);
            }
            case LINK: {
                NutsTextLink p = (NutsTextLink) n;
                return toAttributedString(
                        NutsTexts.of(session).ofPlain(p.getValue()),
                        styles.append(NutsTextStyle.underlined()),
                        session);
            }
            case LIST: {
                NutsTextList p = (NutsTextList) n;
                AttributedStringBuilder b = new AttributedStringBuilder();
                for (NutsText a : p) {
                    b.append(toAttributedString(a, styles, session));
                }
                return b.toAttributedString();
            }
            case STYLED: {
                NutsTextStyled p = (NutsTextStyled) n;
                if (styles.isPlain()) {
                    return toAttributedString(p.getChild(), p.getStyles(), session);
                } else {
                    return toAttributedString(
                            p.getChild(),
                            styles.append(p.getStyles()),
                            session);
                }
            }
        }
        return new AttributedString(n.toString());
    }

    public void prepare(NutsSession session) {
        if (terminal != null) {
            return;
        }
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.streams(System.in, System.out);
        builder.system(true);
        builder.dumb(false);

        try {
            terminal = builder.build();
        } catch (Throwable ex) {
            //unable to create system terminal
            //Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
            throw new UncheckedIOException(new IOException("unable to create JLine system terminal: " + ex.getMessage(), ex));
        }
        reader = LineReaderBuilder.builder()
                .completer(new NutsJLineCompleter(session, this))
                .highlighter(new Highlighter() {
                    @Override
                    public AttributedString highlight(LineReader reader, String buffer) {
                        NutsTexts text = NutsTexts.of(session);
                        String ct = getCommandHighlighter();
                        if (NutsBlankable.isBlank(ct)) {
                            ct = "system";
                        }
                        NutsText n = NutsTexts.of(session).ofCode(ct, buffer).highlight(session);
                        return toAttributedString(n, NutsTextStyles.PLAIN, session);
                    }

                    @Override
                    public void setErrorPattern(Pattern ptrn) {

                    }

                    @Override
                    public void setErrorIndex(int i) {

                    }

                })
                .terminal(terminal)
                //                .completer(completer)
                //                .parse(parse)
                .build();
        reader.unsetOpt(LineReader.Option.INSERT_TAB);
        reader.setVariable(LineReader.HISTORY_FILE, session.locations().getWorkspaceLocation().resolve("history").normalize().toFile());
        if (reader instanceof LineReaderImpl) {
            ((LineReaderImpl) reader).setHistory(new NutsJLineHistory(reader, session, this));
        }
        this.out = NutsPrintStream.of(
                new TransparentPrintStream(
                        new PrintStream(reader.getTerminal().output(), true),
                        System.out
                ),
                NutsTerminalMode.FORMATTED, this, session);
        this.err = NutsPrintStream.of(
                new TransparentPrintStream(
                        new PrintStream(reader.getTerminal().output(), true),
                        System.err
                ),
                NutsTerminalMode.FORMATTED, this, session);//.setColor(NutsPrintStream.RED);
        this.in = new TransparentInputStream(reader.getTerminal().input(), System.in);
    }

    protected void close() {
        if (reader != null) {
            try {
                reader.getTerminal().close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "error closing terminal", ex);
            }
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        NutsSession session = criteria.getSession();
        try {
            prepare(session);
        } catch (Exception ex) {
            NutsLogger.of(NutsJLineTerminal.class, session)
                    .with().level(Level.FINEST).verb(NutsLogVerb.FAIL).error(ex)
                    .log(NutsMessage.jstyle("unable to create NutsJLineTerminal. ignored."));
            return NO_SUPPORT;
        }
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        prepare(session);
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
        }
        String readLine = null;
        try {
            readLine = reader.readLine(NutsTexts.of(session).toText(message).toString());
        } catch (UserInterruptException e) {
            throw new NutsJLineInterruptException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return readLine;
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
        prepare(session);
        if (out == null) {
            return reader.readLine(NutsTexts.of(session).toText(message).toString(), '*').toCharArray();
        } else {
            //should I use some out??
        }
        return reader.readLine(NutsTexts.of(session).toText(message).toString(), '*').toCharArray();
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public NutsPrintStream getOut() {
        return out;
    }

    @Override
    public NutsPrintStream getErr() {
        return err;
    }

    @Override
    public NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        return autoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return true;
    }

    @Override
    public NutsJLineTerminal setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver) {
        this.autoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NutsCommandHistory getCommandHistory() {
        return commandHistory;
    }

    @Override
    public NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history) {
        this.commandHistory = history;
        return this;
    }

    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    public NutsJLineTerminal setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NutsTerminalCommand command, NutsSession session) {
        switch (command.getName()) {
            case NutsTerminalCommand.Ids.GET_CURSOR: {
                org.jline.terminal.Cursor c = terminal.getCursorPosition(new IntConsumer() {
                    @Override
                    public void accept(int value) {
                        //
                    }
                });
                if (c != null) {
                    return new NutsSystemTerminalBase.Cursor(
                            c.getX(), c.getY()
                    );
                }
                return null;
            }
            case NutsTerminalCommand.Ids.GET_SIZE: {
                org.jline.terminal.Size c = terminal.getSize();
                if (c != null) {
                    return new NutsSystemTerminalBase.Size(
                            c.getColumns(), c.getRows()
                    );
                }
                return null;
            }
            default: {
                String s = NutsAnsiTermHelper.of(session).command(command, session);
                if (s != null) {
                    try {
                        reader.getTerminal().output().write(s.getBytes());
                    } catch (IOException e) {
                        throw new NutsIOException(session, e);
                    }
                }
                return null;
            }
        }
    }

    public void setStyles(NutsTextStyles styles, NutsSession session) {
        String s = NutsAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            try {
                reader.getTerminal().output().write(s.getBytes());
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    }

    private static class TransparentInputStream extends FilterInputStream implements NutsInputStreamTransparentAdapter {

        private InputStream root;

        public TransparentInputStream(InputStream in, InputStream root) {
            super(in);
            this.root = root;
        }

        @Override
        public InputStream baseInputStream() {
            return root;
        }
    }

    private static class TransparentPrintStream extends PrintStream implements NutsOutputStreamTransparentAdapter {

        private OutputStream root;

        public TransparentPrintStream(OutputStream out, OutputStream root) {
            super(out, true);
            this.root = root;
        }

        @Override
        public OutputStream baseOutputStream() {
            return root;
        }

    }

}
