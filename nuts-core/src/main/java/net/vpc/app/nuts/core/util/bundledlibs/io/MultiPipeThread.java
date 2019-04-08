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
package net.vpc.app.nuts.core.util.bundledlibs.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class MultiPipeThread extends Thread implements StopMonitor {

    private static class PipeInfo{
        private final String id;
        private final String name;
        private final NonBlockingInputStream in;
        private final OutputStream out;
        private long pipedBytesCount = 0;

        public PipeInfo(String id, String name,NonBlockingInputStream in, OutputStream out) {
            this.id = id;
            this.name = name;
            this.in = in;
            this.out = out;
        }
    }
    private final Object lock = new Object();
    private boolean requestStop = false;
    private boolean stopped = false;
    private Map<String,PipeInfo> list = new HashMap<>();
    private String[] ids= null;

    public MultiPipeThread(String name) {
        super(name);
    }


    public String add(String name,NonBlockingInputStream in, OutputStream out){
        String id = UUID.randomUUID().toString();
        list.put(id,new PipeInfo(id,name,in,out));
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
        if(ids==null){
            ids=list.keySet().toArray(new String[list.size()]);
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
                        ids=null;
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

    public boolean isEmpty(){
        return list.isEmpty();
    }

    public int size(){
        return list.size();
    }

    public boolean isStopped() {
        return stopped;
    }
}
