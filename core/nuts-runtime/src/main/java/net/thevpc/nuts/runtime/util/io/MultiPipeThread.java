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
package net.thevpc.nuts.runtime.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class MultiPipeThread extends Thread implements StopMonitor {

    private static class PipeInfo {

        private final String id;
        private final String name;
        private final NonBlockingInputStream in;
        private final OutputStream out;
        private long pipedBytesCount = 0;

        public PipeInfo(String id, String name, NonBlockingInputStream in, OutputStream out) {
            this.id = id;
            this.name = name;
            this.in = in;
            this.out = out;
        }
    }
    private final Object lock = new Object();
    private boolean requestStop = false;
    private boolean stopped = false;
    private Map<String, PipeInfo> list = new HashMap<>();
    private String[] ids = null;

    public MultiPipeThread(String name) {
        super(name);
    }

    public String add(String name, NonBlockingInputStream in, OutputStream out) {
        String id = UUID.randomUUID().toString();
        list.put(id, new PipeInfo(id, name, in, out));
        return id;
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

    private String[] getIds() {
        if (ids == null) {
            ids = list.keySet().toArray(new String[list.size()]);
        }
        return ids;
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
                for (String s : getIds()) {
                    PipeInfo p = list.get(s);
                    if (p.in.hasMoreBytes()) {
                        count = p.in.readNonBlocking(bytes);
                        if (count > 0) {
                            p.pipedBytesCount += count;
                            p.out.write(bytes, 0, count);
                            p.out.flush();
                        }
                    } else {
                        p.out.close();
                        list.remove(s);
                        ids = null;
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //
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

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public boolean isStopped() {
        return stopped;
    }
}
