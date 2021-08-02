package net.thevpc.nuts.toolbox.nadmin.optional.oswindows;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntry;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfo;
import net.thevpc.nuts.toolbox.nadmin.util._IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WindowFreeDesktopEntryWriter implements FreeDesktopEntryWriter {
    private NutsSession session;
    private Path desktopPath;

    public WindowFreeDesktopEntryWriter(Path desktopPath, NutsSession session) {
        this.session = session;
        this.desktopPath = desktopPath;
    }

    @Override
    public PathInfo[] writeDesktop(FreeDesktopEntry file, boolean doOverride) {
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
        se.getConsoleData().setFont(mslinks.extra.ConsoleData.Font.Consolas);

        try {
            //.setFontSize(16)
            //.setTextColor(5)
            File m = new File(desktopPath.toString());
            File q = new File(m, file.getOrCreateDesktopEntry().getName() + ".lnk");
            boolean alreadyExists = q.exists();
            if (alreadyExists && doOverride) {
                return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, q.toPath(), PathInfo.Status.DISCARDED)};
            }
            se.saveTo(q.toString());
            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, q.toPath(), alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED)};
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry file, String menuPath, boolean doOverride) {
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
        se.getConsoleData().setFont(mslinks.extra.ConsoleData.Font.Consolas);


        String[] part = Arrays.stream((menuPath == null ? "" : menuPath).split("/")).filter(x -> !x.isEmpty()).toArray(String[]::new);
        File m = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs");
        if (part.length > 0) {
            m = new File(m, String.join("\\", part));
            m.mkdirs();
        }
        try {
            //.setFontSize(16)
            //.setTextColor(5)
            File q = new File(m, file.getOrCreateDesktopEntry().getName() + ".lnk");
            boolean alreadyExists = q.exists();
            if (alreadyExists && doOverride) {
                return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, q.toPath(), PathInfo.Status.DISCARDED)};
            }
            se.saveTo(q.getPath());
            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, q.toPath(), alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED)};
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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
