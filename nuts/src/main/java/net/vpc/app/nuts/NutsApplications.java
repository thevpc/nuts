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

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for Nuts Applications
 *
 * @author vpc
 * @since 0.5.5
 */
public class NutsApplications {

    private static final Logger LOG = Logger.getLogger(NutsApplications.class.getName());

    private NutsApplications() {
    }

    /**
     * process throwables and return exit code
     *
     * @param ex exception
     * @param args application arguments to check from if a '--verbose' or
     * '--debug' option is armed
     * @param out out stream
     * @return exit code
     */
    public static int processThrowable(Throwable ex, String[] args, PrintStream out) {
        if (ex == null) {
            return 0;
        }
        int errorCode = 204;
        boolean showTrace = PrivateNutsUtils.getSysBoolNutsProperty("debug", false);

        if (!showTrace && args != null) {
            for (String arg : args) {
                if (arg.startsWith("-")) {
                    if (arg.equals("--verbose") || arg.equals("--debug")) {
                        showTrace = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (ex instanceof NutsExecutionException) {
            NutsExecutionException ex2 = (NutsExecutionException) ex;
            if (ex2.getExitCode() == 0) {
                return 0;
            } else {
                errorCode = ex2.getExitCode();
            }
        }
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        NutsWorkspace ws = null;
        if (ex instanceof NutsException) {
            ws = ((NutsException) ex).getWorkspace();
        }
        if (ws == null) {
            if (ex instanceof NutsSecurityException) {
                ws = ((NutsSecurityException) ex).getWorkspace();
            }
        }
        LOG.log(Level.SEVERE, m);
        LOG.log(Level.SEVERE, ex.getMessage(), ex);
        if (out == null && ws != null) {
            try {
                out = ws.io().getSystemTerminal().getOut();
                m = "@@" + m + "@@";
            } catch (Exception ex2) {
                //
            }
        }
        if (out == null) {
            out = System.err;
        }
        out.println(m);
        if (showTrace) {
            ex.printStackTrace(out);
        }
        out.flush();
        return (errorCode);
    }

    /**
     * run application with given life cycle.
     *
     * @param args application arguments
     * @param ws workspace
     * @param lifeCycle application life cycle
     */
    public static void runApplication(String[] args, NutsWorkspace ws, NutsApplicationLifeCycle lifeCycle) {
        long startTimeMillis = System.currentTimeMillis();
        if (lifeCycle == null) {
            throw new NullPointerException("Null Application");
        }
        boolean inherited = false;
        if (ws == null) {
            inherited = true;
            ws = Nuts.openInheritedWorkspace(args);
        }
        LOG.log(Level.FINE, "Running Application {0}: {1} {2}", new Object[]{
            inherited ? "(inherited)" : "",
            lifeCycle, ws.commandLine().setArgs(args).toString()});
        NutsApplicationContext applicationContext = null;
        applicationContext = lifeCycle.createApplicationContext(ws, args, startTimeMillis);
        if (applicationContext == null) {
            applicationContext = ws.io().createApplicationContext(args, lifeCycle.getClass(), null, startTimeMillis);
        }
        switch (applicationContext.getMode()) {
            /**
             * both RUN and AUTO_COMPLETE executes the save branch. Later
             * applicationContext.isExecMode()
             */
            case RUN:
            case AUTO_COMPLETE: {
                lifeCycle.onRunApplication(applicationContext);
                return;
            }
            case INSTALL: {
                lifeCycle.onInstallApplication(applicationContext);
                return;
            }
            case UPDATE: {
                lifeCycle.onUpdateApplication(applicationContext);
                return;
            }
            case UNINSTALL: {
                lifeCycle.onUninstallApplication(applicationContext);
                return;
            }
        }
        throw new NutsExecutionException(ws, "Unsupported execution mode " + applicationContext.getMode(), 204);
    }
}
