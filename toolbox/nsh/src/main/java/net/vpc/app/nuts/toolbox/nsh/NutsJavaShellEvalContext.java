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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.common.javashell.*;
import net.vpc.common.javashell.cmds.Command;
import net.vpc.common.javashell.cmds.CommandContext;
import net.vpc.common.javashell.parser.nodes.Node;
import net.vpc.common.strings.StringUtils;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class NutsJavaShellEvalContext extends DefaultConsoleContext implements NutsConsoleContext {

    private NutsConsoleContext commandContext;
    private NutsWorkspace workspace;
    private String serviceName;
    private NutsSession session;
    private NutsCommandAutoComplete autoComplete;
    private boolean verbose;
    private NutsTerminalMode terminalMode;

    public NutsJavaShellEvalContext() {
        super(null);
    }

    public NutsJavaShellEvalContext(NutsWorkspace workspace) {
        super(null);
        this.workspace = workspace;
        this.session = (workspace == null ? null : workspace.createSession());
    }

    public NutsJavaShellEvalContext(ConsoleContext parentContext) {
        super(parentContext);
        if (parentContext instanceof NutsJavaShellEvalContext) {
            NutsJavaShellEvalContext parentContext1 = (NutsJavaShellEvalContext) parentContext;
            this.commandContext = parentContext1.commandContext.copy();
            this.commandContext.getUserProperties().put(ConsoleContext.class.getName(), this);
            this.workspace = parentContext1.workspace;
            this.session = (workspace == null ? null : workspace.createSession());
        }
    }

    public NutsJavaShellEvalContext(NutsJavaShell shell, String[] args, Node root, Node parent, NutsConsoleContext parentContext, NutsWorkspace workspace, NutsSession session, Env env) {
        super(shell, env, root, parent, null, null, null, args);
        this.commandContext = parentContext;//.copy();
        this.workspace = workspace;
        if (session == null) {
            if (workspace != null) {
                session = workspace.createSession();
            }
        }
        this.session = session;
    }

    public NutsConsoleContext getCommandContext() {
        return commandContext;
    }

    public NutsSessionTerminal getTerminal() {
        if (commandContext != null) {
            return commandContext.getTerminal();
        }
        return session.getTerminal();
    }

    @Override
    public InputStream in() {
        return getTerminal().getIn();
    }

    @Override
    public ConsoleContext setOut(PrintStream out) {
        getTerminal().setOut(out);
//        commandContext.getTerminal().setOut(workspace.createPrintStream(out,
//                true//formatted
//        ));
        return this;
    }

    @Override
    public ConsoleContext setIn(InputStream in) {
        getTerminal().setIn(in);
        return this;
    }

    public CommandContext createCommandContext(Command command) {
        DefaultNutsCommandContext c = new DefaultNutsCommandContext(this, (NutsCommand) command);
        c.setTerminalMode(getTerminalMode());
        c.setVerbose(isVerbose());
        return c;
    }

    protected void copyFrom(ConsoleContext other) {
        super.copyFrom(other);
        if (other instanceof NutsJavaShellEvalContext) {
            NutsJavaShellEvalContext o = (NutsJavaShellEvalContext) other;
            this.workspace = o.workspace;
            this.serviceName = o.serviceName;
            this.commandContext = o.commandContext;
            this.terminalMode = o.terminalMode;
            this.verbose = o.verbose;
            this.session = o.session.copy();
        }
    }

    public NutsConsoleContext copy() {
        NutsJavaShellEvalContext c = new NutsJavaShellEvalContext();
        c.copyFrom(this);
        return c;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public NutsConsoleContext setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsConsoleContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public void setAutoComplete(NutsCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }

    @Override
    public PrintStream getFormattedOut() {
        return getTerminal().getFormattedOut();
    }

    @Override
    public PrintStream getFormattedErr() {
        return getTerminal().getFormattedErr();
    }

    @Override
    public PrintStream getFormattedOut(boolean forceNoColors) {
        return getTerminal().getFormattedOut(forceNoColors);
    }

    @Override
    public PrintStream getFormattedErr(boolean forceNoColors) {
        return getTerminal().getFormattedErr(forceNoColors);
    }

    @Override
    public PrintStream out() {
        return getTerminal().getOut();
    }

    @Override
    public PrintStream err() {
        return getTerminal().getErr();
    }

    public List<AutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine) {
        Command command = this.getShell().findCommand(commandName);
        NutsCommandAutoComplete autoComplete = new NutsCommandAutoCompleteBase() {
            @Override
            public String getLine() {
                return autoCompleteLine;
            }

            @Override
            public List<String> getWords() {
                return autoCompleteWords;
            }

            @Override
            public int getCurrentWordIndex() {
                return wordIndex;
            }
        };

        if (command != null && command instanceof NutsCommand) {
            ((NutsCommand) command).autoComplete(new DefaultNutsCommandContext(this, (NutsCommand) command), autoComplete);
        } else {
            NutsWorkspace ws = this.getWorkspace();
            List<NutsId> nutsIds = ws.find()
                    .addId(commandName)
                    .latestVersions()
                    .addScope(NutsDependencyScope.PROFILE_RUN)
                    .setIncludeOptional(false)
                    .offline()
                    .setSession(this.getSession())
                    .getResultIds().list();
            if (nutsIds.size() == 1) {
                NutsId selectedId = nutsIds.get(0);
                NutsDefinition def = ws.find().id(selectedId).effective(true).session(this.getSession()).offline().getResultDefinitions().required();
                NutsDescriptor d = def.getDescriptor();
                String nuts_autocomplete_support = StringUtils.trim(d.getProperties().get("nuts.autocomplete"));
                if (d.isNutsApplication()
                        || "true".equalsIgnoreCase(nuts_autocomplete_support)
                        || "supported".equalsIgnoreCase(nuts_autocomplete_support)) {
                    NutsExecCommand t = ws.exec()
                            .grabOutputString()
                            .grabErrorString()
                            .command(
                                    selectedId
                                            .getLongName(),
                                    "--nuts-exec-mode=auto-complete " + wordIndex
                            )
                            .addCommand(autoCompleteWords)
                            .run();
                    if (t.getResult() == 0) {
                        String rr = t.getOutputString();
                        for (String s : rr.split("\n")) {
                            s = s.trim();
                            if (s.length() > 0) {
                                if (s.startsWith(NutsApplicationContext.AUTO_COMPLETE_CANDIDATE_PREFIX)) {
                                    s = s.substring(NutsApplicationContext.AUTO_COMPLETE_CANDIDATE_PREFIX.length()).trim();
                                    String[] args = NutsCommandLine.parseCommandLine(s);
                                    String value = null;
                                    String display = null;
                                    if (args.length > 0) {
                                        value = args[0];
                                        if (args.length > 1) {
                                            display = args[1];
                                        }
                                    }
                                    if (value != null) {
                                        if (display == null) {
                                            display = value;
                                        }
                                        autoComplete.addCandidate(
                                                value, display
                                        );
                                    }
                                } else {
                                    //ignore all the rest!
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }
        List<AutoCompleteCandidate> all = new ArrayList<>();
        for (NutsArgumentCandidate a : autoComplete.getCandidates()) {
            all.add(new AutoCompleteCandidate(a.getValue(), a.getDisplay()));
        }
        return all;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public NutsJavaShellEvalContext setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
    }

    public NutsJavaShellEvalContext setTerminalMode(NutsTerminalMode mode) {
        this.terminalMode = mode;
        return this;
    }
}
