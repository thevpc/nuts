package net.thevpc.nuts.toolbox.nadmin.optional.oswindows;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.AbstractFreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntry;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfoType;
import net.thevpc.nuts.toolbox.nadmin.util._IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WindowFreeDesktopEntryWriter extends AbstractFreeDesktopEntryWriter {
    private NutsSession session;
    private Path desktopPath;

    public WindowFreeDesktopEntryWriter(Path desktopPath, NutsSession session) {
        this.session = session;
        this.desktopPath = desktopPath;
    }

    @Override
    public PathInfo[] writeShortcut(FreeDesktopEntry descriptor, Path path, boolean doOverride, NutsId id) {
        path = Paths.get(ensureName(path == null ? null : path.toString(), descriptor.getOrCreateDesktopEntry().getName(), "lnk"));
        FreeDesktopEntry.Group g = descriptor.getOrCreateDesktopEntry();
        if (g == null || g.getType() != FreeDesktopEntry.Type.APPLICATION) {
            throw new IllegalArgumentException("invalid entry");
        }
        String wd = g.getPath();
        if (wd == null) {
            wd = System.getProperty("user.home");
        }
        String[] cmd = session.getWorkspace().commandLine().parse(g.getExec()).toStringArray();
        mslinks.ShellLink se = mslinks.ShellLink.createLink(cmd[0])
                .setWorkingDir(wd)
                .setCMDArgs(session.getWorkspace().commandLine().create(
                        Arrays.copyOfRange(cmd,1,cmd.length)
                ).toString())
                ;
        if (g.getIcon() == null) {
            se.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
            se.getHeader().setIconIndex(148);
        } else {
            se.setIconLocation(g.getIcon());
        }
        se.getConsoleData().setFont(mslinks.extra.ConsoleData.Font.Consolas);

        try {
            //.setFontSize(16)
            //.setTextColor(5)
            File q = path.toFile();
            boolean alreadyExists = q.exists();
            if (alreadyExists && doOverride) {
                return new PathInfo[]{new PathInfo(NdiScriptInfoType.DESKTOP_SHORTCUT, id, q.toPath(), PathInfo.Status.DISCARDED)};
            }
            se.saveTo(q.toString());
            return new PathInfo[]{new PathInfo(NdiScriptInfoType.DESKTOP_SHORTCUT, id, q.toPath(), alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED)};
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id) {
        fileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "lnk")).getFileName().toString();
        File q = desktopPath.resolve(fileName).toFile();
        return writeShortcut(descriptor, q.toPath(), doOverride, id);
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id) {
        List<PathInfo> result = new ArrayList<>();
        FreeDesktopEntry.Group root = descriptor.getOrCreateDesktopEntry();
        if (root == null || root.getType() != FreeDesktopEntry.Type.APPLICATION) {
            throw new IllegalArgumentException("invalid entry");
        }
        String wd = root.getPath();
        if (wd == null) {
            wd = System.getProperty("user.home");
        }
        mslinks.ShellLink se = mslinks.ShellLink.createLink(root.getExec())
                .setWorkingDir(wd);
        if (root.getIcon() == null) {
            se.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
            se.getHeader().setIconIndex(148);
        } else {
            se.setIconLocation(root.getIcon());
        }
        se.getConsoleData().setFont(mslinks.extra.ConsoleData.Font.Consolas);
        List<String> categories = new ArrayList<>(root.getCategories());
        if (categories.isEmpty()) {
            categories.add("/");
        }
        for (String category : categories) {
            List<String> part = Arrays.stream((category == null ? "" : category).split("/")).filter(x -> !x.isEmpty()).collect(Collectors.toList());

            File m = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs");
            if (!part.isEmpty() && part.get(0).equals("Applications")) {
                part.remove(0);
            }
            if (part.size() > 0) {
                m = new File(m, String.join("\\", part));
                m.mkdirs();
            }
            try {
                //.setFontSize(16)
                //.setTextColor(5)
                File q = new File(m, descriptor.getOrCreateDesktopEntry().getName() + ".lnk");
                boolean alreadyExists = q.exists();
                if (alreadyExists && doOverride) {
                    result.add(new PathInfo(NdiScriptInfoType.DESKTOP_MENU, id, q.toPath(), PathInfo.Status.DISCARDED));
                } else {
                    se.saveTo(q.getPath());
                    result.add(new PathInfo(NdiScriptInfoType.DESKTOP_MENU, id, q.toPath(), alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED));
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
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

        mslinks.ShellLink se = mslinks.ShellLink.createLink(g.getExec())
                .setWorkingDir(wd);
        if (g.getIcon() == null) {
            se.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
            se.getHeader().setIconIndex(148);
        } else {
            se.setIconLocation(g.getIcon());
        }

        se.getConsoleData()
                .setFont(mslinks.extra.ConsoleData.Font.Consolas);
        try {
            //.setFontSize(16)
            //.setTextColor(5)
            se.saveTo(out.toString());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean tryWrite(FreeDesktopEntry file, Path out) {
        byte[] old = _IOUtils.loadFileContentLenient(out);
        if (old.length == 0) {
            write(file, out);
            return true;
        }
        write(file, out);
        byte[] next = _IOUtils.loadFileContentLenient(out);
        return !Arrays.equals(old, next);
    }

    public boolean tryWrite(FreeDesktopEntry file, File out) {
        return tryWrite(file, out.toPath());
    }

    public void write(FreeDesktopEntry file, File out) {
        try (PrintStream p = new PrintStream(out)) {
            write(file, p);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void write(FreeDesktopEntry file, NutsPrintStream out) {
        write(file, out.asPrintStream());
    }

    public void write(FreeDesktopEntry file, PrintStream out) {
        out.println("#!/usr/bin/env xdg-open");
        for (FreeDesktopEntry.Group group : file.getGroups()) {
            out.println();
            String gn = group.getGroupName();
            if (gn == null || gn.trim().length() == 0) {
                throw new IllegalArgumentException("invalid group name");
            }
            FreeDesktopEntry.Type t = group.getType();
            if (t == null) {
                throw new IllegalArgumentException("missing type");
            }
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
