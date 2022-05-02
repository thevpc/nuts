package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.NutsSession;

class LocalTomcatLogLineVisitor {

    boolean outOfMemoryError;
    String startMessage;
    String shutdownMessage;
    Boolean started;
    String path;
    NutsSession session;

    public LocalTomcatLogLineVisitor(String path, String startMessage, String shutdownMessage, NutsSession session) {
        this.path = path;
        this.startMessage = startMessage;
        this.shutdownMessage = shutdownMessage;
        this.session = session;
    }

    public void visit() {
        NutsPath.of(path,session).lines()
                .forEach(this::nextLine);
    }

    public boolean nextLine(String line) {
        if (line.contains("OutOfMemoryError")) {
            outOfMemoryError = true;
        } else if (startMessage != null && line.contains(startMessage)) {
            started = true;
        } else if (shutdownMessage != null && line.contains(shutdownMessage)) {
            started = false;
        }
        return true;
    }
}
