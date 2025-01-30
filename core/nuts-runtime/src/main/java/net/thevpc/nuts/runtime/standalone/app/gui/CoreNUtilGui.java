package net.thevpc.nuts.runtime.standalone.app.gui;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.text.NTexts;

import javax.swing.*;

public final class CoreNUtilGui {
    private CoreNUtilGui() {
    }

    public static boolean isGraphicalDesktopEnvironment() {
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
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

    public static class GuiResult {
        Object value;
        boolean rememberMe;

        public GuiResult(Object value, boolean rememberMe) {
            this.value = value;
            this.rememberMe = rememberMe;
        }

        public Object getValue() {
            return value;
        }

        public boolean isRememberMe() {
            return rememberMe;
        }
    }

    public static GuiResult inputString(NMsg message, NMsg title, boolean rememberMe) {
        try {
            NTexts text = NTexts.of();
            if (title == null) {
                title = NMsg.ofC("Nuts Package Manager - %s", Nuts.getVersion());
            }
            String line = javax.swing.JOptionPane.showInputDialog(
                    null,
                    text.of(message).filteredText(), text.of(title).filteredText(), javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (line == null) {
                line = "";
            }
            return new GuiResult(line, rememberMe && false);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            NSession session = NSession.get().get();
            session.err().println(NMsg.ofC("[Graphical Environment Unsupported] %s", title));
            String line = session.getTerminal().readLine(NMsg.ofPlain(message.toString()));
            return new GuiResult(line, rememberMe && false);
        }
    }

    public static GuiResult inputPassword(NMsg message, NMsg title, boolean rememberMe) {
        if (title == null) {
            title = NMsg.ofC("Nuts Package Manager - %s", Nuts.getVersion());
        }
        if (message == null) {
            message = NMsg.ofPlain("");
        }
        NTexts text = NTexts.of();
        String messageString = text.of(message).filteredText();
        String titleString = text.of(title).filteredText();
        try {
            javax.swing.JPanel panel = new javax.swing.JPanel();
            javax.swing.JLabel label = new javax.swing.JLabel(messageString);
            javax.swing.JPasswordField pass = new javax.swing.JPasswordField(10);
            panel.add(label);
            panel.add(pass);
            String[] options = new String[]{"OK", "Cancel"};
            int option = javax.swing.JOptionPane.showOptionDialog(null, panel, titleString,
                    javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE,
                    null, options, options[1]);
            if (option == 0) {
                return new GuiResult(new String(pass.getPassword()), rememberMe && false);
            }
            return new GuiResult("", rememberMe && false);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            NSession session = NSession.get().get();
            session.err().println(NMsg.ofC("[Graphical Environment Unsupported] %s", title));
            return new GuiResult(
                    session.getTerminal().readLine(NMsg.ofPlain(message.toString())),
                    rememberMe && false
            );
        }
    }

    public static void showMessage(NMsg message, NMsg title) {
        if (title == null) {
            title = NMsg.ofC("Nuts Package Manager - %s", Nuts.getVersion());
        }
        NTexts text = NTexts.of();
        String messageString = text.of(message == null ? "" : message).filteredText();
        String titleString = text.of(title).filteredText();
        try {
            javax.swing.JOptionPane.showMessageDialog(null, messageString, titleString, JOptionPane.QUESTION_MESSAGE);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            NSession session = NSession.get().get();
            session.err().println(NMsg.ofC("[Graphical Environment Unsupported] %s", title));
        }
    }
}
