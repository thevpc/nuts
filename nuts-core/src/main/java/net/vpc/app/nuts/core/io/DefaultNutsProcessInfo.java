package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.NutsProcessInfo;

public class DefaultNutsProcessInfo implements NutsProcessInfo {
    private String id;
    private String name;
    private String title;
    private String commandLine;

    public DefaultNutsProcessInfo(String id, String name, String title, String commandLine) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.commandLine = commandLine;
    }

    @Override
    public String getPid() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }
}
