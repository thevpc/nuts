package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NCoreCollectionUtils;
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

public class DefaultNCustomCommand implements NCustomCmd {

    private String name;
    private NId owner;
    private String factoryId;
    private List<String> command;
    private List<String> helpCommand;
    private String helpText;
    private List<String> executorOptions;
    private NWorkspace workspace;

    public DefaultNCustomCommand(NWorkspace workspace) {
        this.workspace = workspace;
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
            return NLog.of(DefaultNCustomCommand.class);
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
    public int exec(String[] args, NCmdExecOptions options) {
        NSession session = workspace.currentSession();
        if (session.isDry()) {
            List<String> executorOptions = new ArrayList<>(options.getExecutorOptions());
            executorOptions.addAll(this.getExecutorOptions());
            List<String> r = new ArrayList<>(this.getCommand());
            r.addAll(Arrays.asList(args));
            args = r.toArray(new String[0]);

            return NExecCmd.of()
                    .addCommand(args)
                    .addExecutorOptions(executorOptions)
                    .setDirectory(options.getDirectory())
                    .failFast()
                    .setEnv(options.getEnv())
                    .setExecutionType(options.getExecutionType())
                    .run()
                    .getResultCode();

            //load all needed dependencies!
//        return ((DefaultNWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
        } else {
            List<String> executorOptions = new ArrayList<>(options.getExecutorOptions());
            executorOptions.addAll(this.getExecutorOptions());
            List<String> r = new ArrayList<>(this.getCommand());
            r.addAll(Arrays.asList(args));
            args = r.toArray(new String[0]);

            return NExecCmd.of()
                    .addCommand(args)
                    .addExecutorOptions(executorOptions)
                    .setDirectory(options.getDirectory())
                    .failFast()
                    .setEnv(options.getEnv())
                    .setExecutionType(options.getExecutionType())
                    .run()
                    .getResultCode();

            //load all needed dependencies!
//        return ((DefaultNWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
        }
    }


    @Override
    public NText getHelpText() throws NExecutionException {
        if (!NBlankable.isBlank(helpText)) {
            return NText.ofPlain(helpText);
        }
        if (helpCommand != null && helpCommand.size() > 0) {
            try {
                return NText.ofPlain(
                        NExecCmd.of()
                                .addCommand(helpCommand)
                                .setFailFast(false)
                                .run()
                                .getGrabbedAllString()
                );
            } catch (Exception ex) {
                _LOGOP().level(Level.FINE).error(ex).log(NMsg.ofC("failed to retrieve help for %s", getName()));
                return NText.ofStyled("failed to retrieve help for " + getName(), NTextStyle.error());
            }
        }
        return null;
    }

    @Override
    public List<String> getCommand() {
        return NCoreCollectionUtils.unmodifiableList(command);
    }

    public DefaultNCustomCommand setCommand(List<String> command) {
        this.command = NCoreCollectionUtils.nonNullList(command);
        return this;
    }

    public DefaultNCustomCommand setCommand(String[] command) {
        this.command = NCoreCollectionUtils.nonNullListFromArray(command);
        return this;
    }

    @Override
    public List<String> getExecutorOptions() {
        return NCoreCollectionUtils.unmodifiableList(executorOptions);
    }

    public DefaultNCustomCommand setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = NCoreCollectionUtils.nonNullList(executorOptions);
        return this;
    }

    public DefaultNCustomCommand setExecutorOptions(String[] executorOptions) {
        this.executorOptions = NCoreCollectionUtils.nonNullListFromArray(executorOptions);
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
        return workspace;
    }

    public void setWorkspace(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String toString() {
        return "NWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + command + ", executorOptions=" + executorOptions + '}';
    }
}
