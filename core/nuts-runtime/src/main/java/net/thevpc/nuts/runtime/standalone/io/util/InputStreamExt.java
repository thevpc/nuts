/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NFormat;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.text.NTextStyle;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author thevpc
 */
public class InputStreamExt extends InputStream implements NInputSource, Interruptible {

    private InputStream base;
    private Runnable onClose;
    private boolean interrupted;
    private DefaultNInputSourceMetadata md;

    public InputStreamExt(InputStream base, NInputSourceMetadata md0, Runnable onClose) {
        this.base = base;
        this.onClose = onClose;
        if (md0 == null) {
            if (base instanceof NInputSource) {
                md = new DefaultNInputSourceMetadata(((NInputSource) base).getInputMetaData());
            } else {
                md = new DefaultNInputSourceMetadata();
            }
        } else {
            md = new DefaultNInputSourceMetadata(md0);
            if (base instanceof NInputSource) {
                NInputSourceMetadata md2 = ((NInputSource) base).getInputMetaData();
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
    public NInputSourceMetadata getInputMetaData() {
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
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NFormatSPI() {
            @Override
            public String getName() {
                return "input-stream";
            }

            @Override
            public void print(NOutStream out) {
                NOptional<NMsg> m = getInputMetaData().getMessage();
                if(m.isPresent()){
                    out.print(m.get());
                }else {
                    out.append(getClass().getSimpleName(), NTextStyle.path());
                }
            }

            @Override
            public boolean configureFirst(NCommandLine commandLine) {
                return false;
            }
        });
    }

    @Override
    public String toString() {
        NOutPlainStream out = new NOutPlainStream();
        NOptional<NMsg> m = getInputMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else {
            out.append(getClass().getSimpleName(), NTextStyle.path());
        }
        return out.toString();
    }

}
