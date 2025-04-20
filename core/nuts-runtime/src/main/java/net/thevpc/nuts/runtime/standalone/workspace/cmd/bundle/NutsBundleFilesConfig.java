package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.util.NStringBuilder;

class NutsBundleFilesConfig {
    NStringBuilder text = new NStringBuilder();

    public void install(String from, String to) {
        text.println("install " + from
                + " "
                + to
        );
    }

    public void install(NOsFamily osFamily, String from, String to) {
        if (osFamily == null) {
            install(from, to);
            return;
        }
        text.println("install --" + osFamily.id() + " "
                + from
                + " "
                + to
        );
    }

    public void installExecutable(NOsFamily osFamily, String from, String to) {
        if (osFamily == null) {
            install(from, to);
            return;
        }
        text.println("install --" + osFamily.id() + " "
                + from
                + " "
                + to
        );
        switch (osFamily) {
            case UNIX:
            case LINUX:
            case MACOS: {
                text.println("set-executable --" + osFamily.id() + " " + to);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
