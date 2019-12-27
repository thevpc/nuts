package net.vpc.app.nuts.runtime.app;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.DefaultFormatBase;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
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
    public NutsCommandLineFormat value(NutsCommandLine value) {
        return setValue(value);
    }

    @Override
    public NutsCommandLineFormat value(String[] args) {
        return value(args == null ? null : create(args));
    }

    @Override
    public NutsCommandLineFormat value(String args) {
        return value(args == null ? null : parse(args));
    }

    @Override
    public NutsCommandLine getValue() {
        return value;
    }

    @Override
    public NutsCommandLine parse(String line) {
        return new DefaultNutsCommandLine(ws, NutsCommandLineUtils.parseCommandLine(ws, line));
    }

    @Override
    public NutsCommandLine create(String... args) {
        return new DefaultNutsCommandLine(ws, args);
    }

    @Override
    public NutsCommandLine create(List<String> args) {

        return new DefaultNutsCommandLine(ws, args, null);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

    @Override
    public void print(PrintStream out) {
        if (value != null) {
            int index = 0;
            NutsCommandLine cmd = new DefaultNutsCommandLine(ws, value.toArray());
            while (cmd.hasNext()) {
                NutsArgument n = cmd.next();
                if (index > 0) {
                    out.print(" ");
                }
                out.print(escapeArgument(n));
                index++;
            }
        }
    }

    private String escapeArgument(NutsArgument arg) {
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
                sb.append("**" + escapeArgument(ws, name) + "**");
            } else {
                if (!arg.isEnabled()) {
                    sb.append("**" + escapeArgument(ws, prefix) + "**");
                    sb.append("@@//@@");
                    sb.append("**" + escapeArgument(ws, name) + "**");
                } else {
                    sb.append("**" + escapeArgument(ws, prefix + name) + "**");
                }
            }
            if (arg.isKeyValue()) {
                sb.append("{{\\=}}");
                sb.append(escapeArgument(ws, arg.getStringValue()));
            }
            return sb.toString();
        } else {
            if (!arg.isEnabled()) {
                return "@@" + escapeArgument(ws, arg.getString()) + "@@";
            }
            return escapeArgument(ws, arg.getString());
        }
    }

    private static String escapeArgument(NutsWorkspace ws, String arg) {
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
        return ws.io().terminalFormat().escapeText(sb.toString());
    }

    @Override
    public NutsArgument createArgument(String argument) {
        return Factory.createArgument0(ws, argument, '=');
    }

    @Override
    public NutsArgumentCandidate createCandidate(String value, String label) {
        return Factory.createCandidate0(ws, value, label);
    }

    @Override
    public NutsArgumentName createName(String type, String label) {
        return Factory.createName0(ws, type, label);
    }

    @Override
    public NutsArgumentName createName(String type) {
        return createName(type, type);
    }

    public static class Factory {
        public static NutsArgument createArgument0(NutsWorkspace ws, String argument, char eq) {
            return new DefaultNutsArgument(argument, eq);
        }

        public static NutsArgumentCandidate createCandidate0(NutsWorkspace ws, String value, String label) {
            return new NutsDefaultArgumentCandidate(value, CoreStringUtils.isBlank(label) ? value : label);
        }

        public static NutsArgumentName createName0(NutsWorkspace ws, String type, String label) {
            if (type == null) {
                type = "";
            }
            if (label == null) {
                label = type;
            }
            switch (type) {
                case "arch": {
                    return new ArchitectureNonOption(label, ws);
                }
                case "packaging": {
                    return new PackagingNonOption(ws, label);
                }
                case "extension": {
                    return new ExtensionNonOption(type, null);
                }
                case "file": {
                    return new FileNonOption(ws, type);
                }
                case "boolean": {
                    return new ValueNonOption(ws, type, "true", "false");
                }
                case "repository": {
                    return new RepositoryNonOption(ws, label);
                }
                case "repository-type": {
                    return new RepositoryTypeNonOption(label, ws);
                }
                case "right": {
                    return new PermissionNonOption(label, ws, null, null, false);
                }
                case "user": {
                    return new UserNonOption(label, ws);
                }
                case "group": {
                    return new GroupNonOption(label, ws);
                }
                default: {
                    return new DefaultNonOption(ws, label);
                }
            }
        }
    }
}
