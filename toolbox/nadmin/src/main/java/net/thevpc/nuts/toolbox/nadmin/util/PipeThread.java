/**
 * ====================================================================
 *            vpc-common-io : common reusable library for
 *                          input/output
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nadmin.util;


import java.io.IOException;
import java.io.OutputStream;

public class PipeThread extends Thread implements StopMonitor {

    private final NonBlockingInputStream in;
    private final OutputStream out;
    private final Object lock = new Object();
    private long pipedBytesCount = 0;
    private boolean requestStop = false;
    private boolean stopped = false;

    public PipeThread(String name, NonBlockingInputStream in, OutputStream out) {
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
    }

    @Override
    public void run() {
        try {
            byte[] bytes = new byte[10240];
            int count;
            while (true) {
                if (this.shouldStop()) {
                    break;
                }
                if (in.hasMoreBytes()) {
                    count = in.readNonBlocking(bytes, 500);
                    if (count > 0) {
                        pipedBytesCount += count;
                        out.write(bytes, 0, count);
                        out.flush();
                        }
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

    public NonBlockingInputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }
}
