package net.thevpc.nuts.lib.nswing;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class UI {
    public static void async(Runnable r){
        new Thread(r).start();
    }

    public static void withinGUI(Runnable r){
        if(SwingUtilities.isEventDispatchThread()){
            r.run();
        }else{
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
