package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateNutsUtilCollections {
    public static <T,V> Map<T,V> nonNullMap(Map<T,V> other) {
        if (other == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(other);
    }

    public static <T> List<T> nonNullListFromArray(T[] other) {
        return nonNullList(Arrays.asList(other));
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

    public static <T> List<T> addUniqueNonBlankList(List<T> list,Collection<T> other) {
        if(other!=null){
            for (T t : other) {
                if(!NutsBlankable.isBlank(t)){
                    if(!list.contains(t)){
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
    public static <T,V> Map<T,V> unmodifiableMap(Map<T,V> other) {
        return other == null ? Collections.emptyMap() : Collections.unmodifiableMap(nonNullMap(other));
    }

    public static <T> List<T> unmodifiableUniqueList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(uniqueList(other));
    }

    public static <K, V> LinkedHashMap<K, V> copy(Map<K, V> o) {
        if (o == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(o);
    }

    public static <K> LinkedHashSet<K> copy(Set<K> o) {
        if (o != null) {
            return new LinkedHashSet<>(o);
        }
        return new LinkedHashSet<>();
    }
}
