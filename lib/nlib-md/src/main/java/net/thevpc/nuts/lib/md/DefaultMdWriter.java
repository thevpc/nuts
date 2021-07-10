/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.lib.md;

import java.io.Writer;

/**
 *
 * @author thevpc
 */
public class DefaultMdWriter extends AbstractMdWriter {

    public DefaultMdWriter(Writer out) {
        super(out);
    }

    @Override
    public void write(MdDocument document) {
        write(document.getContent());
    }

    public void writeInline(MdElement element) {
        switch (element.getElementType().type()) {
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

    @Override
    public void writeImpl(MdElement node) {
        switch (node.getElementType().type()) {
            case TITLE: {
                MdTitle t = (MdTitle) node;
                writeln();
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < node.getElementType().depth(); i++) {
                    sb.append('=');
                }
                writeln(sb+" " + t.getValue());
                break;
            }
            case UNNUMBRED_ITEM: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < node.getElementType().depth(); i++) {
                    sb.append('*');
                }
                write(sb+" ");
                writeInline(t.getValue());
                writeln();
                break;
            }
            case NUMBRED_ITEM: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < node.getElementType().depth(); i++) {
                    sb.append('.');
                }
                write(sb+" ");
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
