/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultSpawnExecutableRemote extends AbstractNExecutableCommand {

    NDefinition def;
    String[] cmd;
    List<String> executorOptions;
    NSession session;
    NSession execSession;
    NExecCommand execCommand;
    NConnexionString connexionString;
    private boolean showCommand = false;
    private final boolean inheritSystemIO;

    private static class RemoteConnexionStringInfo {
        public String javaCommand = "java";
        public String nutsJar;
        long lastChecked;
        long timoutMS = 1000;
        String userHome;
        String nutsCommand;

        void update(DefaultSpawnExecutableRemote r) {
            if (userHome == null) {
                userHome = NStringUtils.trim(r.runOnceGrab("echo", "$HOME"));
            }
            if (NBlankable.isBlank(nutsCommand)) {
                try {
                    nutsCommand = NStringUtils.trim(r.runOnceGrab("which", "nuts"));
                } catch (Exception e) {
                    //
                }
            }
            if (NBlankable.isBlank(nutsCommand)) {
                NSession session = r.session;
                if (NBlankable.isBlank(nutsCommand)) {
                    if (NBlankable.isBlank(nutsJar)) {
                        List<String> nutsPaths = new ArrayList<>();
                        try {
                            String ls = NStringUtils.trim(r.runOnceGrab("ls", userHome + "/bin/nuts*.jar"));
                            nutsPaths.addAll(Arrays.asList(ls.split("\n")));
                        } catch (Exception e) {
                            //
                        }
                        if (!nutsPaths.isEmpty()) {
                            nutsJar = nutsPaths.get(0);
                        }
                    }

                    if (NBlankable.isBlank(nutsJar)) {
                        NPath e = NFetchCommand.of(session)
                                .setId(session.getWorkspace().getApiId())
                                .setContent(true)
                                .getResultContent();
                        String nutsJar2 = userHome + "/bin/" + e.getName();
                        e.copyTo(
                                NPath.of(
                                        NConnexionString.of(r.execCommand.getHost()).get()
                                                .copy()
                                                .setPath(null)
                                                + ":" + nutsJar2,
                                        session
                                )
                        );
                        nutsJar = nutsJar2;
                    }
                }
            }

        }

        boolean isUpdatable() {
            return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
        }
    }

    private NExecCommandExtension commExec;

    public DefaultSpawnExecutableRemote(NExecCommandExtension commExec, NDefinition def, String[] cmd,
                                        List<String> executorOptions, NSession session, NSession execSession, NExecCommand execCommand) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM);
        this.inheritSystemIO = execCommand.isInheritSystemIO();
        this.cmd = cmd;
        this.def = def;
        this.execCommand = execCommand;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.session = session;
        this.execSession = execSession;
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get(session);
            switch (aa.key()) {
                case "--show-command": {
                    cmdLine.withNextFlag((v, a, s) -> this.showCommand = (v));
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
    }

    @Override
    public NId getId() {
        return null;
    }

    private AbstractSyncIProcessExecHelper resolveExecHelper() {
//        Map<String, String> e2 = null;
//        Map<String, String> env1 = execCommand.getEnv();
//        if (env1 != null) {
//            e2 = new HashMap<>((Map) env1);
//        }
//        return ProcessExecHelper.ofArgs(null,
//                execCommand.getCommand().toArray(new String[0]), e2,
//                execCommand.getDirectory() == null ? null : execCommand.getDirectory().toFile(),
//                session.getTerminal(),
//                execSession.getTerminal(), showCommand, true, execCommand.getSleepMillis(),
//                inheritSystemIO,
//                /*redirectErr*/ false,
//                /*fileIn*/ execCommand.getRedirectInputFile(),
//                /*fileOut*/ execCommand.getRedirectOutputFile(),
//                execCommand.getRunAs(),
//                session);


        return new AbstractSyncIProcessExecHelper(session) {
            @Override
            public int exec() {
                String[] nec = resolveNutsExecutableCommand();
                ArrayList<String> cmd2 = new ArrayList<>();
                cmd2.addAll(Arrays.asList(nec));
                cmd2.add("---caller-app=remote-nuts");
                cmd2.add("--exec");
                cmd2.addAll(executorOptions);
                if (def != null) {
                    cmd2.add(def.getId().toString());
                }
                cmd2.addAll(Arrays.asList(cmd));
                return runOnce(cmd2.toArray(new String[0]));
            }
        };
    }

    private String[] resolveNutsExecutableCommand() {
        Map<String, RemoteConnexionStringInfo> m = execSession.getOrComputeWorkspaceProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                s -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(execCommand.getHost(), v -> new RemoteConnexionStringInfo());
        if (k.isUpdatable()) {
            k.update(this);
        }
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(k.javaCommand);
        cmd.add("-jar");
        cmd.add(k.nutsJar);
        return cmd.toArray(new String[0]);
    }

    private String runOnceGrab(String... cmd) {
        NSession sc = execSession.copy();
        sc.setTerminal(NSessionTerminal.ofMem(sc));
        int e = commExec.exec(new DefaultNExecCommandExtensionContext(
                execCommand.getHost(),
                cmd, sc
        ));
        if (e != 0) {
            execSession.err().println(sc.out().toString());
            execSession.err().println(sc.err().toString());
            throw new NExecutionException(session, NMsg.ofC("command exit with code :%s", e), e);
        }
        return sc.out().toString();
    }

    private int runOnce(String[] cmd) {
        return commExec.exec(new DefaultNExecCommandExtensionContext(
                execCommand.getHost(),
                cmd, execSession
        ));
    }


    @Override
    public void execute() {
        resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of(execSession).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(session).ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of(session).ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return execCommand.getRunAs() + " " + NCmdLine.of(cmd).toString();
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
