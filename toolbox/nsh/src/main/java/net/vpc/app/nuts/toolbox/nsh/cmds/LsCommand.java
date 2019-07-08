/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.*;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.common.util.BytesSizeFormat;

/**
 * Created by vpc on 1/7/17.
 */
public class LsCommand extends SimpleNshBuiltin {

    private static final FileSorter FILE_SORTER = new FileSorter();
    private HashSet<String> fileTypeArchive = new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "tar", "gz"));
    private HashSet<String> fileTypeExec2 = new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "bin", "exe", "tar", "gz", "class", "sh"));
    private HashSet<String> fileTypeConfig = new HashSet<String>(Arrays.asList("xml", "config", "cfg", "json", "iml", "ipr"));

    public LsCommand() {
        super("ls", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean a = false;
        boolean d = false;
        boolean l = false;
        boolean h = false;
        List<String> paths = new ArrayList<>();
        BytesSizeFormat byteFormat = new BytesSizeFormat("iD1F");
    }

    private static class ResultSuccess {

        String workingDir;
        List<ResultGroup> result = new ArrayList<>();
    }

    public static class ResultError {

        boolean error = true;
        String workingDir;
        Map<String, String> result;
    }

    public static class ResultGroup {

        String name;
        ResultItem file;
        List<ResultItem> children;
    }

    public static class ResultItem {

        String name;
        String path;
        char type;
        String uperms;
        String jperms;
        String owner;
        String group;
        long length;
        Date modified;
        Date created;
        Date accessed;
//        boolean dir;
//        boolean regular;
//        boolean link;
//        boolean other;
        boolean config;
        boolean exec2;
        boolean archive;
        boolean hidden;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-d", "--dir")) != null) {
            options.d = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextBoolean("-l", "--list")) != null) {
            options.l = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextBoolean("-a", "--all")) != null) {
            options.a = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextBoolean("-h")) != null) {
            options.h = a.getBooleanValue();
            return true;
        } else if (commandLine.peek().isNonOption()) {
            String path = commandLine.next(commandLine.createName("file")).getString();
            options.paths.add(path);
            options.paths.addAll(Arrays.asList(commandLine.toArray()));
            commandLine.skip();
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        ResultSuccess success = new ResultSuccess();
        success.workingDir = context.getRootContext().getAbsolutePath(".");
        ResultError errors = null;
        int exitCode = 0;
        if (options.paths.isEmpty()) {
            options.paths.add(".");
        }
        for (String path : options.paths) {
            File file = new File(context.getRootContext().getAbsolutePath(path));
            if (!file.exists()) {
                exitCode = 1;
                if (errors == null) {
                    errors = new ResultError();
                    errors.workingDir = context.getRootContext().getAbsolutePath(".");
                }
                errors.result.put(path, "cannot access '" + file + "': No such file or directory");
            } else {
                ResultGroup g = new ResultGroup();
                success.result.add(g);
                g.name = path;
                if (!file.isDirectory() || options.d) {
                    g.file = build(file);
                } else {
                    File[] children = file.listFiles();
                    if (children != null) {
                        Arrays.sort(children, FILE_SORTER);
                        g.children = new ArrayList<>();
                        for (File ch : children) {
                            ResultItem b = build(ch);
                            if (options.a || !b.hidden) {
                                g.children.add(b);
                            }
                        }
                    }
                }
            }
        }
        context.setPrintlnOutObject(success);
        context.setPrintlnErrObject(errors);
        if (exitCode != 0) {
            throw new NutsExecutionException(context.getWorkspace(), exitCode);
        }
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        PrintStream out = context.out();
        Options options = context.getOptions();
        if (context.getResult() instanceof ResultSuccess) {
            ResultSuccess s = context.getResult();
            for (ResultGroup resultGroup : s.result) {
                if (resultGroup.children != null) {
                    if (s.result.size() > 1) {
                        out.printf("%s:\n", resultGroup.name);
                    }
                    for (ResultItem resultItem : resultGroup.children) {
                        printPlain(resultItem, options, out);
                    }
                } else {
                    printPlain(resultGroup.file, options, out);
                }
            }
        } else if (context.getResult() instanceof ResultError) {
            ResultError s = context.getResult();
            for (Map.Entry<String, String> e : s.result.entrySet()) {
                out.printf("{{%s}} : @@%s@@%n", e.getKey(), e.getValue());
            }
        } else {
            super.printPlainObject(context);
        }
    }

    private void printPlain(ResultItem item, Options options, PrintStream out) {
        if (options.l) {
            out.print(item.type);
            out.print(item.uperms != null ? item.uperms : item.jperms);
            out.print(" ");
            out.print(" ");

            out.printf("%s", item.owner);
            out.print(" ");
            out.printf("%s", item.group);
            out.print(" ");
            if (options.h) {
                out.printf("%s", options.byteFormat.format(item.length));
            } else {
                out.printf("%s", String.format("%9d", item.length));
            }
            out.print(" ");
            out.printf("%s", SIMPLE_DATE_FORMAT.format(item.modified));
            out.print(" ");
        }
        String name = new File(item.path).getName();
        if (item.hidden) {
            out.printf("<<%s>>\n", name);
        } else if (item.type == 'd') {
            out.printf("==%s==\n", name);
        } else if (item.exec2 || item.jperms.charAt(2) == 'x') {
            out.printf("[[%s]]\n", name);
        } else if (item.config) {
            out.printf("{{%s}}\n", name);
        } else if (item.archive) {
            out.printf("##%s##\n", name);
        } else {
            out.printf("%s\n", name);
        }
    }

    private ResultItem build(File path) {
        ResultItem r = new ResultItem();
        r.path = path.getPath();
        r.name = path.getName();
        boolean dir = path.isDirectory();
        boolean regular = path.isFile();
        boolean link = false;
        boolean other = false;
        r.jperms = (path.canRead() ? "r" : "-") + (path.canWrite() ? "w" : "-") + (path.canExecute() ? "x" : "-");
        r.modified = new Date(path.lastModified());
        PosixFileAttributes uattr = null;
        try {
            uattr = Files.readAttributes(path.toPath(), PosixFileAttributes.class);
        } catch (Exception ex) {
            //
        }
        try {
            if (uattr != null) {
                r.owner = uattr.owner().getName();
            }
        } catch (Exception ex) {
            r.owner = "unknown";
        }
        try {
            if (uattr != null) {
                r.group = uattr.group().getName();
            }
        } catch (Exception ex) {
            r.group = "unknown";
        }
        try {
            if (uattr != null) {
                r.created = new Date(uattr.creationTime().toMillis());
            }
        } catch (Exception ex) {
            //
        }
        try {
            if (uattr != null) {
                r.accessed = new Date(uattr.lastAccessTime().toMillis());
            }
        } catch (Exception ex) {
            //
        }
        try {
            if (uattr != null) {
                r.modified = new Date(uattr.lastModifiedTime().toMillis());
            }
        } catch (Exception ex) {
            //
        }
        try {
            if (uattr != null) {
                link = uattr.isSymbolicLink();
            }
        } catch (Exception ex) {
            //
        }
        try {
            if (uattr != null) {
                other = uattr.isOther();
            }
        } catch (Exception ex) {
            //
        }
        try {
            if (uattr != null) {
                Set<PosixFilePermission> permissions = uattr.permissions();
                char[] perms = new char[9];
                perms[0] = permissions.contains(PosixFilePermission.OWNER_READ) ? 'r' : '-';
                perms[1] = permissions.contains(PosixFilePermission.OWNER_WRITE) ? 'w' : '-';
                perms[2] = permissions.contains(PosixFilePermission.OWNER_EXECUTE) ? 'x' : '-';
                perms[3] = permissions.contains(PosixFilePermission.GROUP_READ) ? 'r' : '-';
                perms[4] = permissions.contains(PosixFilePermission.GROUP_WRITE) ? 'w' : '-';
                perms[5] = permissions.contains(PosixFilePermission.GROUP_EXECUTE) ? 'x' : '-';
                perms[6] = permissions.contains(PosixFilePermission.OTHERS_READ) ? 'r' : '-';
                perms[7] = permissions.contains(PosixFilePermission.OTHERS_WRITE) ? 'w' : '-';
                perms[8] = permissions.contains(PosixFilePermission.OTHERS_EXECUTE) ? 'x' : '-';
                r.uperms = new String(perms);
            }
        } catch (Exception ex) {
            //
        }

        r.length = path.length();

        String p = path.getName().toLowerCase();
        if (!dir) {
            if (p.startsWith(".") || p.endsWith(".log") || p.contains(".log.")) {
                r.hidden = true;
            } else {
                int i = p.lastIndexOf('.');
                if (i > -1) {
                    String suffix = p.substring(i + 1);
                    if (fileTypeConfig.contains(suffix)) {
                        r.config = true;
                    }
                    if (fileTypeArchive.contains(suffix)) {
                        r.archive = true;
                    }
                    if (fileTypeExec2.contains(suffix)) {
                        r.exec2 = true;
                    }
                }
            }
        } else {
            if (p.startsWith(".")) {
                r.hidden = true;
            }
        }
        r.type = dir ? 'd' : regular ? '-' : link ? 'l' : other ? 'o' : '?';
        return r;
    }

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static class FileSorter implements Comparator<File> {

        boolean foldersFirst = true;
        boolean groupCase = true;
//        boolean hiddenFirst = true;

        @Override
        public int compare(File o1, File o2) {
            int d1 = o1.isDirectory() ? 0 : o1.isFile() ? 1 : 2;
            int d2 = o2.isDirectory() ? 0 : o2.isFile() ? 1 : 2;
            int x = 0;
            if (foldersFirst) {
                x = d1 - d2;
                if (x != 0) {
                    return x;
                }
            }
            if (groupCase) {
                x = o1.getPath().toLowerCase().compareTo(o2.getPath().toLowerCase());
                if (x != 0) {
                    return x;
                }
            }
            x = o1.getPath().compareTo(o2.getPath());
            return x;
        }

    }
}
