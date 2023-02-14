package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.io.NPath;

public class ZipInfo {
    private NPath cwd;
    private NPath src;
    private NPath target;

    public ZipInfo(NPath cwd, NPath src, NPath target) {
        this.cwd = cwd;
        this.src = src;
        this.target = target;
    }

    public NPath getCwd() {
        return cwd;
    }

    public NPath getSrc() {
        return src;
    }

    public NPath getTarget() {
        return target;
    }
}
