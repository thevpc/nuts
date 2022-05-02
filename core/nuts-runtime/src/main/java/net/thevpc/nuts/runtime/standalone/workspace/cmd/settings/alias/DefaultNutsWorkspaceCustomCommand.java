package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class DefaultNutsWorkspaceCustomCommand implements NutsWorkspaceCustomCommand {

    private NutsLogger LOG;
    private String name;
    private NutsId owner;
    private String factoryId;
    private List<String> command;
    private List<String> helpCommand;
    private String helpText;
    private List<String> executorOptions;
    private NutsWorkspace ws;

    public DefaultNutsWorkspaceCustomCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsWorkspaceCustomCommand.class,session);
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
        List<String> executorOptions = new ArrayList<>(options.getExecutorOptions());
        executorOptions.addAll(this.getExecutorOptions());
        List<String> r = new ArrayList<>(this.getCommand());
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);

        session.exec()
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
        List<String> executorOptions = new ArrayList<>(options.getExecutorOptions());
        executorOptions.addAll(this.getExecutorOptions());
        List<String> r = new ArrayList<>(this.getCommand());
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);

        session.exec()
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
        if (!NutsBlankable.isBlank(helpText)) {
            return helpText;
        }
        if (helpCommand != null && helpCommand.size() > 0) {
            try {
                return session.exec()
                        .addCommand(helpCommand)
                        .setFailFast(false)
                        .setRedirectErrorStream(true)
                        .grabOutputString()
                        .run()
                        .getOutputString();
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINE).error(ex).log(NutsMessage.jstyle("failed to retrieve help for {0}", getName()));
                return "failed to retrieve help for " + getName();
            }
        }
        return null;
    }

    @Override
    public List<String> getCommand() {
        return PrivateNutsUtilCollections.unmodifiableList(command);
    }

    public DefaultNutsWorkspaceCustomCommand setCommand(List<String> command) {
        this.command = PrivateNutsUtilCollections.nonNullList(command);
        return this;
    }
    public DefaultNutsWorkspaceCustomCommand setCommand(String[] command) {
        this.command = PrivateNutsUtilCollections.nonNullListFromArray(command);
        return this;
    }

    @Override
    public List<String> getExecutorOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(executorOptions);
    }

    public DefaultNutsWorkspaceCustomCommand setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = PrivateNutsUtilCollections.nonNullList(executorOptions);
        return this;
    }

    public DefaultNutsWorkspaceCustomCommand setExecutorOptions(String[] executorOptions) {
        this.executorOptions = PrivateNutsUtilCollections.nonNullListFromArray(executorOptions);
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

    public DefaultNutsWorkspaceCustomCommand setHelpCommand(List<String> helpCommand) {
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
        return "DefaultNutsWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + command + ", executorOptions=" + executorOptions + '}';
    }
}
