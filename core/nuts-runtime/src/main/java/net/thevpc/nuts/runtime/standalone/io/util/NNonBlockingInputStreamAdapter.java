/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NNonBlockingInputStreamAdapter extends FilterInputStream implements NNonBlockingInputStream, NInterruptible<InputStream>, NContentMetadataProvider {

    private boolean hasMoreBytes = true;
    private boolean closed = false;
    private boolean interrupted = false;
    private NWorkspace worksapce;
    private NContentMetadata md;
    private InputStream base;
    private NMsg sourceName;

    public NNonBlockingInputStreamAdapter(InputStream base, NContentMetadata md, NMsg sourceName, NWorkspace workspace) {
        super(base);
        this.base = base;
        this.worksapce = workspace;
        this.md = CoreIOUtils.createContentMetadata(md, base);
        if (sourceName == null) {
            NMsg m2 = this.md.getMessage().orElse(null);
            if (m2 != null) {
                sourceName = m2;
            }
        }
        if (sourceName == null) {
            String m2 = this.md.getName().orElse(null);
            if (m2 != null) {
                sourceName = NMsg.ofPlain(m2);
            }
        }
        this.sourceName = sourceName;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NIOException(NMsg.ofPlain("stream is interrupted"));
        }
    }

    @Override
    public InputStream base() {
        return this;
    }

    @Override
    public void interrupt() throws NInterruptException {
        this.interrupted = true;
        if (base instanceof NInterruptible) {
            ((NInterruptible) base).interrupt();
        }
    }

    @Override
    public int read() throws IOException {
        checkInterrupted();
        if (closed) {
            return -1;
        }
        if (available() == 0 && !hasMoreBytes()) {
            return -1;
        }
        int read = super.read();
        if (read < 0) {
            hasMoreBytes = false;
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        checkInterrupted();
        if (available() == 0 && !hasMoreBytes()) {
            return -1;
        }
        int read = super.read(b);
        if (read < 0) {
            hasMoreBytes = false;
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkInterrupted();
        if (available() == 0 && !hasMoreBytes()) {
            return -1;
        }
        int read = -1;
        try {
            read = super.read(b, off, len);
        } catch (IOException ex) {
            if (ex.getMessage().equals("Stream closed")) {
                //
            } else {
                throw ex;
            }
        }
        if (read < 0) {
            hasMoreBytes = false;
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        checkInterrupted();
        if (available() == 0 && !hasMoreBytes()) {
            return 0;
        }
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        checkInterrupted();
        if (closed) {
            return -1;
        }
        int available = -1;
        try {
            available = super.available();
        } catch (IOException ex) {
            return -1;
        }
        if (available < 0) {
            if (!closed) {
                close();
            }
            return -1;
        }
        if (available == 0 && !hasMoreBytes) {
            return -1;
        }
        if (closed) {
            return -1;
        }

        return available;
    }

    @Override
    public int readNonBlocking(byte[] b, long timeout) throws IOException {
        return readNonBlocking(b, 0, b.length, timeout);
    }

    @Override
    public int readNonBlocking(byte[] b, int off, int len, long timeout) throws IOException {
        checkInterrupted();
        long now = System.currentTimeMillis();
        long then = now + timeout;
        long tic = 100;
//        int read=0;
        while (true) {
            checkInterrupted();
            if (closed) {
                break;
            }
            int available = available();
            if (available < 0) {
                hasMoreBytes = false;
                break;
            } else if (available > 0) {
                return read(b, off, len);
            } else if (!hasMoreBytes()) {
                break;
            }
            now = System.currentTimeMillis();
            if (now > then) {
                break;
            }
            NWorkspaceProfilerImpl.sleep(tic,"NNonBlockingInputStreamAdapter::readNonBlocking");
        }
        return 0;
    }

    @Override
    public int readNonBlocking(byte[] b) throws IOException {
        return readNonBlocking(b, 0, b.length);
    }

    @Override
    public int readNonBlocking(byte[] b, int off, int len) throws IOException {
        checkInterrupted();
        int available = available();
        if (available < 0) {
            hasMoreBytes = false;
        } else if (available > 0) {
            return read(b, off, len);
        } else if (!hasMoreBytes()) {
        }
        return 0;
    }

    public void noMoreBytes() {
        hasMoreBytes = false;
    }

    @Override
    public boolean hasMoreBytes() {
        return hasMoreBytes;
    }

    @Override
    public void close() throws IOException {
        super.close();
        hasMoreBytes = false;
        closed = true;
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    public NMsg getSourceName() {
        return sourceName;
    }

    @Override
    public String toString() {
        NMemoryPrintStream out = NPrintStream.ofMem(NTerminalMode.FILTERED);
        NOptional<NMsg> m = getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else if (sourceName != null) {
            out.print(NText.ofStyled(sourceName,NTextStyle.path()));
        } else {
            out.print(getClass().getSimpleName(), NTextStyle.path());
        }
        return out.toString();
    }

}
