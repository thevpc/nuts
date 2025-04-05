/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NIdLocation;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author thevpc
 */
public class NCoreCollectionUtils {
    public static <T> boolean addAllNonNull(Collection<T> container, Collection<T> newElements) {
        boolean someAdded = false;
        if(newElements !=null){
            for (T t : newElements) {
                if(t!=null){
                    container.add(t);
                    someAdded=true;
                }
            }
        }
        return someAdded;
    }

    public static <T> List<T> unmodifiableList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(nonNullList(other));
    }

    public static <T, V> Map<T, V> nonNullMap(Map<T, V> other) {
        if (other == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(other);
    }


    public static <T> List<T> nonNullList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(other);
    }

    public static Set<String> toTrimmedNonEmptySet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = NStringUtils.trim(a);
                if (!NBlankable.isBlank(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static ArrayList<String> toDistinctTrimmedNonEmptyList(String[] values0) {
        return new ArrayList<>(toTrimmedNonEmptySet(values0));
    }

    public static Set<NIdLocation> toSet(NIdLocation[] classifierMappings) {
        LinkedHashSet<NIdLocation> set = new LinkedHashSet<>();
        if (classifierMappings != null) {
            for (NIdLocation a : classifierMappings) {
                if (a != null) {
                    set.add(a);
                }
            }
        }
        return set;
    }



    public static <T, V> Map<T, V> unmodifiableMap(Map<T, V> other) {
        return other == null ? Collections.emptyMap() : Collections.unmodifiableMap(nonNullMap(other));
    }

    public static <T> List<T> nonNullListFromArray(T[] other) {
        return nonNullList(Arrays.asList(other));
    }

    public static <T> Stream<T> finiteStream(Supplier<T> supplier){
        return stream(supplier, null);
    }
    public static <T> Stream<T> stream(Supplier<T> supplier, Predicate<T> stopCondition){
        if(stopCondition==null){
            stopCondition= Objects::isNull;
        }
        Predicate<T> finalStopCondition = stopCondition;
        return stream(new Iterator<T>() {
            T value;
            @Override
            public boolean hasNext() {
                value = supplier.get();
                if(finalStopCondition.test(value)){
                    return false;
                }
                return true;
            }

            @Override
            public T next() {
                return value;
            }
        });
    }
    public static <T> Stream<T> stream(Iterator<T> iterator){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }
}
