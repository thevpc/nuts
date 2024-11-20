package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;

class LocalTomcatLogLineVisitor {

    boolean outOfMemoryError;
    String startMessage;
    String shutdownMessage;
    Boolean started;
    String path;
    NSession session;

    public LocalTomcatLogLineVisitor(String path, String startMessage, String shutdownMessage, NSession session) {
        this.path = path;
        this.startMessage = startMessage;
        this.shutdownMessage = shutdownMessage;
        this.session = session;
    }

    public void visit() {
        NPath.of(path).getLines()
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
