/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public class NutsCommandArg {

    private final String stringValue;

    public NutsCommandArg(String line) {
        this.stringValue = line;
    }

    public boolean isOption() {
        return stringValue.startsWith("-");
    }

    public boolean isNonOption() {
        return !stringValue.startsWith("-");
    }

    public boolean isKeyValue() {
        return stringValue.indexOf('=') >= 0;
    }

    public boolean hasValue() {
        return stringValue.indexOf('=') >= 0;
    }

    public String strKey() {
        return getStrKey();
    }
    
    public String getStrKey() {
        return getKey().getString();
    }
    
    public NutsCommandArg getKey() {
        int x = stringValue.indexOf('=');
        if (x >= 0) {
            String p = stringValue.substring(0, x);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < p.length()) {
                switch (p.charAt(i)) {
                    case '-': {
                        sb.append(p.charAt(i));
                        break;
                    }
                    case '!': {
                        sb.append(p.substring(i + 1));
                        return new NutsCommandArg(sb.toString());
                    }
                    case '/': {
                        if (sb.length() > 0 && i + 1 < p.length() && p.charAt(i + 1) == '/') {
                            sb.append(p.substring(i + 2));
                            return new NutsCommandArg(sb.toString());
                        }
                    }
                    default: {
                        return new NutsCommandArg(p);
                    }
                }
                i++;
            }
            return new NutsCommandArg(p);
        }
        return this;
    }

    public NutsCommandArg getName() {
        int x = stringValue.indexOf('=');
        if (x >= 0) {
            return new NutsCommandArg(stringValue.substring(0, x));
        }
        return new NutsCommandArg(null);
    }

    public NutsCommandArg getValue() {
        int x = stringValue.indexOf('=');
        if (x >= 0) {
            return new NutsCommandArg(stringValue.substring(x + 1));
        }
        return new NutsCommandArg(null);
    }

    public String getString() {
        return stringValue;
    }

    public boolean isNull() {
        return stringValue == null;
    }

    public boolean isBlank() {
        return stringValue == null || stringValue.trim().isEmpty();
    }

    public boolean isEmpty() {
        return stringValue == null || stringValue.isEmpty();
    }

    public boolean isNegated() {
        int i = 0;
        while (i < stringValue.length()) {
            switch (stringValue.charAt(i)) {
                case '-': {
                    //ignore leading dashes
                    break;
                }
                case '!': {
                    return true;
                }
                default: {
                    return false;
                }
            }
            i++;
        }
        return false;
    }

    public boolean isComment() {
        int i = 0;
        boolean opt = false;
        boolean slash = false;
        while (i < stringValue.length()) {
            switch (stringValue.charAt(i)) {
                case '-': {
                    opt = true;
                    break;
                }
                case '/': {
                    if (!opt) {
                        return false;
                    }
                    if (slash) {
                        return true;
                    }
                    slash = true;
                    break;
                }
                default: {
                    return false;
                }
            }
            i++;
        }
        return false;
    }

    public boolean isInt() {
        try {
            if (stringValue != null) {
                Integer.parseInt(stringValue);
                return true;
            }
        } catch (NumberFormatException ex) {
        }
        return false;
    }

    public int getInt() {
        return getInt(0);
    }

    public int getInt(int defaultValue) {
        if (NutsUtils.isBlank(stringValue)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean isLong() {
        try {
            if (stringValue != null) {
                Long.parseLong(stringValue);
                return true;
            }
        } catch (NumberFormatException ex) {
        }
        return false;
    }

    public long getLong() {
        return getLong(0);
    }

    public long getLong(long defaultValue) {
        if (NutsUtils.isBlank(stringValue)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(stringValue);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean getBoolean() {
        return getBoolean(false);
    }

    public boolean isBoolean() {
        if (stringValue != null) {
            switch (stringValue.trim().toLowerCase()) {
                case "ok":
                case "true":
                case "yes":
                case "always":
                case "enable":
                case "enabled":
                case "on":
                case "y":
                case "false":
                case "no":
                case "none":
                case "never":
                case "disable":
                case "n":
                case "off":
                    return true;
            }
        }
        return false;
    }

    public boolean getBoolean(boolean defaultValue) {
        if (stringValue == null) {
            return defaultValue;
        }
        switch (stringValue.trim().toLowerCase()) {
            case "ok":
            case "true":
            case "yes":
            case "always":
            case "enable":
            case "enabled":
            case "on":
            case "y":
                return true;
            case "false":
            case "no":
            case "none":
            case "never":
            case "disable":
            case "disabled":
            case "n":
            case "off":
                return false;
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return String.valueOf(stringValue);
    }

    public boolean getBooleanValue() {
        return getValue().getBoolean(!isNegated());
    }

}
