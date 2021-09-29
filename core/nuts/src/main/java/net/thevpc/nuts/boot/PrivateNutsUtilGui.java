package net.thevpc.nuts.boot;

import net.thevpc.nuts.Nuts;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Supplier;

final class PrivateNutsUtilGui {

    public static boolean isGraphicalDesktopEnvironment() {
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                return false;
            }
            try {
                java.awt.GraphicsDevice[] screenDevices = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                if (screenDevices == null || screenDevices.length == 0) {
                    return false;
                }
            } catch (java.awt.HeadlessException e) {
                return false;
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            return false;
        } catch (Exception e) {
            //exception may occur if the sdk is built without awt package for instance!
            return false;
        }
    }

    public static String inputString(String message, String title, Supplier<String> in, PrintStream err) {
        try {
            if (title == null) {
                title = "Nuts Package Manager - " + Nuts.getVersion();
            }
            String line = javax.swing.JOptionPane.showInputDialog(
                    null,
                    message, title, javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (line == null) {
                line = "";
            }
            return line;
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            if(err==null) {
                err=System.err;
            }
            err.printf("[Graphical Environment Unsupported] %s%n", title);
            if(in==null){
                return new Scanner(System.in).nextLine();
            }
            return in.get();
        }
    }

    public static void showMessage(String message, String title, PrintStream err) {
        if (title == null) {
            title = "Nuts Package Manager";
        }
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            if(err==null){
                err=System.err;
            }
            err.printf("[Graphical Environment Unsupported] %s%n", title);
        }
    }
}
