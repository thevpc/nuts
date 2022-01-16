/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.nio.file.Files;
import java.time.Instant;
import java.util.Stack;

/**
 * Created by vpc on 1/7/17.
 */
public class TestCommand extends SimpleJShellBuiltin {

    public TestCommand() {
        super("test", DEFAULT_SUPPORT, Options.class);
    }

    private static NutsPath evalPath(Eval a, JShellExecutionContext context) {
        return NutsPath.of(evalStr(a, context),context.getSession());
    }

    private static String evalStr(Eval a, JShellExecutionContext context) {
        if (a instanceof EvalArg) {
            return ((EvalArg) a).arg.getString();
        }
        return String.valueOf(a.eval(context));
    }

    private static int evalInt(Eval a, JShellExecutionContext context) {
        if (a instanceof EvalArg) {
            return ((EvalArg) a).arg.toElement().getInt();
        }
        return a.eval(context);
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
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        commandLine.setExpandSimpleOptions(false);
        Options options=context.getOptions();
        NutsArgument a = commandLine.next();
        switch (a.getString()) {
            case "(": {
                options.operators.add(a.getString());
                return true;
            }
            case ")": {
                reduce(options.operators, options.operands, 0);
                if (options.operators.size() < 1 || !options.operators.peek().equals("(")) {
                    throw new IllegalArgumentException("')' has no equivalent '('");
                }
                options.operators.pop();
                return true;
            }
            case "]": {
                reduce(options.operators, options.operands, 0);
                if (options.operators.size() > 0 && !options.operators.peek().equals("[")) {
                    throw new IllegalArgumentException("']' has no equivalent '['");
                }
                if (options.operators.size() == 1 && options.operators.peek().equals("[")) {
                    options.operators.pop();
                }
                return true;
            }
            default: {
                if (getArgsCount(a.getString()) > 0) {
                    reduce(options.operators, options.operands, getArgsPrio(a.getString()));
                    options.operators.add(a.getString());
                } else {
                    options.operands.add(new EvalArg(a));
                }
                return true;
            }
        }
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        Options options=context.getOptions();
        if(options.operands.isEmpty()){
            throwExecutionException("result",1,session);
        }
        reduce(options.operators, options.operands, 0);
        if (options.operands.size() != 1) {
            throw new IllegalArgumentException("missing operand");
        }
        if (!options.operators.isEmpty()) {
            throw new IllegalArgumentException("too many operators");
        }
        int result = options.operands.pop().eval(context);
        if (result != 0) {
            throwExecutionException("result",result,session);
        }
    }

    public interface Eval {

        String getType();

        int eval(JShellExecutionContext context);
    }

    private static class Options {
        Stack<String> operators = new Stack<>();
        Stack<Eval> operands = new Stack<>();

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
                    NutsPath pp = evalPath(arg, context);
                    return 1;
                }
                case "-c": {
                    NutsPath pp = evalPath(arg, context);
                    return 1;
                }
                case "-d": {
                    NutsPath pp = evalPath(arg, context);
                    return pp.isDirectory() ? 0 : 1;
                }
                case "-f": {
                    NutsPath pp = evalPath(arg, context);
                    return pp.isRegularFile() ? 0 : 1;
                }
                case "-e": {
                    NutsPath pp = evalPath(arg, context);
                    return pp.exists() ? 0 : 1;
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
                        NutsPath pp = evalPath(arg, context);
                        //FILE exists and is a socket
                        return 1;//Files.exists(Paths.get(path)) ? 0 : 1;
                    }
                    return 1;
                }
                case "-N": {
                    try {
                        NutsPath pp = evalPath(arg, context);
                        Instant lastAccessTime = pp.getLastAccessInstant();
                        Instant lastModifedTime = pp.getLastModifiedInstant();
//                            FileTime createTime = attributes.creationTime();
                        return lastModifedTime!=null && lastAccessTime!=null && (lastModifedTime.compareTo(lastAccessTime) >= 0) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-O": {
                    try {
                        NutsPath pp = evalPath(arg, context);
                        String up = pp.owner();
                        return (up!=null && up.equals(System.getProperty("user.name"))) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-r": {
                    EvalArg a = (EvalArg) arg;
                    String path = a.arg.getString();
                    try {
                        NutsPath pp = evalPath(arg, context);
                        return pp.exists() && pp.getPermissions().contains(NutsPathPermission.CAN_READ) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-w": {
                    try {
                        NutsPath pp = evalPath(arg, context);
                        return pp.exists() && pp.getPermissions().contains(NutsPathPermission.CAN_WRITE) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-x": {
                    try {
                        NutsPath pp = evalPath(arg, context);
                        return pp.exists() && pp.getPermissions().contains(NutsPathPermission.CAN_EXECUTE) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-s": {
                    try {
                        NutsPath pp = evalPath(arg, context);
                        return pp.isRegularFile() && pp.getContentLength() > 0 ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
            }
            return 1;
        }

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
                    NutsPath s1 = evalPath(arg1, context);
                    NutsPath s2 = evalPath(arg2, context);
                    try {
//                        Object at1 = Files.getFileAttributeView(s1, BasicFileAttributeView.class).readAttributes().fileKey();
//                        Object at2 = Files.getFileAttributeView(s2, BasicFileAttributeView.class).readAttributes().fileKey();
                        return (s1.normalize().toAbsolute().equals(s2.normalize().toAbsolute())) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-nt": {
                    NutsPath s1 = evalPath(arg1, context);
                    NutsPath s2 = evalPath(arg2, context);
                    try {
                        Instant at1 = s1.getLastModifiedInstant();
                        Instant at2 = s2.getLastModifiedInstant();
                        return (at1!=null && at1.compareTo(at2) > 0) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
                case "-ot": {
                    NutsPath s1 = evalPath(arg1, context);
                    NutsPath s2 = evalPath(arg2, context);
                    try {
                        Instant at1 = s1.getLastModifiedInstant();
                        Instant at2 = s2.getLastModifiedInstant();
                        return (at1!=null && at1.compareTo(at2) < 0) ? 0 : 1;
                    } catch (Exception ex) {
                        return 1;
                    }
                }
            }
            return 1;
        }

    }

}
