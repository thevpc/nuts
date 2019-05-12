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
