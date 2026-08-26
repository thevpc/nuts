package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.List;
import java.util.Objects;

public class NBootCompleteRequest {
    // which word in the (post-mode-flag) argument list is currently being completed. E.g. if the user typed nuts start --da and hit Tab, the word list is ["start", "--da"], and the word being completed is index 1.
    int argIndex;
    // where the cursor sits inside that word, counted in characters from the start of the word. For a normal trailing Tab (cursor at the very end of --da), offset = 4 (length of --da). Offset only differs from "end of word" in the mid-word case — e.g. completing --e|s where the cursor sits between e and s — offset would be 2 there, with s remaining after the cursor, untouched.
    int argOffset;

    public static NBootCompleteRequest parseOrNull(String arg) {
        if (arg == null) return null;
        for (String prefix : new String[]{"--nuts-exec-mode=complete", "--nuts-exec-mode=auto-complete", "complete", "auto-complete"}) {
            if (arg.startsWith(prefix)) {
                return parseSuffix(arg.substring(prefix.length()));
            }
        }
        return null;
    }

    private static NBootCompleteRequest parseSuffix(String s) {
        if (s.isEmpty()) {
            return new NBootCompleteRequest(0, 0);
        }
        if (!s.startsWith(",")) {
            return null; // not a real match, e.g. "--nuts-exec-mode=completely..."
        }
        List<String> r = NBootUtils.split(s.substring(1), ",", true, false);
        if (r.size() > 2) return null;
        Integer index = r.size() < 1 ? 0 : r.get(0).isEmpty() ? 0 : NBootUtils.parseInt(r.get(0));
        Integer offset = r.size() < 2 ? 0 : r.get(1).isEmpty() ? 0 : NBootUtils.parseInt(r.get(1));
        if (index != null && offset != null && index >= 0 && offset >= 0) {
            return new NBootCompleteRequest(index, offset);
        }
        return null;
    }

    public NBootCompleteRequest(int argIndex, int argOffset) {
        this.argIndex = argIndex;
        this.argOffset = argOffset;
    }

    public int argOffset() {
        return argOffset;
    }

    public int argIndex() {
        return argIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NBootCompleteRequest that = (NBootCompleteRequest) o;
        return argOffset == that.argOffset && argIndex == that.argIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(argOffset, argIndex);
    }

}
