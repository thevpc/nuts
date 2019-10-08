/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.term;

import net.vpc.app.nuts.*;
import net.vpc.common.javashell.JShellInterruptException;
import org.jline.reader.*;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
@NutsPrototype
public class NutsJLineTerminal implements NutsSystemTerminalBase,NutsWorkspaceAware {

    private static final Logger LOG = Logger.getLogger(NutsJLineTerminal.class.getName());
    private Terminal terminal;
    private LineReader reader;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;
    private NutsTerminalMode outMode;
    private NutsTerminalMode errMode;

    public NutsJLineTerminal() {
    }

    @Override
    public NutsTerminalBase setOutMode(NutsTerminalMode mode) {
        this.outMode = mode;
        return this;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return outMode;
    }

    @Override
    public NutsTerminalBase setErrMode(NutsTerminalMode mode) {
        this.errMode = mode;
        return this;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return errMode;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        if(workspace!=null) {
            this.workspace = workspace;
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.streams(System.in, System.out);
            builder.system(true);
            builder.dumb(false);

            try {
                terminal = builder.build();
            } catch (Throwable ex) {
                //unable to create system terminal
                //Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
                throw new UncheckedIOException(new IOException(ex));
            }
            reader = LineReaderBuilder.builder()
                    .completer(new NutsJLineCompleter(workspace))
                    .terminal(terminal)
                    //                .completer(completer)
                    //                .parse(parse)
                    .build();
            reader.setVariable(LineReader.HISTORY_FILE, workspace.config().getWorkspaceLocation().resolve("history").normalize().toFile());
            ((LineReaderImpl) reader).setHistory(new NutsJLineHistory(reader, workspace));
            this.out = workspace.io().createPrintStream(
                    new TransparentPrintStream(
                            new PrintStream(reader.getTerminal().output(), true),
                            System.out
                    ),
                    NutsTerminalMode.FORMATTED);
            this.err = workspace.io().createPrintStream(
                    new TransparentPrintStream(
                            new PrintStream(reader.getTerminal().output(), true),
                            System.err
                    ),
                    NutsTerminalMode.FORMATTED);//.setColor(NutsPrintStream.RED);
            this.in = new TransparentInputStream(reader.getTerminal().input(), System.in);
        }else{
            try {
                reader.getTerminal().close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error closing terminal", ex);
            }
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = System.out;
        }
        String readLine = null;
        try {
            out.printf(prompt, params);
            out.flush();
            readLine = reader.readLine("");
        } catch (UserInterruptException e) {
            throw new JShellInterruptException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return readLine;
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = System.out;
        }
        out.printf(prompt, params);
        out.flush();
        return reader.readLine("", '*').toCharArray();
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public PrintStream getErr() {
        return err;
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

    @Override
    public NutsTerminalBase getParent() {
        return null;
    }
}
