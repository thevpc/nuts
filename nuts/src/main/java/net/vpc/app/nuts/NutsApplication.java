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
        NutsApplications.runApplication(args, ws, new NutsApplicationLifeCycle() {
            @Override
            public void onRunApplication(NutsApplicationContext applicationContext) {
                NutsApplication.this.run(applicationContext);
            }

            @Override
            public void onInstallApplication(NutsApplicationContext applicationContext) {
                NutsApplication.this.onInstallApplication(applicationContext);
            }

            @Override
            public void onUpdateApplication(NutsApplicationContext applicationContext) {
                NutsApplication.this.onUpdateApplication(applicationContext);
            }

            @Override
            public void onUninstallApplication(NutsApplicationContext applicationContext) {
                NutsApplication.this.onUninstallApplication(applicationContext);
            }

            @Override
            public NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
                return NutsApplication.this.createApplicationContext(ws, args, startTimeMillis);
            }
        });
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

    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
        return ws.io().createApplicationContext(args, getClass(), null, startTimeMillis);
    }

    public abstract void run(NutsApplicationContext applicationContext);

}
