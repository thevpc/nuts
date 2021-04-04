/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import net.thevpc.common.swing.UndoRedoHelper;
import net.thevpc.jeep.editor.JSyntaxDocument;

/**
 *
 * @author vpc
 */
public class OtherUtils {


    public static String trim(String in) {
        return in == null ? "" : in.trim();
    }

    public static boolean isBlank(String in) {
        return in == null || in.trim().isEmpty();
    }

    public static String toEscapedName(String in) {
        return toEscapedString(in, '`', false, "<no-name>");
    }

    public static String toEscapedValue(String in) {
        return toEscapedString(in, '\"', true, "null");
    }

    public static String toEscapedString(String in, char quoteType, boolean always, String nullValue) {
        if (in == null) {
            if (nullValue == null) {
                if (always && quoteType != '\0') {
                    return "null";
                }
                return "<null>";
            } else {
                return nullValue;
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean hasSpace = true;
        for (char c : in.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    break;
                }
                case '\\': {
                    sb.append("\\\\");
                    break;
                }
                case ' ': {
                    if (quoteType == '\0') {
                        sb.append("\\ ");
                    } else {
                        sb.append(" ");
                    }
                    hasSpace = true;
                    break;
                }
                case '`':
                case '\'':
                case '\"': {
                    if (quoteType == c) {
                        sb.append('\\').append(c);
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        if (hasSpace) {
            always = true;
        }
        if (always && quoteType != '\0') {
            sb.insert(0, quoteType);
            sb.append(quoteType);
        }
        return sb.toString();
    }

    public static byte[] toByteArray(InputStream in) {

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            // read bytes from the input stream and store them in buffer
            while ((len = in.read(buffer)) != -1) {
                // write bytes from the buffer into output stream
                os.write(buffer, 0, len);
            }

            return os.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static <T> boolean switchListValues(List<T> values, int index1, int index2) {
        if (values != null) {
            if (index1 >= 0 && index1 < values.size()) {
                if (index2 >= 0 && index2 < values.size()) {
                    T a = values.get(index1);
                    values.set(index1, values.get(index2));
                    values.set(index2, a);
                }
            }
        }
        return false;
    }

    public static String escapeHtml(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }


    public static String toHex(int value, int pad) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(value));
        while (sb.length() < pad) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }


}
