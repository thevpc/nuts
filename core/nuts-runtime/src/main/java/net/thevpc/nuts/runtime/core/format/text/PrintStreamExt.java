package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.*;
import java.util.Locale;

public class PrintStreamExt extends PrintStream implements ExtendedFormatAware, NutsSessionAware {
    private OutputStream out;
    private boolean autoFlash2;
    private NutsWorkspace ws;
    private NutsSession session;

    public PrintStreamExt(OutputStream out) {
        super(out);
        this.out = out;
        this.autoFlash2 = false;
    }

    public PrintStreamExt(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        this.out = out;
        this.autoFlash2 = autoFlush;
    }

    public PrintStreamExt(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        this.out = out;
        this.autoFlash2 = autoFlush;
    }

    public PrintStreamExt(String fileName) throws FileNotFoundException {
        this(new FileOutputStream(fileName), false);
        this.autoFlash2 = false;
    }

    public PrintStreamExt(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(fileName), false, csn);
        this.autoFlash2 = false;
    }

    public PrintStreamExt(File file) throws FileNotFoundException {
        this(new FileOutputStream(file), false);
        this.autoFlash2 = false;
    }

    public PrintStreamExt(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(file), false, csn);
        this.autoFlash2 = false;
    }


    public boolean isAutoFlash() {
        return autoFlash2;
    }

    public OutputStream getOut() {
        return out;
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        OutputStream base = getOut();
        if (base instanceof ExtendedFormatAware) {
            return ((ExtendedFormatAware) base).getModeOp();
        }
        return NutsTerminalModeOp.NOP;
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        if(out instanceof ExtendedFormatAware){
            return ((ExtendedFormatAware) out).convert(other);
        }
        return new RawOutputStream(out,ws).convert(other);
    }

    @Override
    public void setSession(NutsSession session) {
        this.session=session;
        this.ws=session==null?null:session.getWorkspace();
    }

    @Override
    public PrintStreamExt format(String format, Object... args) {
        return format(null,format,args);
    }

    @Override
    public PrintStreamExt format(Locale l, String format, Object... args) {
        if(l==null){
            NutsTextNode s = session.getWorkspace().formats().text().setSession(session).nodeFor(
                    NutsMessage.cstyle(
                            format, args
                    )
            );
            print(s);
        }else{
            NutsSession sess = this.session.copy().setLocale(l.toString());
            NutsTextNode s = sess.getWorkspace().formats().text().setSession(sess).nodeFor(
                    NutsMessage.cstyle(
                            format, args
                    )
            );
            print(s);
        }
        return this;
    }

}
