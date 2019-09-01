package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsIOManager;
import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class NutsLogConsoleHandler extends StreamHandler {
    private OutputStream out;

    public NutsLogConsoleHandler(PrintStream out,boolean closeable) {
        setOutputStream(out,closeable);
    }

    protected synchronized void setOutputStream(OutputStream out,boolean closable) throws SecurityException {
        this.out=out;
        if(closable) {
            super.setOutputStream(out);
        }else{
            super.setOutputStream(new PrintStream(out){
                @Override
                public void close() {
                    //
                }
            });
        }
    }

    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        if (record instanceof NutsLogRecord) {
            NutsLogRecord rr = (NutsLogRecord) record;
            NutsWorkspace ws = rr.getWorkspace();
            NutsIOManager io = ws.io();
            NutsTerminalFormat tf = io==null?null:io.terminalFormat();
            if (tf!=null && tf.isFormatted(out)) {
                setFormatter(NutsLogRichFormatter.RICH);
            } else {
                setFormatter(NutsLogPlainFormatter.PLAIN);
            }
        } else {
            setOutputStream(System.err);
        }
        super.publish(record);
//        flush();
    }
}
