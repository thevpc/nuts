package net.vpc.app.nuts.app;

import net.vpc.app.nuts.*;

/**
 * <pre>
 *   public class MyApplication extends NutsApplication{
 *     public static void main(String[] args) {
 *         // just create an instance and call launchAndExit in the main method
 *         new MyApplication().launchAndExit(args);
 *     }
 *     // do the main staff in launch method
 *     public int launch(NutsApplicationContext appContext) {
 *         boolean myBooleanOption=false;
 *         Argument a;
 *         while(cmd.hasNext()){
 *             if(appContext.configure(cmd)){
 *                 //do nothing
 *             }else if((a=cmd.readBooleanOption("-o","--option"))!=null){
 *                 myBooleanOption=a.getBooleanValue();
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
 */
public abstract class NutsApplication {

    public void runAndExit(String[] args) {
        try {
            run((NutsWorkspace) null, args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex, args, System.err));
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
            public NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args) {
                NutsApplicationContext c = NutsApplication.this.createApplicationContext(ws, args);
                if (c == null) {
                    c = new NutsApplicationContext(ws, args, NutsApplication.this.getClass(), null);
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

    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args) {
        return new NutsApplicationContext(ws, args, getClass(), null);
    }

    public abstract void run(NutsApplicationContext applicationContext);

}
