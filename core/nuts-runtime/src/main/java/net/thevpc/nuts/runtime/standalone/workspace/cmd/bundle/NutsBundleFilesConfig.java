package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.util.NStringBuilder;

class NutsBundleFilesConfig {
    NStringBuilder text = new NStringBuilder();

    public void install(String from, String to) {
        text.println("install " + from
                + " "
                + to
        );
    }

    public void installWindows(String from, String to) {
        text.println("install --windows "
                + from
                + " "
                + to
        );
    }

    public void installPosixExecutable(String from, String to) {
        installPosix(from, to);
        setExecutablePosix(to);
    }

    public void setExecutablePosix(String to) {
        text.println("set-executable --posix "
                + to
        );
    }

    public void installPosix(String from, String to) {
        text.println("install --posix "
                + from
                + " "
                + to
        );
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
