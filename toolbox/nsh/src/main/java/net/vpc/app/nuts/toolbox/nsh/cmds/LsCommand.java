/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.FileNonOption;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import net.vpc.app.nuts.NutsSessionTerminal;

/**
 * Created by vpc on 1/7/17.
 */
public class LsCommand extends AbstractNutsCommand {

    private static final FileSorter FILE_SORTER = new FileSorter();

    public LsCommand() {
        super("ls", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean d = false;
        boolean l = false;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<File> folders = new ArrayList<>();
        List<File> files = new ArrayList<>();
        List<File> invalids = new ArrayList<>();
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if ((a = cmdLine.readBooleanOption("-d", "--dir")) != null) {
                options.d = a.getBooleanValue();
            } else if ((a = cmdLine.readBooleanOption("-l", "--list")) != null) {
                options.l = a.getBooleanValue();
            } else {
                String path = cmdLine.readRequiredNonOption(new FileNonOption("FileOrFolder")).getStringExpression();
                File file = new File(context.getShell().getAbsolutePath(path));
                ;
                if (file.isDirectory()) {
                    folders.add(file);
                } else if (file.exists()) {
                    files.add(file);
                } else {
                    invalids.add(file);
                }
            }
        }
        if (cmdLine.isExecMode()) {
            int exitCode = 0;
            boolean first = true;
            PrintStream out = context.out();
            for (File f : invalids) {
                exitCode = 1;
                ls(f, options, context, out, false);
            }
            for (File f : files) {
                first = false;
                ls(f, options, context, out, false);
            }
            for (File f : folders) {
                if (first) {
                    first = false;
                } else {
                    out.println();
                }
                ls(f, options, context, out, folders.size() > 0 || files.size() > 0);
            }
            if (invalids.size() + files.size() + folders.size() == 0) {
                ls(new File(context.getShell().getCwd()), options, context, out, false);
            }
            return exitCode;
        }
        return 0;
    }

    private void ls(File path, Options options, NutsCommandContext context, PrintStream out, boolean addPrefix) {
        if (!path.exists()) {
            throw new NutsIllegalArgumentException("ls: cannot access '" + path.getPath() + "': No such file or directory");
        } else if (path.isDirectory()) {
            if (addPrefix) {
                out.printf("%s:\n", path.getName());
            }
            File[] arr = path.listFiles();
            if (arr != null) {
                Arrays.sort(arr, FILE_SORTER);
                for (File file1 : arr) {
                    ls0(file1, options, out);
                }
            }
        } else {
            ls0(path, options, out);
        }
    }

    private void ls0(File path, Options options, PrintStream out) {
        String name = path.getName();
        if (options.l) {
            out.print((path.isDirectory() ? "d" : path.isFile() ? "-" : "?"));
            out.print((path.canRead() ? "r" : "-"));
            out.print((path.canWrite() ? "w" : "-"));
            out.print((path.canExecute() ? "x" : "-"));
            out.print(" ");
            String owner = null;
            String group = null;

            try {
                owner = Files.getFileAttributeView(path.toPath(), FileOwnerAttributeView.class).getOwner().getName();
            } catch (IOException ex) {
                //
                owner = "unknown";
            }
            try {
                group = Files.readAttributes(path.toPath(), PosixFileAttributes.class).group().getName();
            } catch (IOException ex) {
                //
                group = "unknown";
            }
            out.printf("%s", owner);
            out.print(" ");
            out.printf("%s", group);
            out.print(" ");
            out.printf("%s", String.format("%9d", path.length()));
            out.print(" ");
            out.printf("%s", SIMPLE_DATE_FORMAT.format(path.lastModified()));
            out.print(" ");
            printPathName(path, name, out);
        } else {
            printPathName(path, name, out);
        }
    }

    private void printPathName(File path, String name, PrintStream out) {
        if (path.isDirectory()) {
            out.printf("==%s==\n", name);
        } else {
            String p = path.getName().toLowerCase();
            if (p.startsWith(".") || p.endsWith(".log") || p.contains(".log.")) {
                out.printf("<%s>\n", name);
            } else {
                int i = p.lastIndexOf('.');
                if (i > -1) {
                    String suffix = p.substring(i + 1);
                    if (new HashSet<String>(Arrays.asList("xml", "config", "cfg", "json", "iml", "ipr")).contains(suffix)) {
                        out.printf("##%s##\n", name);
                    } else if (
                            new HashSet<String>(Arrays.asList("jar", "war", "ear", "rar", "zip", "bin", "exe", "tar", "gz", "class", "sh")).contains(suffix)
                                    || path.canExecute()
                    ) {
                        out.printf("[[%s]]\n", name);
                    } else {
                        out.printf("%s\n", name);
                    }
                } else {
                    out.printf("%s\n", name);
                }
            }
        }
    }

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static class FileSorter implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            int d1 = o1.isDirectory() ? 0 : o1.isFile() ? 1 : 2;
            int d2 = o2.isDirectory() ? 0 : o2.isFile() ? 1 : 2;
            int x = d1 - d2;
            if (x != 0) {
                return x;
            }
            return o1.getPath().compareTo(o2.getPath());
        }

    }
}
