package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsTitleNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.NutsTextNumbering;

/**
 */
public class DefaultNutsTitleNumberSequence implements NutsTextNumbering {
    private NutsTitleNumber[] value;
    private String[] separators;

    public DefaultNutsTitleNumberSequence(String pattern) {
        List<NutsTitleNumber> p = new ArrayList<>();
        List<String> s = new ArrayList<>();
        for (char c : pattern.toCharArray()) {
            switch (c) {
                case '0':
                case '1': {
                    p.add(new IntNutsTitleNumber(0));
                    break;
                }
                case 'A': {
                    p.add(AlphabetNutsTitleNumber.ofUpperCased());
                    break;
                }
                case 'a': {
                    p.add(AlphabetNutsTitleNumber.ofLowerCased());
                    break;
                }
                case '.':
                case '-':
                case '/': {
                    s.add(String.valueOf(c));
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("unsupported sequence type " + pattern + " (error at '" + c + "')");
                }
            }
        }
        this.value = p.toArray(new NutsTitleNumber[0]);
    }

    public DefaultNutsTitleNumberSequence(NutsTitleNumber... pattern) {
        this.value = new NutsTitleNumber[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            this.value[i] = pattern[i];
        }
        this.separators = new String[0];
    }

    @Override
    public List<NutsTitleNumber> getPattern() {
        NutsTitleNumber[] pattern = new NutsTitleNumber[value.length];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = value[i].none();
        }
        return new ArrayList<>(Arrays.asList(pattern));
    }

    public NutsTitleNumber numberAt(int level, NutsTitleNumber[] all0, NutsTitleNumber[] all) {
        int mm = Math.min(all0.length, all.length);
        int a = Math.min(level, mm - 1);
        for (int i = a; i >= 0; i--) {
            if (i < all0.length && all0[i] != null) {
                return all0[i];
            }
            if (i < all.length && all[i] != null) {
                return all[i];
            }
        }
        return new IntNutsTitleNumber(0);
    }

    public NutsTextNumbering newLevel(int level) {
//        level = level - 1;
        if (level <= 0) {
            throw new IllegalArgumentException("invalid level. must be >= 1");
        }
        int max = level >= value.length ? level + 1 : value.length;
        NutsTitleNumber[] pattern = new NutsTitleNumber[max];
        for (int i = level + 1; i < value.length; i++) {
            pattern[i] = value[i];
        }
        for (int i = 0; i < level; i++) {
            NutsTitleNumber nn = numberAt(i, value, pattern);
            if (nn.isNone()) {
                nn = nn.first();
            }
            pattern[i] = nn;
        }
        NutsTitleNumber nn = numberAt(level, value, pattern);
        pattern[level] = nn.next();
        for (int i = level + 1; i < pattern.length; i++) {
            pattern[i] = value[i].none();
        }
        return setValue(pattern);
    }

    private int depth() {
        for (int i = 0; i < value.length; i++) {
            if (value[i].isNone()) {
                return i;
            }
        }
        return value.length;
    }

    @Override
    public NutsTitleNumber getNumber(int index) {
        if (index >= 0 && index < value.length) {
            if (value[index] == null || value[index].isNone()) {
                return null;
            }
            return value[index];
        }
        return null;
    }

    @Override
    public int size() {
        return value.length;
    }

    public List<NutsTitleNumber> getValue() {
        List<NutsTitleNumber> ok = new ArrayList<>();
        for (int i = 0; i < value.length; i++) {
            if (value[i] == null || value[i].isNone()) {
                break;
            }
            ok.add(value[i]);
        }
        return ok;
    }

    //        @Override
    public NutsTextNumbering setValue(NutsTitleNumber[] newValue) {
        return new DefaultNutsTitleNumberSequence(newValue);
    }

    @Override
    public String getSeparator(int index) {
        if (index >= 0) {
            String sep = ".";
            if (index < separators.length) {
                sep = separators[index];
            } else if (separators.length > 0) {
                sep = separators[separators.length - 1];
            }
            return sep;
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<NutsTitleNumber> v = getValue();
        for (int i = 0; i < v.size(); i++) {
            if (i > 0) {
                sb.append(getSeparator(i - 1));
            }
            sb.append(v.get(i).toString());
        }
        return sb.toString();
    }
}
