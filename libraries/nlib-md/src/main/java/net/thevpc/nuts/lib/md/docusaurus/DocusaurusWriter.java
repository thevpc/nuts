/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.base.AbstractMdWriter;

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


    public void writeImpl(MdElement element, WriteContext context) {
        switch (element.type().group()) {
            case TITLE: {
                MdTitle t = (MdTitle) element;
                writeln();
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < element.type().depth(); i++) {
                    sb.append('#');
                }
                writeln(sb+" " + t.getValue());
                break;
            }
            case UNNUMBERED_ITEM: {
                MdUnNumberedItem t = (MdUnNumberedItem) element;
                writeln();
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < element.type().depth(); i++) {
                    sb.append('*');
                }
                write(sb+" ");
                write(t.getValue());
                writeln();
                break;
            }
            case NUMBERED_ITEM: {
                MdNumberedItem t = (MdNumberedItem) element;
                writeln();
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < element.type().depth()-1; i++) {
                    sb.append('\t');
                }
                write(sb+"1. ");
                write(t.getValue());
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
            case BODY:
            case UNNUMBERED_LIST:
            case NUMBERED_LIST:
            case PHRASE:
            {
                MdParent t = (MdParent) element;
                if (t.isInline()) {
                    for (MdElement mdElement : t.getChildren()) {
                        write(mdElement);
                    }
                } else {
                    for (MdElement mdElement : t.getChildren()) {
                        write(mdElement);
                        writeln();
                    }
                    writeln();
                }
                break;
            }
            case CODE: {
                MdCode c = (MdCode) element;
                if (c.isInline()) {
                    write(" `");
                    String r = element.asCode().getValue();
                    StringBuilder sb = new StringBuilder();
                    for (char cc : r.toCharArray()) {
                        switch (cc) {
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
                                sb.append(cc);
                                break;
                            }
                        }
                    }
                    write(sb.toString());
                    write("` ");
                    return;
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
                write(element.asText().getText());
                if(!c.isInline()) {
                    writeln();
                }
                break;
            }
            case BOLD: {
                write("**");
                write(element.asBold().getContent());
                write("**");
                return;
            }
            case ITALIC: {
                write("__");
                write(element.asItalic().getContent());
                write("__");
                return;
            }

            case TABLE: {
                MdTable tab = (MdTable) element;
                writeln();
                writeln("|===");
                for (MdColumn cell : tab.getColumns()) {
                    write("|");
                    write(cell.getName());
                    write(" ");
                }
                writeln();
                for (MdRow row : tab.getRows()) {
                    writeln();
                    for (MdElement cell : row.getCells()) {
                        write("|");
                        write(cell);
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

            default:{
                throw new IllegalArgumentException("Unable to write " + element.type());
            }
        }
    }

}
