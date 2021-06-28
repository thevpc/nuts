/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.lib.md.*;

import java.io.Writer;

/**
 * @author thevpc
 */
public class DocusaurusWriter extends AbstractMdWriter {
    public DocusaurusWriter(Writer out) {
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
        writeln("---");
        Object id = document.getId();
        String title = document.getTitle();
        if(id==null && title==null){
            throw new IllegalArgumentException("missing id and title");
        }
        if(title==null){
            title=id.toString();
        }else if(id==null){
            id=title;
        }
        String sidebar_label=(String) document.getProperty("sidebar_label");
        if(sidebar_label==null){
            sidebar_label=title;
        }
        writeln("id: " + id);
        writeln("title: " + title);
        writeln("sidebar_label: " + sidebar_label);
        writeln("---");
        writeln();
        write(document.getContent());
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
                writeInline(element.asItalic().getContent());
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
                        default: {
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
            case CODE_LINK: {
                write("```");
                write(element.asCodeLink().getLinkCode());
                write("```");
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
            case LINE_BREAK: {
                writeln();
                return;
            }
            case HORIZONTAL_RULE: {
                writeln("--------");
                return;
            }
        }
        throw new IllegalArgumentException("Unable to inline " + element.getElementType());
    }

    public void writeImpl(MdElement element) {
        switch (element.getElementType()) {
            case TITLE1: {
                MdTitle t = (MdTitle) element;
                writeln();
                writeln("# " + t.getValue());
                break;
            }
            case TITLE2: {
                MdTitle t = (MdTitle) element;
                writeln();
                writeln("## " + t.getValue());
                break;
            }
            case TITLE3: {
                MdTitle t = (MdTitle) element;
                writeln();
                writeln("### " + t.getValue());
                break;
            }
            case TITLE4: {
                MdTitle t = (MdTitle) element;
                writeln();
                writeln("#### " + t.getValue());
                break;
            }
            case TITLE5: {
                MdTitle t = (MdTitle) element;
                writeln();
                writeln("##### " + t.getValue());
                break;
            }
            case TITLE6: {
                MdTitle t = (MdTitle) element;
                writeln();
                writeln("###### " + t.getValue());
                break;
            }
            case UNNUMBRED_ITEM1: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                write("* ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM2: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                write("** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM3: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                write("*** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM4: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                write("**** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM5: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                write("***** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case UNNUMBRED_ITEM6: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                write("****** ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM1: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                write("1. ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM2: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                write("\t1. ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM3: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                write("\t\t1. ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM4: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                write("\t\t\t1. ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM5: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                write("\t\t\t\t1. ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM6: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                write("\t\t\t\t\t1. ");
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
                MdAdmonition t = (MdAdmonition) element;
                writeln();
                write(t.getType().toString() + ": ");
                write(t.getContent());
                writeln();
                break;
            }
            case SEQ: {
                MdSequence t = (MdSequence) element;
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
                MdCode c = (MdCode) element;
                if (c.isInline()) {
                    writeInline(c);
                } else {
                    writeln();
                    writeln("```" + convertLanguage(c.getLanguage()));
                    writeln(c.getValue());
                    writeln("```");
                }
                break;
            }
            case TEXT: {
                MdText c = (MdText) element;
                writeln(c.getText());
                break;
            }
            case TABLE: {
                MdTable tab = (MdTable) element;
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
            case LINE_BREAK: {
                writeln();
                return;
            }
            case HORIZONTAL_RULE: {
                writeln("--------");
                return;
            }
            default:{
                throw new IllegalArgumentException("Unable to write " + element.getElementType());
            }
        }
    }

}
