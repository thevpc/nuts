package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.cmdline.DefaultNCmdLine;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.text.parser.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NTextNodeWriterStringer extends AbstractNTextNodeWriter {

    private OutputStream out;

    public NTextNodeWriterStringer(OutputStream out) {
        this.out = out;
    }

    public static String toString(NText n, NWorkspace workspace) {
        if (n == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new NTextNodeWriterStringer(bos).writeNode(n);
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
            throw new NIOException(ex);
        }
        return true;
    }

    @Override
    public void writeNode(NText node) {
        if (node == null) {
            return;
        }
        switch (node.type()) {
            case PLAIN:
                NTextPlain p = (NTextPlain) node;
                writeEscaped(p.value());
                break;
            case LIST: {
                NTextList s = (NTextList) node;
                for (NText n : s) {
                    writeNode(n);
                }
                break;
            }
            case BUILDER: {
                NTextBuilder s = (NTextBuilder) node;
                for (NText n : s) {
                    writeNode(n);
                }
                break;
            }
            case STYLED: {
                DefaultNTextStyled s = (DefaultNTextStyled) node;
                NTextStyles styles = s.styles();
                writeRaw("##{" + styles.id() + ":");
                writeNode(s.child());
                writeRaw("}##");
                writeRaw(NConstants.Ntf.SILENT);
                break;
            }
            case TITLE: {
                DefaultNTextTitle s = (DefaultNTextTitle) node;
                writeRaw(CoreStringUtils.fillString('#', s.level()) + ") ");
                writeNode(s.child());
                writeRaw("\n");
                break;
            }
            case COMMAND: {
                NTextCmd s = (NTextCmd) node;
                writeRaw("```!");
                NCmdLine cmd = new DefaultNCmdLine();
                cmd.add(s.terminalCommand().name());
                cmd.addAll(s.terminalCommand().args());
                writeEscapedSpecial(cmd.toString());
                writeRaw("```");
                break;
            }
            case ANCHOR: {
                NTextAnchor s = (NTextAnchor) node;
                writeRaw("```!anchor");
                writeRaw(s.separator());
                writeEscapedSpecial(s.value());
                writeRaw("```");
                break;
            }
            case LINK: {
                NTextLink s = (NTextLink) node;
                writeRaw("```!link");
                writeRaw(s.separator());
                writeEscaped(s.value());
                writeRaw("```");
                break;
            }
            case INCLUDE: {
                NTextInclude s = (NTextInclude) node;
                writeRaw("```!include");
                writeRaw(s.separator());
                writeEscaped(s.text());
                writeRaw("```");
                break;
            }
            case CODE: {
                NTextCode s = (NTextCode) node;
                writeRaw("```");
                writeRaw(s.qualifier());
                writeRaw(s.separator());
                writeEscapedSpecial(s.value());
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
                case NConstants.Ntf.SILENT: {
                    sb.append('\\');
                    sb.append(cc[i]);
                    break;
                }
                case '`': {
                    if (i < cc.length - 2) {
                        if (cc[i] == '`' && cc[i + 1] == '`' && cc[i + 2] == '`') {
                            sb.append('\\');
                            sb.append(cc[i]);
                            sb.append(cc[i + 1]);
                            sb.append(cc[i + 2]);
                            i += 2;
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
            throw new NIOException(ex);
        }
    }

    private void writeStyledStart(NTextStyles styles, boolean complex) {
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
