package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultNCustomCommand implements NCustomCmd {

    private String name;
    private NId owner;
    private String factoryId;
    private List<String> command;
    private List<String> helpCommand;
    private String helpText;
    private List<String> executorOptions;

    public DefaultNCustomCommand() {
    }

    protected NLog _LOG() {
            return NLog.of(DefaultNCustomCommand.class);
    }

    @Override
    public String factoryId() {
        return factoryId;
    }

    @Override
    public NId owner() {
        return owner;
    }

    @Override
    public String name() {
        return name;
    }

    public DefaultNCustomCommand setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int exec(String[] args, NCmdExecOptions options) {
        NSession session = NSession.of();
        if (session.isDry()) {
            List<String> executorOptions = new ArrayList<>(options.executorOptions());
            executorOptions.addAll(this.executorOptions());
            List<String> r = new ArrayList<>(this.command());
            r.addAll(Arrays.asList(args));
            args = r.toArray(new String[0]);

            return NExec.of()
                    .addCommand(args)
                    .addExecutorOptions(executorOptions)
                    .directory(options.directory())
                    .failFast(true)
                    .env(options.env())
                    .executionType(options.executionType())
                    .run()
                    .exitCode();

            //load all needed dependencies!
//        return ((DefaultNWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
        } else {
            List<String> executorOptions = new ArrayList<>(options.executorOptions());
            executorOptions.addAll(this.executorOptions());
            List<String> r = new ArrayList<>(this.command());
            r.addAll(Arrays.asList(args));
            args = r.toArray(new String[0]);

            return NExec.of()
                    .addCommand(args)
                    .addExecutorOptions(executorOptions)
                    .directory(options.directory())
                    .failFast(true)
                    .env(options.env())
                    .executionType(options.executionType())
                    .run()
                    .exitCode();

            //load all needed dependencies!
//        return ((DefaultNWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
        }
    }


    @Override
    public NText helpText() throws NExecutionException {
        if (!NBlankable.isBlank(helpText)) {
            return NText.ofPlain(helpText);
        }
        if (helpCommand != null && helpCommand.size() > 0) {
            try {
                return NText.ofPlain(
                        NExec.of()
                                .addCommand(helpCommand)
                                .failFast(false)
                                .run()
                                .grabbedAll()
                );
            } catch (Exception ex) {
                _LOG().log(NMsg.ofC("failed to retrieve help for %s", name()).asFine(ex));
                return NText.ofStyled("failed to retrieve help for " + name(), NTextStyle.error());
            }
        }
        return null;
    }

    @Override
    public List<String> command() {
        return NCollections.unmodifiableList(command);
    }

    public DefaultNCustomCommand setCommand(List<String> command) {
        this.command = NCollections.nonNullList(command);
        return this;
    }

    public DefaultNCustomCommand setCommand(String[] command) {
        this.command = NCollections.nonNullListFromArray(command);
        return this;
    }

    @Override
    public List<String> executorOptions() {
        return NCollections.unmodifiableList(executorOptions);
    }

    public DefaultNCustomCommand setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = NCollections.nonNullList(executorOptions);
        return this;
    }

    public DefaultNCustomCommand setExecutorOptions(String[] executorOptions) {
        this.executorOptions = NCollections.nonNullListFromArray(executorOptions);
        return this;
    }

    @Override
    public NCommandConfig toCommandConfig() {
        return new NCommandConfig()
                .command(command())
                .factoryId(factoryId())
                .owner(owner())
                .executorOptions(executorOptions())
                .name(name())
                .helpCommand(helpCommand)
                .helpText(helpText);
    }

    public DefaultNCustomCommand setOwner(NId owner) {
        this.owner = owner;
        return this;
    }

    public DefaultNCustomCommand setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public DefaultNCustomCommand setHelpCommand(List<String> helpCommand) {
        this.helpCommand = helpCommand;
        return this;
    }

    public DefaultNCustomCommand setHelpText(String helpText) {
        this.helpText = helpText;
        return this;
    }

    @Override
    public String toString() {
        return "NWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + command + ", executorOptions=" + executorOptions + '}';
    }
}
