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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.*;
import net.thevpc.jshell.JShellInterruptException;
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
    private NutsCommandAutoCompleteProcessor autoCompleteResolver;

    public NutsJLineTerminal() {
    }

    @Override
    public NutsCommandAutoCompleteProcessor getAutoCompleteResolver() {
        return autoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported(){
        return true;
    }

    @Override
    public NutsJLineTerminal setAutoCompleteResolver(NutsCommandAutoCompleteProcessor autoCompleteResolver) {
        this.autoCompleteResolver = autoCompleteResolver;
        return this;
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
                throw new UncheckedIOException(new IOException("unable to create JLine system terminal: "+ex.getMessage(),ex));
            }
            reader = LineReaderBuilder.builder()
                    .completer(new NutsJLineCompleter(workspace,this))
                    .terminal(terminal)
                    //                .completer(completer)
                    //                .parse(parse)
                    .build();
            reader.setVariable(LineReader.HISTORY_FILE, workspace.locations().getWorkspaceLocation().resolve("history").normalize().toFile());
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
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
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
