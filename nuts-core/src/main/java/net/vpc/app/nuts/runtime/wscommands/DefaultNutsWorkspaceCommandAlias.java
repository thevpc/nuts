package net.vpc.app.nuts.runtime.wscommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.vpc.app.nuts.NutsCommandExecOptions;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsWorkspaceCommandAlias;

public class DefaultNutsWorkspaceCommandAlias implements NutsWorkspaceCommandAlias {
    private final NutsLogger LOG;
    private String name;
    private NutsId owner;
    private String factoryId;
    private String[] command;
    private String[] helpCommand;
    private String helpText;
    private String[] executorOptions;
    private NutsWorkspace ws;

    public DefaultNutsWorkspaceCommandAlias(NutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(DefaultNutsWorkspaceCommandAlias.class);
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNutsWorkspaceCommandAlias setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsId getOwner() {
        return owner;
    }

    public DefaultNutsWorkspaceCommandAlias setOwner(NutsId owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    public DefaultNutsWorkspaceCommandAlias setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public DefaultNutsWorkspaceCommandAlias setHelpCommand(String[] helpCommand) {
        this.helpCommand = helpCommand;
        return this;
    }

    public DefaultNutsWorkspaceCommandAlias setHelpText(String helpText) {
        this.helpText = helpText;
        return this;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public void setWs(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String[] getCommand() {
        return command == null ? new String[0] : Arrays.copyOf(command, command.length);
    }

    public DefaultNutsWorkspaceCommandAlias setCommand(String[] command) {
        this.command = command;
        return this;
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : Arrays.copyOf(executorOptions, command.length);
    }

    public DefaultNutsWorkspaceCommandAlias setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public void exec(String[] args, NutsCommandExecOptions options, NutsSession session) {
        String[] executorOptions = options.getExecutorOptions();
        executorOptions = CoreCommonUtils.concatArrays(this.getExecutorOptions(), executorOptions);
        List<String> r = new ArrayList<>(Arrays.asList(this.getCommand()));
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);

        ws.exec()
                .command(args)
                .executorOptions(executorOptions)
                .directory(options.getDirectory())
                .failFast()
                .session(session)
                .env(options.getEnv())
                .executionType(options.getExecutionType())
                .failFast()
                .run();

        //load all needed dependencies!
//        return ((DefaultNutsWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
    }

    @Override
    public void dryExec(String[] args, NutsCommandExecOptions options, NutsSession session) throws NutsExecutionException {
        String[] executorOptions = options.getExecutorOptions();
        executorOptions = CoreCommonUtils.concatArrays(this.getExecutorOptions(), executorOptions);
        List<String> r = new ArrayList<>(Arrays.asList(this.getCommand()));
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);

        ws.exec()
                .command(args)
                .executorOptions(executorOptions)
                .directory(options.getDirectory())
                .failFast()
                .session(session)
                .env(options.getEnv())
                .executionType(options.getExecutionType())
                .failFast()
                .run();

        //load all needed dependencies!
//        return ((DefaultNutsWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
    }

    @Override
    public String getHelpText() throws NutsExecutionException {
        if (!CoreStringUtils.isBlank(helpText)) {
            return helpText;
        }
        if (helpCommand != null && helpCommand.length > 0) {
            try {
                return ws.exec()
                        .command(helpCommand)
                        .failFast(false)
                        .redirectErrorStream()
                        .grabOutputString()
                        .run()
                        .getOutputString();
            } catch (Exception ex) {
                LOG.with().level(Level.FINE).error(ex).log( "Failed to retrieve help for {0}",getName());
                return "Failed to retrieve help for " + getName();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "DefaultNutsWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + Arrays.toString(command) + ", executorOptions=" + Arrays.toString(executorOptions) + '}';
    }

}
