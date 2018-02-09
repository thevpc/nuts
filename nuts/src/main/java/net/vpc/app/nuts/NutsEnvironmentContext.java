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
package net.vpc.app.nuts;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * Created by vpc on 1/15/17.
 */
public class NutsEnvironmentContext {

    private static ThreadLocal<Stack<NutsWorkspace>> WORKSPACE = new ThreadLocal<>();
    private static ThreadLocal<Stack<NutsRepository>> REPOSITORY = new ThreadLocal<>();

    public static InvocationHandler createHandler(NutsWorkspace workspace) {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Stack<NutsWorkspace> s = WORKSPACE.get();
                if (s == null) {
                    s = new Stack<>();
                    WORKSPACE.set(s);
                }
                boolean pushed = false;
                try {
                    if (!s.isEmpty() && s.peek() == workspace) {
                        //do nothing already in the context!!
                    } else {
                        s.push(workspace);
                        pushed = true;
                    }
                    return method.invoke(workspace, args);
                } finally {
                    if (pushed) {
                        s.pop();
                    }
                }
            }
        };
    }

    public static InvocationHandler createHandler(NutsRepository repository) {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Stack<NutsRepository> s = REPOSITORY.get();
                if (s == null) {
                    s = new Stack<>();
                    REPOSITORY.set(s);
                }
                boolean pushed = false;
                try {
                    if (!s.isEmpty() && s.peek() == repository) {
                        //do nothing already in the context!!
                    } else {
                        s.push(repository);
                        pushed = true;
                    }
                    return method.invoke(repository, args);
                } finally {
                    if (pushed) {
                        s.pop();
                    }
                }
            }
        };
    }

    public static <T> T run(NutsWorkspace workspace, Callable<T> callable) {
        Stack<NutsWorkspace> s = WORKSPACE.get();
        if (s == null) {
            s = new Stack<>();
            WORKSPACE.set(s);
        }
        boolean pushed = false;
        try {
            if (!s.isEmpty() && s.peek() == workspace) {
                //do nothing already in the context!!
            } else {
                s.push(workspace);
                pushed = true;
            }
            T v;
            try {
                v = callable.call();
            } catch (RuntimeException ex) {
                throw new NutsException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return v;
        } finally {
            if (pushed) {
                s.pop();
            }
        }
    }

    public static <T> T run(NutsRepository repository, Callable<T> callable) {
        Stack<NutsRepository> s = REPOSITORY.get();
        if (s == null) {
            s = new Stack<>();
            REPOSITORY.set(s);
        }
        boolean pushed = false;
        try {
            if (!s.isEmpty() && s.peek() == repository) {
                //do nothing already in the context!!
            } else {
                s.push(repository);
                pushed = true;
            }
            T v;
            try {
                v = callable.call();
            } catch (RuntimeException ex) {
                throw new NutsException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return v;
        } finally {
            if (pushed) {
                s.pop();
            }
        }
    }

    public static NutsWorkspace getNutsWorkspace() {
        Stack<NutsWorkspace> s = WORKSPACE.get();
        if (s == null || s.isEmpty()) {
            throw new NutsException("Missing Workspace in the context");
        }
        return s.peek();
    }

    public static NutsRepository getNutsRepository() {
        Stack<NutsRepository> s = REPOSITORY.get();
        if (s == null || s.isEmpty()) {
            throw new NutsException("Missing Repository in the context");
        }
        return s.peek();
    }
}
