package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;

import java.text.DecimalFormat;

public class ExamplesOfCp {
    public void executeAll() {
        executeSomeCommand();
        executeCustomMonitor();
    }

    public void executeSomeCommand() {
        NSession session = NSession.of();
        session.out().println("Example of ## Cp ##");
        NCp.of()
                .from(NPath.of("http://www.google.com"))
                .to(NPath.of("example.txt"))
                .addOptions(NPathOption.LOG, NPathOption.TRACE)
                .run();
    }

    public void executeCustomMonitor() {
        NSession session = NSession.of();
        session.out().println("Example of ## Cp ##");
        NCp.of()
                .from(NPath.of("http://www.google.com"))
                .to(NPath.of("example.txt"))
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
