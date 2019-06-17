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

    private final boolean inclusiveLowerBoundary;
    private final boolean inclusiveUpperBoundary;
    private final String min;
    private final String max;

    public DefaultNutsVersionInterval(boolean inclusiveLowerBoundary, boolean inclusiveUpperBoundary, String min, String max) {
        this.inclusiveLowerBoundary = inclusiveLowerBoundary;
        this.inclusiveUpperBoundary = inclusiveUpperBoundary;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean acceptVersion(NutsVersion version) {
        if (!CoreStringUtils.isBlank(min) && !min.equals(NutsConstants.Versions.LATEST) && !min.equals(NutsConstants.Versions.RELEASE)) {
            int t = version.compareTo(min);
            if ((inclusiveLowerBoundary && t < 0) || (!inclusiveLowerBoundary && t <= 0)) {
                return false;
            }
        }
        if (!CoreStringUtils.isBlank(max) && !max.equals(NutsConstants.Versions.LATEST) && !max.equals(NutsConstants.Versions.RELEASE)) {
            int t = version.compareTo(max);
            return (!inclusiveUpperBoundary || t <= 0) && (inclusiveUpperBoundary || t < 0);
        }
        return true;
    }

    @Override
    public boolean isFixedValue() {
        return inclusiveLowerBoundary && inclusiveUpperBoundary && CoreStringUtils.trim(min).equals(CoreStringUtils.trim(max))
                && !NutsConstants.Versions.LATEST.equals(min) && !NutsConstants.Versions.RELEASE.equals(min);
    }

    @Override
    public String toString() {
        return (inclusiveLowerBoundary ? "[" : "]")
                + (min == null ? "" : min)
                + ","
                + (max == null ? "" : max)
                + (inclusiveUpperBoundary ? "]" : "[");
    }

}
