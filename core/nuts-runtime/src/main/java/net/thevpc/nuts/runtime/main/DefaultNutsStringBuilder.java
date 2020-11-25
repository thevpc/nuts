package net.thevpc.nuts.runtime.main;

import net.thevpc.nuts.*;

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
    public NutsStringBuilder append(String s) {
        sb.append(s);
        return this;
    }

    @Override
    public NutsStringBuilder append(Object s) {
        sb.append(s);
        return this;
    }

    @Override
    public NutsStringBuilder appendRaw(String s) {
        append(ws.formats().text().escapeText(s));
        return this;
    }

    @Override
    public NutsStringBuilder append(String formatType, String rawString) {
        String suffix = getSuffix(formatType);
        sb.append(formatType);
        sb.append(ws.formats().text().escapeText(rawString));
        sb.append(suffix);
        return this;
    }

    @Override
    public NutsStringBuilder appendHashed(Object o, Object hash) {
        if(hash==null){
            hash=o;
        }
        int h = hash==null?0:Math.abs(hash.hashCode()) % (AVAILABLE_FORMATS.length + 1);
        String et = ws.formats().text().escapeText(String.valueOf(o));
        if (h == 0) {
            return appendRaw(et);
        } else {
            return append(AVAILABLE_FORMATS[h - 1], et);
        }
    }

    @Override
    public NutsStringBuilder appendRandom(Object o) {
        int h = (int)Math.random()*(AVAILABLE_FORMATS.length + 1);
        String et = ws.formats().text().escapeText(String.valueOf(o));
        if (h == 0) {
            return appendRaw(et);
        } else {
            return append(AVAILABLE_FORMATS[h - 1], et);
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
        return ws.formats().text().filterText(sb.toString());
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
