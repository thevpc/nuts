/**
 * ====================================================================
 *             Doovos (Distributed Object Oriented Operating System)
 *
 * Doovos is a new Open Source Distributed Object Oriented Operating System
 * Design and implementation based on the Java Platform.
 * Actually, it is a try for designing a distributed operation system in
 * top of existing centralized/network OS.
 * Designed OS will follow the object oriented architecture for redefining
 * all OS resources (memory,process,file system,device,...etc.) in a highly
 * distributed context.
 * Doovos is also a distributed Java virtual machine that implements JVM
 * specification on top the distributed resources context.
 *
 * Doovos BIN is a standard implementation for Doovos boot sequence, shell and
 * common application tools. These applications are running onDoovos guest JVM
 * (distributed jvm).
 * <br>
 *
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
package net.thevpc.nuts.toolbox.nsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * <pre>
 *      Process process = Runtime.getRuntime().exec(new String[]{"/bin/java","-version"}, null, new File("."));
 *      ProcessWatcher w = new ProcessWatcher(process, new ProcessWatcherHandler() {
 *          public void started(Process process) {
 *              System.out.println("Process started");
 *          }
 *
 *          public void stdout(Process process, String line) {
 *              System.out.println(line);
 *          }
 *
 *          public void stderr(Process process, String line) {
 *              System.err.println(line);
 *          }
 *
 *          public void ended(Process process, int value) {
 *              System.out.println("Process Shutdown. Exit Value :" + value);
 *          }
 *
 *          public void error(Process process, Throwable th) {
 *              System.err.println(th);
 *          }
 *      });
 *      w.start();
 * </pre>
 *
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 27 juin 2007 12:08:13
 */
public class ProcessWatcher2 extends ProcessWatcher {

    private Process process;
    private CloseableThread end;
    private CloseableThread in;
    private CloseableThread out;
    private CloseableThread err;
    private int result;
    private Throwable resultError;
    private boolean stopped = false;
//    private boolean endStreamIn = false;
    private boolean endStreamOut = false;
    private boolean endStreamErr = false;
    private boolean endStreamIn = false;
    private ExecProcessInfo info;

    public ProcessWatcher2(Process process, ExecProcessInfo info) {
        this.info = info;
        this.process = process;
    }

    public void waitForStreams() {
        long timeEnded = System.currentTimeMillis();
        while (!endStreamOut || !endStreamErr || !endStreamIn) {

            long now = System.currentTimeMillis();
            if ((now - timeEnded) > 3000) {
                StringBuilder s = new StringBuilder(info.cmdarray[0]);
                s.append(" ");
                s.append("stream(s) still open :");
                if (!endStreamOut) {
                    s.append(" stdout");
                    out.close();
                    out.interrupt();
                }
                if (!endStreamErr) {
                    s.append(" stderr");
                    err.close();
                    err.interrupt();
                }
                if (!endStreamIn) {
                    s.append(" stdin");
                    in.close();
                    in.interrupt();
                }
                logError(s.toString());
                endStreamOut = true;
                endStreamErr = true;
                endStreamIn = true;
            } else if ((now - timeEnded) > 2000) {
                if (!endStreamOut) {
                    out.close();
                }
                if (!endStreamErr) {
                    err.close();
                }
                if (!endStreamIn) {
                    in.close();
                }
                break;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    protected void logInfo(String s) {
        System.out.printf("[logInfo] %s\n" ,s);
    }

    protected void logError(String s) {
        System.err.printf("[logError] %s\n", s);
    }

    protected void logError(Throwable s) {
        System.err.printf("[logError] %s \n" ,s.toString());
    }

    public void start() {
        end = new CloseableThread("pw-end-" + info.cmdarray[0]) {
            @Override
            public void run() {
                try {
                    result = process.waitFor();
                } catch (Throwable e) {
                    resultError = e;
                    logError(e.toString());
//                    handler.error(process, e);
                } finally {
                    stopped = true;
                }
                close();
//                handler.ended(process, result);
            }

            @Override
            public void close() {
                closed = true;
                waitForStreams();
            }

        };
        out = new CloseableThread("pw-out-" + info.cmdarray[0]) {
            @Override
            public void run() {
                try {
                    InputStream in = process.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    try {
                        while ((len = in.read(buffer)) > 0) {
                            info.out.write(buffer, 0, len);
                        }
                    } catch (Throwable e) {
                        logError(e);
                    }
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessWatcher2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    endStreamOut = true;
                }

            }

            @Override
            public void close() {
                closed = true;
                try {
                    process.getInputStream().close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessWatcher2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        err = new CloseableThread("pw-err-" + info.cmdarray[0]) {
            @Override
            public void run() {
                try {
                    InputStream in = process.getErrorStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    try {
                        while ((len = in.read(buffer)) > 0) {
                            info.err.write(buffer, 0, len);
                        }
                    } catch (Throwable e) {
                        logError(e);
                    }
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessWatcher2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    endStreamErr = true;
                }
            }

            @Override
            public void close() {
                closed = true;
                try {
                    process.getErrorStream().close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessWatcher2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };
        in = new CloseableThread("pw-in-" + info.cmdarray[0]) {
            @Override
            public void run() {
                try {
                    InputStream in = info.in;
                    OutputStream out = process.getOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    try {
                        if (in instanceof JavaShellNonBlockingInputStream) {
                            JavaShellNonBlockingInputStream ine = (JavaShellNonBlockingInputStream) in;
                            while (!stopped && ine.hasMoreBytes()) {
                                int available = in.available();
                                if (available > 0) {
                                    if (!((len = in.read(buffer)) > 0)) {
                                        break;
                                    }
                                    out.write(buffer, 0, len);
                                } else if (((len = ((JavaShellNonBlockingInputStream) in).readNonBlocking(buffer, 0, buffer.length, 3000)) > 0)) {
                                    out.write(buffer, 0, len);
                                }else{
                                    break;
                                }
                            }
                        } else {
                            while (!stopped) {
                                int available = in.available();
                                if (available > 0) {
                                    if (!((len = in.read(buffer)) > 0)) {
                                        break;
                                    }
                                    out.write(buffer, 0, len);
                                } else {
                                    //should we check if
                                }
                            }
                        }
                    } catch (Throwable e) {
                        logError(e);
                    }
                    try {
                        out.close();
//                            in.close();
                    } catch (Exception ex) {
                        Logger.getLogger(ProcessWatcher2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    endStreamIn = true;
                }
            }

            @Override
            public void close() {
                closed = true;
//                try {
////                    info.in.close();
//                } catch (Exception ex) {
//                    Logger.getLogger(ProcessWatcher2.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }

        };
        end.start();
        out.start();
        err.start();
        in.start();
    }

    public int waitfor() {
        while (!stopped || !endStreamOut || !endStreamErr || !endStreamIn) {
            Thread.yield();
        }
        return result;
    }

    public static abstract class CloseableThread extends Thread {

        public boolean closed;

        public CloseableThread(String name) {
            super(name);
        }

        public abstract void close();
    }
}
