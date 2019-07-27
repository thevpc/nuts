/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nuts Application is the Top Level class to be handled by nuts as rich console
 * application. By default NutsApplication classes :
 * <ul>
 * <li>have a nutsApplication=true in their descriptor file</li>
 * <li>support inheritance of all workspace options (from caller nuts
 * process)</li>
 * <li>enables auto-complete mode to help forecasting the next token in the
 * command line</li>
 * <li>enables install mode to be executed when the jar is installed in nuts
 * repos</li>
 * <li>enables uninstall mode to be executed when the jar is uninstaleld from
 * nuts repos</li>
 * <li>enables update mode to be executed when the a new version of the same jar
 * has been installed</li>
 * <li>have many default options enabled (such as --help, --version, --json,
 * --table, etc.) and thus support natively multi output channels</li>
 * </ul>
 * Typically a Nuts Application follows this code pattern :
 * <pre>
 *   public class MyApplication extends NutsApplication{
 *     public static void main(String[] args) {
 *         // just create an instance and call runAndExit in the main method
 *         new MyApplication().runAndExit(args);
 *     }
 *     // do the main staff in launch method
 *     public void run(NutsApplicationContext appContext) {
 *         boolean myBooleanOption=false;
 *         NutsCommandLine cmdLine=appContext.getCommandLine()
 *         boolean boolOption=false;
 *         String stringOption=null;
 *         Argument a;
 *         while(cmdLine.hasNext()){
 *             if(appContext.configureFirst(cmdLine)){
 *                 //do nothing
 *             }else {
 *                  a=cmdLine.peek();
 *                  switch(a.getStringKey())[
 *                      case "-o": case "--option":{
 *                          boolOption=cmdLine.nextBoolean().getBooleanValue();
 *                          break;
 *                      }
 *                      case "-n": case "--name":{
 *                          stringOption=cmdLine.nextString().getStringValue();
 *                          break;
 *                      }
 *                      default:{
 *                          cmdLine.unexpectedArgument();
 *                      }
 *                  }
 *             }
 *         }
 *         // test if application is running in exec mode
 *         // (and not in autoComplete mode)
 *         if(cmdLine.isExecMode()){
 *              //do the good staff here
 *         }
 *     }
 *   }
 * </pre>
 *
 * @since 0.5.5
 */
public abstract class NutsApplication {

    /**
     * run the application and <strong>EXIT</strong> process
     *
     * @param args arguments
     */
    public void runAndExit(String[] args) {
        try {
            run((NutsWorkspace) null, args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex, args, null));
            return;
        }
        System.exit(0);
    }

    /**
     * run the application with the given arguments. If the first arguments is
     * in the form of --nuts-exec-mode=... the argument will be removed and the
     * corresponding mode is activated.
     *
     * @param args application arguments. should not be null or contain nulls
     */
    public void run(String[] args) {
        run((NutsWorkspace) null, args);
    }

    /**
     * run the application with the given arguments against the given workspace
     * If the first arguments is in the form of --nuts-exec-mode=... the
     * argument will be removed and the corresponding mode is activated.
     *
     * @param ws workspace (can be null)
     * @param args application arguments. should not be null or contain nulls
     */
    public void run(NutsWorkspace ws, String[] args) {
        NutsApplications.runApplication(args, ws, getClass(), new NutsApplicationLifeCycleImpl(this));
    }

    /**
     * this method should be overridden to perform specific business when
     * application is installed
     *
     * @param applicationContext context
     */
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
    }

    /**
     * this method should be overridden to perform specific business when
     * application is updated
     *
     * @param applicationContext context
     */
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
    }

    /**
     * this method should be overridden to perform specific business when
     * application is uninstalled
     *
     * @param applicationContext context
     */
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }

    /**
     * create application context or return null for default
     * @param ws workspace
     * @param args arguments
     * @param startTimeMillis start time
     * @return new instance of NutsApplicationContext or null
     */
    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
        return null;
    }

    /**
     * run application within the given context
     * @param applicationContext app context
     */
    public abstract void run(NutsApplicationContext applicationContext);

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Default NutsApplicationLifeCycle implementation based on NutsApplication class.
     */
    private static class NutsApplicationLifeCycleImpl implements NutsApplicationLifeCycle {
        /**
         * application
         */
        private final NutsApplication app;

        /**
         * application
         * @param app application
         */
        NutsApplicationLifeCycleImpl(NutsApplication app) {
            this.app = app;
        }

        @Override
        public void onRunApplication(NutsApplicationContext applicationContext) {
            app.run(applicationContext);
        }

        @Override
        public void onInstallApplication(NutsApplicationContext applicationContext) {
            app.onInstallApplication(applicationContext);
        }

        @Override
        public void onUpdateApplication(NutsApplicationContext applicationContext) {
            app.onUpdateApplication(applicationContext);
        }

        @Override
        public void onUninstallApplication(NutsApplicationContext applicationContext) {
            app.onUninstallApplication(applicationContext);
        }

        @Override
        public NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
            return app.createApplicationContext(ws, args, startTimeMillis);
        }

        @Override
        public String toString() {
            return app.toString();
        }
    }

}
