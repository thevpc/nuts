package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class DefaultNCustomCommand implements NCustomCommand {

    private NLog LOG;
    private String name;
    private NId owner;
    private String factoryId;
    private List<String> command;
    private List<String> helpCommand;
    private String helpText;
    private List<String> executorOptions;
    private NWorkspace ws;

    public DefaultNCustomCommand(NWorkspace ws) {
        this.ws = ws;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNCustomCommand.class, session);
        }
        return LOG;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    @Override
    public NId getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNCustomCommand setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int exec(String[] args, NCommandExecOptions options, NSession session) {
        if (session.isDry()) {
            List<String> executorOptions = new ArrayList<>(options.getExecutorOptions());
            executorOptions.addAll(this.getExecutorOptions());
            List<String> r = new ArrayList<>(this.getCommand());
            r.addAll(Arrays.asList(args));
            args = r.toArray(new String[0]);

            return NExecCommand.of(session)
                    .addCommand(args)
                    .addExecutorOptions(executorOptions)
                    .setDirectory(options.getDirectory())
                    .setFailFast(true)
                    .setEnv(options.getEnv())
                    .setExecutionType(options.getExecutionType())
                    .setFailFast(true)
                    .run()
                    .getResult();

            //load all needed dependencies!
//        return ((DefaultNWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
        } else {
            List<String> executorOptions = new ArrayList<>(options.getExecutorOptions());
            executorOptions.addAll(this.getExecutorOptions());
            List<String> r = new ArrayList<>(this.getCommand());
            r.addAll(Arrays.asList(args));
            args = r.toArray(new String[0]);

            return NExecCommand.of(session)
                    .addCommand(args)
                    .addExecutorOptions(executorOptions)
                    .setDirectory(options.getDirectory())
                    .setFailFast(true)
                    .setEnv(options.getEnv())
                    .setExecutionType(options.getExecutionType())
                    .setFailFast(true)
                    .run()
                    .getResult();

            //load all needed dependencies!
//        return ((DefaultNWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
        }
    }


    @Override
    public NText getHelpText(NSession session) throws NExecutionException {
        if (!NBlankable.isBlank(helpText)) {
            return NTexts.of(session).ofPlain(helpText);
        }
        if (helpCommand != null && helpCommand.size() > 0) {
            try {
                return NTexts.of(session).ofPlain(
                        NExecCommand.of(session)
                                .addCommand(helpCommand)
                                .setFailFast(false)
                                .redirectErrorStream()
                                .grabOutputString()
                                .run()
                                .getOutputString()
                );
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINE).error(ex).log(NMsg.ofJ("failed to retrieve help for {0}", getName()));
                return NTexts.of(session).ofStyled("failed to retrieve help for " + getName(), NTextStyle.error());
            }
        }
        return null;
    }

    @Override
    public List<String> getCommand() {
        return CoreCollectionUtils.unmodifiableList(command);
    }

    public DefaultNCustomCommand setCommand(List<String> command) {
        this.command = CoreCollectionUtils.nonNullList(command);
        return this;
    }

    public DefaultNCustomCommand setCommand(String[] command) {
        this.command = CoreCollectionUtils.nonNullListFromArray(command);
        return this;
    }

    @Override
    public List<String> getExecutorOptions() {
        return CoreCollectionUtils.unmodifiableList(executorOptions);
    }

    public DefaultNCustomCommand setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        return this;
    }

    public DefaultNCustomCommand setExecutorOptions(String[] executorOptions) {
        this.executorOptions = CoreCollectionUtils.nonNullListFromArray(executorOptions);
        return this;
    }

    @Override
    public NCommandConfig toCommandConfig() {
        return new NCommandConfig()
                .setCommand(getCommand())
                .setFactoryId(getFactoryId())
                .setOwner(getOwner())
                .setExecutorOptions(getExecutorOptions())
                .setName(getName())
                .setHelpCommand(helpCommand)
                .setHelpText(helpText);
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

    public NWorkspace getWorkspace() {
        return ws;
    }

    public void setWs(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String toString() {
        return "NWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + command + ", executorOptions=" + executorOptions + '}';
    }
}
