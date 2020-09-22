/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NonBlockingInputStreamAdapter extends FilterInputStream implements NonBlockingInputStream,Interruptible {

    private boolean hasMoreBytes = true;
    private boolean closed = false;
    private boolean interrupted = false;
    private String name;

    public NonBlockingInputStreamAdapter(String name, InputStream in) {
        super(in);
        this.name = name;
    }

    @Override
    public void interrupt() throws InterruptException {
        this.interrupted=true;
    }

    @Override
    public int read() throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
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
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
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
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
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
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        if (available() == 0 && !hasMoreBytes()) {
            return 0;
        }
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
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
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        long now = System.currentTimeMillis();
        long then = now + timeout;
        long tic = 100;
//        int read=0;
        while (true) {
            if(interrupted){
                throw new IOException(new InterruptException("Interrupted"));
            }
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
            try {
                Thread.sleep(tic);
            } catch (InterruptedException e) {
                break;
            }
        }
        return 0;
    }

    @Override
    public int readNonBlocking(byte[] b) throws IOException {
        return readNonBlocking(b, 0, b.length);
    }

    @Override
    public int readNonBlocking(byte[] b, int off, int len) throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
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
}
