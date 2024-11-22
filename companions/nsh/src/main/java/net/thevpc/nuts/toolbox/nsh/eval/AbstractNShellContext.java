package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.toolbox.nsh.autocomplete.NShellAutoCompleteCandidate;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

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
public abstract class AbstractNShellContext implements NShellContext {


    private final List<String> args = new ArrayList<>();
    private NSession session;
    private String serviceName;

    @Override
    public InputStream in() {
        return getSession().getTerminal().in();
    }

    @Override
    public NPrintStream out() {
        return getSession().getTerminal().getOut();
    }

    @Override
    public NPrintStream err() {
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
                                out().print(buffer, 0, x);
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
                                err().print(buffer, 0, x);
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
                if (!some && w.isAskStopped()) {
                    break;
                }
            }
        }).start();
        return w;
    }

    @Override
    public NShellContext setOut(PrintStream out) {
        getSession().getTerminal().setOut(
                NPrintStream.of(out)
        );
//        commandContext.getTerminal().setOut(workspace.createPrintStream(out,
//                true//formatted
//        ));
        return this;
    }

    public NShellContext setErr(PrintStream err) {
        getSession().getTerminal().setErr(
                NPrintStream.of(err)
        );
        return this;
    }

    @Override
    public NShellContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    public NShellExecutionContext createCommandContext(NShellBuiltin command) {
        DefaultNShellExecutionContext c = new DefaultNShellExecutionContext(this, command);
//        c.setMode(getTerminalMode());
//        c.setVerbose(isVerbose());
        return c;
    }

    @Override
    public List<NShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine) {
        NShellBuiltin command = this.builtins().find(commandName);
        NCmdLineAutoComplete autoComplete = new DefaultNCmdLineAutoComplete()
                .setLine(autoCompleteLine).setWords(autoCompleteWords).setCurrentWordIndex(wordIndex);

        if (command != null) {
            command.autoComplete(new DefaultNShellExecutionContext(this, command), autoComplete);
        } else {
            NSession session = this.getSession();
            List<NId> nutsIds = NSearchCmd.of()
                    .setFetchStrategy(NFetchStrategy.OFFLINE)
                    .addId(commandName)
                    .setLatest(true)
                    .addScope(NDependencyScopePattern.RUN)
                    .setOptional(false)
                    .getResultIds().toList();
            if (nutsIds.size() == 1) {
                NId selectedId = nutsIds.get(0);
                NDefinition def = NSearchCmd.of()
                        .setFetchStrategy(NFetchStrategy.OFFLINE)
                        .addId(selectedId).setEffective(true)
                        .getResultDefinitions().findFirst().get();
                NDescriptor d = def.getDescriptor();
                String nuts_autocomplete_support = NStringUtils.trim(d.getPropertyValue("nuts.autocomplete").flatMap(NLiteral::asString).get());
                if (d.isApplication()
                        || "true".equalsIgnoreCase(nuts_autocomplete_support)
                        || "supported".equalsIgnoreCase(nuts_autocomplete_support)) {
                    NExecCmd t = NExecCmd.of()
                            .grabAll()
                            .addCommand(
                                    selectedId
                                            .getLongName(),
                                    "--nuts-exec-mode=auto-complete " + wordIndex
                            )
                            .addCommand(autoCompleteWords)
                            .run();
                    if (t.getResultCode() == 0) {
                        String rr = t.getGrabbedOutString();
                        for (String s : rr.split("\n")) {
                            s = s.trim();
                            if (s.length() > 0) {
                                if (s.startsWith(NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX)) {
                                    s = s.substring(NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX.length()).trim();
                                    NCmdLine args = NCmdLine.of(s, NShellFamily.BASH).setExpandSimpleOptions(false);
                                    String value = null;
                                    String display = null;
                                    if (args.hasNext()) {
                                        value = args.next().flatMap(NLiteral::asString).get();
                                        if (args.hasNext()) {
                                            display = args.next().flatMap(NLiteral::asString).get();
                                        }
                                    }
                                    if (value != null) {
                                        if (display == null) {
                                            display = value;
                                        }
                                        autoComplete.addCandidate(
                                                new DefaultNArgCandidate(
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
        List<NShellAutoCompleteCandidate> all = new ArrayList<>();
        for (NArgCandidate a : autoComplete.getCandidates()) {
            all.add(new NShellAutoCompleteCandidate(a.getValue(), a.getDisplay()));
        }
        return all;
    }

    @Override
    public String getAbsolutePath(String path) {
        if (NPath.of(path).isAbsolute()) {
            return getFileSystem().getAbsolutePath(path, getSession());
        }
        return getFileSystem().getAbsolutePath(getDirectory() + "/" + path, getSession());
    }

    @Override
    public String[] expandPaths(String path) {
        return NPath.of(path).walkGlob().map(NFunction.of(NPath::toString).withDesc(NEDesc.of("toString"))).toArray(String[]::new);
    }

    @Override
    public void setAll(NShellContext other) {
        if (other != null) {
            setSession(other.getSession() == null ? null : other.getSession().copy());
            setAliases(other.aliases());
            setBuiltins(other.builtins());
            setRootNode(other.getRootNode());
            setParentNode(other.getParentNode());
            setFileSystem(other.getFileSystem());
            setDirectory(other.getDirectory());
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
    public NSession getSession() {
        return session;
    }

    @Override
    public NShellContext setSession(NSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

}
