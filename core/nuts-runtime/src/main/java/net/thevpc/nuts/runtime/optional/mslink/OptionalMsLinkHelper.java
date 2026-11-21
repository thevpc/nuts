package net.thevpc.nuts.runtime.optional.mslink;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NTypeLoader;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NTypeLoaderImpl;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

/**
 * ARCH-03: mslinks is Windows-only and optional. All access is reflective so this
 * class can be loaded (and queried) on systems where the library is absent.
 */
public class OptionalMsLinkHelper {
    private static final NTypeLoader mslinksShellLink = new NTypeLoaderImpl("mslinks.ShellLink");
    private final String[] command;
    private final String wd;
    private final String icon;
    private final String filePath;

    public OptionalMsLinkHelper(String[] command, String wd, String icon, String filePath) {
        this.command = command;
        this.wd = wd;
        this.icon = icon;
        this.filePath = filePath;
    }

    public static boolean isSupported() {
        if (!mslinksShellLink.type().isPresent()) {
            return false;
        }
        try {
            Method createLink = mslinksShellLink.getDeclaredMethod("createLink", String.class).orNull();
            if (createLink == null) {
                return false;
            }
            Iterator<Path> a = FileSystems.getDefault().getRootDirectories().iterator();
            if (a.hasNext()) {
                Path someRoot = a.next();
                createLink.invoke(null, someRoot.resolve("anyName").toString());
                return true;
            }
        } catch (Throwable a) {
            // not supported on this platform
        }
        return false;
    }

    public PathInfo.Status write() {
        boolean alreadyExists = false;
        Path outputFile = Paths.get(filePath);
        try {
            alreadyExists = Files.isRegularFile(outputFile);
        } catch (Exception ex) {
            //
        }
        byte[] oldContent = CoreIOUtils.loadFileContentLenient(outputFile);
        String[] cmd = command;
        Object link;
        try {
            Method createLink = mslinksShellLink.getDeclaredMethod("createLink", String.class).orNull();
            if (createLink == null) {
                throw new NIOException(NMsg.ofC("mslinks is not available"));
            }
            link = createLink.invoke(null, cmd[0]);
            invoke(link, "setWorkingDir", new Class<?>[]{String.class}, wd);
            invoke(link, "setCMDArgs", new Class<?>[]{String.class},
                    NCmdLine.of(Arrays.copyOfRange(cmd, 1, cmd.length)).toString());

            if (NBlankable.isBlank(icon)) {
                invoke(link, "setIconLocation", new Class<?>[]{String.class}, "%SystemRoot%\\system32\\SHELL32.dll");
                Object header = invoke(link, "getHeader", new Class<?>[0]);
                invoke(header, "setIconIndex", new Class<?>[]{int.class}, 148);
            } else {
                invoke(link, "setIconLocation", new Class<?>[]{String.class}, NStringUtils.strip(icon));
            }
            NTypeLoader fontTypeLoader = new NTypeLoaderImpl("mslinks.extra.ConsoleData$Font");
            Class<?> fontType = fontTypeLoader.type().orNull();
            if (fontType != null && fontType.isEnum()) {
                Object consoleData = invoke(link, "getConsoleData", new Class<?>[0]);
                invoke(consoleData, "setFont", new Class<?>[]{fontType}, Enum.valueOf((Class) fontType, "Consolas"));
            }
            NPath.of(outputFile).mkParentDirs();
            invoke(link, "saveTo", new Class<?>[]{String.class}, filePath);
        } catch (NIOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NIOException(ex);
        }
        if (alreadyExists) {
            byte[] newContent = CoreIOUtils.loadFileContentLenient(outputFile);
            if (Arrays.equals(oldContent, newContent)) {
                return PathInfo.Status.DISCARDED;
            }
            return PathInfo.Status.OVERRIDDEN;
        } else {
            return PathInfo.Status.CREATED;
        }
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(name, parameterTypes);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new NIOException(NMsg.ofC("unable to invoke mslinks.%s", name), e);
        }
    }
}
