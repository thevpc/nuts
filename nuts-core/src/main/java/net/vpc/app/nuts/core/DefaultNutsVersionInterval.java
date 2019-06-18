/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.io.Serializable;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionInterval;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

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
        this.lowerBound = CoreStringUtils.trimToNull(min);
        this.upperBound = CoreStringUtils.trimToNull(max);
    }

    @Override
    public boolean acceptVersion(NutsVersion version) {
        if (!CoreStringUtils.isBlank(lowerBound) && !lowerBound.equals(NutsConstants.Versions.LATEST) && !lowerBound.equals(NutsConstants.Versions.RELEASE)) {
            int t = version.compareTo(lowerBound);
            if ((includeLowerBound && t < 0) || (!includeLowerBound && t <= 0)) {
                return false;
            }
        }
        if (!CoreStringUtils.isBlank(upperBound) && !upperBound.equals(NutsConstants.Versions.LATEST) && !upperBound.equals(NutsConstants.Versions.RELEASE)) {
            int t = version.compareTo(upperBound);
            return (!includeUpperBound || t <= 0) && (includeUpperBound || t < 0);
        }
        return true;
    }

    @Override
    public boolean isFixedValue() {
        return includeLowerBound && includeUpperBound && CoreStringUtils.trim(lowerBound).equals(CoreStringUtils.trim(upperBound))
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
        if (lowerBound != null && upperBound != null && lowerBound.equals(upperBound) && includeLowerBound && includeUpperBound) {
            return "[" + lowerBound + "]";
        }
        return (includeLowerBound ? "[" : "]")
                + (lowerBound == null ? "" : lowerBound)
                + ","
                + (upperBound == null ? "" : upperBound)
                + (includeUpperBound ? "]" : "[");
    }

}
