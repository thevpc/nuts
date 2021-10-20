package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBootException;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsMessage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Simplistic implementation of Nuts Version (does not implement the hole interface nor does it implement all the features).
 * This implementation is mainly meant to handle nuts-runtime and its dependencies versions. For instance it doe not
 * not support multiple intervals.
 */
public class NutsBootVersion {
    private final String from;
    private final String to;
    private final boolean includeFrom;
    private final boolean includeTo;

    private NutsBootVersion(String from, String to, boolean includeFrom, boolean includeTo) {
        this.from = from;
        this.to = to;
        this.includeFrom = includeFrom;
        this.includeTo = includeTo;
    }

    public static NutsBootVersion parse(String s) {
        if (s == null) {
            s = "";
        } else {
            s = s.trim();
        }
        if (s.length() == 0) {
            return new NutsBootVersion("", "", true, true);
        }
        if (s.charAt(0) == '[' || s.charAt(0) == ']') {
            char last = s.charAt(s.length() - 1);
            String verStr = null;
            boolean startInclude = s.charAt(0) == '[';
            boolean endInclude = true;
            if (s.length() > 1 && (last == ']' || last == '[')) {
                endInclude = last == ']';
                verStr = s.substring(1, s.length() - 1).trim();
            } else {
                verStr = s.substring(1).trim();
            }
            int x = verStr.indexOf(',');
            if (x < 0) {
                return new NutsBootVersion(verStr, verStr, startInclude, endInclude);
            } else {
                return new NutsBootVersion(
                        verStr.substring(0, x).trim(),
                        verStr.substring(x + 1).trim(),
                        startInclude, endInclude
                );
            }
        } else {
            return new NutsBootVersion(s, s, true, true);
        }
    }

    public boolean isBlank() {
        return from.isEmpty() && to.isEmpty();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isInterval() {
        return !from.equals(to);
    }

    public boolean isSingleValue() {
        return from.equals(to) && includeFrom && includeTo;
    }

    public boolean isIncludeFrom() {
        return includeFrom;
    }

    public boolean isIncludeTo() {
        return includeTo;
    }

    public boolean accept(NutsBootVersion other) {
        if (!other.isSingleValue()) {
            throw new NutsBootException(NutsMessage.cstyle("expected single value version: %s", other));
        }
        NutsBootVersion a = parse(getFrom());
        if (a.isBlank()) {
            //ok
        } else {
            int c = a.compareTo(other);
            if (isIncludeFrom()) {
                if (c > 0) {
                    return false;
                }
            } else {
                if (c >= 0) {
                    return false;
                }
            }
        }
        a = parse(getTo());
        if (a.isBlank()) {
            //ok
        } else {
            int c = a.compareTo(other);
            if (isIncludeTo()) {
                return c >= 0;
            } else {
                return c > 0;
            }
        }
        return true;
    }

    public int compareTo(String other) {
        return compareTo(parse(other));
    }

    public int compareTo(NutsBootVersion other) {
        if (this.isSingleValue() && other.isSingleValue()) {
            String a1i = from;
            String a2i = other.from;
            if (a1i.equals(a2i)) {
                return 0;
            }
            if ("".equals(a1i)) {
                return 1;
            }
            if ("".equals(a2i)) {
                return -1;
            }
            if (NutsConstants.Versions.LATEST.equals(a1i)) {
                return 1;
            }
            if (NutsConstants.Versions.LATEST.equals(a2i)) {
                return -1;
            }
            if (NutsConstants.Versions.RELEASE.equals(a1i)) {
                return 1;
            }
            if (NutsConstants.Versions.RELEASE.equals(a2i)) {
                return -1;
            }
            String[] p1 = splitVersionParts(a1i);
            String[] p2 = splitVersionParts(a2i);
            int m = Math.min(p1.length, p2.length);
            for (int j = 0; j < m; j++) {
                int x = compareVersionPart(p1[j], p2[j]);
                if (x != 0) {
                    return x;
                }
            }
            if (p1.length < p2.length) {
                return -1;
            }
            if (p1.length > p2.length) {
                return 1;
            }
            int i1 = NutsApiUtils.parseInt(a1i,-1,-1);
            int i2 = NutsApiUtils.parseInt(a2i,-1,-1);
            if (i1 != i2) {
                return Integer.compare(i1, i2);
            }
            return 0;
        }
        throw new NutsBootException(NutsMessage.plain("unsupported compare versions"));
    }

    private BigInteger asInt(String s) {
        try {
            return new BigInteger(s);
        } catch (Exception e) {
            return null;
        }
    }

    private int compareVersionPart(String a, String b) {
        BigInteger ia = asInt(a);
        BigInteger ib = asInt(b);
        if (ia != null && ib != null) {
            return ia.compareTo(ib);
        }
        return a.compareTo(b);
    }

    private String[] splitVersionParts(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder sb = null;
        boolean digits = false;
        for (char c : s.toCharArray()) {
            if (sb == null) {
                if (c >= '0' && c <= '9') {
                    digits = true;
                }
                sb = new StringBuilder();
                sb.append(c);
            } else {
                if (digits) {
                    if (c >= '0' && c <= '9') {
                        sb.append(c);
                    } else {
                        if (sb.length() > 0) {
                            parts.add(sb.toString());
                        }
                        sb = new StringBuilder();
                        sb.append(c);
                        digits = false;
                    }
                } else {
                    if (c >= '0' && c <= '9') {
                        if (sb.length() > 0) {
                            parts.add(sb.toString());
                        }
                        sb = new StringBuilder();
                        sb.append(c);
                        digits = true;
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        if (sb != null && sb.length() > 0) {
            parts.add(sb.toString());
        }
        return parts.toArray(new String[0]);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, includeFrom, includeTo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsBootVersion that = (NutsBootVersion) o;
        return includeFrom == that.includeFrom && includeTo == that.includeTo && Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public String toString() {
        if (from.equals(to)) {
            if (includeFrom && includeTo) {
                return from;
            } else {
                //why
                return (includeFrom ? "[" : "]") + from + (includeTo ? "]" : "[");
            }
        } else {
            return (includeFrom ? "[" : "]") + from + "," + to + (includeTo ? "]" : "[");
        }
    }
}
