package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreArrayUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class DefaultNutsWorkspaceCustomCommand implements NutsWorkspaceCustomCommand {

    private NutsLogger LOG;
    private String name;
    private NutsId owner;
    private String factoryId;
    private String[] command;
    private String[] helpCommand;
    private String helpText;
    private String[] executorOptions;
    private NutsWorkspace ws;

    public DefaultNutsWorkspaceCustomCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.getWorkspace().log().setSession(session).of(DefaultNutsWorkspaceCustomCommand.class);
        }
        return LOG;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    @Override
    public NutsId getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNutsWorkspaceCustomCommand setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void exec(String[] args, NutsCommandExecOptions options, NutsSession session) {
        String[] executorOptions = options.getExecutorOptions();
        executorOptions = CoreArrayUtils.concatArrays(this.getExecutorOptions(), executorOptions);
        List<String> r = new ArrayList<>(Arrays.asList(this.getCommand()));
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);

        ws.exec()
                .addCommand(args)
                .addExecutorOptions(executorOptions)
                .setDirectory(options.getDirectory())
                .setFailFast(true)
                .setSession(session)
                .setEnv(options.getEnv())
                .setExecutionType(options.getExecutionType())
                .setFailFast(true)
                .run();

        //load all needed dependencies!
//        return ((DefaultNutsWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
    }

    @Override
    public void dryExec(String[] args, NutsCommandExecOptions options, NutsSession session) throws NutsExecutionException {
        String[] executorOptions = options.getExecutorOptions();
        executorOptions = CoreArrayUtils.concatArrays(this.getExecutorOptions(), executorOptions);
        List<String> r = new ArrayList<>(Arrays.asList(this.getCommand()));
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);

        session.getWorkspace().exec()
                .addCommand(args)
                .addExecutorOptions(executorOptions)
                .setDirectory(options.getDirectory())
                .setFailFast(true)
                .setSession(session)
                .setEnv(options.getEnv())
                .setExecutionType(options.getExecutionType())
                .setFailFast(true)
                .run();

        //load all needed dependencies!
//        return ((DefaultNutsWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
    }

    @Override
    public String getHelpText(NutsSession session) throws NutsExecutionException {
        if (!CoreStringUtils.isBlank(helpText)) {
            return helpText;
        }
        if (helpCommand != null && helpCommand.length > 0) {
            try {
                return ws.exec()
                        .addCommand(helpCommand)
                        .setFailFast(false)
                        .setRedirectErrorStream(true)
                        .grabOutputString()
                        .run()
                        .getOutputString();
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINE).error(ex).log("failed to retrieve help for {0}", getName());
                return "failed to retrieve help for " + getName();
            }
        }
        return null;
    }

    @Override
    public String[] getCommand() {
        return command == null ? new String[0] : Arrays.copyOf(command, command.length);
    }

    public DefaultNutsWorkspaceCustomCommand setCommand(String[] command) {
        this.command = command;
        return this;
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : Arrays.copyOf(executorOptions, command.length);
    }

    public DefaultNutsWorkspaceCustomCommand setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public NutsCommandConfig toCommandConfig() {
        return new NutsCommandConfig()
                .setCommand(getCommand())
                .setFactoryId(getFactoryId())
                .setOwner(getOwner())
                .setExecutorOptions(getExecutorOptions())
                .setName(getName())
                .setHelpCommand(helpCommand)
                .setHelpText(helpText);
    }

    public DefaultNutsWorkspaceCustomCommand setOwner(NutsId owner) {
        this.owner = owner;
        return this;
    }

    public DefaultNutsWorkspaceCustomCommand setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public DefaultNutsWorkspaceCustomCommand setHelpCommand(String[] helpCommand) {
        this.helpCommand = helpCommand;
        return this;
    }

    public DefaultNutsWorkspaceCustomCommand setHelpText(String helpText) {
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
    public String toString() {
        return "DefaultNutsWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + Arrays.toString(command) + ", executorOptions=" + Arrays.toString(executorOptions) + '}';
    }
}
