/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipal;
import java.util.Stack;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;

/**
 * Created by vpc on 1/7/17.
 */
public class TestCommand extends AbstractNshBuiltin {

    public TestCommand() {
        super("test", DEFAULT_SUPPORT);
    }

    private int getArgsCount(String s) {
        switch (s) {
//            case "(":
            //unary
            case "-n":
            case "-z":
            case "-b":
            case "-c":
            case "-d":
            case "-e":
            case "-f":
            case "-g":
            case "-G":
            case "-h":
            case "-k":
            case "-L":
            case "-N":
            case "-O":
            case "-p":
            case "-r":
            case "-s":
            case "-S":
            case "-t":
            case "-u":
            case "-w":
            case "-x": {
                return 1;
            }
            case "=":
            case "!=":
            case "-a":
            case "-o":
            case "-eq":
            case "-ge":
            case "-gt":
            case "-le":
            case "-lt":
            case "-ne":
            case "-ef":
            case "-nt":
            case "-ot": {
                return 2;
            }
        }
        return -1;
    }

    private int getArgsPrio(String s) {
        switch (s) {
            case "(": {
                return 0;
            }
            //unary
            case "-n":
            case "-z":
            case "-b":
            case "-c":
            case "-d":
            case "-e":
            case "-f":
            case "-g":
            case "-G":
            case "-h":
            case "-k":
            case "-L":
            case "-N":
            case "-O":
            case "-p":
            case "-r":
            case "-s":
            case "-S":
            case "-t":
            case "-u":
            case "-w":
            case "-x": {
                return 10;
            }
            case "=":
            case "!=":
            case "-a":
            case "-o":
            case "-eq":
            case "-ge":
            case "-gt":
            case "-le":
            case "-lt":
            case "-ne":
            case "-ef":
            case "-nt":
            case "-ot": {
                return 5;
            }
        }
        return -1;
    }

    private void reduce(Stack<String> operators, Stack<Eval> operands, int prio) {
        while (!operators.isEmpty()
                && !operators.peek().equals("(")
                && !operators.peek().equals("[")
                && getArgsPrio(operators.peek()) >= prio) {
            String op = operators.pop();
            int c = getArgsCount(op);
            switch (c) {
                case 1: {
                    operands.push(new EvalUnary(op, operands.pop()));
                    break;
                }
                case 2: {
                    Eval ab = operands.pop();
                    Eval aa = operands.pop();
                    operands.push(new EvalBinary(op, aa, ab));
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported");
                }
            }
        }
    }

