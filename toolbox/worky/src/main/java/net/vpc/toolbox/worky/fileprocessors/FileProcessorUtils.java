package net.vpc.toolbox.worky.fileprocessors;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import net.vpc.common.textsource.JTextSource;
import net.vpc.common.textsource.JTextSourceFactory;
import net.vpc.common.textsource.JTextSourcePositionTracker;
import net.vpc.common.textsource.log.JTextSourceLog;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprEvaluator;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNode;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNodeParser;

public class FileProcessorUtils {

    public static JTextSource createSource(String uri, String workingDir) {
        return JTextSourceFactory.fromURI(toAbsolute(uri, workingDir));
    }

    public static String processSource(JTextSource source, String workingDir, ExprEvaluator exprEvaluator, JTextSourceLog messages) {
        StringBuilder sb0 = new StringBuilder();
        JTextSourcePositionTracker tracker = new JTextSourcePositionTracker();
        char[] charArray = source.charArray();
        boolean dollarAtLineStart = false;
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];
            switch (c) {
                case '\\': {
                    if (i + 1 < charArray.length && charArray[i + 1] == '$') {
                        sb0.append('$');
                        tracker.onReadString("\\$");
                        i++;
                    } else {
                        tracker.onReadChar(c);
                        sb0.append(c);
                    }
                    break;
                }
                case '$': {
                    if (i + 1 < charArray.length && charArray[i + 1] == '{') {
                        dollarAtLineStart = (i == 0 || charArray[i - 1] == '\n');
                        StringBuilder sb2 = new StringBuilder();
                        StringBuilder sb20 = new StringBuilder();
                        sb20.append("${");
                        i += 2;
                        int offset = i;
                        boolean end = false;
                        while (!end && i < charArrayLength) {
                            c = charArray[i];
                            switch (c) {
                                case '\\': {
                                    if (i + 1 < charArray.length && charArray[i + 1] == '}') {
                                        sb0.append('}');
                                        i++;
                                    } else {
                                        sb0.append(c);
                                    }
                                    i++;
                                    break;
                                }
                                case '}': {
                                    if (dollarAtLineStart && i + 2 < charArray.length && charArray[i + 1] == '\r' && charArray[i + 2] == '\n') {
                                        sb20.append("}\r\n");
                                        i += 2;
                                    } else if (dollarAtLineStart && i + 1 < charArray.length && charArray[i + 1] == '\n') {
                                        sb20.append("}\n");
                                        i++;
                                    } else {
                                        sb20.append("}");
                                    }
                                    end = true;
                                    break;
                                }
                                default: {
                                    sb2.append(c);
                                    sb20.append(c);
                                    i++;
                                }
                            }
                        }
                        sb0.append(FileProcessorUtils.processExpr(sb2.toString(), workingDir, exprEvaluator, messages));
                        tracker.onReadString(sb20.toString());
                    } else {
                        sb0.append(c);
                    }
                    break;
                }
                default: {
                    sb0.append(c);
                }
            }
        }
        return sb0.toString();
    }

    public static String processExpr(String content, String workingDir, ExprEvaluator exprEvaluator, JTextSourceLog messages) {
        content = content.trim();
        ExprNode p = new ExprNodeParser(content, messages, workingDir).parseDocument();
        if (p != null) {
            return String.valueOf(exprEvaluator.eval(p, messages, workingDir));
        }
        return "";
    }

    public static String extractWorkDir(String pathString, String defaultWorkdir) {
        if (pathString.startsWith("http://")
                || pathString.startsWith("https://")) {
            return ".";
        }
        if (pathString.startsWith("file://")) {
            try {
                File ff = Paths.get(new URL(pathString).toURI()).toFile();
                File pf = ff.getParentFile();
                if (pf != null) {
                    return pf.getAbsolutePath();
                }
                return defaultWorkdir;
            } catch (Exception e) {
                //
            }
            return defaultWorkdir;
        }
        File f = null;
        try {
            File d0 = new File(pathString);
            if (d0.isAbsolute()) {
                f = d0.getCanonicalFile().getParentFile();
            } else {
                f = new File(defaultWorkdir + File.separator + pathString).getCanonicalFile();
            }
        } catch (IOException ex) {
            //
        }
        if (f == null) {
            return defaultWorkdir;
        }
        return f.getPath();
    }

    public static String toAbsolute(String pathString, String workingDir) {
        if (!new File(pathString).isAbsolute()) {
            pathString = workingDir + File.separator + pathString;
        }
        return pathString;
    }
}
