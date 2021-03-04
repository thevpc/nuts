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
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.common.strings.StringUtils;
import net.thevpc.jshell.*;
import net.thevpc.jshell.JShellNode;
import net.thevpc.nuts.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultNutsShellContext extends DefaultJShellContext implements NutsShellContext {

    private NutsWorkspace workspace;
    private NutsSession session;
    //    private NutsSessionTerminal terminal;
    private NutsCommandAutoComplete autoComplete;

//    public DefaultNutsShellContext(NutsJavaShell shell,NutsWorkspace workspace) {
//        super(shell);
//        this.workspace = workspace;
//        this.session = (workspace == null ? null : workspace.createSession());
//    }

    public DefaultNutsShellContext(NutsJavaShell shell, JShellNode rootNode, JShellNode parentNode,
                                   NutsShellContext parentContext, NutsWorkspace workspace, NutsSession session, JShellVariables vars) {
        super(shell);
        this.parentContext = parentContext;//.copy();
        if (parentContext != null) {
            setCwd(parentContext.getCwd());
        }
        this.workspace = workspace != null ? workspace : parentContext != null ? parentContext.getWorkspace() : null;
        if (session == null) {
            if (this.workspace != null) {
                session = this.workspace.createSession();
            }
        }
        this.session = session;
        setRoot(rootNode);
        setParentNode(parentNode);
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
            setBuiltins(new NutsBuiltinManager());
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

    @Override
    public NutsSession session() {
        return getSession();
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
    public NutsWorkspace workspace() {
        return getWorkspace();
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

    public NutsShellContext getParentContext() {
        return (NutsShellContext) super.getParentContext();
    }

    @Override
    public void copyFrom(JShellContext other) {
        super.copyFrom(other);
        if (other instanceof DefaultNutsShellContext) {
            DefaultNutsShellContext o = (DefaultNutsShellContext) other;
            this.workspace = o.workspace;
            this.session = o.session == null ? null : o.session.copy();
        }
    }

//    @Override
//    public NutsShellContext copy() {
//        DefaultNutsShellContext c = new DefaultNutsShellContext();
//        c.copyFrom(this);
//        return c;
//    }

    //    @Override
//    public NutsSessionTerminal getTerminal() {
//        if (terminal != null) {
//            return terminal;
//        }
//        if (commandContext != null) {
//            return commandContext.getTerminal();
//        }
//        return session.getTerminal();
//    }
    @Override
    public InputStream in() {
        return getSession().getTerminal().in();
    }

    @Override
    public PrintStream out() {
        return getSession().getTerminal().getOut();
    }

    @Override
    public PrintStream err() {
        return getSession().getTerminal().getErr();
    }

    @Override
    public JShellContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    @Override
    public JShellContext setOut(PrintStream out) {
        getSession().getTerminal().setOut(out);
//        commandContext.getTerminal().setOut(workspace.createPrintStream(out,
//                true//formatted
//        ));
        return this;
    }

    public JShellExecutionContext createCommandContext(JShellBuiltin command, JShellFileContext context) {
        DefaultNshExecutionContext c = new DefaultNshExecutionContext(this, (NshBuiltin) command,context);
//        c.setMode(getTerminalMode());
//        c.setVerbose(isVerbose());
        return c;
    }

    @Override
    public List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine, JShellFileContext ctx) {
        JShellBuiltin command = this.builtins().find(commandName);
        NutsCommandAutoComplete autoComplete = new NutsCommandAutoCompleteBase() {
            @Override
            public NutsSession getSession() {
                return session;
            }

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

        if (command != null && command instanceof NshBuiltin) {
            ((NshBuiltin) command).autoComplete(new DefaultNshExecutionContext(this, (NshBuiltin) command,ctx), autoComplete);
        } else {
            NutsWorkspace ws = this.getWorkspace();
            List<NutsId> nutsIds = ws.search()
                    .addId(commandName)
                    .setLatest(true)
                    .addScope(NutsDependencyScopePattern.RUN)
                    .setOptional(false)
                    .setSession(this.getSession().copy().setTrace(false).setFetchStrategy(NutsFetchStrategy.OFFLINE))
                    .getResultIds().list();
            if (nutsIds.size() == 1) {
                NutsId selectedId = nutsIds.get(0);
                NutsDefinition def = ws.search().addId(selectedId).setEffective(true).setSession(this.getSession()
                        .copy().setTrace(false).setFetchStrategy(NutsFetchStrategy.OFFLINE)).getResultDefinitions().required();
                NutsDescriptor d = def.getDescriptor();
                String nuts_autocomplete_support = StringUtils.trim(d.getProperties().get("nuts.autocomplete"));
                if (d.isApplication()
                        || "true".equalsIgnoreCase(nuts_autocomplete_support)
                        || "supported".equalsIgnoreCase(nuts_autocomplete_support)) {
                    NutsExecCommand t = ws.exec()
                            .grabOutputString()
                            .grabErrorString()
                            .addCommand(
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
                                    NutsCommandLineManager commandLineFormat = workspace.commandLine();
                                    NutsCommandLine args = commandLineFormat.parse(s);
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
                                                commandLineFormat.createCandidate(
                                                        value
                                                ).build()
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
        List<JShellAutoCompleteCandidate> all = new ArrayList<>();
        for (NutsArgumentCandidate a : autoComplete.getCandidates()) {
            all.add(new JShellAutoCompleteCandidate(a.getValue(), a.getDisplay()));
        }
        return all;
    }

}
