package net.vpc.app.nuts.main;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsString;
import net.vpc.app.nuts.NutsStringBuilder;
import net.vpc.app.nuts.NutsWorkspace;

public class DefaultNutsStringBuilder implements NutsStringBuilder {
    private static String[] AVAILABLE_FORMATS = new String[]{
            "@@",
            "[[",
            "((",
            "{{",
            "<<",
            "**",
            "##",
            "^^",
            "__",
            "==",
            "@@@",
            "[[[",
            "(((",
            "{{{",
            "<<<",
            "***",
            "###",
            "^^^",
            "___",
            "===",
            "@@@@",
            "[[[[",
            "((((",
            "{{{{",
            "<<<<",
            "****",
            "####",
            "^^^^",
            "____",
            "====",
    };
    private StringBuilder sb = new StringBuilder();
    private NutsWorkspace ws;

    public DefaultNutsStringBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsStringBuilder appendFormatted(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NutsStringBuilder append(Object s) {
        if (s instanceof NutsString) {
            sb.append(((NutsString) s).getValue());
        }
        if (s instanceof NutsStringBuilder) {
            sb.append(((NutsStringBuilder) s).toFormattedString());
        }
        sb.append(s);
        return this;
    }

    @Override
    public NutsStringBuilder appendRaw(String s) {
        appendFormatted(ws.io().term().getTerminalFormat().escapeText(s));
        return this;
    }

    @Override
    public NutsStringBuilder appendRaw(String type, String s) {
        String suffix = getSuffix(type);
        sb.append(type);
        sb.append(ws.io().term().getTerminalFormat().escapeText(s));
        sb.append(suffix);
        return this;
    }

    @Override
    public NutsStringBuilder appendHashed(Object o, Object hash) {
        if(hash==null){
            hash=o;
        }
        int h = hash==null?0:Math.abs(hash.hashCode()) % (AVAILABLE_FORMATS.length + 1);
        String et = ws.io().term().getTerminalFormat().escapeText(String.valueOf(o));
        if (h == 0) {
            return appendRaw(et);
        } else {
            return appendRaw(AVAILABLE_FORMATS[h - 1], et);
        }
    }

    @Override
    public NutsStringBuilder appendRandom(Object o) {
        int h = (int)Math.random()*(AVAILABLE_FORMATS.length + 1);
        String et = ws.io().term().getTerminalFormat().escapeText(String.valueOf(o));
        if (h == 0) {
            return appendRaw(et);
        } else {
            return appendRaw(AVAILABLE_FORMATS[h - 1], et);
        }
    }

    @Override
    public NutsStringBuilder appendHashed(Object o) {
        return appendHashed(o, o);
    }

    @Override
    public String toFormattedString() {
        return sb.toString();
    }

    @Override
    public NutsString toNutsString() {
        return new NutsString(sb.toString());
    }

    @Override
    public String toFilteredString() {
        return ws.io().term().getTerminalFormat().filterText(sb.toString());
    }

    @Override
    public String toString() {
        return toFormattedString();
    }

    private String getSuffix(String type) {
        if (type != null) {
            switch (type) {
                case "<":
                    return ">";
                case "<<":
                    return ">>";
                case "<<<":
                    return ">>>";
                case "<<<<":
                    return ">>>>";
                case "{":
                    return "}";
                case "{{":
                    return "}}";
                case "{{{":
                    return "}}}";
                case "{{{{":
                    return "}}}}";
                case "(":
                    return ")";
                case "((":
                    return "))";
                case "(((":
                    return ")))";
                case "((((":
                    return "))))";
                case "[":
                    return "]";
                case "[[":
                    return "]]";
                case "[[[":
                    return "]]]";
                case "[[[[":
                    return "]]]]";
                case "@@":
                case "@@@":
                case "@@@@":
                case "**":
                case "***":
                case "****":
                case "##":
                case "###":
                case "####":
                case "^^":
                case "^^^":
                case "^^^^":
                case "__":
                case "___":
                case "____":
                case "==":
                case "===":
                case "====":
                    return type;
            }
        }
        throw new NutsIllegalArgumentException(ws, "Invalid format prefix : '" + type + "'");
    }

    @Override
    public NutsStringBuilder clear() {
        sb.delete(0,sb.length());
        return this;
    }
}
