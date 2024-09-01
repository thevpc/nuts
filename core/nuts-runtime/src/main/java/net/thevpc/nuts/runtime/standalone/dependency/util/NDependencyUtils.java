/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.runtime.standalone.dependency.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author thevpc
 */
public class NDependencyUtils {

    public static String normalizeDependencyType(String s1) {
        return NStringUtils.trimToNull(s1);
    }

    public static String toExclusionListString(NId[] exclusions) {
        TreeSet<String> ex = new TreeSet<>();
        for (NId exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    public static boolean isRequiredDependency(NDependency d) {
        if (d.isOptional()) {
            return false;
        }
        if (NDependencyScope.parse(d.getScope()).orElse(NDependencyScope.API) == NDependencyScope.SYSTEM) {
            //NutsEnvCondition c = d.getDependency().getCondition();
            //if(c!=null && c.getProfiles().length>0) {
            //Should add some log!
            return false;
            //}
        }
        return true;
    }

    public static Iterator<NDependency> itIdToDep(NIterator<NId> id, NSession session) {
        return IteratorBuilder.of(id, session).map(NFunction.of(NId::toDependency).withDesc(NEDesc.of("IdToDependency"))).build();
    }

    public static Iterator<NDependency> itIdToDep(NIterator<NId> id, NDependency copyFrom, NSession session) {
        String _optional = copyFrom.getOptional();
        String _scope = copyFrom.getScope();
        return IteratorBuilder.of(id, session).map(NFunction.of(
                        (NId x) -> x.toDependency().builder()
                                .setOptional(_optional).setScope(_scope).build())
                .withDesc(NEDesc.of("IdToDependency"))
        ).build();
    }

    public static boolean isDefaultOptional(String s1) {
        s1 = NStringUtils.trim(s1);
        return s1.isEmpty() || s1.equals("false");
    }
}
