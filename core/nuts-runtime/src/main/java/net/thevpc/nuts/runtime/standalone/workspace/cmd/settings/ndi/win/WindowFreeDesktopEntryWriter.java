package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.FreeDesktopEntry;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.AbstractFreeDesktopEntryWriter;
import net.thevpc.nuts.util.NAssert;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WindowFreeDesktopEntryWriter extends AbstractFreeDesktopEntryWriter {
    private final NWorkspace workspace;
    private final NPath desktopPath;

    public WindowFreeDesktopEntryWriter(NPath desktopPath, NWorkspace workspace) {
        this.workspace = workspace;
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
        PathInfo.Status newStatus = new OptionalMsLinkHelper(g.getExec(), wd, g.getIcon(), q.toString()).write();
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
        String[] cmd = NCmdLine.parseDefault(root.getExec()).get().setExpandSimpleOptions(false).toStringArray();
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
                PathInfo.Status newStatus = new OptionalMsLinkHelper(root.getExec(), wd, root.getIcon(), q.toString()).write();
                result.add(new PathInfo("desktop-menu", id, q, newStatus));
            }

        }
        return result.toArray(new PathInfo[0]);
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
            NAssert.requireNonBlank(gn, "group name");
            FreeDesktopEntry.Type t = group.getType();
            NAssert.requireNonBlank(t, "type");
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
