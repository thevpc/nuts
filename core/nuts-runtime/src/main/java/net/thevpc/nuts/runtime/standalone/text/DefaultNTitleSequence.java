package net.thevpc.nuts.runtime.standalone.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.text.NTitleSequence;
import net.thevpc.nuts.text.NTitleNumber;

/**
 *
 */
public class DefaultNTitleSequence implements NTitleSequence {
    private NTitleNumberAndSep[] value;
    private String end;
    private String start;
    private String stringPattern;

    private static class NTitleNumberAndSep {
        NTitleNumber n;
        String sep;

        public NTitleNumberAndSep(NTitleNumber n, String sep) {
            this.n = n;
            this.sep = sep;
        }

        @Override
        public String toString() {
            return "NutsTitleNumberAndSep{" +
                    "n=" + n +
                    ", sep='" + sep + '\'' +
                    '}';
        }
    }

    private static boolean hasBullets(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (isBullet(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAlphaNum(String s) {
        for (char c : s.toCharArray()) {
            if (isAlphaNum(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBullet(char c) {
        return c == '*' || c == '-' || c == '+';
    }

    private static boolean isSep(char c) {
        return c == '.' || c == '-' || c == '_' || c == '/';
    }

    private static boolean isAlphaNum(char c) {
        return c == '0' || c == '1' || c == 'a' || c == 'A';
    }

    public DefaultNTitleSequence(String pattern) {
        if (pattern.isEmpty()) {
            pattern = "1.";
        }
        this.stringPattern = pattern;
        StringBuilder end = new StringBuilder();
        StringBuilder start = new StringBuilder();
        List<NTitleNumberAndSep> p = new ArrayList<>();
        char[] charArray = pattern.toCharArray();
        String lastSep = null;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (c) {
                case '0':
                case '1': {
                    if (end.length() == 0) {
                        if (lastSep == null) {
                            if (i + 1 < charArray.length && isSep(charArray[i + 1])) {
                                lastSep = String.valueOf(charArray[i + 1]);
                            } else if (p.size() > 0 && p.get(0).n instanceof IntNTitleNumber) {
                                lastSep = ".";
                            } else if (p.size() == 0) {
                                lastSep = ".";
                            }
                        }
                        p.add(new NTitleNumberAndSep(new IntNTitleNumber(0), lastSep));
                        lastSep = null;
                    } else {
                        end.append(c);
                    }
                    break;
                }
                case 'A': {
                    if (end.length() == 0) {
                        if (lastSep == null) {
                            if (i + 1 < charArray.length && isSep(charArray[i + 1])) {
                                lastSep = String.valueOf(charArray[i + 1]);
                            } else if (p.size() > 0 && p.get(0).n instanceof AlphabetNTitleNumber) {
                                lastSep = "-";
                            } else if (p.size() == 0) {
                                lastSep = "-";
                            }
                        }
                        p.add(new NTitleNumberAndSep(AlphabetNTitleNumber.ofUpperCased(), lastSep));
                        lastSep = null;
                    } else {
                        end.append(c);
                    }
                    break;
                }
                case 'a': {
                    if (end.length() == 0) {
                        if (lastSep == null) {
                            if (i + 1 < charArray.length && isSep(charArray[i + 1])) {
                                lastSep = String.valueOf(charArray[i + 1]);
                            } else if (p.size() > 0 && p.get(0).n instanceof AlphabetNTitleNumber) {
                                lastSep = "-";
                            } else if (p.size() == 0) {
                                lastSep = "-";
                            }
                        }
                        p.add(new NTitleNumberAndSep(AlphabetNTitleNumber.ofLowerCased(), lastSep));
                        lastSep = null;
                    } else {
                        end.append(c);
                    }
                    break;
                }
                case '*':
                case '+': {
                    if (end.length() == 0) {
                        p.add(new NTitleNumberAndSep(BulletNTitleNumber.ofBullet(c), lastSep));
                        lastSep = null;
                    } else {
                        end.append(c);
                    }
                    break;
                }
                case '-': {
                    if (end.length() == 0) {
                        if (p.isEmpty() || p.get(p.size() - 1).n.isBullet()) {
                            p.add(new NTitleNumberAndSep(BulletNTitleNumber.ofBullet(c), lastSep));
                            lastSep = null;
                        } else {
                            if (lastSep == null) {
                                lastSep = String.valueOf(c);
                            } else {
                                lastSep += String.valueOf(c);
                            }
                        }
                    } else {
                        end.append(c);
                    }
                    break;
                }
                case '.':
                case '_':
                case '/': {
                    if (end.length() == 0) {
                        if (lastSep == null) {
                            lastSep = String.valueOf(c);
                        } else {
                            lastSep += String.valueOf(c);
                        }
                    } else {
                        end.append(c);
                    }
                    break;
                }
                default: {
                    if (p.isEmpty()) {
                        start.append(c);
                    } else {
                        end.append(c);
                    }
                }
            }
        }
        if (lastSep != null) {
            end.append(lastSep);
        }
        this.value = p.toArray(new NTitleNumberAndSep[0]);
        this.start = start.toString();
        this.end = end.toString();
    }

    public DefaultNTitleSequence() {
        this("");
    }

    protected DefaultNTitleSequence(NTitleNumberAndSep[] pattern, String start, String end, String stringPattern) {
        this.value = pattern;
        this.start = start;
        this.end = end;
        this.stringPattern = stringPattern;
    }


    private NTitleNumber numberAt(int level, NTitleNumberAndSep[] pattern) {
        int mm = Math.min(value.length, pattern.length);
        int a = Math.min(level, mm - 1);
        for (int i = a; i >= 0; i--) {
            if (i < value.length && value[i] != null) {
                return value[i].n;
            }
            if (i < pattern.length && pattern[i] != null) {
                return pattern[i].n;
            }
        }
        return new IntNTitleNumber(0);
    }

    public NTitleSequence next(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("invalid level. must be >= 1");
        }
        int max = Math.max(level, value.length);
        NTitleNumberAndSep[] pattern = new NTitleNumberAndSep[max];
        for (int i = level; i < value.length; i++) {
            pattern[i] = new NTitleNumberAndSep(value[i].n, value[i].sep);
        }
        for (int i = 0; i < level; i++) {
            NTitleNumber nn = numberAt(i, pattern);
            if (nn.isNone()) {
                nn = nn.first();
            }
            pattern[i] = new NTitleNumberAndSep(nn, getSepAt(i));
        }
        NTitleNumber nn = numberAt(level, pattern);
        pattern[level - 1].n = nn.next();
        pattern[level - 1].sep = getSepAt(level - 1);
        for (int i = level; i < pattern.length; i++) {
            pattern[i].n = value[i].n.none();
            pattern[i].sep = value[i].sep;
        }
        return newInstance(pattern);
    }

    private String getSepAt(int i) {
        if (i >= value.length) {
            i = value.length - 1;
        }
        if (i >= 0) {
            return value[i].sep;
        }
        return ".";
    }

    private int depth() {
        for (int i = 0; i < value.length; i++) {
            if (value[i].n.isNone()) {
                return i;
            }
        }
        return value.length;
    }

    @Override
    public NTitleNumber getNumber(int index) {
        if (index >= 0 && index < value.length) {
            if (value[index] == null || value[index].n.isNone()) {
                return null;
            }
            return value[index].n;
        }
        return null;
    }

    @Override
    public int size() {
        return value.length;
    }

    //    @Override
    public List<NTitleNumber> getPattern() {
        NTitleNumber[] pattern = new NTitleNumber[value.length];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = value[i].n.none();
        }
        return new ArrayList<>(Arrays.asList(pattern));
    }

    public List<NTitleNumber> getValue() {
        List<NTitleNumber> ok = new ArrayList<>();
        for (int i = 0; i < value.length; i++) {
            if (value[i] == null || value[i].n.isNone()) {
                break;
            }
            ok.add(value[i].n);
        }
        return ok;
    }

    protected NTitleSequence newInstance(NTitleNumberAndSep[] newValue) {
        return new DefaultNTitleSequence(newValue, start, end, stringPattern);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int maxBullet = -1;
        int minBullet = -1;
        for (int i = 0; i < value.length; i++) {
            NTitleNumberAndSep e = value[i];
            if (!e.n.isNone() && e.n.isBullet()) {
                if (minBullet < 0) {
                    minBullet = i;
                }
                maxBullet = i;
            }
        }
        if (maxBullet > 0) {
            int j = maxBullet - minBullet + 2;
            for (int i = 0; i < j; i++) {
                sb.append(' ');
            }
            NTitleNumberAndSep h = value[maxBullet];
            sb.append(h.n);
        } else {
            for (int i = 0; i < value.length; i++) {
                NTitleNumberAndSep h = value[i];
                if (h.n.isNone()) {
                    break;
                }
                if (i > 0) {
                    if (h.sep != null) {
                        sb.append(h.sep);
                    }
                }
                sb.append(h.n);
            }
            if (sb.length() > 0) {
                sb.insert(0, start);
                sb.append(end);
            }
        }
        return sb.toString();
    }
}
