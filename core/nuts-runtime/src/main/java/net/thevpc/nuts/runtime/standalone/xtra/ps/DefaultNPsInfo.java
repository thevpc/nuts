package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.io.NPsInfo;

public class DefaultNPsInfo implements NPsInfo {
    private String id;
    private String name;
    private String title;
    private String cmdLine;

    public DefaultNPsInfo(String id, String name, String title, String cmdLine) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.cmdLine = cmdLine;
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
        return cmdLine;
    }
}
