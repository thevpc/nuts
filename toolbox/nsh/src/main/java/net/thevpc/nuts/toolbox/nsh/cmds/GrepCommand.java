/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends AbstractNshBuiltin {

    public GrepCommand() {
        super("grep", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean regexp = false;
        boolean invertMatch = false;
        boolean word = false;
        boolean lineRegexp = false;
        boolean ignoreCase = false;
        boolean n = false;
    }

    public int execImpl(String[] args, NshExecutionContext context) {
        NutsCommandLine commandLine = cmdLine(args, context);
        Options options = new Options();
        List<File> files = new ArrayList<>();
        String expression = null;
        PrintStream out = context.out();
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (commandLine.next("-") != null) {
                files.add(null);
            } else if (commandLine.next("-e", "--regexp") != null) {
                options.regexp = true;
            } else if (commandLine.next("-v", "--invert-match") != null) {
                options.invertMatch = true;
            } else if (commandLine.next("-w", "--word-regexp") != null) {
                options.word = true;
            } else if (commandLine.next("-x", "--line-regexp") != null) {
                options.lineRegexp = true;
            } else if (commandLine.next("-i", "--ignore-case") != null) {
                options.ignoreCase = true;
            } else if (commandLine.next("--version") != null) {
                out.printf("%s\n", "1.0");
                return 0;
            } else if (commandLine.next("-n") != null) {
                options.n = true;
            } else if (commandLine.next("--help") != null) {
                out.printf("%s\n", getHelp());
                return 0;
            }else if (commandLine.peek().isNonOption()){
                if (expression == null) {
                    expression = commandLine.next().getString();
                } else {
                    String path = commandLine.next().getString();
                    File file = new File(context.getGlobalContext().getAbsolutePath(path));
                    files.add(file);
                }
            } else {
                context.configureLast(commandLine);
            }
        }
        if (files.isEmpty()) {
            files.add(null);
        }
        if (expression == null) {
            throw new NutsExecutionException(context.getSession(), "missing Expression", 2);
        }
        String baseExpr = options.regexp ? ("^" + simpexpToRegexp(expression, false) + "$") : expression;
        if (options.word) {
            baseExpr = "\\b" + baseExpr + "\\b";
        }
        if (!options.lineRegexp) {
            baseExpr = ".*" + baseExpr + ".*";
        }
        if (options.ignoreCase) {
            baseExpr = "(?i)" + baseExpr;
        }
        Pattern p = Pattern.compile(baseExpr);
        //text mode
        boolean prefixFileName = files.size() > 1;
        int x=0;
        for (File f : files) {
            x=grepFile(f, p, options, context, prefixFileName);
        }
        return x;
    }

    protected int grepFile(File f, Pattern p, Options options, NshExecutionContext context, boolean prefixFileName) {

        Reader reader = null;
        try {
            try {
                String fileName = null;
                if (f == null) {
                    reader = new InputStreamReader(context.in());
                } else if (f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        for (File ff : files) {
                            grepFile(ff, p, options, context, true);
                        }
                    }
                    return 0;
                } else {
                    fileName = f.getPath();
                    reader = new FileReader(f);
                }
                try (BufferedReader r = new BufferedReader(reader)) {
                    String line = null;
                    int nn = 1;
                    PrintStream out = context.out();
                    while ((line = r.readLine()) != null) {
                        boolean matches = p.matcher(line).matches();
                        if (matches != options.invertMatch) {
                            if (options.n) {
                                if (fileName != null && prefixFileName) {
                                    out.print(fileName);
                                    out.print(":");
                                }
                                out.print(nn);
                                out.print(":");
                            }
                            out.println(line);
                        }
                        nn++;
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(context.getSession(), ex.getMessage(), ex, 100);
        }
        return  0;
    }

    public static String simpexpToRegexp(String pattern, boolean contains) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '[':
                case ']':
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '*': {
//                    if (i + 1 < cc.length && cc[i + 1] == '*') {
//                        i++;
//                        sb.append("[a-zA-Z_0-9_$.-]*");
//                    } else {
//                        sb.append("[a-zA-Z_0-9_$-]*");
//                    }
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        if (!contains) {
            sb.insert(0, '^');
            sb.append('$');
        }
        return sb.toString();
    }

}
