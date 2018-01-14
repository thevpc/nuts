/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import java.io.IOException;
import java.io.OutputStream;

public class PipeThread extends Thread implements StopMonitor {

    private final NutsNonBlockingInputStream in;
    private final OutputStream out;
    private final Object lock = new Object();
    private long pipedBytesCount = 0;
    private boolean requestStop = false;
    private boolean stopped = false;

    public PipeThread(String name, NutsNonBlockingInputStream in, OutputStream out) {
        super(name);
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean shouldStop() {
        return requestStop;
    }

    public void requestStop() {
        requestStop = true;
        if (!stopped) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//            for (int i = 0; i < 100; i++) {
//                if (stopped) {
//                    return;
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    //e.printStackTrace();
//                }
//            }
//            throw new RuntimeException("Unable to stop");
    }

    @Override
    public void run() {
        try {
//            CoreIOUtils.copy(in, out, false, false, this);

            byte[] bytes = new byte[10240];
            int count;
            while (true) {
                if (this.shouldStop()) {
                    break;
                }
                if (in.hasMoreBytes()) {
                    count = in.readNonBlocking(bytes, 500);
                    if(count>0) {
                        pipedBytesCount += count;
                        out.write(bytes, 0, count);
                    }
//                        System.out.println("push "+count);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopped = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public NutsNonBlockingInputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }
}
