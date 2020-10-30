package net.vpc.app.nuts.runtime.app;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.DefaultFormatBase;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.PrintStream;
import java.util.List;

public class DefaultNutsCommandLineFormat extends DefaultFormatBase<NutsCommandLineFormat> implements NutsCommandLineFormat {
    private NutsCommandLine value;

    public DefaultNutsCommandLineFormat(NutsWorkspace ws) {
        super(ws, "commandline");
    }

    @Override
    public NutsCommandLineFormat setValue(NutsCommandLine value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(String[] args) {
        return setValue(args == null ? null : getWorkspace().commandLine().create(args));
    }

    @Override
    public NutsCommandLineFormat setValue(String args) {
        return setValue(args == null ? null : getWorkspace().commandLine().parse(args));
    }

    @Override
    public NutsCommandLine getValue() {
        return value;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

    @Override
    public void print(PrintStream out) {
        if (value != null) {
            int index = 0;
            NutsCommandLine cmd = new DefaultNutsCommandLine(getWorkspace(), value.toArray());
            while (cmd.hasNext()) {
                NutsArgument n = cmd.next();
                if (index > 0) {
                    out.print(" ");
                }
                out.print(escapeArgumentFormat(n));
                index++;
            }
        }
    }

    private String escapeArgumentFormat(NutsArgument arg) {
        if (arg.isOption()) {

            StringBuilder sb = new StringBuilder();
            String prefix = arg.getStringOptionPrefix();
            String name = arg.getStringOptionName();
            if (arg.isNegated()) {
                sb.append("**" + prefix + "**");
                if (!arg.isEnabled()) {
                    sb.append("@@//@@");
                }
                sb.append("[[!]]");
                sb.append("**" + escapeArgumentFormat(getWorkspace(), name) + "**");
            } else {
                if (!arg.isEnabled()) {
                    sb.append("**" + escapeArgumentFormat(getWorkspace(), prefix) + "**");
                    sb.append("@@//@@");
                    sb.append("**" + escapeArgumentFormat(getWorkspace(), name) + "**");
                } else {
                    sb.append("**" + escapeArgumentFormat(getWorkspace(), prefix + name) + "**");
                }
            }
            if (arg.isKeyValue()) {
                sb.append("{{\\=}}");
                sb.append(escapeArgumentFormat(getWorkspace(), arg.getStringValue()));
            }
            return sb.toString();
        } else {
            if (!arg.isEnabled()) {
                return "@@" + escapeArgumentFormat(getWorkspace(), arg.getString()) + "@@";
            }
            return escapeArgumentFormat(getWorkspace(), arg.getString());
        }
    }

    private static String escapeArgumentFormat(NutsWorkspace ws, String arg) {
        StringBuilder sb = new StringBuilder();
        if (arg != null) {
            for (char c : arg.toCharArray()) {
                switch (c) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case '\'':
                        sb.append("\\'");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\r':
                        sb.append("\\r");
                    case '\f':
                        sb.append("\\f");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }
        return ws.io().term().getTerminalFormat().escapeText(sb.toString());
    }
}
