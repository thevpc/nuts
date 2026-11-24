package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.FreeDesktopEntry;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.AbstractFreeDesktopEntryWriter;
import net.thevpc.nuts.util.NAssert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WindowFreeDesktopEntryWriter extends AbstractFreeDesktopEntryWriter {
    private final NPath desktopPath;

    public WindowFreeDesktopEntryWriter(NPath desktopPath) {
        this.desktopPath = desktopPath;
    }

    @Override
    public PathInfo[] writeShortcut(FreeDesktopEntry descriptor, NPath path, boolean doOverride, NId id) {
        path = NPath.of(ensureName(path == null ? null : path.toString(), descriptor.getOrCreateDesktopEntry().getName(), "lnk"));
        FreeDesktopEntry.Group g = descriptor.getOrCreateDesktopEntry();
        if (g == null || g.getType() != FreeDesktopEntry.Type.APPLICATION) {
            throw new IllegalArgumentException("invalid entry");
        }
        String wd = g.getPath();
        if (wd == null) {
            wd = System.getProperty("user.home");
        }
        NPath q = path;
        boolean alreadyExists = q.exists();
        if (alreadyExists && !doOverride) {
            return new PathInfo[]{new PathInfo("desktop-shortcut", id, q, PathInfo.Status.DISCARDED)};
        }
        PathInfo.Status newStatus = writeLnk(q, g.getExec(), wd, g.getIcon());
        return new PathInfo[]{new PathInfo("desktop-shortcut", id, q, newStatus)};
    }

    @Override
    public PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NId id) {
        fileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "lnk")).getFileName().toString();
        NPath q = desktopPath.resolve(fileName);
        return writeShortcut(descriptor, q, doOverride, id);
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NId id) {
        List<PathInfo> result = new ArrayList<>();
        FreeDesktopEntry.Group root = descriptor.getOrCreateDesktopEntry();
        if (root == null || root.getType() != FreeDesktopEntry.Type.APPLICATION) {
            throw new IllegalArgumentException("invalid entry");
        }
        String wd = root.getPath();
        if (wd == null) {
            wd = System.getProperty("user.home");
        }
        List<String> categories = new ArrayList<>(root.getCategories());
        if (categories.isEmpty()) {
            categories.add("/");
        }
        for (String category : categories) {
            List<String> part = Arrays.stream((category == null ? "" : category).split("/")).filter(x -> !x.isEmpty()).collect(Collectors.toList());

            NPath m = NPath.ofUserHome().resolve("AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs");
            if (!part.isEmpty() && part.get(0).equals("Applications")) {
                part.remove(0);
            }
            if (part.size() > 0) {
                m = m.resolve(String.join("\\", part));
                m.mkdirs();
            }

            NPath q = m.resolve(descriptor.getOrCreateDesktopEntry().getName() + ".lnk");
            boolean alreadyExists = q.exists();
            if (alreadyExists && !doOverride) {
                result.add(new PathInfo("desktop-menu", id, q, PathInfo.Status.DISCARDED));
            } else {
                PathInfo.Status newStatus = writeLnk(q, root.getExec(), wd, root.getIcon());
                result.add(new PathInfo("desktop-menu", id, q, newStatus));
            }

        }
        return result.toArray(new PathInfo[0]);
    }

    /**
     * Write a Windows .lnk. Prefer mslinks, but fall back to Windows Script
     * Host (WScript.Shell) when mslinks is missing or fails. This keeps NDI
     * able to create shortcuts even without the optional mslinks helper.
     */
    private PathInfo.Status writeLnk(NPath target, String[] exec, String wd, String icon) {
        boolean existed = target.exists();
        try {
            return new OptionalMsLinkHelper(exec, wd, icon, target.toString()).write();
        } catch (Throwable mslinksError) {
            target.mkParentDirs();
            String arguments = exec.length > 1
                    ? NCmdLine.of(Arrays.copyOfRange(exec, 1, exec.length)).toString() : "";
            String iconLocation = (icon == null || icon.trim().isEmpty())
                    ? "%SystemRoot%\\system32\\SHELL32.dll,148" : icon;
            String script = "$w=New-Object -ComObject WScript.Shell;"
                    + "$s=$w.CreateShortcut('" + psQuote(target.toString()) + "');"
                    + "$s.TargetPath='" + psQuote(exec[0]) + "';"
                    + "$s.Arguments='" + psQuote(arguments) + "';"
                    + "$s.WorkingDirectory='" + psQuote(wd == null ? System.getProperty("user.home") : wd) + "';"
                    + "$s.IconLocation='" + psQuote(iconLocation) + "';"
                    + "$s.Save();";
            try {
                Process process = new ProcessBuilder(
                        "powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                        "-Command", script)
                        .redirectErrorStream(true)
                        .start();
                try (InputStream in = process.getInputStream()) {
                    byte[] buffer = new byte[4096];
                    while (in.read(buffer) >= 0) {
                        // drain output so the process cannot block
                    }
                }
                int code = process.waitFor();
                if (code != 0 || !target.exists()) {
                    throw new NIOException(NMsg.ofC("powershell failed to create shortcut %s (exit %s)", target, code));
                }
            } catch (IOException ex) {
                throw new NIOException(NMsg.ofC("unable to create shortcut %s : %s", target, ex), ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new NIOException(NMsg.ofC("interrupted while creating shortcut %s", target), ex);
            }
            return existed ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
        }
    }

    private static String psQuote(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    public void write(FreeDesktopEntry file, Path out) {
        FreeDesktopEntry.Group g = file.getOrCreateDesktopEntry();
        if (g == null || g.getType() != FreeDesktopEntry.Type.APPLICATION) {
            throw new IllegalArgumentException("invalid entry");
        }
        String wd = g.getPath();
        if (wd == null) {
            wd = System.getProperty("user.home");
        }
        new OptionalMsLinkHelper(g.getExec(), wd, g.getIcon(), out.toString()).write();
    }

    public boolean tryWrite(FreeDesktopEntry file, Path out) {
        byte[] old = CoreIOUtils.loadFileContentLenient(out);
        if (old.length == 0) {
            write(file, out);
            return true;
        }
        write(file, out);
        byte[] next = CoreIOUtils.loadFileContentLenient(out);
        return !Arrays.equals(old, next);
    }

    public boolean tryWrite(FreeDesktopEntry file, File out) {
        return tryWrite(file, out.toPath());
    }

    public void write(FreeDesktopEntry file, File out) {
        try (PrintStream p = new PrintStream(out)) {
            write(file, p);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public void write(FreeDesktopEntry file, NPrintStream out) {
        write(file, out.asPrintStream());
    }

    public void write(FreeDesktopEntry file, PrintStream out) {
        out.println("#!/usr/bin/env xdg-open");
        for (FreeDesktopEntry.Group group : file.getGroups()) {
            out.println();
            String gn = group.getGroupName();
            NAssert.requireNamedNonBlank(gn, "group name");
            FreeDesktopEntry.Type t = group.getType();
            NAssert.requireNamedNonBlank(t, "type");
            out.println("[" + gn.trim() + "]");
            for (Map.Entry<String, Object> e : group.toMap().entrySet()) {
                Object v = e.getValue();
                if (v instanceof Boolean || v instanceof String) {
                    out.println(e.getKey() + "=" + e.getValue());
                } else if (v instanceof List) {
                    char sep = ';';
                    out.println(e.getKey() + "=" +
                            ((List<String>) v).stream().map(x -> {
                                StringBuilder sb = new StringBuilder();
                                for (char c : x.toCharArray()) {
                                    if (c == sep || c == '\\') {
                                        sb.append('\\');
                                    }
                                    sb.append(c);
                                }
                                return sb.toString();
                            }).collect(Collectors.joining("" + sep))
                    );
                } else {
                    throw new IllegalArgumentException("unsupported value type for " + e.getKey());
                }
            }

        }
    }

}
