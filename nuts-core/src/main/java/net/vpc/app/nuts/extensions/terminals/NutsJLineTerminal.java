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
import net.vpc.app.nuts.boot.DefaultNutsTerminal;
import net.vpc.app.nuts.util.IOUtils;
import org.jline.reader.*;
import org.jline.terminal.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.extensions.cmd.AbstractNutsCommandAutoComplete;
import net.vpc.app.nuts.util.StringUtils;
import org.jline.terminal.TerminalBuilder;

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

    public NutsJLineTerminal()  {
    }

    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) throws IOException{
        System.out.println("Install NutsJLineTerminal");
        if(in!=null || out!=null || err!=null || System.console() == null){
            fallback = new DefaultNutsTerminal();
            fallback.install(workspace,in,out,err);
            System.out.println("Fallback Temrinal");
        } else {
            TerminalBuilder builder = TerminalBuilder.builder();
            builder.streams(System.in, System.out);
            builder.system(true);
            try {
                terminal = builder.build();
            } catch (Throwable ex) {
                System.out.println("Fallback Temrinal");
                //unable to create system terminal
            }
            if (terminal == null) {
                builder.system(false);
                try {
                    terminal = builder.build();
                } catch (IOException ex) {
                    System.out.println("Fallback Temrinal");
                    Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }

            reader = LineReaderBuilder.builder()
                    .completer(new Completer() {
                        @Override
                        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
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

                                            @Override
                                            public NutsCommandContext getCommandContext() {
                                                return nutsCommandContext;
                                            }

                                        };
                                        command.autoComplete(autoComplete);
                                        if (autoComplete.getCandidates() != null) {
                                            for (ArgumentCandidate cmdCandidate : autoComplete.getCandidates()) {
                                                if (cmdCandidate != null) {
                                                    String value = cmdCandidate.getValue();
                                                    String display = cmdCandidate.getDisplay();
                                                    if (!StringUtils.isEmpty(value) || !StringUtils.isEmpty(display)) {
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
            reader.setVariable(LineReader.HISTORY_FILE, IOUtils.createFile(workspace.getWorkspaceLocation(), "history"));
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
        return CORE_SUPPORT +1;
    }

    @Override
    public String readLine(String prompt) throws IOException {
        if(inReplace!=null){
            if(outReplace!=null){
                outReplace.print(prompt);
            }
            if(inReplaceReader==null){
                inReplaceReader=new BufferedReader(new InputStreamReader(inReplace));
            }
            return inReplaceReader.readLine();
        }
        if (fallback != null) {
            return fallback.readLine(prompt);
        }
        String readLine = reader.readLine(prompt);
        reader.getHistory().save();
        return readLine;
    }

    @Override
    public String readPassword(String prompt) throws IOException {
        if(inReplace!=null){
            if(inReplaceReader==null){
                inReplaceReader=new BufferedReader(new InputStreamReader(inReplace));
            }
            return inReplaceReader.readLine();
        }
        return reader.readLine(prompt, '*');
    }

    @Override
    public InputStream getIn() {
        if(inReplace!=null){
            return inReplace;
        }
        if (fallback != null) {
            return fallback.getIn();
        }
        return reader.getTerminal().input();
    }

    @Override
    public void setOut(NutsPrintStream out) {
        outReplace=out;
    }

    @Override
    public void setIn(InputStream in) {
        inReplace=in;
    }

    @Override
    public void setErr(NutsPrintStream err) {
        errReplace=err;
    }

    @Override
    public NutsPrintStream getOut() {
        if(outReplace!=null){
            return outReplace;
        }
        if (fallback != null) {
            return fallback.getOut();
        }
        return out;
    }

    @Override
    public NutsPrintStream getErr() {
        if(errReplace!=null){
            return errReplace;
        }
        if (fallback != null) {
            return fallback.getErr();
        }
        return err;
    }
}
