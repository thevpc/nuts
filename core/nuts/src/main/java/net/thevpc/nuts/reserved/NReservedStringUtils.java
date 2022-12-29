package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NValue;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NReservedStringUtils {
    public static List<String> splitDefault(String str) {
        return NStringUtils.split(str, " ;,\n\r\t|", true, true);
    }

    public static List<String> parseAndTrimToDistinctList(String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        return splitDefault(s).stream().map(String::trim)
                .filter(x -> x.length() > 0)
                .distinct().collect(Collectors.toList());
    }

    public static String joinAndTrimToNull(List<String> args) {
        return NStringUtils.trimToNull(
                String.join(",", args)
        );
    }

    public static NOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(session -> NMsg.ofPlain("empty size"));
        }
        value = value.trim();
        Integer i = NValue.of(value).asInt().orNull();
        if (i != null) {
            if (defaultMultiplier != null) {
                return NOptional.of(i * defaultMultiplier);
            } else {
                return NOptional.of(i);
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = NValue.of(v).asInt().orNull();
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return NOptional.of(i * 1024);
                        case "m":
                        case "mb":
                            return NOptional.of(i * 1024 * 1024);
                        case "g":
                        case "gb":
                            return NOptional.of(i * 1024 * 1024 * 1024);
                    }
                }
            }
        }
        String finalValue = value;
        return NOptional.ofError(session -> NMsg.ofCstyle("invalid size :%s", finalValue));
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (char aChar : chars) {
                if (value[i] == aChar) {
                    return i;
                }
            }
        }
        return -1;
    }
}
