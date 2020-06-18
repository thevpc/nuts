package net.vpc.toolbox.worky.fileprocessors;

import net.vpc.common.textsource.JTextSource;
import net.vpc.common.textsource.JTextSourcePositionTracker;
import net.vpc.common.textsource.log.JTextSourceLog;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprEvaluator;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNode;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNodeParser;

public class FileProcessorUtils {
    public static String processSource(JTextSource source, ExprEvaluator exprEvaluator,JTextSourceLog messages) {
        StringBuilder sb0 = new StringBuilder();
        JTextSourcePositionTracker tracker = new JTextSourcePositionTracker();
        char[] charArray = source.charArray();
        boolean dollarAtLineStart=false;
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
                        dollarAtLineStart=(i==0 || charArray[i-1]=='\n');
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
                        sb0.append(FileProcessorUtils.processExpr(sb2.toString(), exprEvaluator, messages));
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

    public static String processExpr(String content, ExprEvaluator exprEvaluator, JTextSourceLog messages) {
        content = content.trim();
        ExprNode p=new ExprNodeParser(content,messages).parseDocument();
        if(p!=null){
            return String.valueOf(exprEvaluator.eval(p,messages));
        }
        return "";
    }

}
