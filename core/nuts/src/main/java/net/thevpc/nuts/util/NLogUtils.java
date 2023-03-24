package net.thevpc.nuts.util;

import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

import java.util.logging.Level;

public class NLogUtils {
    private NLogUtils() {
    }
    public static NOptional<Level> parseLogLevel(String value) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return NOptional.ofNamedEmpty("log level");
        }
        switch (value.trim().toLowerCase()) {
            case "off": {
                return NOptional.of(Level.OFF);
            }
            case "verbose":
            case "finest": {
                return NOptional.of(Level.FINEST);
            }
            case "finer": {
                return NOptional.of(Level.FINER);
            }
            case "fine": {
                return NOptional.of(Level.FINE);
            }
            case "info": {
                return NOptional.of(Level.INFO);
            }
            case "all": {
                return NOptional.of(Level.ALL);
            }
            case "warning": {
                return NOptional.of(Level.WARNING);
            }
            case "severe": {
                return NOptional.of(Level.SEVERE);
            }
            case "config": {
                return NOptional.of(Level.CONFIG);
            }
        }
        Integer i = NLiteral.of(value).asInt().orNull();
        if (i != null) {
            switch (i) {
                case Integer.MAX_VALUE:
                    return NOptional.of(Level.OFF);
                case 1000:
                    return NOptional.of(Level.SEVERE);
                case 900:
                    return NOptional.of(Level.WARNING);
                case 800:
                    return NOptional.of(Level.INFO);
                case 700:
                    return NOptional.of(Level.CONFIG);
                case 500:
                    return NOptional.of(Level.FINE);
                case 400:
                    return NOptional.of(Level.FINER);
                case 300:
                    return NOptional.of(Level.FINEST);
                case Integer.MIN_VALUE:
                    return NOptional.of(Level.ALL);
            }
            return NOptional.of(new CustomLogLevel("LEVEL" + i, i));
        }
        String finalValue = value;
        return NOptional.ofError(s -> NMsg.ofC("invalid level %s", finalValue));
    }

    private static class CustomLogLevel extends Level {
        public CustomLogLevel(String name, int value) {
            super(name, value);
        }
    }
}
