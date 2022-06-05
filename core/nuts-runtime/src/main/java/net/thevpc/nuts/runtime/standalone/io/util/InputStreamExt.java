/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.text.NutsTextStyle;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author thevpc
 */
public class InputStreamExt extends InputStream implements NutsInputSource, Interruptible {

    private InputStream base;
    private Runnable onClose;
    private boolean interrupted;
    private DefaultNutsInputSourceMetadata md;

    public InputStreamExt(InputStream base, NutsInputSourceMetadata md0, Runnable onClose) {
        this.base = base;
        this.onClose = onClose;
        if (md0 == null) {
            if (base instanceof NutsInputSource) {
                md = new DefaultNutsInputSourceMetadata(((NutsInputSource) base).getInputMetaData());
            } else {
                md = new DefaultNutsInputSourceMetadata();
            }
        } else {
            md = new DefaultNutsInputSourceMetadata(md0);
            if (base instanceof NutsInputSource) {
                NutsInputSourceMetadata md2 = ((NutsInputSource) base).getInputMetaData();
                if (md.getContentLength().isNotPresent()) {
                    md.setContentLength(md2.getContentLength().orNull());
                }
                if (md.getContentType().isNotPresent()) {
                    md.setContentType(md2.getContentType().orNull());
                }
                if (md.getMessage().isNotPresent()) {
                    md.setMessage(md2.getMessage().orNull());
                }
                if (md.getName().isNotPresent()) {
                    md.setName(md2.getName().orNull());
                }
                if (md.getKind().isNotPresent()) {
                    md.setKind(md2.getKind().orNull());
                }
            }
        }
    }

    @Override
    public void interrupt() throws InterruptException {
        this.interrupted = true;
    }

    @Override
    public int read() throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        return base.read();
    }

    @Override
    public void close() throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        base.close();
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public int available() throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        return base.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        return base.read(b, off, len);
    }


    @Override
    public NutsInputSourceMetadata getInputMetaData() {
        return md;
    }

    @Override
    public InputStream getInputStream() {
        return this;
    }

    @Override
    public boolean isMultiRead() {
        return false;
    }

    @Override
    public void disposeMultiRead() {
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return NutsFormat.of(session, new NutsFormatSPI() {
            @Override
            public String getName() {
                return "input-stream";
            }

            @Override
            public void print(NutsPrintStream out) {
                NutsOptional<NutsMessage> m = getInputMetaData().getMessage();
                if(m.isPresent()){
                    out.print(m.get());
                }else {
                    out.append(getClass().getSimpleName(), NutsTextStyle.path());
                }
            }

            @Override
            public boolean configureFirst(NutsCommandLine commandLine) {
                return false;
            }
        });
    }

    @Override
    public String toString() {
        NutsPlainPrintStream out = new NutsPlainPrintStream();
        NutsOptional<NutsMessage> m = getInputMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else {
            out.append(getClass().getSimpleName(), NutsTextStyle.path());
        }
        return out.toString();
    }

}
