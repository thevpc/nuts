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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.dependency.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.util.NutsFunction;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author thevpc
 */
public class NutsDependencyUtils {

    public static String normalizeDependencyType(String s1) {
        return NutsStringUtils.trimToNull(s1);
    }

    public static String toExclusionListString(NutsId[] exclusions){
        TreeSet<String> ex = new TreeSet<>();
        for (NutsId exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    public static boolean isRequiredDependency(NutsDependency d){
        if (d.isOptional()) {
            return false;
        }
        if (NutsDependencyScope.parse(d.getScope()).orElse(NutsDependencyScope.API) == NutsDependencyScope.SYSTEM) {
            //NutsEnvCondition c = d.getDependency().getCondition();
            //if(c!=null && c.getProfiles().length>0) {
            //Should add some log!
            return false;
            //}
        }
        return true;
    }

    public static Iterator<NutsDependency> itIdToDep(NutsIterator<NutsId> id, NutsSession session) {
        return IteratorBuilder.of(id, session).map(NutsFunction.of(NutsId::toDependency, "IdToDependency")).build();
    }

    public static Iterator<NutsDependency> itIdToDep(NutsIterator<NutsId> id, NutsDependency copyFrom, NutsSession session) {
        String _optional = copyFrom.getOptional();
        String _scope = copyFrom.getScope();
        return IteratorBuilder.of(id, session).map(NutsFunction.of(
                x -> x.toDependency().builder().setOptional(_optional).setScope(_scope).build(), "IdToDependency")).build();
    }

    public static boolean isDefaultOptional(String s1) {
        s1 = NutsStringUtils.trim(s1);
        return s1.isEmpty() || s1.equals("false");
    }
}