    @Override
    public int execImpl(String[] args, JShellExecutionContext context) {
        NutsCommandLine commandLine = context.getWorkspace().commandLine().create(args)
                .setCommandName("test")
                .setExpandSimpleOptions(false)
                ;
        if (commandLine.isEmpty()) {
            return 1;
        }
        if (args.length > 0) {
            Stack<String> operators = new Stack<>();
            Stack<Eval> operands = new Stack<>();
            while (commandLine.hasNext()) {
                NutsArgument a = commandLine.next();
                switch (a.getString()) {
                    case "(": {
                        operators.add(a.getString());
                        break;
                    }
                    case ")": {
                        reduce(operators, operands, 0);
                        if (operators.size() < 1 || !operators.peek().equals("(")) {
                            throw new IllegalArgumentException("')' has no equivalent '('");
                        }
                        operators.pop();
                        break;
                    }
                    case "]": {
                        reduce(operators, operands, 0);
                        if (operators.size() > 0 && !operators.peek().equals("[")) {
                            throw new IllegalArgumentException("']' has no equivalent '['");
                        }
                        if (operators.size() == 1 && operators.peek().equals("[")) {
                            operators.pop();
                        }
                        break;
                    }
                    default: {
                        if (getArgsCount(a.getString()) > 0) {
                            reduce(operators, operands, getArgsPrio(a.getString()));
                            operators.add(a.getString());
                        } else {
                            operands.add(new EvalArg(a));
                        }
                        break;
                    }
                }
            }
            reduce(operators, operands, 0);
            if (operands.size() != 1) {
                throw new IllegalArgumentException("missing operand");
            }
            if (!operators.isEmpty()) {
                throw new IllegalArgumentException("too many operators");
            }
            int result = operands.pop().eval(context);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    public static interface Eval {

        String getType();

        int eval(JShellExecutionContext context);
    }

    public static abstract class EvalBase implements Eval {

        protected String type;

        public EvalBase(String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return getType();
        }
    }

    private static class EvalArg extends EvalBase {

        private NutsArgument arg;

        public EvalArg(NutsArgument arg) {
            super("arg");
            this.arg = arg;
        }

        @Override
        public int eval(JShellExecutionContext context) {
            return arg.getString().length() > 0 ? 0 : 1;
        }

    }

    private static class EvalUnary extends EvalBase {

        protected Eval arg;

        public EvalUnary(String type, Eval arg) {
            super(type);
            this.arg = arg;
        }

        @Override
        public int eval(JShellExecutionContext context) {
            switch (type) {
                case "!": {
                    return 1 - arg.eval(context);
                }
                case "-n": {
                    String pp = evalStr(arg, context);
                    return pp.length() > 0 ? 0 : 1;
                }
                case "-z": {
                    String pp = evalStr(arg, context);
                    return pp.length() > 0 ? 1 : 0;
                }
                case "-b": {
                    Path pp = evalPath(arg, context);
                    return 1;
                }
                case "-c": {
                    Path pp = evalPath(arg, context);
                    return 1;
                }
                case "-d": {
                    Path pp = evalPath(arg, context);
                    return Files.isDirectory(pp) ? 0 : 1;
                }
                case "-f": {
                    Path pp = evalPath(arg, context);
                    return Files.isRegularFile(pp) ? 0 : 1;
                }
                case "-e": {
                    Path pp = evalPath(arg, context);
                    return Files.exists(pp) ? 0 : 1;
                }
                case "-g":
                case "-G":
                case "-h":
                case "-L":
                case "-k":
                case "-S":
                case "-t":
                case "-u": {
                    if (arg instanceof EvalArg) {
                        EvalArg a = (EvalArg) arg;
                        Path pp = evalPath(arg, context);
                        //FILE exists and is a socket
                        return 1;//Files.exists(Paths.get(path)) ? 0 : 1;
                    }
                    return 1;
                }
                case "-N": {
                    try {
                        Path pp = evalPath(arg, context);
                        BasicFileAttributeView view = Files.getFileAttributeView(pp, BasicFileAttributeView.class);
                        BasicFileAttributes attributes = view.readAttributes();
                        // calculate time of modification and creation.
                        FileTime lastAccessTime = attributes.lastAccessTime();
                        FileTime lastModifedTime = attributes.lastModifiedTime();
//                            FileTime createTime = attributes.creationTime();
                        return (lastModifedTime.compareTo(lastAccessTime) >= 0) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-O": {
                    try {
                        Path pp = evalPath(arg, context);
                        UserPrincipal up = Files.getOwner(pp);
                        return (up.getName().equals(System.getProperty("user.name"))) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-r": {
                    EvalArg a = (EvalArg) arg;
                    String path = a.arg.getString();
                    try {
                        Path pp = evalPath(arg, context);
                        return Files.exists(pp) && Files.isReadable(pp) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-w": {
                    try {
                        Path pp = evalPath(arg, context);
                        return Files.exists(pp) && Files.isWritable(pp) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-x": {
                    try {
                        Path pp = evalPath(arg, context);
                        return Files.exists(pp) && Files.isExecutable(pp) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-s": {
                    try {
                        Path pp = evalPath(arg, context);
                        return Files.isRegularFile(pp) && Files.size(pp) > 0 ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
            }
            return 1;
        }

    }

    private static Path evalPath(Eval a, JShellExecutionContext context) {
        return Paths.get(evalStr(a, context));
    }

    private static String evalStr(Eval a, JShellExecutionContext context) {
        if (a instanceof EvalArg) {
            return ((EvalArg) a).arg.getString();
        }
        return String.valueOf(a.eval(context));
    }

    private static int evalInt(Eval a, JShellExecutionContext context) {
        if (a instanceof EvalArg) {
            return ((EvalArg) a).arg.getInt();
        }
        return a.eval(context);
    }

    private static class EvalBinary extends EvalBase {

        protected Eval arg1;
        protected Eval arg2;

        public EvalBinary(String type, Eval arg1, Eval arg2) {
            super(type);
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Override
        public int eval(JShellExecutionContext context) {
            switch (type) {
                case "=": {
                    String s1 = evalStr(arg1, context);
                    String s2 = evalStr(arg2, context);
                    return s1.equals(s2) ? 0 : 1;
                }
                case "!=": {
                    String s1 = evalStr(arg1, context);
                    String s2 = evalStr(arg2, context);
                    return (!s1.equals(s2)) ? 0 : 1;
                }
                case "-a": {
                    int i1 = evalInt(arg1, context);
                    if (i1 == 1) {
                        return 1;
                    }
                    int i2 = evalInt(arg2, context);
                    return (i1 == 0 && i2 == 0) ? 0 : 1;
                }
                case "-o": {
                    int i1 = evalInt(arg1, context);
                    if (i1 == 0) {
                        return 0;
                    }
                    int i2 = evalInt(arg2, context);
                    return (i1 == 0 && i2 == 0) ? 0 : 1;
                }
                case "-eq": {
                    int s1 = evalInt(arg1, context);
                    int s2 = evalInt(arg2, context);
                    return Integer.compare(s1, s2) == 0 ? 0 : 1;
                }
                case "-gt": {
                    int s1 = evalInt(arg1, context);
                    int s2 = evalInt(arg2, context);
                    return Integer.compare(s1, s2) > 0 ? 0 : 1;
                }
                case "-ge": {
                    int s1 = evalInt(arg1, context);
                    int s2 = evalInt(arg2, context);
                    return Integer.compare(s1, s2) >= 0 ? 0 : 1;
                }
                case "-lt": {
                    int s1 = evalInt(arg1, context);
                    int s2 = evalInt(arg2, context);
                    return Integer.compare(s1, s2) < 0 ? 0 : 1;
                }
                case "-le": {
                    int s1 = evalInt(arg1, context);
                    int s2 = evalInt(arg2, context);
                    return Integer.compare(s1, s2) <= 0 ? 0 : 1;
                }
                case "-ne": {
                    int s1 = evalInt(arg1, context);
                    int s2 = evalInt(arg2, context);
                    return Integer.compare(s1, s2) <= 0 ? 0 : 1;
                }
                case "-ef": {
                    Path s1 = evalPath(arg1, context);
                    Path s2 = evalPath(arg2, context);
                    try {
                        Object at1 = Files.getFileAttributeView(s1, BasicFileAttributeView.class).readAttributes().fileKey();
                        Object at2 = Files.getFileAttributeView(s2, BasicFileAttributeView.class).readAttributes().fileKey();
                        return (at1 != null && at1.equals(at2)) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-nt": {
                    Path s1 = evalPath(arg1, context);
                    Path s2 = evalPath(arg2, context);
                    try {
                        FileTime at1 = Files.getFileAttributeView(s1, BasicFileAttributeView.class).readAttributes().lastModifiedTime();
                        FileTime at2 = Files.getFileAttributeView(s2, BasicFileAttributeView.class).readAttributes().lastModifiedTime();
                        return (at1.compareTo(at2) > 0) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-ot": {
                    Path s1 = evalPath(arg1, context);
                    Path s2 = evalPath(arg2, context);
                    try {
                        FileTime at1 = Files.getFileAttributeView(s1, BasicFileAttributeView.class).readAttributes().lastModifiedTime();
                        FileTime at2 = Files.getFileAttributeView(s2, BasicFileAttributeView.class).readAttributes().lastModifiedTime();
                        return (at1.compareTo(at2) < 0) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
            }
            return 1;
        }

    }

}
