package net.thevpc.nuts.boot;

import net.thevpc.nuts.Nuts;

final class PrivateNutsGui {

    public static boolean isGraphicalDesktopEnvironment() {
        try {
            if(!java.awt.GraphicsEnvironment.isHeadless()){
                return false;
            }
            try {
                java.awt.GraphicsDevice[] screenDevices = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                if(screenDevices == null || screenDevices.length == 0){
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

    public static String inputString(String message, String title) {
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
            PrivateNutsTerm.errln("[Graphical Environment Unsupported] %s", title);
            return PrivateNutsTerm.readLine();
        }
    }

    public static void showMessage(String message, String title) {
        if (title == null) {
            title = "Nuts Package Manager";
        }
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            PrivateNutsTerm.errln("[Graphical Environment Unsupported] %s", title);
        }
    }
}
