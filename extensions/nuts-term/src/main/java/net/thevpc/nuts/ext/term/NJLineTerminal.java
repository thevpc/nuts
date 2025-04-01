/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.ext.term;

import java.awt.Color;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NAnsiTermHelper;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import org.jline.reader.*;
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
@NComponentScope(NScopeType.PROTOTYPE)
public class NJLineTerminal extends NSystemTerminalBaseImpl {

    private static final Logger LOG = Logger.getLogger(NJLineTerminal.class.getName());
    private Terminal terminal;
    private LineReader reader;
    private NPrintStream out;
    private NPrintStream err;
    private InputStream in;
    private NCmdLineAutoCompleteResolver autoCompleteResolver;
    private NCmdLineHistory commandHistory;
    private String commandHighlighter;

    public NJLineTerminal() {
        super();
    }

    private AttributedString toAttributedString(NText n, NTextStyles styles) {
        switch (n.getType()) {
            case PLAIN: {
                styles = NTexts.of().getTheme().toBasicStyles(styles,false);
                NTextPlain p = (NTextPlain) n;
                if (styles.isPlain()) {
                    return new AttributedString(p.getText());
                } else {
                    AttributedStyle s = AttributedStyle.DEFAULT;
                    for (int i = 0; i < styles.size(); i++) {
                        NTextStyle ii = styles.get(i);
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
                NTextCode p = (NTextCode) n;
                NText nn = p.highlight();
                return toAttributedString(nn, NTextStyles.PLAIN);
            }
            case TITLE: {
                NTextTitle p = (NTextTitle) n;
                return toAttributedString(p.getChild(), NTextStyles.PLAIN);
            }
            case LINK: {
                NTextLink p = (NTextLink) n;
                return toAttributedString(
                        NText.ofPlain(p.getText()),
                        styles.append(NTextStyle.underlined())
                );
            }
            case INCLUDE: {
                NTextLink p = (NTextLink) n;
                return toAttributedString(
                        NText.ofList(
                                NText.ofPlain("include"),
                                NText.ofPlain(p.getText())
                        ),
                        styles.append(NTextStyle.danger())
                );
            }
            case LIST: {
                NTextList p = (NTextList) n;
                AttributedStringBuilder b = new AttributedStringBuilder();
                for (NText a : p) {
                    b.append(toAttributedString(a, styles));
                }
                return b.toAttributedString();
            }
            case STYLED: {
                NTextStyled p = (NTextStyled) n;
                if (styles.isPlain()) {
                    return toAttributedString(p.getChild(), p.getStyles());
                } else {
                    return toAttributedString(
                            p.getChild(),
                            styles.append(p.getStyles())
                    );
                }
            }
        }
        return new AttributedString(n.toString());
    }

    public void prepare() {
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
                .completer(new NJLineCompleter(this))
                .highlighter(new Highlighter() {
                    @Override
                    public AttributedString highlight(LineReader reader, String buffer) {
                        NTexts text = NTexts.of();
                        String ct = getCommandHighlighter();
                        if (NBlankable.isBlank(ct)) {
                            ct = "system";
                        }
                        NText n = NText.ofCode(ct, buffer).highlight();
                        return toAttributedString(n, NTextStyles.PLAIN);
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
        reader.setVariable(LineReader.HISTORY_FILE, NWorkspace.of().getWorkspaceLocation().resolve("history").normalize().toPath().get());
        if (reader instanceof LineReaderImpl) {
            ((LineReaderImpl) reader).setHistory(new NJLineHistory(reader, this));
        }
        this.out = NPrintStream.of(
                new TransparentPrintStream(
                        new PrintStream(reader.getTerminal().output(), true),
                        System.out
                ),
                NTerminalMode.FORMATTED, this);
        this.err = NPrintStream.of(
                new TransparentPrintStream(
                        new PrintStream(reader.getTerminal().output(), true),
                        System.err
                ),
                NTerminalMode.FORMATTED, this);//.setColor(NutsPrintStream.RED);
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
    public int getSupportLevel(NSupportLevelContext criteria) {
        try {
            prepare();
        } catch (Exception ex) {
            NLog.of(NJLineTerminal.class)
                    .with().level(Level.FINEST).verb(NLogVerb.FAIL).error(ex)
                    .log(NMsg.ofPlain("unable to create NutsJLineTerminal. ignored."));
            return NConstants.Support.NO_SUPPORT;
        }
        return NConstants.Support.DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(NPrintStream out, NMsg message) {
        prepare();
//        if (out == null) {
//            out = getOut();
//        }
//        if (out == null) {
//            out = NIO.of().stdout();
//        }
        String readLine = null;
        try {
            readLine = reader.readLine(NText.of(message).toString());
        } catch (UserInterruptException e) {
            throw new NJLineInterruptException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return readLine;
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg message) {
        prepare();
        if (out == null) {
            return reader.readLine(NText.of(message).toString(), '*').toCharArray();
        } else {
            //should I use some out??
        }
        return reader.readLine(NText.of(message).toString(), '*').toCharArray();
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public NPrintStream getOut() {
        return out;
    }

    @Override
    public NPrintStream getErr() {
        return err;
    }

    @Override
    public NCmdLineAutoCompleteResolver getAutoCompleteResolver() {
        return autoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return true;
    }

    @Override
    public NJLineTerminal setCommandAutoCompleteResolver(NCmdLineAutoCompleteResolver autoCompleteResolver) {
        this.autoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NCmdLineHistory getCommandHistory() {
        return commandHistory;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCmdLineHistory history) {
        this.commandHistory = history;
        return this;
    }

    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    public NJLineTerminal setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NTerminalCmd command, NPrintStream printStream) {
        switch (command.getName()) {
            case NTerminalCmd.Ids.GET_CURSOR: {
                org.jline.terminal.Cursor c = terminal.getCursorPosition(new IntConsumer() {
                    @Override
                    public void accept(int value) {
                        //
                    }
                });
                if (c != null) {
                    return new NSystemTerminalBase.Cursor(
                            c.getX(), c.getY()
                    );
                }
                return null;
            }
            case NTerminalCmd.Ids.GET_SIZE: {
                org.jline.terminal.Size c = terminal.getSize();
                if (c != null) {
                    return new NSystemTerminalBase.Size(
                            c.getColumns(), c.getRows()
                    );
                }
                return null;
            }
            default: {
                String s = NAnsiTermHelper.of().command(command);
                if (s != null) {
                    byte[] bytes = s.getBytes();
                    printStream.writeRaw(bytes,0,bytes.length);
//                    try {
//                        reader.getTerminal().output().write(bytes);
//                    } catch (IOException e) {
//                        throw new NIOException(session, e);
//                    }
                }
                return null;
            }
        }
    }

    public void setStyles(NTextStyles styles, NPrintStream printStream) {
        String s = NAnsiTermHelper.of().styled(styles);
        if (s != null) {
            byte[] bytes = s.getBytes();
            printStream.writeRaw(bytes,0,bytes.length);
//            try {
//                reader.getTerminal().output().write(s.getBytes());
//            } catch (IOException e) {
//                throw new NIOException(session, e);
//            }
        }
    }

    private static class TransparentInputStream extends FilterInputStream implements NInputStreamTransparentAdapter {

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

    private static class TransparentPrintStream extends PrintStream implements NOutputStreamTransparentAdapter {

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
