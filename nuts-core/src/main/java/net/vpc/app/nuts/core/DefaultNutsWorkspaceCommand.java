package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.NutsCommandExecOptions;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceCommand;
import net.vpc.common.util.ArrayUtils;

public class DefaultNutsWorkspaceCommand implements NutsWorkspaceCommand {

    private String name;
    private NutsId owner;
    private String factoryId;
    private String[] command;
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
    public int exec(String[] args, NutsCommandExecOptions options, NutsSession session) {
        String[] executorOptions=options.getExecutorOptions();
        NutsDefinition nutToRun = ws.fetch(this.getCommand()[0]).setSession(session).installed().fetchDefinition();
        List<String> r = new ArrayList<>(Arrays.asList(this.getCommand()));
        //remove first element
        r.remove(0);
        r.addAll(Arrays.asList(args));
        args = r.toArray(new String[0]);
        executorOptions = ArrayUtils.concatArrays(this.getExecutorOptions(), executorOptions);

        //load all needed dependencies!
        return ((DefaultNutsWorkspace) ws).exec(nutToRun, this.getName(), args, executorOptions, options.getEnv(), options.getDirectory(), options.isFailFast(), session, options.isEmbedded());
    }
}
