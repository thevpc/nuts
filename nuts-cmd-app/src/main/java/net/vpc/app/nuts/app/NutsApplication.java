package net.vpc.app.nuts.app;

import net.vpc.app.nuts.*;

import java.io.PrintStream;
import java.util.Arrays;

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
 *                 //fo nothing
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
 *  </pre>
 */
public abstract class NutsApplication {

    public void launchAndExit(String[] args) {
        try {
            System.exit(launch(args));
        } catch (Exception ex) {
            int errorCode = 204;
            boolean showTrace=false;
            for (int i = 0; i < args.length; i++) {
                if(args[i].startsWith("-")){
                    if(args[i].equals("--verbose")){
                        showTrace=true;
                        break;
                    }
                }else{
                    break;
                }
            }
            if (ex instanceof NutsExecutionException) {
                NutsExecutionException ex2 = (NutsExecutionException) ex;
                if (ex2.getExitCode() == 0) {
                    System.exit(0);
                    return;
                } else {
                    errorCode = ex2.getExitCode();
                }
            }
            String m = ex.getMessage();
            if (m == null || m.length()<5) {
                m = ex.toString();
            }
            System.err.println(m);
            if (showTrace) {
                ex.printStackTrace();
            }
            System.exit(errorCode);
        }
    }

    public int launch(String[] args) {
        long startTimeMillis = System.currentTimeMillis();
        NutsWorkspace ws = Nuts.openInheritedWorkspace(args);
        NutsApplicationContext applicationContext=null;
        try {
            applicationContext = createApplicationContext(ws);
            applicationContext.setStartTimeMillis(startTimeMillis);
            switch (applicationContext.getMode()){
                case "launch":
                case "auto-complete":{
//                    if(!applicationContext.getWorkspace().isInstalled(applicationContext.getAppId(),false,applicationContext.getSession())){
//                        int i = onInstallApplication(applicationContext);
//                        if(i!=0){
//                            throw new NutsExecutionException("Unable to install "+applicationContext.getAppId(),i);
//                        }
//                        return i;
//                    }
                    return launch(applicationContext);
                }
                case "on-install":{
                    return onInstallApplication(applicationContext);
                }
                case "on-update":{
                    return onUpdateApplication(applicationContext);
                }
                case "on-uninstall":{
                    return onUninstallApplication(applicationContext);
                }
            }
            throw new NutsExecutionException("Unsupported execution mode "+applicationContext.getMode(),204);
        } catch (NutsExecutionException ex) {
            if (ex.getExitCode() == 0) {
                return 0;
            }
            throw ex;
        }
    }

    protected int onInstallApplication(NutsApplicationContext applicationContext){
        return 0;
    }

    protected int onUpdateApplication(NutsApplicationContext applicationContext){
        return 0;
    }

    protected int onUninstallApplication(NutsApplicationContext applicationContext){
        return 0;
    }

    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws) {
        return new NutsApplicationContext(ws, getClass(), null);
    }

    public abstract int launch(NutsApplicationContext applicationContext);
}
