/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.asciidoctor;

import net.thevpc.nuts.lib.md.*;

import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class AsciiDoctorWriter extends AbstractMdWriter {


    public AsciiDoctorWriter(Writer out) {
        super(out);
    }

    @Override
    public void write(MdDocument document) {
        String[] headers=buildHeader(document);
        for (String header : headers) {
            writeln(header);
        }
        write(document.getContent());
    }

    private String[] buildHeader(MdDocument document) {
        Map<String,String> m=new LinkedHashMap<>();
        if(document.getVersion()!=null){
            m.put("revnumber",document.getVersion());
        }
        if(document.getDate()!=null){
            Temporal d = document.getDate();
            if(d instanceof LocalDate) {
                LocalDate ld = (LocalDate) d;
                m.put("revdate", ld.format(DateTimeFormatter.BASIC_ISO_DATE));
            }else {
                m.put("revdate",d.toString());
            }
        }
        if(document.getSubTitle()!=null && document.getSubTitle().length()>0){
            m.put("revremark", document.getSubTitle());
        }

        List<String> extra=new ArrayList<>();
        if(document.getProperties().get("headers") instanceof String[]){
            String[] s=(String[]) document.getProperties().get("headers");
            for (String s1 : s) {
                String k=null;
                String v=null;
                if(s1.startsWith(":")){
                    int i=s1.indexOf(':',1);
                    if(i>0){
                        k=s1.substring(1,i);
                        v=s1.substring(i+1).trim();
                    }
                }
                if(k!=null){
                    m.put(k,v);
                }else{
                    extra.add(s1);
                }
            }
        }else if(document.getProperties().get("headers") instanceof Map){
            Map<String,String> s=(Map<String,String>) document.getProperties().get("headers");
            m.putAll(s);
        }
        List<String> all=new ArrayList<>();
        for (Map.Entry<String, String> e : m.entrySet()) {
            all.add(":"+e.getKey()+":"+(e.getValue().length()>0?(" "+e.getValue()):""));
        }
        all.addAll(extra);
        return all.toArray(new String[0]);
    }

    public void writeInline(MdElement element) {
        switch (element.getElementType()) {
            case TEXT: {
                write(element.asText().getText());
                return;
            }
            case BOLD: {
                write("**");
                writeInline(element.asBold().getContent());
                write("**");
                return;
            }
            case ITALIC: {
                write("__");
                writeInline(element.asBold().getContent());
                write("__");
                return;
            }
            case CODE: {
                write(" `");
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
                        default:{
                            sb.append(c);
                            break;
                        }
                    }
                }
                write(sb.toString());
                write("` ");
                return;
            }

            case LINK: {
                write("link:");
                write(element.asLink().getLinkUrl());
                write("[");
                write(element.asLink().getLinkTitle());
                write("]");
                return;
            }
            case IMAGE: {
                write("image:");
                write(element.asImage().getImageUrl());
                write("[");
                write(element.asImage().getImageTitle());
                write("]");
                return;
            }
            case SEQ: {
                MdSequence t = element.asSeq();
                for (MdElement mdElement : t.getElements()) {
                    writeInline(mdElement);
                }
                return;
            }
        }
        throw new IllegalArgumentException("Unable to inline " + element.getElementType());
    }

    public void writeImpl(MdElement node) {
        switch (node.getElementType()) {
            case TITLE1: {
                MdTitle t = (MdTitle) node;
                writeln();
                writeln("= " + t.getValue());
                break;
            }
            case TITLE2: {
                MdTitle t = (MdTitle) node;
                writeln();
                writeln("== " + t.getValue());
                break;
            }
            case TITLE3: {
                MdTitle t = (MdTitle) node;
                writeln();
                writeln("=== " + t.getValue());
                break;
            }
            case TITLE4: {
                MdTitle t = (MdTitle) node;
                writeln();
                writeln("==== " + t.getValue());
                break;
            }
            case TITLE5: {
                MdTitle t = (MdTitle) node;
                writeln();
                writeln("===== " + t.getValue());
                break;
            }
            case TITLE6: {
                MdTitle t = (MdTitle) node;
                writeln();
                writeln("====== " + t.getValue());
                break;
            }
            case UNNUMBRED_ITEM1: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                write("* ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM2: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                write("** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM3: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                write("*** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM4: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                write("**** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM5: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                write("***** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM6: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                write("****** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM1: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                write(". ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM2: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                write(".. ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM3: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                write("... ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM4: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                write(".... ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM5: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                write("..... ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM6: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                write("...... ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case LINE_SEPARATOR: {
                writeln();
                writeln();
                writeln("'''");
                writeln();
                break;
            }

            case ADMONITION: {
                MdAdmonition t = (MdAdmonition) node;
                writeln();
                write(t.getType().toString() + ": ");
                write(t.getContent());
                writeln();
                break;
            }
            case SEQ: {
                MdSequence t = (MdSequence) node;
                if (t.isInline()) {
                    for (MdElement mdElement : t.getElements()) {
                        writeInline(mdElement);
                    }
                } else {
                    for (MdElement mdElement : t.getElements()) {
                        write(mdElement);
                    }
                }
                break;
            }
            case CODE: {
                MdCode c = (MdCode) node;
                if (c.isInline()) {
                    writeInline(c);
                } else {
                    writeln();
                    writeln("[source," + convertLanguage(c.getLanguage()) + "]");
                    writeln("----");
                    writeln(c.getValue());
                    writeln("----");
                }
                break;
            }
            case TEXT: {
                MdText c = (MdText) node;
                writeln(c.getText());
                break;
            }
            case TABLE: {
                MdTable tab = (MdTable) node;
                writeln();
                writeln("|===");
                for (MdColumn cell : tab.getColumns()) {
                    write("|");
                    writeInline(cell.getName());
                    write(" ");
                }
                writeln();
                for (MdRow row : tab.getRows()) {
                    writeln();
                    for (MdElement cell : row.getCells()) {
                        write("|");
                        writeInline(cell);
                        write(" ");
                    }
                    writeln();
                }
                writeln("|===");

                break;
            }
        }
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

}
