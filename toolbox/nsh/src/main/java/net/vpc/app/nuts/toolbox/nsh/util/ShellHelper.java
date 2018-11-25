package net.vpc.app.nuts.toolbox.nsh.util;

import net.vpc.app.nuts.NutsSession;
import net.vpc.common.ssh.SshListener;

import java.io.PrintStream;

public class ShellHelper {
    public static String[] splitNameAndValue(String arg) {
        int i = arg.indexOf('=');
        if (i >= 0) {
            return new String[]{
                    i == 0 ? "" : arg.substring(0, i),
                    i == arg.length() - 1 ? "" : arg.substring(i + 1),};
        }
        return null;
    }

    public static boolean isInt(String v1) {
        try {
            if (v1.length() == 0) {
                return false;
            }
            Integer.parseInt(v1);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static class WsSshListener implements SshListener {
        PrintStream out;

        public WsSshListener(NutsSession session) {
            out = session.getTerminal().getFormattedOut();
        }

        @Override
        public void onExec(String command) {
            out.println("[[\\[SSH-EXEC\\]]] %s\n" + command);
        }

        @Override
        public void onGet(String from, String to, boolean mkdir) {
            out.println("[[\\[SSH-GET \\]]] %s -> %s%\n" + from + " " + to);
        }

        @Override
        public void onPut(String from, String to, boolean mkdir) {
            out.println("[[\\[SSH-PUT \\]]] %s -> %s%\n" + from + " " + to);
        }
    }
}
