package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNutsCommandLine;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class NutsTextNodeWriterStringer extends AbstractNutsTextNodeWriter {

    private OutputStream out;
    private NutsSession session;

    public NutsTextNodeWriterStringer(OutputStream out, NutsSession session) {
        this.out = out;
        this.session = session;
    }

    public static String toString(NutsText n, NutsSession ws) {
        if (n == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new NutsTextNodeWriterStringer(bos, ws).writeNode(n);
        return bos.toString();
    }


    @Override
    public void writeRaw(byte[] buf, int off, int len) {
        writeRaw(new String(buf, off, len));
    }

    @Override
    public void writeRaw(char[] buf, int off, int len) {
        writeRaw(new String(buf, off, len));
    }


    @Override
    public boolean flush() {
        try {
            out.flush();
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return true;
    }

    @Override
    public void writeNode(NutsText node) {
        if (node == null) {
            return;
        }
        switch (node.getType()) {
            case PLAIN:
                NutsTextPlain p = (NutsTextPlain) node;
                writeEscaped(p.getText());
                break;
            case LIST: {
                NutsTextList s = (NutsTextList) node;
                for (NutsText n : s) {
                    writeNode(n);
                }
                break;
            }
            case STYLED: {
                DefaultNutsTextStyled s = (DefaultNutsTextStyled) node;
                NutsTextStyles styles = s.getStyles();
                writeRaw("##{" + styles.id() + ":");
                writeNode(s.getChild());
                writeRaw("}##");
                writeRaw(NutsConstants.Ntf.SILENT);
                break;
            }
            case TITLE: {
                DefaultNutsTextTitle s = (DefaultNutsTextTitle) node;
                writeRaw(CoreStringUtils.fillString('#', s.getLevel()) + ") ");
                writeNode(s.getChild());
                writeRaw("\n");
                break;
            }
            case COMMAND: {
                NutsTextCommand s = (NutsTextCommand) node;
                writeRaw("```!");
                NutsCommandLine cmd = new DefaultNutsCommandLine();
                cmd.add(s.getCommand().getName());
                cmd.addAll(s.getCommand().getArgs());
                writeEscapedSpecial(cmd.toString());
                writeRaw("```");
                break;
            }
            case ANCHOR: {
                NutsTextAnchor s = (NutsTextAnchor) node;
                writeRaw("```!anchor");
                writeRaw(s.getSeparator());
                writeEscapedSpecial(s.getValue());
                writeRaw("```");
                break;
            }
            case LINK: {
                NutsTextLink s = (NutsTextLink) node;
                writeRaw("```!link");
                writeRaw(s.getSeparator());
                writeEscaped(s.getText());
                writeRaw("```");
                break;
            }
            case INCLUDE: {
                NutsTextInclude s = (NutsTextInclude) node;
                writeRaw("```!include");
                writeRaw(s.getSeparator());
                writeEscaped(s.getText());
                writeRaw("```");
                break;
            }
            case CODE: {
                NutsTextCode s = (NutsTextCode) node;
                writeRaw("```");
                writeRaw(s.getQualifier());
                writeRaw(s.getSeparator());
                writeEscapedSpecial(s.getText());
                writeRaw("```");
                break;
            }
            default:
                throw new UnsupportedOperationException("invalid node type : " + node.getClass().getSimpleName());
        }
    }

    public final void writeEscapedSpecial(String rawString) {
        char[] cc = rawString.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cc.length; i++) {
            if (i <= cc.length - 3 && cc[i] == '`' && cc[i + 1] == '`' && cc[i + 2] == '`') {
                sb.append('\\');
            }
            sb.append(cc[i]);
        }
        writeRaw(sb.toString());
    }

    public final void writeEscaped(String rawString) {
        char[] cc = rawString.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cc.length; i++) {
            switch (cc[i]) {
                case '\\':
                case NutsConstants.Ntf.SILENT: {
                    sb.append('\\');
                    sb.append(cc[i]);
                    break;
                }
                case '`': {
                    if (i < cc.length - 3) {
                        if (cc[i] == '`' && cc[i + 1] == '`' && cc[i + 2] == '`') {
                            sb.append('\\');
                            sb.append(cc[i]);
                        } else {
                            sb.append(cc[i]);
                        }
                    } else if (i < cc.length - 1) {
                        if (cc[i] == '`' && cc[i + 1] == '`') {
                            sb.append('\\');
                            sb.append(cc[i]);
                        } else {
                            sb.append(cc[i]);
                        }
                    } else {
                        sb.append('\\');
                        sb.append(cc[i]);
                    }
                    break;
                }
                case '#': {
                    if (i < cc.length - 1 && cc[i + 1] != cc[i]) {
                        sb.append(cc[i]);
                    } else {
                        sb.append('\\');
                        sb.append(cc[i]);
                    }
                    break;
                }
                default: {
                    sb.append(cc[i]);
                }
            }
        }
        writeRaw(sb.toString());
    }

    public final void writeRaw(char rawChar) {
        writeRaw(String.valueOf(rawChar));
    }

    public final void writeRaw(String rawString) {
        try {
            out.write(rawString.getBytes());
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    private void writeStyledStart(NutsTextStyles styles, boolean complex) {
        StringBuilder sb = new StringBuilder();
        if (complex) {
            sb.append("##{");
        } else {
            sb.append("##:");
        }
        sb.append(styles.id());
        sb.append(":");
        writeRaw(sb.toString());
    }
}
