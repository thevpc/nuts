package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 11/4/16.
 */
public abstract class AbstractJShellContext implements JShellContext {


    private final List<String> args = new ArrayList<>();
    private NutsSession session;
    private String serviceName;

    @Override
    public InputStream in() {
        return getSession().getTerminal().in();
    }

    @Override
    public NutsPrintStream out() {
        return getSession().getTerminal().getOut();
    }

    @Override
    public NutsPrintStream err() {
        return getSession().getTerminal().getErr();
    }

    @Override
    public Watcher bindStreams(InputStream out, InputStream err, OutputStream in) {
        WatcherImpl w = new WatcherImpl();
        new Thread(() -> {
            byte[] buffer = new byte[4024];
            int x;
            boolean some = false;
            while (true) {
                if (out != null) {
                    try {
                        if (out.available() > 0) {
                            x = out.read(buffer);
                            if (x > 0) {
                                out().write(buffer, 0, x);
                                some = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (err != null) {
                    try {
                        if (err.available() > 0) {
                            x = err.read(buffer);
                            if (x > 0) {
                                err().write(buffer, 0, x);
                                some = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        if (in().available() > 0) {
                            x = in().read(buffer);
                            if (x > 0) {
                                in.write(buffer, 0, x);
                                some = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!some && w.askStopped) {
                    break;
                }
            }
        }).start();
        return w;
    }

    @Override
    public JShellContext setOut(PrintStream out) {
        getSession().getTerminal().setOut(
                NutsPrintStream.of(out, getSession())
        );
//        commandContext.getTerminal().setOut(workspace.createPrintStream(out,
//                true//formatted
//        ));
        return this;
    }

    public JShellContext setErr(PrintStream err) {
        getSession().getTerminal().setErr(
                NutsPrintStream.of(err, getSession())
        );
        return this;
    }

    @Override
    public JShellContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    //    public JShellExecutionContext createCommandContext(JShellBuiltin command, JShellFileContext context) {
//        return new DefaultJShellExecutionContext(context);
//    }
    public JShellExecutionContext createCommandContext(JShellBuiltin command) {
        DefaultJShellExecutionContext c = new DefaultJShellExecutionContext(this, command);
//        c.setMode(getTerminalMode());
//        c.setVerbose(isVerbose());
        return c;
    }

    @Override
    public List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine) {
        JShellBuiltin command = this.builtins().find(commandName);
        NutsCommandAutoComplete autoComplete = new NutsDefaultCommandAutoComplete()
                .setSession(getSession()).setLine(autoCompleteLine).setWords(autoCompleteWords).setCurrentWordIndex(wordIndex);

        if (command != null) {
            command.autoComplete(new DefaultJShellExecutionContext(this, command), autoComplete);
        } else {
            NutsSession session = this.getSession();
            List<NutsId> nutsIds = session.search()
                    .addId(commandName)
                    .setLatest(true)
                    .addScope(NutsDependencyScopePattern.RUN)
                    .setOptional(false)
                    .setSession(this.getSession().copy().setFetchStrategy(NutsFetchStrategy.OFFLINE))
                    .getResultIds().toList();
            if (nutsIds.size() == 1) {
                NutsId selectedId = nutsIds.get(0);
                NutsDefinition def = session.search().addId(selectedId).setEffective(true).setSession(this.getSession()
                        .copy().setFetchStrategy(NutsFetchStrategy.OFFLINE)).getResultDefinitions().required();
                NutsDescriptor d = def.getDescriptor();
                String nuts_autocomplete_support = NutsUtilStrings.trim(d.getPropertyValue("nuts.autocomplete"));
                if (d.isApplication()
                        || "true".equalsIgnoreCase(nuts_autocomplete_support)
                        || "supported".equalsIgnoreCase(nuts_autocomplete_support)) {
                    NutsExecCommand t = session.exec()
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
                                    NutsCommandLine args = NutsCommandLine.of(s,NutsShellFamily.BASH, session).setExpandSimpleOptions(false);
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
                                                new NutsArgumentCandidate(
                                                        value
                                                )
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

    @Override
    public String getAbsolutePath(String path) {
        if (NutsPath.of(path, getSession()).isAbsolute()) {
            return getFileSystem().getAbsolutePath(path, getSession());
        }
        return getFileSystem().getAbsolutePath(getCwd() + "/" + path, getSession());
    }

    @Override
    public String[] expandPaths(String path) {
        return NutsPath.of(path, getSession()).walkGlob().map(NutsFunction.of(NutsPath::toString, "toString")).toArray(String[]::new);
    }

    @Override
    public void copyFrom(JShellContext other) {
        if (other != null) {
            setSession(other.getSession() == null ? null : other.getSession().copy());
            setAliases(other.aliases());
            setBuiltins(other.builtins());
            setRootNode(other.getRootNode());
            setParentNode(other.getParentNode());
            setFileSystem(other.getFileSystem());
            setCwd(other.getCwd());
            setFunctionManager(other.functions());
        }
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void setArgs(String[] args) {
        this.args.clear();
        this.args.addAll(Arrays.asList(args));
    }

    @Override
    public String getArg(int index) {
        List<String> argsList = getArgsList();
        if (index >= 0 && index < argsList.size()) {
            String r = argsList.get(index);
            return r == null ? "" : r;
        }
        return "";
    }

    @Override
    public int getArgsCount() {
        return args.size();
    }

    @Override
    public String[] getArgsArray() {
        return args.toArray(new String[0]);
    }

    @Override
    public List<String> getArgsList() {
        return args;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public JShellContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

}
