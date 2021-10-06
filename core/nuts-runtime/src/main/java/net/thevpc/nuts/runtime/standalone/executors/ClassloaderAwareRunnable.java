/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public abstract class ClassloaderAwareRunnable implements Runnable {

    Object result;
    ClassLoader initialClassLoader;
    ClassLoader classLoader;
    Throwable error;
    NutsSession session;

    public ClassloaderAwareRunnable(NutsSession session,ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    public abstract Object runWithContext() throws Throwable;

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    @Override
    public void run() {
        initialClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            try {

                result = runWithContext();
            } catch (Throwable th) {
                error = th;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
        }
    }

    public Object getResult() {
        return result;
    }

    public void runAndWaitFor() throws Throwable {
        try {
            getSession().concurrent().executorService().submit(this).get();
        } catch (InterruptedException ex) {
            setError(ex);
        }
        if (getError() != null) {
            throw getError();
        }
    }

}
