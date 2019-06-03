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
import net.vpc.common.javashell.parser.nodes.Node;
import net.vpc.common.strings.StringUtils;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import net.vpc.common.javashell.JShellCommandContext;
import net.vpc.common.javashell.JShellCommand;

public class NutsJavaShellEvalContext extends DefaultJShellContext implements NutsShellContext {

    private NutsShellContext commandContext;
    private NutsWorkspace workspace;
    private String serviceName;
    private NutsSession session;
    private NutsSessionTerminal terminal;
    private NutsCommandAutoComplete autoComplete;
    private boolean verbose;
    private NutsTerminalMode terminalMode;

    public NutsJavaShellEvalContext() {
        super();
    }

    public NutsJavaShellEvalContext(NutsWorkspace workspace) {
        super();
        this.workspace = workspace;
        this.session = (workspace == null ? null : workspace.createSession());
        this.terminalMode = workspace.io().getSystemTerminal().getOutMode();
    }

    public NutsJavaShellEvalContext(JShellContext parentContext) {
        super(parentContext);
//        if (parentContext instanceof NutsJavaShellEvalContext) {
        NutsJavaShellEvalContext parentContext1 = (NutsJavaShellEvalContext) parentContext;
        this.commandContext = parentContext1.commandContext.copy();
        this.commandContext.getUserProperties().put(JShellContext.class.getName(), this);
        this.workspace = parentContext1.workspace;
        this.session = (this.workspace == null ? null : this.workspace.createSession());
        this.terminalMode = parentContext1.getTerminalMode();
//        }
    }

    public NutsJavaShellEvalContext(NutsJavaShell shell, String[] args, Node root, Node parent, NutsShellContext parentContext, NutsWorkspace workspace, NutsSession session, JShellVariables vars) {
        super(shell);
        this.commandContext = parentContext;//.copy();
        this.workspace = workspace == null ? parentContext.getWorkspace() : workspace;
        if (session == null) {
            if (this.workspace != null) {
                session = this.workspace.createSession();
            }
        }
        this.session = session;
        this.terminalMode = parentContext != null ? parentContext.getTerminalMode()
                : this.workspace != null ? this.workspace.io().getSystemTerminal().getOutMode()
                        : NutsTerminalMode.FORMATTED;
        setRoot(root);
        setArgs(args);
        setParent(parent);
        if (parentContext != null) {
            vars().set(parentContext.vars());
            setBuiltins(parentContext.builtins());
            for (String a : parentContext.aliases().getAll()) {
                aliases().set(a, parentContext.aliases().get(a));
            }
        } else {
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                vars().export(entry.getKey(), entry.getValue());
            }
            setBuiltins(new NutsJavaShell.NutsBuiltinManager());
            JShellAliasManager a = aliases();
            a.set(".", "source");
            a.set("[", "test");

            a.set("ll", "ls");
            a.set("..", "cd ..");
            a.set("...", "cd ../..");
        }
        if (vars != null) {
            for (Map.Entry<Object, Object> entry : vars.getAll().entrySet()) {
                vars().set((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public NutsShellContext getCommandContext() {
        return commandContext;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        if (terminal != null) {
            return terminal;
        }
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
    public JShellContext setOut(PrintStream out) {
        getTerminal().setOut(out);
//        commandContext.getTerminal().setOut(workspace.createPrintStream(out,
//                true//formatted
//        ));
        return this;
    }

    @Override
    public JShellContext setIn(InputStream in) {
        getTerminal().setIn(in);
        return this;
    }

    public JShellCommandContext createCommandContext(JShellCommand command) {
        DefaultNutsCommandContext c = new DefaultNutsCommandContext(this, (NshCommand) command);
        c.setTerminalMode(getTerminalMode());
        c.setVerbose(isVerbose());
        return c;
    }

    protected void copyFrom(JShellContext other) {
        super.copyFrom(other);
        if (other instanceof NutsJavaShellEvalContext) {
            NutsJavaShellEvalContext o = (NutsJavaShellEvalContext) other;
            this.workspace = o.workspace;
            this.serviceName = o.serviceName;
            this.commandContext = o.commandContext;
            this.terminalMode = o.terminalMode;
            this.verbose = o.verbose;
            this.session = o.session == null ? null : o.session.copy();
        }
    }

    public NutsShellContext copy() {
        NutsJavaShellEvalContext c = new NutsJavaShellEvalContext();
        c.copyFrom(this);
        return c;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public NutsShellContext setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsShellContext setSession(NutsSession session) {
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
        JShellCommand command = this.builtins().find(commandName);
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

        if (command != null && command instanceof NshCommand) {
            ((NshCommand) command).autoComplete(new DefaultNutsCommandContext(this, (NshCommand) command), autoComplete);
        } else {
            NutsWorkspace ws = this.getWorkspace();
            List<NutsId> nutsIds = ws.search()
                    .id(commandName)
                    .latest()
                    .addScope(NutsDependencyScope.PROFILE_RUN)
                    .optional(false)
                    .offline()
                    .setSession(this.getSession())
                    .getResultIds().list();
            if (nutsIds.size() == 1) {
                NutsId selectedId = nutsIds.get(0);
                NutsDefinition def = ws.search().id(selectedId).effective(true).session(this.getSession()).offline().getResultDefinitions().required();
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
                                    NutsCommand args = workspace.parser().parseCommand(s);
                                    String value = null;
                                    String display = null;
                                    if (args.hasNext()) {
                                        value = args.next().getString();
                                        if (args.hasNext()) {
                                            display = args.next().getString();
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

    @Override
    public NutsJavaShellEvalContext setTerminalMode(NutsTerminalMode mode) {
        this.terminalMode = mode;
        return this;
    }

    @Override
    public NutsShellContext setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
        return this;
    }

//    @Override
    public NutsShellContext printObject(Object anyObject, PrintStream out) {
        NutsObjectFormat a = this.getWorkspace().formatter().createObjectFormat(this.getSession(), anyObject);
        a.configure(
                this.getWorkspace().parser().parseCommand(this.getWorkspace().config().getOptions().getOutputFormatOptions()), true
        );
        a.configure(
                this.getWorkspace().parser().parseCommand(this.getSession().getOutputFormatOptions()), true
        );
        a.println(out);
        return this;
    }

    @Override
    public NutsShellContext printOutObject(Object anyObject) {
        return printObject(anyObject, out());
    }

    @Override
    public NutsShellContext printErrObject(Object anyObject) {
        return printObject(anyObject, err());
    }
}
