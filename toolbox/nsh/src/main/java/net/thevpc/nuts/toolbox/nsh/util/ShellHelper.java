package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.lib.ssh.SshListener;
import net.thevpc.nuts.toolbox.nsh.bundles._IOUtils;

public class ShellHelper {

    public static List<NutsPath> xfilesOf(List<String> all, String cwd, NutsSession session) {
        List<NutsPath> xall = new ArrayList<>();
        for (String v : all) {
            xall.add(xfileOf(v, cwd,session));
        }
        return xall;
    }

    public static NutsPath xfileOf(String expression, String cwd, NutsSession session) {
        NutsIOManager io = session.io();
        if (expression.startsWith("file:") || expression.contains("://")) {
            return io.path(expression);
        }
        return io.path(_IOUtils.getAbsoluteFile2(expression, cwd));
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

        NutsPrintStream out;
        NutsSession session;

        public WsSshListener(NutsSession session) {
            this.session = session;
            out = session.out();
        }

        private boolean isTrace() {
            return session.isPlainTrace();
        }

        @Override
        public void onExec(String command) {
            if (isTrace()) {
                out.printf("##:primary4:[SSH-EXEC]## %s\n", command);
            }
        }

        @Override
        public void onGet(String from, String to, boolean mkdir) {
            if (isTrace()) {
                out.printf("##:primary4:[SSH-GET ]## %s -> %s\n", from, to);
            }
        }

        @Override
        public void onPut(String from, String to, boolean mkdir) {
            if (isTrace()) {
                out.printf("##:primary4:[SSH-PUT ]## %s -> %s\n", from, to);
            }
        }

        @Override
        public InputStream monitorInputStream(InputStream stream, long length, NutsString message) {
            return session.io().monitor().setSource(stream).setLength(length).setName(message).create();
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
