package net.thevpc.nuts.build.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPath;

public class PropFileEditor {
    private NPath path;
    private NSession session;
    private AbstractRunner runner;

    public PropFileEditor(NPath path, NSession session, AbstractRunner runner) {
        this.path = path;
        this.session = session;
        this.runner = runner;
    }

    public void set(String name, String value) {
        runner.sed(name + "=.*", name + "=" + value, path);
    }

    public NPath getPath() {
        return path;
    }
}
