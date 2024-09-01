/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
 * @author thevpc
 */
public class DefaultMdWriter extends AbstractMdWriter {

    public DefaultMdWriter(Writer out) {
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
        write(document.getContent());
    }

    @Override
    public void writeImpl(MdElement node, WriteContext context) {
        switch (node.type().group()) {
            case TITLE: {
                MdTitle t = (MdTitle) node;
                writeln();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < node.type().depth(); i++) {
                    sb.append('=');
                }
                writeln(sb + " " + t.getValue());
                break;
            }
            case UNNUMBERED_ITEM: {
                MdUnNumberedItem t = (MdUnNumberedItem) node;
                writeln();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < node.type().depth(); i++) {
                    sb.append('*');
                }
                write(sb + " ");
                write(t.getValue());
                writeln();
                break;
            }
            case NUMBERED_ITEM: {
                MdNumberedItem t = (MdNumberedItem) node;
                writeln();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < node.type().depth(); i++) {
                    sb.append('.');
                }
                write(sb + " ");
                write(t.getValue());
                writeln();
                break;
            }
            case HORIZONTAL_RULE: {
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
            case BODY:
            case PHRASE:
            case NUMBERED_LIST:
            case UNNUMBERED_LIST: {
                MdParent t = (MdParent) node;
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
                MdCode c = (MdCode) node;
                if (c.isInline()) {
                    write(" `");
                    String r = node.asCode().getValue();
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
                write(c.getText());
                if (!node.isInline()) {
                    writeln();
                }
                break;
            }
            case TABLE: {
                MdTable tab = (MdTable) node;
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
            case CODE_LINK: {
                write("```");
                write(node.asCodeLink().getLinkCode());
                write("```");
                return;
            }
            case BOLD: {
                write("**");
                write(node.asBold().getContent());
                write("**");
                return;
            }
            case ITALIC: {
                write("__");
                write(node.asBold().getContent());
                write("__");
                return;
            }

            case LINK: {
                write("link:");
                write(node.asLink().getLinkUrl());
                write("[");
                write(node.asLink().getLinkTitle());
                write("]");
                return;
            }
            case IMAGE: {
                write("image:");
                write(node.asImage().getImageUrl());
                write("[");
                write(node.asImage().getImageTitle());
                write("]");
                return;
            }
            case LINE_BREAK: {
                writeln();
                break;
            }
            default:{
                throw new IllegalArgumentException("Unable to write " + node.type());
            }
        }
    }

}
