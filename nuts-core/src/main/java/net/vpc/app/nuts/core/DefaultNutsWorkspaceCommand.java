package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.NutsCommandExecOptions;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceCommand;
import net.vpc.app.nuts.core.util.CoreCommonUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

public class DefaultNutsWorkspaceCommand implements NutsWorkspaceCommand {

    private String name;
    private NutsId owner;
    private String factoryId;
    private String[] command;
    private String[] helpCommand;
    private String helpText;
    private String[] executorOptions;
    private NutsWorkspace ws;

    public DefaultNutsWorkspaceCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNutsWorkspaceCommand setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsId getOwner() {
        return owner;
    }

    public DefaultNutsWorkspaceCommand setOwner(NutsId owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    public DefaultNutsWorkspaceCommand setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public DefaultNutsWorkspaceCommand setHelpCommand(String[] helpCommand) {
        this.helpCommand = helpCommand;
        return this;
    }

    public DefaultNutsWorkspaceCommand setHelpText(String helpText) {
        this.helpText = helpText;
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    public void setWs(NutsWorkspace ws) {
        this.ws = ws;
    }

    public String[] getCommand() {
        return command;
    }

    public DefaultNutsWorkspaceCommand setCommand(String[] command) {
        this.command = command;
        return this;
    }

    public String[] getExecutorOptions() {
        return executorOptions;
    }

    public DefaultNutsWorkspaceCommand setExecutorOptions(String[] executorOptions) {
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
                .exec();

        //load all needed dependencies!
//        return ((DefaultNutsWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
    }

    @Override
    public String getHelpText() throws NutsExecutionException {
        if (!CoreStringUtils.isBlank(helpText)) {
            return helpText;
        }
        if (helpCommand != null && helpCommand.length > 0) {
            try{
            return ws.exec()
                    .command(helpCommand)
                    .failFast(false)
                    .redirectErrorStream()
                    .grabOutputString()
                    .exec()
                    .getOutputString();
            }catch(Exception ex){
                //ignore
                return "Failed to retrieve help for "+getName();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "DefaultNutsWorkspaceCommand{" + "name=" + name + ", owner=" + owner + ", factoryId=" + factoryId + ", command=" + command + ", executorOptions=" + executorOptions + '}';
    }

}
