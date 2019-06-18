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
package net.vpc.app.nuts.core.filters.version;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vpc.app.nuts.core.DefaultNutsVersionInterval;
import net.vpc.app.nuts.core.filters.id.NutsScriptAwareIdFilter;

/**
 * Examples [2.6,], ]2.6,] .
 * Created by vpc on 1/20/17.
 */
public class DefaultNutsVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsScriptAwareIdFilter, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * version intervals can be in one of the following forms
     * <pre>
     * [ version, ]
     * ] version, ] or ( version, ]
     * [ version, [ or [ version, )
     * ] version, [ or ] version, [
     * 
     * [ ,version ]
     * ] ,version ] or ( ,version ]
     * [ ,version [ or [ ,version )
     * ] ,version [ or ] ,version [
     * 
     * [ version1 , version2 ]
     * ] version1 , version2 ] or ( version1 , version2 ]
     * [ version1 , version2 [ or [ version1 , version2 )
     * ] version1 , version2 [ or ] version1 , version2 [
     * 
     * comma or space separated intervals such as :
     *   [ version1 , version2 ], [ version1 , version2 ]
     *   [ version1 , version2 ]  [ version1 , version2 ]
     * </pre>
     */
    public static final Pattern NUTS_VERSION_PATTERN = Pattern.compile("(((?<VAL1>(?<L1>[\\[\\]\\(])(?<LV1>[^\\[\\]\\(\\),]*),(?<RV1>[^\\[\\]\\(\\),]*)(?<R1>[\\[\\]\\)]))|(?<VAL2>(?<L2>[\\[\\]\\(])(?<V2>[^\\[\\]\\(\\),]*)(?<R2>[\\[\\]\\)]))|(?<VAL3>(?<V3>[^\\[\\\\(\\)], ]+)))(\\s|,|\n)*)");
    private final List<NutsVersionInterval> intervals = new ArrayList<>();

    @Override
    public boolean accept(NutsVersion version, NutsSession session) {
        if (intervals.isEmpty()) {
            return true;
        }
        for (NutsVersionInterval value : intervals) {
            if (value.acceptVersion(version)) {
                return true;
            }
        }
        return false;
    }

    public NutsVersionInterval[] getIntervals() {
        return intervals.toArray(new NutsVersionInterval[0]);
    }

    public static NutsVersionFilter parse(String version) {
        if (DefaultNutsVersion.isBlank(version)) {
            return AllNutsVersionFilter.INSTANCE;
        }

        DefaultNutsVersionFilter d = new DefaultNutsVersionFilter();

        Matcher y = NUTS_VERSION_PATTERN.matcher(version);
        while (y.find()) {
            if (y.group("VAL1") != null) {
                boolean inclusiveLowerBoundary = y.group("L1").equals("[");
                boolean inclusiveUpperBoundary = y.group("R1").equals("]");
                String min = y.group("LV1");
                String max = y.group("RV1");
                d.add(new DefaultNutsVersionInterval(inclusiveLowerBoundary, inclusiveUpperBoundary, min, max));
            } else if (y.group("VAL2") != null) {
                boolean inclusiveLowerBoundary = y.group("L2").equals("[");
                boolean inclusiveUpperBoundary = y.group("R2").equals("]");
                String val = y.group("V2");
                //  [a]  or ]a[
                if ((inclusiveLowerBoundary && inclusiveUpperBoundary) || (!inclusiveLowerBoundary && !inclusiveUpperBoundary)) {
                    d.add(new DefaultNutsVersionInterval(inclusiveLowerBoundary, inclusiveUpperBoundary, val, val));
                    // ]a]    == ],a]
                } else if (!inclusiveLowerBoundary) {
                    d.add(new DefaultNutsVersionInterval(false, true, null, val));
                    // [a[    == [a,[
                } else if (!inclusiveUpperBoundary) {
                    d.add(new DefaultNutsVersionInterval(false, true, val, null));
                }
            } else {
                String v3 = y.group("V3");
                if (v3.endsWith("*")) {
                    String min = v3.substring(0, v3.length() - 1);
                    String max = DefaultNutsVersion.valueOf(min).inc(-1).getValue();
                    d.add(new DefaultNutsVersionInterval(true, false, min, max));
                } else {
                    d.add(new DefaultNutsVersionInterval(true, true, v3, v3));
                }
            }
        }

        return d;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NutsVersionInterval interval : intervals) {
            if (sb.length() > 0) {
                sb.append(",  ");
            }
            sb.append(interval.toString());
        }
        return sb.toString();
    }

    public void add(NutsVersionInterval interval) {
        intervals.add(interval);
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        return "id.version.matches('" + CoreStringUtils.escapeCoteStrings(toString()) + "')";
    }

    @Override
    public NutsVersionFilter simplify() {
        if (intervals.isEmpty()) {
            return null;
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.intervals);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsVersionFilter other = (DefaultNutsVersionFilter) obj;
        if (!Objects.equals(this.intervals, other.intervals)) {
            return false;
        }
        return true;
    }

}
