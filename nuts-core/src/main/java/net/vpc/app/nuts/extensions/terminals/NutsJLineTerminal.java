/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import org.jline.reader.*;
import org.jline.terminal.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.extensions.cmd.AbstractNutsCommandAutoComplete;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.apps.javashell.interpreter.InterrupShellException;
import org.jline.terminal.Attributes;
import org.jline.terminal.Cursor;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Size;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsJLineTerminal implements NutsTerminal {

    private Terminal terminal;
    private LineReader reader;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private NutsCommandContext nutsCommandContext;
    private DefaultNutsTerminal fallback;
    private NutsPrintStream outReplace;
    private NutsPrintStream errReplace;
    private InputStream inReplace;
    private BufferedReader inReplaceReader;

    public NutsJLineTerminal() {
    }

    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) {
        if (in != null || out != null || err != null || System.console() == null) {
            fallback = new DefaultNutsTerminal();
            fallback.install(workspace, in, out, err);
        } else {
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.streams(System.in, System.out);
            builder.system(true);
            try {
                terminal = builder.build();
            } catch (Throwable ex) {
                //unable to create system terminal
            }
            if (terminal == null) {
                builder.system(false);
                try {
                    terminal = builder.build();
                } catch (IOException ex) {
                    Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }

            reader = LineReaderBuilder.builder()
                    .completer(new Completer() {
                        @Override
                        public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
                            if (nutsCommandContext != null) {
                                if (line.wordIndex() == 0) {
                                    for (NutsCommand command : nutsCommandContext.getCommandLine().getCommands()) {
                                        candidates.add(new Candidate(command.getName()));
                                    }
                                } else {
                                    NutsCommand command = nutsCommandContext.getCommandLine().findCommand(line.words().get(0));
                                    if (command != null) {
                                        AbstractNutsCommandAutoComplete autoComplete = new AbstractNutsCommandAutoComplete() {
                                            @Override
                                            public String getLine() {
                                                int x = line.words().get(0).length();
                                                return line.line().substring(x);
                                            }

                                            @Override
                                            public List<String> getWords() {
                                                ArrayList<String> arrayList = new ArrayList<>(line.words().subList(1, line.words().size()));
//                                            if(arrayList.size()>0 && arrayList.get(arrayList.size()-1).length()==0){
//                                                arrayList.remove(arrayList.size()-1);
//                                            }
                                                return arrayList;
                                            }

                                            @Override
                                            public int getCurrentWordIndex() {
                                                return line.wordIndex() - 1;
                                            }
                                        };
                                        command.autoComplete(nutsCommandContext, autoComplete);
                                        if (autoComplete.getCandidates() != null) {
                                            for (NutsArgumentCandidate cmdCandidate : autoComplete.getCandidates()) {
                                                if (cmdCandidate != null) {
                                                    String value = cmdCandidate.getValue();
                                                    String display = cmdCandidate.getDisplay();
                                                    if (!CoreStringUtils.isEmpty(value) || !CoreStringUtils.isEmpty(display)) {
                                                        candidates.add(new Candidate(
                                                                value,
                                                                display,
                                                                null, null, null, null, true
                                                        ));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                    .terminal(terminal)
                    //                .completer(completer)
                    //                .parser(parser)
                    .build();
            reader.setVariable(LineReader.HISTORY_FILE, CoreIOUtils.createFile(workspace.getWorkspaceLocation(), "history"));
            this.out = workspace.createEnhancedPrintStream(reader.getTerminal().output());
            this.err = workspace.createEnhancedPrintStream(reader.getTerminal().output());//.setColor(NutsPrintStream.RED);
        }
    }

    @Override
    public void setCommandContext(NutsCommandContext context) {
        this.nutsCommandContext = context;
        if (fallback != null) {
            fallback.setCommandContext(context);
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT + 1;
    }

    @Override
    public String readLine(String prompt) {
        if (inReplace != null) {
            if (outReplace != null) {
                outReplace.print(prompt);
            }
            if (inReplaceReader == null) {
                inReplaceReader = new BufferedReader(new InputStreamReader(inReplace));
            }
            try {
                return inReplaceReader.readLine();
            } catch (UserInterruptException e) {
                throw new InterrupShellException();
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
        }
        if (fallback != null) {
            return fallback.readLine(prompt);
        }
        String readLine=null;
        try {
            readLine = reader.readLine(prompt);
        } catch (UserInterruptException e) {
            throw new InterrupShellException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        return readLine;
    }

    @Override
    public String readPassword(String prompt) {
        if (inReplace != null) {
            if (inReplaceReader == null) {
                inReplaceReader = new BufferedReader(new InputStreamReader(inReplace));
            }
            try {
                return inReplaceReader.readLine();
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
        }
        return reader.readLine(prompt, '*');
    }

    @Override
    public InputStream getIn() {
        if (inReplace != null) {
            return inReplace;
        }
        if (fallback != null) {
            return fallback.getIn();
        }
        return reader.getTerminal().input();
    }

    @Override
    public void setOut(NutsPrintStream out) {
        outReplace = out;
    }

    @Override
    public void setIn(InputStream in) {
        inReplace = in;
    }

    @Override
    public void setErr(NutsPrintStream err) {
        errReplace = err;
    }

    @Override
    public NutsPrintStream getOut() {
        if (outReplace != null) {
            return outReplace;
        }
        if (fallback != null) {
            return fallback.getOut();
        }
        return out;
    }

    @Override
    public NutsPrintStream getErr() {
        if (errReplace != null) {
            return errReplace;
        }
        if (fallback != null) {
            return fallback.getErr();
        }
        return err;
    }
    
//    private static class AdapterTerminal implements Terminal{
//        private Terminal base;
//        private Map<Signal,SignalHandler> sig=new HashMap<Signal, SignalHandler>();
//        private Map<Signal,SignalHandler> raisable=new HashMap<Signal, SignalHandler>();
//
//        public AdapterTerminal(Terminal base) {
//            this.base = base;
//            for (Signal value : Signal.values()) {
//                base.handle(value, new )
//            }
//        }
//        
//        @Override
//        public String getName() {
//            return base.getName();
//        }
//
//        @Override
//        public SignalHandler handle(Signal signal, SignalHandler handler) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void raise(Signal signal) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public NonBlockingReader reader() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public PrintWriter writer() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public Charset encoding() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public InputStream input() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public OutputStream output() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public Attributes enterRawMode() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public boolean echo() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public boolean echo(boolean echo) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public Attributes getAttributes() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void setAttributes(Attributes attr) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public Size getSize() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void setSize(Size size) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void flush() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public String getType() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public boolean puts(InfoCmp.Capability capability, Object... params) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public boolean getBooleanCapability(InfoCmp.Capability capability) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public Integer getNumericCapability(InfoCmp.Capability capability) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public String getStringCapability(InfoCmp.Capability capability) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public Cursor getCursorPosition(IntConsumer discarded) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public boolean hasMouseSupport() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public boolean trackMouse(MouseTracking tracking) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public MouseEvent readMouseEvent() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public MouseEvent readMouseEvent(IntSupplier reader) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void close() throws IOException {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//        
//    }
}
