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
package net.vpc.app.nuts.toolbox.nsh.term;

import net.vpc.app.nuts.*;
import net.vpc.common.commandline.AbstractCommandAutoComplete;
import net.vpc.app.nuts.toolbox.nsh.NutsCommand;
import net.vpc.common.commandline.CommandAutoComplete;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.ArgumentCandidate;
import net.vpc.common.io.FileUtils;
import net.vpc.common.javashell.InterrupShellException;
import net.vpc.common.strings.StringUtils;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsJLineTerminal implements NutsTerminal {

    private Terminal terminal;
    private LineReader reader;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private NutsCommandContext nutsCommandContext;
    private NutsTerminal fallback;
    private NutsPrintStream outReplace;
    private NutsPrintStream errReplace;
    private InputStream inReplace;
    private BufferedReader inReplaceReader;
    private NutsWorkspace workspace;

    public NutsJLineTerminal() {
    }

    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) {
        this.workspace=workspace;
        if (in != null || out != null || err != null || System.console() == null) {
            fallback = workspace.getExtensionManager().createDefaultTerminal();
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
                    throw new NutsIOException(ex);
                }
            }

            reader = LineReaderBuilder.builder()
                    .completer(new Completer() {
                        @Override
                        public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
                            if (nutsCommandContext != null) {
                                if (line.wordIndex() == 0) {
                                    for (NutsCommand command : nutsCommandContext.getConsole().getCommands()) {
                                        candidates.add(new Candidate(command.getName()));
                                    }
                                } else {
                                    NutsCommand command = nutsCommandContext.getConsole().findCommand(line.words().get(0));
                                    if (command != null) {
                                        CommandAutoComplete autoComplete = new AbstractCommandAutoComplete() {
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
                                            for (Object cmdCandidate0 : autoComplete.getCandidates()) {
                                                ArgumentCandidate cmdCandidate = (ArgumentCandidate) cmdCandidate0;
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
            reader.setVariable(LineReader.HISTORY_FILE, FileUtils.getAbsoluteFile(new File(workspace.getConfigManager().getWorkspaceLocation()), "history"));
            this.out = workspace.getExtensionManager().createPrintStream(reader.getTerminal().output(),true);
            this.err = workspace.getExtensionManager().createPrintStream(reader.getTerminal().output(),true);//.setColor(NutsPrintStream.RED);
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(String promptFormat, Object ... params) {
        if (inReplace != null) {
            if (outReplace != null) {
                outReplace.print(promptFormat);
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
            return fallback.readLine(promptFormat, params);
        }
        String readLine = null;
        try {
            readLine = reader.readLine(promptFormat);
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

    @Override
    public NutsFormattedPrintStream getFormattedOut() {
        return workspace.getExtensionManager().createsFormattedPrintStream(getOut());
    }

    @Override
    public NutsFormattedPrintStream getFormattedErr() {
        return workspace.getExtensionManager().createsFormattedPrintStream(getErr());
    }
}
