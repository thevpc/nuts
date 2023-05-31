/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.artifact;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.boot.NWorkspaceCmdLineParser;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommandExtensionContext;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.RemoteConnexionStringInfo;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultSpawnExecutableNutsRemote extends AbstractNExecutableCommand {

    NDefinition def;
    String[] cmd;
    List<String> executorOptions;
    NConnexionString connexionString;
    private boolean showCommand = false;
    private NExecCommandExtension commExec;
    NExecInput in;
    NExecOutput out;
    NExecOutput err;


    public DefaultSpawnExecutableNutsRemote(NExecCommandExtension commExec, NDefinition def, String[] cmd,
                                            List<String> executorOptions, NExecCommand execCommand,
                                            NExecInput in,
                                            NExecOutput out,
                                            NExecOutput err

    ) {
        super(def.getId().toString(),
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.def = def;
        this.in = in;
        this.out = out;
        this.err = err;
        this.cmd = cmd;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get(getSession());
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
        return def.getId();
    }

    private AbstractSyncIProcessExecHelper resolveExecHelper() {
        return new AbstractSyncIProcessExecHelper(getSession()) {
            @Override
            public int exec() {
                String[] nec = resolveNutsExecutableCommand();
                ArrayList<String> cmd2 = new ArrayList<>();
                cmd2.addAll(Arrays.asList(nec));
                return runOnce(cmd2.toArray(new String[0]));
            }
        };
    }


    private String[] resolveNutsExecutableCommand() {
        NSession session = getSession();
        NExecCommand execCommand = getExecCommand();
        NWorkspaceOptionsBuilder wOptions = new DefaultNWorkspaceOptionsBuilder();
        wOptions.setBot(session.isBot());
        wOptions.setConfirm(session.getConfirm());
        wOptions.setDry(session.isDry());
        wOptions.setExpireTime(session.getExpireTime());
        wOptions.setGui(session.isGui());
        wOptions.setLocale(session.getLocale());
        wOptions.setTerminalMode(session.getTerminal().getOut().getTerminalMode());
        wOptions.setTrace(session.isTrace());
        wOptions.setTransitive(session.isTransitive());
        // will be processed "in amont"
        //wOptions.setRunAs(session1.getRunAs());
        //if(getExecCommand().getRunAs()!=null) {
        //    wOptions.setRunAs(getExecCommand().getRunAs());
        //}
        wOptions.setFetchStrategy(session.getFetchStrategy());
        wOptions.setExecutionType(session.getExecutionType());
        if (getExecCommand().getExecutionType() != null) {
            wOptions.setExecutionType(getExecCommand().getExecutionType());
        }
        wOptions.setOutLinePrefix(session.getOutLinePrefix());
        wOptions.setOutputFormat(session.getOutputFormat());
        wOptions.setOutputFormatOptions(session.getOutputFormatOptions());

        String[] executorOptions = execCommand.getExecutorOptions().toArray(new String[0]);
        RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(execCommand.getTarget(), session);
        wOptions.setWorkspace(k.getWorkspaceName(commExec, session));

        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(k.getJavaCommand(commExec, session));
        cmd.add("-jar");
        cmd.add(k.getNutsJar(commExec, session));

        NCmdLine ncmdLine = wOptions.toCommandLine(new NWorkspaceOptionsConfig().setCompact(true));
        cmd.addAll(ncmdLine.toStringList());


        int dependenciesCount = 0;
        boolean requireTempRepo = false;
        if (def != null) {
            for (NDependency d : def.getDependencies().get()) {
                NId id = d.toId();
                //just ignore nuts base deps!
                if (
                        !id.equalsLongId(session.getWorkspace().getApiId())
                                && !id.equalsLongId(session.getWorkspace().getRuntimeId())
                ) {
                    k.copyId(id, k.getStoreLocationCacheRepoSSH(commExec, getSession()), session, null);
                    dependenciesCount++;
                }
            }
            if (true) {
                k.copyId(def.getId(), k.getStoreLocationCacheRepoSSH(commExec, getSession()), session, null);
                dependenciesCount++;
            }
        }
        //if (dependenciesCount > 0) {
        //    if (requireTempRepo) {
                cmd.add("-r=" + k.getStoreLocationCacheRepoSSH(commExec, getSession()).getLocation());
        //    }
        //}
        cmd.add("---caller-app=remote-nuts");

        // I'll see later on what to include here...
        List<String> exportedExecutorOptions = new ArrayList<>();

        {
            NCmdLine executorOptionsCmdLine = NCmdLine.of(executorOptions);
            //copy all nuts options here...
            while (executorOptionsCmdLine.hasNext()) {
                NOptional<List<NArg>> o = NWorkspaceCmdLineParser.nextNutsArgument(executorOptionsCmdLine, null, getSession());
                if (o.isPresent()) {
                    switch (o.get().get(0).key()) {
                        case "--exec": {
                            executorOptionsCmdLine.skip();
                            break;
                        }
                        default: {
                            cmd.addAll(o.get().stream().map(x -> x.toString()).collect(Collectors.toList()));
                        }
                    }
                } else {
                    executorOptionsCmdLine.skip();
                }
                break;
            }
        }

        cmd.add("--exec");
        cmd.addAll(exportedExecutorOptions);
        if (def != null) {
            cmd.add(def.getId().toString());
        }
        cmd.addAll(Arrays.asList(this.cmd));
        return k.buildEffectiveCommand(cmd.toArray(new String[0]), execCommand.getRunAs(), executorOptions, commExec, session);
    }


    private int runOnce(String[] cmd) {
        return commExec.exec(new DefaultNExecCommandExtensionContext(
                getExecCommand().getTarget(),
                cmd, getSession(),
                in,
                out,
                err
        ));
    }


    @Override
    public int execute() {
        return resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of(getSession()).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(getSession()).ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of(getSession()).ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return getExecCommand().getRunAs() + " " + (def == null ? "" : (def.getId() + " ")) + NCmdLine.of(cmd).toString();
    }

}
