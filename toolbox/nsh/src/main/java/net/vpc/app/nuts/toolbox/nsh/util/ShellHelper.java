package net.vpc.app.nuts.toolbox.nsh.util;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.io.FileUtils;
import net.vpc.common.ssh.SshListener;
import net.vpc.common.xfile.XFile;

import java.io.InputStream;
import java.io.PrintStream;
import net.vpc.app.nuts.NutsSessionTerminal;

public class ShellHelper {
    public static XFile xfileOf(String expression, String cwd) {
        if (expression.startsWith("file:") || expression.contains("://")) {
            return XFile.of(expression);
        }
        return XFile.of(FileUtils.getAbsoluteFile2(expression,cwd));
    }

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
        NutsWorkspace ws;
        NutsSession session;

        public WsSshListener(NutsWorkspace ws, NutsSession session) {
            this.ws = ws;
            this.session = session;
            out = session.getTerminal().getFormattedOut();
        }

        @Override
        public void onExec(String command) {
            out.printf("[[\\[SSH-EXEC\\]]] %s\n", command);
        }

        @Override
        public void onGet(String from, String to, boolean mkdir) {
            out.printf("[[\\[SSH-GET \\]]] %s -> %s\n", from, to);
        }

        @Override
        public void onPut(String from, String to, boolean mkdir) {
            out.printf("[[\\[SSH-PUT \\]]] %s -> %s\n", from, to);
        }

        @Override
        public InputStream monitorInputStream(InputStream stream, long length, String name) {
            return ws.getIOManager().monitorInputStream(stream, length, name, session);
        }
    }

    public static boolean readAccept(NutsSessionTerminal t) {
        while (true) {
            String v = t.readLine("Accept (y/n) : ?");
            if (v == null) {
                return false;
            }
            v = v.trim();
            if ("y".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v)) {
                return true;
            }
            if ("n".equalsIgnoreCase(v) || "no".equalsIgnoreCase(v)) {
                return false;
            }
        }
    }
}
