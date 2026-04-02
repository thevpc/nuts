package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.Collection;
import java.util.function.Function;

public class NEnumUtils {
    private NEnumUtils() {
    }
    public static <T extends Enum> NOptional<T> parseEnum(String value, Class<T> type) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(() -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String normalizedValue = NNameFormat.CONST_NAME.format(value);
        try {
            return NOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NOptional.ofError(() -> NMsg.ofC(type.getSimpleName() + " invalid value : %s", value));
        }
    }

    public static <T extends Enum> NOptional<T> parseEnum(String value, Class<T> type, Function<NEnumCandidate, NOptional<T>> mapper) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(() -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String[] parsedValue = NNameFormat.parse(value);
        String normalizedValue = NNameFormat.CONST_NAME.format(parsedValue);
        if (mapper != null) {
            try {
                NOptional<T> o = mapper.apply(new NEnumCandidate(
                        value,
                        normalizedValue,
                        parsedValue
                ));
                if (o != null) {
                    return o;
                }
            } catch (Exception notFound) {
                //just ignore
            }
        }
        try {
            return NOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NOptional.ofError(() -> NMsg.ofC("%s invalid value : %s", type.getSimpleName(), value), notFound);
        }
    }
    public static <T extends NEnum> NOptional<T> parseEnum(String value, Class<T> type, Collection<T> knownValues, Function<NEnumCandidate, NOptional<T>> mapper) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(() -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String[] parsedValue = NNameFormat.parse(value);
        String normalizedValue = NNameFormat.CONST_NAME.format(parsedValue);
        if (mapper != null) {
            try {
                NOptional<T> o = mapper.apply(new NEnumCandidate(
                        value,
                        normalizedValue,
                        parsedValue
                ));
                if (o != null) {
                    return o;
                }
            } catch (Exception notFound) {
                //just ignore
            }
        }
        for (T v : knownValues) {
            if (NNameFormat.CONST_NAME.format(v.id()).equals(normalizedValue)) {
                return NOptional.of(v);
            }
        }
        return NOptional.ofError(() -> NMsg.ofC("%s invalid value : %s", type.getSimpleName(), value));
    }

    public static class NEnumCandidate {
        private String value;
        private String normalizedValue;
        private String[] parsedParts;

        public NEnumCandidate(String value, String normalizedValue, String[] parsedParts) {
            this.value = value;
            this.normalizedValue = normalizedValue;
            this.parsedParts = parsedParts;
        }

        public String value() {
            return value;
        }

        /**
         * enum name formatted using NNameFormat.CONST_NAME
         * @return name formatted using NNameFormat.CONST_NAME
         */
        public String normalizedValue() {
            return normalizedValue;
        }

        public String[] parsedParts() {
            return parsedParts;
        }
    }
}
