/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipal;
import java.util.Stack;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;

/**
 * Created by vpc on 1/7/17.
 */
public class TestCommand extends AbstractNshCommand {

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
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommand commandLine = context.getWorkspace().parser().parseCommand(args)
                .setCommandName("test");
        if (commandLine.isEmpty()) {
            throw new NutsExecutionException(context.getWorkspace(), 1);
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
                            throw new IllegalArgumentException("')' has no euivalent '('");
                        }
                        operators.pop();
                        break;
                    }
                    case "]": {
                        reduce(operators, operands, 0);
                        if (operators.size() >0 && !operators.peek().equals("[")) {
                            throw new IllegalArgumentException("']' has no euivalent '['");
                        }
                        if (operators.size() ==1 && operators.peek().equals("[")) {
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
                throw new IllegalArgumentException("Missing operand");
            }
            if (!operators.isEmpty()) {
                throw new IllegalArgumentException("Too many operators");
            }
            return operands.pop().eval(context);
        } else {
            return 0;
        }
    }

    public static interface Eval {

        String getType();

        int eval(NutsCommandContext context);
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
        public int eval(NutsCommandContext context) {
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
        public int eval(NutsCommandContext context) {
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

    private static Path evalPath(Eval a, NutsCommandContext context) {
        return Paths.get(evalStr(a, context));
    }

    private static String evalStr(Eval a, NutsCommandContext context) {
        if (a instanceof EvalArg) {
            return ((EvalArg) a).arg.getString();
        }
        return String.valueOf(a.eval(context));
    }

    private static int evalInt(Eval a, NutsCommandContext context) {
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
        public int eval(NutsCommandContext context) {
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
                    if(i1==1){
                        return 1;
                    }
                    int i2 = evalInt(arg2, context);
                    return (i1==0 && i2==0) ? 0 : 1;
                }
                case "-o": {
                    int i1 = evalInt(arg1, context);
                    if(i1==0){
                        return 0;
                    }
                    int i2 = evalInt(arg2, context);
                    return (i1==0 && i2==0) ? 0 : 1;
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
