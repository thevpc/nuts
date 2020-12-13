/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

import net.thevpc.nuts.toolbox.ncode.filters.JavaSourceFilter;
import net.thevpc.nuts.toolbox.ncode.filters.PathSourceFilter;
import net.thevpc.nuts.toolbox.ncode.processors.JavaSourceFormatter;
import net.thevpc.nuts.toolbox.ncode.processors.PathSourceFormatter;
import net.thevpc.nuts.toolbox.ncode.sources.SourceFactory;
import net.thevpc.common.strings.StringComparator;
import net.thevpc.common.strings.StringComparators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.thevpc.nuts.toolbox.ncode.SourceNavigator.navigate;

/**
 * @author thevpc
 */
public class Feenoo {

    public static void main(String[] args) {
        StringComparator type = null;
        StringComparator file = null;
        List<String> paths = new ArrayList<>();
        if (args.length == 0) {
            help0();
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--help")) {
                help();
                System.exit(0);
            }
            if (arg.equals("--version")) {
                version();
                System.exit(0);
            }
            if (arg.startsWith("-")) {
                i++;
                String x = args[i];
                boolean neg = false;
                arg = arg.substring(1);
                if (arg.startsWith("!")) {
                    arg = arg.substring(1);
                    neg = true;
                }
                if (arg.equals("t")) {
                    if (!x.startsWith("^")) {
                        x = "*" + x;
                    }
                    if (!x.startsWith("$")) {
                        x = x + "*";
                    }
                    type = StringComparators.like(x);
                } else if (arg.equals("it")) {
                    if (!x.startsWith("^")) {
                        x = "*" + x;
                    }
                    if (!x.startsWith("$")) {
                        x = x + "*";
                    }
                    type = StringComparators.ilike(x);
                } else if (arg.equals("f")) {
                    if (!x.startsWith("^")) {
                        x = "*" + x;
                    }
                    if (!x.startsWith("$")) {
                        x = x + "*";
                    }
                    file = StringComparators.like(x);
                } else if (arg.equals("if")) {
                    if (!x.startsWith("^")) {
                        x = "*" + x;
                    }
                    if (!x.startsWith("$")) {
                        x = x + "*";
                    }
                    file = StringComparators.ilike(x);
                } else {
                    System.err.println("unsupported type " + args[i - 1]);
                    help();
                    System.exit(1);
                    return;
                }
                if (neg) {
                    if (file != null) {
                        file = StringComparators.not(file);
                    } else if (type != null) {
                        type = StringComparators.not(type);
                    }
                }
            } else {
                for (String p : arg.split("[:;]")) {
                    if (p.length() > 0) {
                        paths.add(p);
                    }
                }
            }
        }
        if (type != null) {
            if (paths.isEmpty()) {
                System.err.println("missing location");
                help();
                System.exit(1);
                return;
            }
            for (String path : paths) {
                navigate(SourceFactory.create(new File(path)), new JavaSourceFilter(type, file), new JavaSourceFormatter());
            }
        } else if (file != null) {
            if (paths.isEmpty()) {
                System.err.println("missing location");
                help();
                System.exit(1);
                return;
            }
            for (String path : paths) {
                navigate(SourceFactory.create(new File(path)), new PathSourceFilter(file), new PathSourceFormatter());
            }
        } else {
            System.err.println("missing arguments");
            System.exit(1);
            help();
        }
    }

    private static void version() {
        System.out.println("feenoo 1.0 (c) Taha Ben Salah");
    }

    private static void help0() {
        version();
        System.out.println("simple file/java class finder");
        System.out.println("type --help for more help");
    }

    private static void help() {
        version();
        System.out.println("simple file/java class finder");
        System.out.println("[Syntax] feenoo -[!][i]t name [folder/file-list]");
        System.out.println("   find java type with name 'name' in folder/file list");
        System.out.println("[Syntax] feenoo -[!][i]f name [folder/file-list]");
        System.out.println("   find file with path 'name' in folder/file list");
        System.out.println("[Examples] ");
        System.out.println("feenoo -t String . foo.zip");
        System.out.println("   search for type which name contains 'String' (case sensitive) in folders . and foo.zip");
        System.out.println("feenoo -it String . foo.zip");
        System.out.println("   search for type which name contains 'String' or 'STRING' (case insensitive) in both folder . and foo.zip");
        System.out.println("feenoo -t ^java.lang.String$ . foo.zip");
        System.out.println("   search for type which exact name 'java.lang.String' in folders . and foo.zip");
    }
}
