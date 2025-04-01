package net.thevpc.nuts.runtime.optional.mslink;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.util.NBlankable;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class OptionalMsLinkHelper {
    private final String command;
    private final String wd;
    private final String icon;
    private final String filePath;

    public OptionalMsLinkHelper(String command, String wd, String icon, String filePath) {
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

    public PathInfo.Status write() {
        boolean alreadyExists = false;
        Path outputFile = Paths.get(filePath);
        try{
            alreadyExists=Files.isRegularFile(outputFile);
        }catch (Exception ex){
            //
        }
        byte[] oldContent=CoreIOUtils.loadFileContentLenient(outputFile);
        String[] cmd = NCmdLine.parseDefault(command).get().setExpandSimpleOptions(false).toStringArray();
        mslinks.ShellLink se = mslinks.ShellLink.createLink(cmd[0])
                .setWorkingDir(wd)
                .setCMDArgs(NCmdLine.of(
                        Arrays.copyOfRange(cmd, 1, cmd.length)
                ).toString());

        if (NBlankable.isBlank(icon)) {
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
            NPath.of(outputFile).mkParentDirs();
            se.saveTo(filePath);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        if(alreadyExists) {
            byte[] newContent = CoreIOUtils.loadFileContentLenient(outputFile);
            if(Arrays.equals(oldContent, newContent)){
                return PathInfo.Status.DISCARDED;
            }
            return PathInfo.Status.OVERRIDDEN;
        }else{
            return PathInfo.Status.CREATED;
        }
    }
}
