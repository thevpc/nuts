package net.thevpc.nuts.toolbox.nsh.util;

import net.thevpc.nuts.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.thevpc.nuts.io.NInputStreamMonitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.ext.ssh.SshListener;
import net.thevpc.nuts.toolbox.nsh.util.bundles._IOUtils;

public class ShellHelper {

    public static List<NPath> xfilesOf(List<String> all, String cwd, NSession session) {
        List<NPath> xall = new ArrayList<>();
        for (String v : all) {
            xall.add(xfileOf(v, cwd,session));
        }
        return xall;
    }

    public static NPath xfileOf(String expression, String cwd, NSession session) {
        if (expression.startsWith("file:") || expression.contains("://")) {
            return NPath.of(expression,session);
        }
        return NPath.of(_IOUtils.getAbsoluteFile2(expression, cwd, session),session);
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

        NPrintStream out;
        NSession session;

        public WsSshListener(NSession session) {
            this.session = session;
            out = session.out();
        }

        private boolean isTrace() {
            return session.isPlainTrace();
        }

        @Override
        public void onExec(String command) {
            if (isTrace()) {
                out.println(NMsg.ofC("##:primary4:[SSH-EXEC]## %s", command));
            }
        }

        @Override
        public void onGet(String from, String to, boolean mkdir) {
            if (isTrace()) {
                out.println(NMsg.ofC("##:primary4:[SSH-GET ]## %s -> %s", from, to));
            }
        }

        @Override
        public void onPut(String from, String to, boolean mkdir) {
            if (isTrace()) {
                out.println(NMsg.ofC("##:primary4:[SSH-PUT ]## %s -> %s", from, to));
            }
        }

        @Override
        public InputStream monitorInputStream(InputStream stream, long length, NMsg message) {
            return NInputStreamMonitor.of(session).setSource(stream).setLength(length).setName(message).create();
        }
    }

    public static boolean readAccept(NSessionTerminal t) {
        while (true) {
            String v = t.readLine(NMsg.ofPlain("Accept (y/n) : ?"));
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

    public static List<String> splitOn(String line,char sep) {
        StringTokenizer st=new StringTokenizer(line,""+sep,true);
        List<String> ret=new ArrayList<>();
        while(st.hasMoreTokens()){
            String e = st.nextToken();
            if(e.charAt(0)==sep){
                String c0 = String.valueOf(e.charAt(0));
                for (int i = e.length()-1; i >=0 ; i--) {
                    ret.add(c0);
                }
            }else {
                ret.add(e);
            }
        }
        return ret;
    }
}
