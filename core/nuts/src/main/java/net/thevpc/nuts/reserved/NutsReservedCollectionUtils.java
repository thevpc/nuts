package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsEnvCondition;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NutsReservedCollectionUtils {
    public static <T, V> Map<T, V> nonNullMap(Map<T, V> other) {
        if (other == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(other);
    }

    public static <T> List<T> nonNullListFromArray(T[] other) {
        return nonNullList(Arrays.asList(other));
    }

    public static <T> List<T> unmodifiableOrNullList(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableList(new ArrayList<>(other));
    }

    public static <T> Set<T> unmodifiableOrNullSet(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(other));
    }

    public static <T, V> Map<T, V> unmodifiableOrNullMap(Map<T, V> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(other));
    }

    public static <T> List<T> copyOrNullList(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return new ArrayList<>(other);
    }

    public static <T> List<T> nonNullList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(other);
    }

    public static <T> Set<T> nonNullSet(Collection<T> other) {
        if (other == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(other);
    }

    public static <T> List<T> uniqueNonBlankList(Collection<T> other) {
        return uniqueList(other).stream().filter(x -> !NutsBlankable.isBlank(x)).collect(Collectors.toList());
    }

    public static <T> List<T> addUniqueNonBlankList(List<T> list, Collection<T> other) {
        if (other != null) {
            for (T t : other) {
                if (!NutsBlankable.isBlank(t)) {
                    if (!list.contains(t)) {
                        list.add(t);
                    }
                }
            }
        }
        return list;
    }

    public static <T> Set<T> nonBlankSet(Collection<T> other) {
        return set(other).stream().filter(x -> !NutsBlankable.isBlank(x)).collect(Collectors.toSet());
    }

    public static <T> List<T> uniqueList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(other));
    }

    public static <T> Set<T> set(Collection<T> other) {
        if (other == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(other);
    }

    public static <T> List<T> unmodifiableList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(nonNullList(other));
    }

    public static <T, V> Map<T, V> unmodifiableMap(Map<T, V> other) {
        return other == null ? Collections.emptyMap() : Collections.unmodifiableMap(nonNullMap(other));
    }

    public static <T> List<T> unmodifiableUniqueList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(uniqueList(other));
    }

    public static Map<String, String> toMap(NutsEnvCondition condition) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;
        if (condition.getArch() != null) {
            s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = NutsReservedUtils.formatStringIdList(condition.getPlatform());
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfile() != null) {
            s = condition.getProfile().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.PROFILE, s);
            }
        }
        if (condition.getProperties() != null) {
            Map<String, String> properties = condition.getProperties();
            if (!properties.isEmpty()) {
                m.put(NutsConstants.IdProperties.CONDITIONAL_PROPERTIES, NutsStringUtils.formatDefaultMap(properties));
            }
        }
        return m;
    }
}
