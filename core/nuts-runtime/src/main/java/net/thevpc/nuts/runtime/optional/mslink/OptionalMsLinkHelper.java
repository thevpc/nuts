package net.thevpc.nuts.runtime.optional.mslink;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class OptionalMsLinkHelper {
    private NutsSession session;
    private String command;
    private String wd;
    private String icon;
    private String filePath;

    public OptionalMsLinkHelper(String command, String wd, String icon, String filePath, NutsSession session) {
        this.session = session;
        this.command = command;
        this.wd = wd;
        this.icon = icon;
        this.filePath = filePath;
    }

    public static boolean isSupported() {
        try {
            Class.forName("mslinks.ShellLink");
        } catch (Exception e) {
            return false;
        }
        try {
            Iterator<Path> a = FileSystems.getDefault().getRootDirectories().iterator();
            Path someRoot = null;
            if (a.hasNext()) {
                someRoot = a.next();
                mslinks.ShellLink.createLink(someRoot.resolve("anyName").toString());
                return true;
            }
        } catch (Throwable a) {
            //
        }
        return false;
    }

    public void write() {
        String[] cmd = session.commandLine().parse(command).toStringArray();
        mslinks.ShellLink se = mslinks.ShellLink.createLink(cmd[0])
                .setWorkingDir(wd)
                .setCMDArgs(session.commandLine().create(
                        Arrays.copyOfRange(cmd, 1, cmd.length)
                ).toString());

        if (NutsBlankable.isBlank(icon)) {
            se.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
            se.getHeader().setIconIndex(148);
        } else {
            se.setIconLocation(icon.trim());
        }
        se.getConsoleData()
                .setFont(mslinks.extra.ConsoleData.Font.Consolas);
        try {
            //.setFontSize(16)
            //.setTextColor(5)
            CoreIOUtils.mkdirs(Paths.get(filePath).getParent(),session);
            se.saveTo(filePath);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
