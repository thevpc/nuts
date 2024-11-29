/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.thevpc.nuts.env.NIdLocation;

/**
 *
 * @author thevpc
 */
public class NCoreArrayUtils {

    public static String[] concatArrays(String[]... arrays) {
        return concatArrays(String.class, arrays);
    }

    public static <T> T[] concatArrays(Class<T> cls, T[]... arrays) {
        List<T> all = new ArrayList<>();
        if (arrays != null) {
            for (T[] v : arrays) {
                if (v != null) {
                    all.addAll(Arrays.asList(v));
                }
            }
        }
        return all.toArray((T[]) Array.newInstance(cls, all.size()));
    }

    public static List<String> toDistinctTrimmedNonEmptyList(List<String> values0) {
        Set<String> set = NCoreCollectionUtils.toTrimmedNonEmptySet(
                values0==null?null:values0.toArray(new String[0])
        );
        return new ArrayList<>(set);
    }
    public static List<String> toDistinctTrimmedNonEmptyList(List<String> values0, List<String>... values) {
        Set<String> set = NCoreCollectionUtils.toTrimmedNonEmptySet(
                values0==null?null:values0.toArray(new String[0])
        );
        if (values != null) {
            for (List<String> value : values) {
                set.addAll(NCoreCollectionUtils.toTrimmedNonEmptySet(
                        values0==null?null:values0.toArray(new String[0])
                ));
            }
        }
        return new ArrayList<>(set);
    }

    public static String[] toDistinctTrimmedNonEmptyArray(String[] values0, String[]... values) {
        Set<String> set = NCoreCollectionUtils.toTrimmedNonEmptySet(values0);
        if (values != null) {
            for (String[] value : values) {
                set.addAll(NCoreCollectionUtils.toTrimmedNonEmptySet(value));
            }
        }
        return set.toArray(new String[0]);
    }

    public static NIdLocation[] toArraySet(NIdLocation[] classifierMappings) {
        Set<NIdLocation> set = NCoreCollectionUtils.toSet(classifierMappings);
        return set.toArray(new NIdLocation[0]);
    }
    
}
