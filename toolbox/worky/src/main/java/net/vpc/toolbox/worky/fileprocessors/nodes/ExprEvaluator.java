package net.vpc.toolbox.worky.fileprocessors.nodes;

import java.io.File;
import net.vpc.common.textsource.JTextSource;
import net.vpc.common.textsource.JTextSourceFactory;
import net.vpc.common.textsource.log.JTextSourceLog;
import net.vpc.toolbox.worky.fileprocessors.FileProcessorUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ExprEvaluator {

    private static final char[] HEXARR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private Map<String, Object> context = new HashMap<>();

    public static String escapeString(String s) {
        StringBuilder outBuffer = new StringBuilder();

        for (char aChar : s.toCharArray()) {
            if (aChar == '\\') {
                outBuffer.append("\\\\");
            } else if (aChar == '"') {
                outBuffer.append("\\\"");
            } else if ((aChar > 61) && (aChar < 127)) {
                outBuffer.append(aChar);
            } else {
                switch (aChar) {
                    case '\t':
                        outBuffer.append("\\t");
                        break;
                    case '\n':
                        outBuffer.append("\\n");
                        break;
                    case '\r':
                        outBuffer.append("\\r");
                        break;
                    case '\f':
                        outBuffer.append("\\f");
                        break;
                    default:
                        if (((aChar < 0x0020) || (aChar > 0x007e))) {
                            outBuffer.append('\\');
                            outBuffer.append('u');
                            outBuffer.append(toHex((aChar >> 12) & 0xF));
                            outBuffer.append(toHex((aChar >> 8) & 0xF));
                            outBuffer.append(toHex((aChar >> 4) & 0xF));
                            outBuffer.append(toHex(aChar & 0xF));
                        } else {
                            outBuffer.append(aChar);
                        }
                }
            }
        }
        return outBuffer.toString();
    }

    private static char toHex(int nibble) {
        return HEXARR[(nibble & 0xF)];
    }

    public Object eval(ExprNode node, JTextSourceLog log, String workingDir) {
        switch (node.getClass().getSimpleName()) {
            case "ExprNodeLiteral": {
                return (((ExprNodeLiteral) node).getValue());
            }
            case "ExprNodeVar": {
                return context.get(((ExprNodeVar) node).getName());
            }
            case "ExprNodeFunction": {
                ExprNodeFunction f = ((ExprNodeFunction) node);
                switch (f.getName()) {
                    case ";": {
                        Object a = "";
                        for (ExprNode arg : f.getArgs()) {
                            a = eval(arg, log, workingDir);
                        }
                        return a;
                    }
                    case "set": {
                        ExprNode[] args = f.getArgs();
                        if (args.length != 2) {
                            throw new IllegalStateException(f.getName() + " : invalid arguments count");
                        }
                        if (!(args[0] instanceof ExprNodeVar)) {
                            throw new IllegalStateException(f.getName() + " : first argument should be a var name");
                        }
                        context.put(((ExprNodeVar) args[0]).getName(), eval(args[1], log, workingDir));
                        return "";
                    }
                    case "include": {
                        ExprNode[] args = f.getArgs();
                        if (args.length != 1) {
                            throw new IllegalStateException(f.getName() + " : invalid arguments count");
                        }
                        String pathString = (String) eval(args[0], log, workingDir);
                        JTextSource source = FileProcessorUtils.createSource(pathString, workingDir);
                        if (source == null) {
                            throw new IllegalStateException(f.getName() + " : file not found : " + pathString);
                        }
                        ExprNodeParser exprNodeParser = new ExprNodeParser(source.text(), log, FileProcessorUtils.extractWorkDir(pathString, workingDir));
                        ExprNode n = exprNodeParser.parseDocument();
                        if (n != null) {
                            return eval(n, log, exprNodeParser.getWorkingDir());
                        }
                        return "";
                    }
                    case "string": {
                        ExprNode[] args = f.getArgs();
                        if (args.length != 1) {
                            throw new IllegalStateException(f.getName() + " : invalid arguments count");
                        }
                        String str = (String) eval(args[0], log, workingDir);
                        return "\"" + escapeString(str) + "\"";
                    }
                    case "processString": {
                        ExprNode[] args = f.getArgs();
                        if (args.length != 1) {
                            throw new IllegalStateException(f.getName() + " : invalid arguments count");
                        }
                        String str = (String) eval(args[0], log, workingDir);
                        return FileProcessorUtils.processSource(
                                JTextSourceFactory.fromString(str, "<Text>"), workingDir,
                                this, log);
                    }
                    case "processFile": {
                        ExprNode[] args = f.getArgs();
                        if (args.length != 1) {
                            throw new IllegalStateException(f.getName() + " : invalid arguments count");
                        }
                        String str = (String) eval(args[0], log, workingDir);
                        JTextSource jTextSource = JTextSourceFactory.fromURI(FileProcessorUtils.toAbsolute(str, workingDir));
                        return FileProcessorUtils.processSource(jTextSource, FileProcessorUtils.extractWorkDir(str, workingDir), this, log);
                    }
                    case "loadFile": {
                        ExprNode[] args = f.getArgs();
                        if (args.length != 1) {
                            throw new IllegalStateException(f.getName() + " : invalid arguments count");
                        }
                        String str = (String) eval(args[0], log, workingDir);
                        JTextSource jTextSource = JTextSourceFactory.fromURI(FileProcessorUtils.toAbsolute(str, workingDir));
                        return jTextSource.text();
                    }
                    default: {
                        throw new IllegalStateException(f.getName() + " : invalid statement " + node);
                    }
                }
            }
            default: {
                throw new IllegalStateException("Invalid statement " + node);
            }
        }
    }

}
