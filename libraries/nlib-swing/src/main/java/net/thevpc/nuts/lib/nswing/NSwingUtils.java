package net.thevpc.nuts.lib.nswing;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NLiteral;

import javax.swing.*;

public class NSwingUtils {
    public static void prepareUI(String[] args) {
        if(System.getProperty("sun.java2d.uiScale")!=null){
            return;
        }
        int scale = 0;
        for (String arg : args) {
            if (
                    arg.equals("--scale")
                    ||arg.equals("--rescale")
            ) {
                scale = 2;
            }else if (arg.startsWith("--scale=")) {
                String a = arg.substring("--scale=".length());
                if("big".equalsIgnoreCase(a)) {
                    scale = 2;
                }else if("small".equalsIgnoreCase(a)) {
                    scale = 1;
                }else  {
                    Integer i = NLiteral.of(a).asInt().orElse(0);
                    if(i>0){
                        scale = i;
                    }
                }
            }else if (arg.startsWith("--rescale=")) {
                String a = arg.substring("--rescale=".length());
                if("big".equalsIgnoreCase(a)) {
                    scale = 2;
                }else if("small".equalsIgnoreCase(a)) {
                    scale = 1;
                }else  {
                    Integer i = NLiteral.of(a).asInt().orElse(0);
                    if(i>0){
                        scale = i;
                    }
                }
            }else if(!arg.startsWith("-")){
                break;
            }
        }
        if (scale != 0) {
            System.setProperty("sun.java2d.uiScale", String.valueOf(scale));
        }
    }
    public static void setSharedWorkspaceInstance(){
        NWorkspace workspace = NWorkspace.of();
        SwingUtilities.invokeLater(workspace::setSharedInstance);
    }
}
