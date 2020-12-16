package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NutsIOManager;
import net.thevpc.nuts.NutsTerminalManager;
import net.thevpc.nuts.NutsWorkspace;

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
            NutsTerminalManager tf = io==null?null:io.term();
            if (tf!=null && tf.isFormatted(out)) {
                if(!rr.isFormatted()) {
                    record=((NutsLogRecord) record).escape();
                }
                setFormatter(NutsLogRichFormatter.RICH);
            } else {
                if(rr.isFormatted()) {
                    record=((NutsLogRecord) record).filter();
                }
                setFormatter(NutsLogPlainFormatter.PLAIN);
            }
        } else {
            setFormatter(NutsLogPlainFormatter.PLAIN);
            setOutputStream(System.err);
        }
        super.publish(record);
//        flush();
    }
}
