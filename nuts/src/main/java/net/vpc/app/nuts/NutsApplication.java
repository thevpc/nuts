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
 * <pre>
 *   public class MyApplication extends NutsApplication{
 *     public static void main(String[] args) {
 *         // just create an instance and call runAndExit in the main method
 *         new MyApplication().runAndExit(args);
 *     }
 *     // do the main staff in launch method
 *     public void run(NutsApplicationContext appContext) {
 *         boolean myBooleanOption=false;
 *         NutsCommand cmdLine=appContext.getCommandLine()
 *         Argument a;
 *         while(cmdLine.hasNext()){
 *             if(appContext.configure(cmd)){
 *                 //do nothing
 *             }else if((a=cmd.nextBoolean("-o","--option"))!=null){
 *                 myBooleanOption=a.getValue().getBoolean();
 *             }else{
 *                 cmd.unexpectedArgument("myapp");
 *             }
 *         }
 *         // test if application is running in exec mode
 *         // (and not in autoComplete mode)
 *         if(cmd.isExecMode()){
 *
 *         }
 *     }
 *   }
 * </pre>
 * @since 0.5.5
 */
public abstract class NutsApplication {

    public void runAndExit(String[] args) {
        try {
            run((NutsWorkspace) null, args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex, args, null));
        }
    }

    public void run(String[] args) {
        run(null, args);
    }

    public void run(NutsWorkspace ws, String[] args) {
        NutsApplications.runApplication(args, ws, new NutsApplicationListener() {
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
                NutsApplicationContext c = NutsApplication.this.createApplicationContext(ws, args, startTimeMillis);
                if (c == null) {
                    c = ws.io().createApplicationContext(args, NutsApplication.this.getClass(), null,startTimeMillis);
                }
                return c;
            }
        });
    }

    protected void onInstallApplication(NutsApplicationContext applicationContext) {
    }

    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
    }

    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }

    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
        return ws.io().createApplicationContext(args, getClass(), null,startTimeMillis);
    }

    public abstract void run(NutsApplicationContext applicationContext);

}
