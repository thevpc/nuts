package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionType;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.util.NProgressEvent;
import net.thevpc.nuts.util.NProgressListener;

import java.text.DecimalFormat;

public class ExamplesOfCp {
    public void executeAll(NSession session) {
        executeSomeCommand(session);
        executeCustomMonitor(session);
    }

    public void executeSomeCommand(NSession session) {
        session.out().println("Example of ## Cp ##");
        NCp.of(session)
                .from(NPath.of("http://www.google.com", session))
                .to(NPath.of("example.txt", session))
                .addOptions(NPathOption.LOG, NPathOption.TRACE)
                .run();
    }

    public void executeCustomMonitor(NSession session) {
        session.out().println("Example of ## Cp ##");
        NCp.of(session)
                .from(NPath.of("http://www.google.com", session))
                .to(NPath.of("example.txt", session))
                .setProgressMonitor(new NProgressListener() {
                    @Override
                    public boolean onProgress(NProgressEvent event) {
                        session.out().println(NMsg.ofC("some progress %s %s",
                                event.isIndeterminate() ? "?" : new DecimalFormat("0.00%").format(event.getProgress())
                                , event.getMessage()));
                        return true;
                    }
                })
                .run();
    }
}
