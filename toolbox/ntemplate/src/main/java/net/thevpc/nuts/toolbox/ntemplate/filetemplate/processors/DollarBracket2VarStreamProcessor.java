package net.thevpc.nuts.toolbox.ntemplate.filetemplate.processors;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.MimeTypeConstants;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.StreamProcessor;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.FileProcessorUtils;

import java.io.*;

public class DollarBracket2VarStreamProcessor implements StreamProcessor {
    public static final DollarBracket2VarStreamProcessor INSTANCE = new DollarBracket2VarStreamProcessor();

    public DollarBracket2VarStreamProcessor() {
    }

    @Override
    public void processStream(InputStream source, OutputStream target, FileTemplater context) {
        try {
            Writer out = new OutputStreamWriter(target);
            char[] charArray = FileProcessorUtils.loadString(source, null).toCharArray();
            boolean dollarAtLineStart = false;
            int brackets = 0;
            for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
                char c = charArray[i];
                switch (c) {
                    case '\\': {
                        if (i + 3 < charArray.length && charArray[i + 1] == '$' && charArray[i + 2] == '{' && charArray[i + 3] == '{') {
                            out.append('$');
                            i++;
                        } else {
                            out.append(c);
                        }
                        break;
                    }
                    case '$': {
                        if (i + 2 < charArray.length && charArray[i + 1] == '{' && charArray[i + 2] == '{') {
                            dollarAtLineStart = (i == 0 || charArray[i - 1] == '\n');
                            StringBuilder sb2 = new StringBuilder();
                            StringBuilder sb20 = new StringBuilder();
                            sb20.append("${{");
                            i += 3;
                            int offset = i;
                            boolean end = false;
                            while (!end && i < charArrayLength) {
                                c = charArray[i];
                                switch (c) {
//                                    case '\\': {
//                                        if (i + 1 < charArray.length && charArray[i + 1] == '}') {
//                                            out.append('}');
//                                            i++;
//                                        } else {
//                                            out.append(c);
//                                        }
//                                        i++;
//                                        break;
//                                    }
                                    case '/': {
                                        if (i + 1 < charArray.length && charArray[i + 1] == '/') {
                                            c = charArray[i];
                                            sb2.append(c);
                                            i++;
                                            sb2.append(charArray[i]);
                                            while (i < charArray.length && charArray[i] != '\n') {
                                                sb2.append(charArray[i]);
                                                i++;
                                            }
                                        } else {
                                            sb2.append(c);
                                        }
                                        break;
                                    }
                                    case '"':
                                    case '\'':
                                    case '`': {
                                        char s = c;
                                        sb2.append(c);
                                        i++;
                                        while (i < charArray.length) {
                                            c = charArray[i];
                                            if (c == s) {
                                                sb2.append(c);
                                                i++;
                                                break;
                                            } else if (c == '\\') {
                                                sb2.append(c);
                                                i++;
                                                if (i < charArray.length) {
                                                    c = charArray[i];
                                                    sb2.append(c);
                                                }
                                            } else {
                                                sb2.append(c);
                                            }
                                            i++;
                                        }
                                        break;
                                    }
                                    case '{': {
                                        sb2.append(c);
                                        brackets++;
                                        i++;
                                        break;
                                    }
                                    case '}': {
                                        if (brackets <= 0 && i + 1 < charArray.length && charArray[i + 1] == '}') {
                                            if (dollarAtLineStart && i + 3 < charArray.length && charArray[i + 2] == '\r' && charArray[i + 3] == '\n') {
                                                sb20.append("}}\r\n");
                                                i += 3-1;
                                            } else if (dollarAtLineStart && i + 2 < charArray.length && charArray[i + 2] == '\n') {
                                                sb20.append("}}\n");
                                                i += 3-1;
                                            } else {
                                                sb20.append("}}");
                                                i += 2-1;
                                            }
                                            end = true;
                                        } else {
                                            i++;
                                            brackets--;
                                            sb2.append(c);
                                            sb20.append(c);
                                        }
                                        break;
                                    }
                                    default: {
                                        sb2.append(c);
                                        sb20.append(c);
                                        i++;
                                    }
                                }
                            }
                            String v = evaluateDollarValue(sb2.toString(), context);
                            out.append(String.valueOf(v));
                        } else {
                            out.append(c);
                        }
                        break;
                    }
                    default: {
                        out.append(c);
                    }
                }
            }
            out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected String evaluateDollarValue(String str, FileTemplater context) {
        return context.executeStream(new ByteArrayInputStream(str.getBytes()), MimeTypeConstants.FTEX);
    }

    @Override
    public String toString() {
        return "Replace(${{...}})";
    }

}
