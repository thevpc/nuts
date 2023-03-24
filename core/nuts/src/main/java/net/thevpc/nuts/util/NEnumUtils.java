package net.thevpc.nuts.util;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

import java.util.function.Function;

public class NEnumUtils {
    private NEnumUtils() {
    }
    public static <T extends Enum> NOptional<T> parseEnum(String value, Class<T> type) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String normalizedValue = NNameFormat.CONST_NAME.format(value);
        try {
            return NOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NOptional.ofError(s -> NMsg.ofC(type.getSimpleName() + " invalid value : %s", value));
        }
    }

    public static <T extends Enum> NOptional<T> parseEnum(String value, Class<T> type, Function<EnumValue, NOptional<T>> mapper) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String[] parsedValue = NNameFormat.parse(value);
        String normalizedValue = NNameFormat.CONST_NAME.format(parsedValue);
        if (mapper != null) {
            try {
                NOptional<T> o = mapper.apply(new EnumValue(
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
            return NOptional.ofError(s -> NMsg.ofC("%s invalid value : %s", type.getSimpleName(), value), notFound);
        }
    }

    public static class EnumValue {
        private String value;
        private String normalizedValue;
        private String[] parsedValue;

        public EnumValue(String value, String normalizedValue, String[] parsedValue) {
            this.value = value;
            this.normalizedValue = normalizedValue;
            this.parsedValue = parsedValue;
        }

        public String getValue() {
            return value;
        }

        public String getNormalizedValue() {
            return normalizedValue;
        }

        public String[] getParsedValue() {
            return parsedValue;
        }
    }
}
