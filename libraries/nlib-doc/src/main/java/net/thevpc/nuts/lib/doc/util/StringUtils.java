/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.util;

import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author thevpc
 */
public class StringUtils {

    public static String toLiteralString(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Number) {
            return String.valueOf(o);
        }
        if (o instanceof CharSequence) {
            return "\"" + StringUtils.escapeString(String.valueOf(o)) + "\"";
        }
        if (o instanceof File) {
            return "\"" + StringUtils.escapeString(String.valueOf(((File)o).getPath())) + "\"";
        }
        if (o instanceof Path) {
            return "\"" + StringUtils.escapeString(String.valueOf(((Path)o).toString())) + "\"";
        }
        if (o instanceof Character) {
            return "\'" + StringUtils.escapeString(String.valueOf(o)) + "\'";
        }
        return "<" + o.getClass().getSimpleName() + ">\"" + StringUtils.escapeString(String.valueOf(o)) + "\"";
    }

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
                            outBuffer.append(FileProcessorUtils.toHex((aChar >> 12) & 0xF));
                            outBuffer.append(FileProcessorUtils.toHex((aChar >> 8) & 0xF));
                            outBuffer.append(FileProcessorUtils.toHex((aChar >> 4) & 0xF));
                            outBuffer.append(FileProcessorUtils.toHex(aChar & 0xF));
                        } else {
                            outBuffer.append(aChar);
                        }
                }
            }
        }
        return outBuffer.toString();
    }

}
