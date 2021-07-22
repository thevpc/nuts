/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.asciidoctor;

import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.util.MdUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class AsciiDoctorWriter extends AbstractMdWriter {


    public AsciiDoctorWriter(OutputStream out) {
        super(new PrintWriter(out));
    }

    public AsciiDoctorWriter(Writer out) {
        super(out);
    }

    private static String convertLanguage(String c) {
        switch (c) {
            case "ruby":
            case "rb": {
                return "ruby";
            }
            case "py":
            case "python": {
                return "python";
            }
            case "js":
            case "javascript": {
                return "javascript";
            }
            case "cs":
            case "c#":
            case "csharp": {
                return "csharp";
            }
        }
        return c;
    }

    @Override
    public void write(MdDocument document) {
        String[] headers = buildHeader(document);
        for (String header : headers) {
            writeln(header);
        }
        write(document.getContent());
    }

    private String[] buildHeader(MdDocument document) {
        Map<String, String> m = new LinkedHashMap<>();
        if (document.getVersion() != null) {
            m.put("revnumber", document.getVersion());
        }
        if (document.getDate() != null) {
            Temporal d = document.getDate();
            if (d instanceof LocalDate) {
                LocalDate ld = (LocalDate) d;
                m.put("revdate", ld.format(DateTimeFormatter.BASIC_ISO_DATE));
            } else {
                m.put("revdate", d.toString());
            }
        }
        if (document.getSubTitle() != null && document.getSubTitle().length() > 0) {
            m.put("revremark", document.getSubTitle());
        }

        List<String> extra = new ArrayList<>();
        if (document.getProperties().get("headers") instanceof String[]) {
            String[] s = (String[]) document.getProperties().get("headers");
            for (String s1 : s) {
                String k = null;
                String v = null;
                if (s1.startsWith(":")) {
                    int i = s1.indexOf(':', 1);
                    if (i > 0) {
                        k = s1.substring(1, i);
                        v = s1.substring(i + 1).trim();
                    }
                }
                if (k != null) {
                    m.put(k, v);
                } else {
                    extra.add(s1);
                }
            }
        } else if (document.getProperties().get("headers") instanceof Map) {
            Map<String, String> s = (Map<String, String>) document.getProperties().get("headers");
            m.putAll(s);
        }
        List<String> all = new ArrayList<>();
        for (Map.Entry<String, String> e : m.entrySet()) {
            all.add(":" + e.getKey() + ":" + (e.getValue().length() > 0 ? (" " + e.getValue()) : ""));
        }
        all.addAll(extra);
        return all.toArray(new String[0]);
    }

    public void writeImpl(MdElement element, WriteContext context) {
        switch (element.type().group()) {
            case TEXT: {
                if(context.isEndWithNewline()){
                    writeln();
                }
                write(element.asText().getText());
                if (!element.isInline()) {
                    writeln();
                }
                context.setLast(element);
                break;
            }
            case BOLD: {
                if(context.isEndWithNewline()){
                    writeln();
                }
                write("**");
                context.setLast(null);
                writeImpl(element.asBold().getContent(), context);
                write("**");
                context.setLast(element);
                break;
            }
            case ITALIC: {
                if(context.isEndWithNewline()){
                    writeln();
                }
                write("__");
                context.setLast(null);
                writeImpl(element.asItalic().getContent(), context);
                write("__");
                context.setLast(element);
                break;
            }

            case LINK: {
                if(context.isEndWithNewline()){
                    writeln();
                }
                write("link:");
                write(element.asLink().getLinkUrl());
                write("[");
                write(element.asLink().getLinkTitle());
                write("]");
                context.setLast(element);
                break;
            }
            case CODE_LINK: {
                if(context.isEndWithNewline()){
                    writeln();
                }
                write("`");
                write(element.asCodeLink().getLinkCode());
                write("`");
                context.setLast(element);
                break;
            }
            case IMAGE: {
                if(context.isEndWithNewline()){
                    writeln();
                }
                write("image:");
                write(element.asImage().getImageUrl());
                write("[");
                write(element.asImage().getImageTitle());
                write("]");
                context.setLast(element);
                break;
            }
            case XML: {
                //just ignore...
                //context.setLast(element);
                break;
            }
            //inline
            case TITLE: {
                if(context.hasLast()){
                    writeln();
                }
                MdTitle t = (MdTitle) element;
                writeln(MdUtils.times('=',element.type().depth()) + " " + t.getValue());
                context.setLast(element);
                for (MdElement child : t.getChildren()) {
                    writeImpl(child,context);
                }
                break;
            }
            case UNNUMBERED_ITEM: {
                if(context.hasLast()){
                    writeln();
                }
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                write(MdUtils.times('*',element.type().depth()) + " ");
                context.setLast(null);
                writeImpl(t.getValue(), context);
                if(t.getValue().isInline()){
                    writeln();
                }
                for (MdElement child : t.getChildren()) {
                    writeImpl(child, context);
                }
                break;
            }
            case NUMBERED_ITEM: {
                if(context.hasLast()){
                    writeln();
                }
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                write(MdUtils.times(".1",element.type().depth()) + " ");
                context.setLast(null);
                writeImpl(t.getValue(), context);
                if(t.getValue().isInline()){
                    writeln();
                }
                context.setLast(element);
                for (MdElement child : t.getChildren()) {
                    writeImpl(child, context);
                }
                break;
            }
            case HORIZONTAL_RULE: {
                if(context.hasLast()){
                    writeln();
                }
                writeln("'''");
                context.setLast(element);
                break;
            }

            case ADMONITION: {
                if(context.hasLast()){
                    writeln();
                }
                MdAdmonition t = (MdAdmonition) element;
                writeln();
                write(t.getType().toString() + ": ");
                writeImpl(t.getContent(), context);
                writeln();
                context.setLast(element);
                break;
            }
            case BODY:{
                if(context.hasLast()){
                    writeln();
                }
                MdBody t = (MdBody) element;
                for (MdElement mdElement : t.getChildren()) {
                    writeImpl(mdElement, context);
                }
                break;
            }

            case PHRASE: {
                MdPhrase t = (MdPhrase) element;
                for (MdElement mdElement : t.getChildren()) {
                    writeImpl(mdElement, context);
                }
                break;
            }
            case NUMBERED_LIST:
            case UNNUMBERED_LIST: {
                MdParent t = (MdParent) element;
                for (MdElement mdElement : t.getChildren()) {
                    writeImpl(mdElement, context);
                }
                break;
            }
            case CODE: {
                if (element.isInline()) {
                    if(context.isEndWithNewline()){
                        writeln();
                    }
                    write("`");
                    String r = element.asCode().getValue();
                    StringBuilder sb = new StringBuilder();
                    for (char c : r.toCharArray()) {
                        switch (c) {
                            case '`': {
                                sb.append("\\`");
                                break;
                            }
//                        case '_': {
//                            sb.append("\\_");
//                            break;
//                        }
                            case '\\': {
                                sb.append("\\\\");
                                break;
                            }
                            default: {
                                sb.append(c);
                                break;
                            }
                        }
                    }
                    write(sb.toString());
                    write("`");
                } else {
                    if(context.hasLast()){
                        writeln();
                    }
                    MdCode c = (MdCode) element;
                    writeln();
                    String lng = convertLanguage(c.getLanguage());
                    if (lng.isEmpty()) {
                        writeln("[source]");
                    } else {
                        writeln("[source," + lng + "]");
                    }
                    writeln("----");
                    writeln(c.getValue());
                    writeln("----");
                }
                context.setLast(element);
                break;
            }

            case TABLE: {
                if(context.hasLast()){
                    writeln();
                }
                MdTable tab = (MdTable) element;
                writeln();
                writeln("|===");
                for (MdColumn cell : tab.getColumns()) {
                    write("|");
                    writeImpl(cell.getName(), context);
                    write(" ");
                }
                writeln();
                for (MdRow row : tab.getRows()) {
                    writeln();
                    for (MdElement cell : row.getCells()) {
                        write("|");
                        writeImpl(cell, context);
                        write(" ");
                    }
                    writeln();
                }
                writeln("|===");
                context.setLast(element);
                break;
            }
            case LINE_BREAK: {
                //writeln();
                writeln();
                context.setLast(element);
                break;
            }
            case ROW:
            case COLUMN:
            default: {
                throw new IllegalArgumentException("unsupported " + element.type().group());
            }
        }
    }

}
