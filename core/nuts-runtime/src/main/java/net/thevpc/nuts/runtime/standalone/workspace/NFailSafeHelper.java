package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NMsg;

import java.io.PrintStream;
import java.util.function.Consumer;

public class NFailSafeHelper {
    public static boolean isWorkspaceInitializing() {
        try {
            return NWorkspaceExt.of().getModel().bootModel.isInitializing();
        }catch (Exception ex){
            return true;
        }
    }
    public static void safeLog(NMsg msg, Throwable any) {
        NFailSafeHelper.log(err -> {
            err.println(msg.toString() + ":");
            if(any!=null) {
                any.printStackTrace();
            }
        });
    }


    public static void log(Consumer<PrintStream> consumer) {
        boolean bot = false;
        try {
            bot = NSession.of().isBot();
        } catch (Throwable ex) {
            //just ignore
        }
        if (!bot) {
            PrintStream err=null;
            try {
                err= NWorkspaceExt.of().getModel().bootModel.getBootTerminal().getErr();
                if (err == null) {
                    err = System.err;
                }
            }catch (Throwable ex) {
                //
            }
            if(err==null){
                err=System.err;
            }

            try {
                consumer.accept(err);
            } catch (Throwable ex) {
                //
            }
        }
    }
}
