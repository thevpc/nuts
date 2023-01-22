package net.thevpc.nuts.toolbox.ndb.base;

import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;

public class CmdRedirect {
    private NCommandLine cmd;
    private NPath path;

    public CmdRedirect(NCommandLine cmd, NPath path) {
        this.cmd = cmd;
        this.path = path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(cmd);
        if (path != null) {
            sb.append(" > ");
            sb.append(path);
        }
        return sb.toString();
    }

    public NCommandLine getCmd() {
        return cmd;
    }

    public NPath getPath() {
        return path;
    }
}
