/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.version;

import java.io.Serializable;

import net.thevpc.nuts.*;

/**
 * Created by vpc on 2/1/17.
 *
 * @since 0.5.4
 */
public class DefaultNutsVersionInterval implements NutsVersionInterval, Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean includeLowerBound;
    private final boolean includeUpperBound;
    private final String lowerBound;
    private final String upperBound;

    public DefaultNutsVersionInterval(boolean inclusiveLowerBoundary, boolean inclusiveUpperBoundary, String min, String max) {
        this.includeLowerBound = inclusiveLowerBoundary;
        this.includeUpperBound = inclusiveUpperBoundary;
        this.lowerBound = NutsUtilStrings.trimToNull(min);
        this.upperBound = NutsUtilStrings.trimToNull(max);
    }

    @Override
    public boolean acceptVersion(NutsVersion version) {
        if (!NutsBlankable.isBlank(lowerBound) && !lowerBound.equals(NutsConstants.Versions.LATEST) && !lowerBound.equals(NutsConstants.Versions.RELEASE)) {
            int t = version.compareTo(lowerBound);
            if ((includeLowerBound && t < 0) || (!includeLowerBound && t <= 0)) {
                return false;
            }
        }
        if (!NutsBlankable.isBlank(upperBound) && !upperBound.equals(NutsConstants.Versions.LATEST) && !upperBound.equals(NutsConstants.Versions.RELEASE)) {
            int t = version.compareTo(upperBound);
            return (!includeUpperBound || t <= 0) && (includeUpperBound || t < 0);
        }
        return true;
    }

    @Override
    public boolean isFixedValue() {
        return includeLowerBound && includeUpperBound && NutsUtilStrings.trim(lowerBound).equals(NutsUtilStrings.trim(upperBound))
                && !NutsConstants.Versions.LATEST.equals(lowerBound) && !NutsConstants.Versions.RELEASE.equals(lowerBound);
    }

    @Override
    public boolean isIncludeLowerBound() {
        return includeLowerBound;
    }

    @Override
    public boolean isIncludeUpperBound() {
        return includeUpperBound;
    }

    @Override
    public String getLowerBound() {
        return lowerBound;
    }

    @Override
    public String getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        if (lowerBound != null && lowerBound.equals(upperBound) && includeLowerBound && includeUpperBound) {
            return "[" + lowerBound + "]";
        }
        return (includeLowerBound ? "[" : "]")
                + (lowerBound == null ? "" : lowerBound)
                + ","
                + (upperBound == null ? "" : upperBound)
                + (includeUpperBound ? "]" : "[");
    }

}
