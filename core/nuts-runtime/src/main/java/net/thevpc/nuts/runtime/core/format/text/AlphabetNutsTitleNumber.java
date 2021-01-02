package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTitleNumber;

/**
 */
public class AlphabetNutsTitleNumber implements NutsTitleNumber {
    String[] names;
    String separator;
    String[] value;

    public static AlphabetNutsTitleNumber ofUpperCased() {
        String[] all = new String[26];
        for (int i = 'A'; i <= 'Z'; i++) {
            all[i - 'A'] = String.valueOf((char) i);
        }
        return new AlphabetNutsTitleNumber(all, "", new String[0]);
    }

    public static AlphabetNutsTitleNumber ofLowerCased() {
        String[] all = new String[26];
        for (int i = 'a'; i <= 'z'; i++) {
            all[i - 'a'] = String.valueOf((char) i);
        }
        return new AlphabetNutsTitleNumber(all, "", new String[0]);
    }

    public AlphabetNutsTitleNumber(String[] names, String separator, String[] value) {
        this.names = names;
        this.separator = separator;
        this.value = value;
    }

    @Override
    public NutsTitleNumber none() {
        return new AlphabetNutsTitleNumber(names, separator, new String[0]);
    }

    @Override
    public boolean isNone() {
        return value.length == 0;
    }

    @Override
    public NutsTitleNumber first() {
        return new AlphabetNutsTitleNumber(names, separator, new String[]{names[0]});
    }

    @Override
    public NutsTitleNumber next() {
        return new AlphabetNutsTitleNumber(names, separator, inc(0, value));
    }

    private int index(String n) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(n)) {
                return i;
            }
        }
        throw new IllegalArgumentException("invalid name " + n);
    }

    private String[] copy(String[] value) {
        String[] t = new String[value.length];
        System.arraycopy(value, 0, t, 0, value.length);
        return t;
    }

    private String[] ensureSize(int size, String[] value) {
        if (size > value.length) {
            String[] t = new String[value.length + 1];
            System.arraycopy(value, 0, t, 0, value.length);
            value = t;
        }
        return value;
    }

    private String[] inc(int pos, String[] value) {
        value = copy(value);
        value = ensureSize(pos + 1, value);
        if (value[pos] == null) {
            value[pos] = names[0];
        } else {
            int o = index(value[pos]);
            if (o < names.length - 1) {
                value[pos] = names[o + 1];
            } else {
                for (int i = 0; i <= pos; i++) {
                    value[i] = names[0];
                }
                return inc(pos + 1, value);
            }
        }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            if (value[i] == null) {
                break;
            }
            if (sb.length() > 0) {
                sb.insert(0, separator);
            }
            sb.insert(0, value[i]);
        }
        return sb.toString();
    }
}
