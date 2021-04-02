/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import net.thevpc.common.swing.UndoRedoHelper;
import net.thevpc.jeep.editor.JSyntaxDocument;

/**
 *
 * @author vpc
 */
public class OtherUtils {

    public static void installUndoRedoManager(JTextComponent c) {
        Document d = c.getDocument();
        if (d instanceof JSyntaxDocument) {
            UndoManager v = ((JSyntaxDocument) d).getUndoManager();
            UndoRedoHelper.installUndoRedoManager(c, v);
        } else {
            UndoRedoHelper.installUndoRedoManager(c, new UndoManager());
        }
    }

    public static Font deriveFont(Font _font, boolean bold, boolean italic, boolean underline, boolean strike) {
        Font f = _font.deriveFont((bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0));
        Map attributes = null;
        if (underline) {
            if (attributes == null) {
                attributes = f.getAttributes();
            }
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        if (strike) {
            if (attributes == null) {
                attributes = f.getAttributes();
            }
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }
        if (attributes != null) {
            f = f.deriveFont(attributes);
        }
        return f;
    }

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

    public static Color parseColor(String s) {
        if (OtherUtils.isBlank(s)) {
            return null;
        }
        if (s.indexOf(",") > 0) {
            String r = null;
            String g = null;
            String b = null;
            String[] sp = s.split(",");
            if (sp.length == 3) {
                try {
                    return new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
                } catch (Exception ex) {
                }
            }
            return null;
        } else if (s.matches("#[a-fA-F0-9]{12}")) {
            try {
                return new Color(Integer.parseInt(s.substring(1, 5), 16),
                        Integer.parseInt(s.substring(5, 9), 16),
                        Integer.parseInt(s.substring(9, 13), 16)
                );
            } catch (Exception ex) {
                //
            }
        } else if (s.matches("[a-fA-F0-9]{12}")) {
            try {
                return new Color(Integer.parseInt(s.substring(0, 4), 16),
                        Integer.parseInt(s.substring(4, 8), 16),
                        Integer.parseInt(s.substring(8, 12), 16)
                );
            } catch (Exception ex) {
                //
            }
        }
        return null;
    }

    public static String toHex(int value, int pad) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(value));
        while (sb.length() < pad) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    public static String formatColor(Color s) {
        if (s == null) {
            return "";
        }
        return "#" + toHex(s.getRed(), 4) + toHex(s.getGreen(), 4) + toHex(s.getBlue(), 4);
    }

}
