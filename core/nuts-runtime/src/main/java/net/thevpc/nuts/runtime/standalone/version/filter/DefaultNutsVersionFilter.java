/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsExprIdFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Examples [2.6,], ]2.6,] . Created by vpc on 1/20/17.
 */
public class DefaultNutsVersionFilter extends AbstractVersionFilter implements NutsExprIdFilter, Serializable {

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
     * comma separated intervals such as :
     *   [ version1 , version2 ], [ version1 , version2 ]
     * </pre>
     */
    private final List<NutsVersionInterval> intervals = new ArrayList<>();

    public DefaultNutsVersionFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    public static NutsOptional<NutsVersionFilter> parse(String version, NutsSession session) {
        if (DefaultNutsVersion.isBlankVersion(version) || "*".equals(version)) {
            return NutsOptional.of(NutsVersionFilters.of(session).always());
        }

        NutsOptional<List<NutsVersionInterval>> r = NutsVersionInterval.ofList(version);
        return r.map(
                x->{
                    DefaultNutsVersionFilter dd = new DefaultNutsVersionFilter(session);
                    for (NutsVersionInterval i : x) {
                        dd.add(i);
                    }
                    return dd;
                }
        );
    }

    @Override
    public boolean acceptVersion(NutsVersion version, NutsSession session) {
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

//    public static NutsVersionFilter parse(String version) {
//        return parse(version,null);
//    }

    @Override
    public NutsOptional<List<NutsVersionInterval>> intervals() {
        return NutsOptional.of(intervals);
    }

    public void add(NutsVersionInterval interval) {
        intervals.add(interval);
    }

    public String toExpr() {
        return "id.version.matches('" + CoreStringUtils.escapeQuoteStrings(toString()) + "')";
    }

    @Override
    public NutsVersionFilter simplify() {
        List<NutsVersionInterval> intervals2 = new ArrayList<>();
        boolean updates = false;
        for (NutsVersionInterval interval : intervals) {
            NutsVersionInterval _interval = CoreFilterUtils.simplify(interval);
            if (_interval != null) {
                if (_interval.getLowerBound() == null && _interval.getUpperBound() == null) {
                    return null;
                }
                if (!_interval.equals(interval)) {
                    updates = true;
                }
                intervals2.add(interval);
            } else {
                updates = true;
            }
        }
        if (intervals2.isEmpty()) {
            return null;
        }
        if (!updates) {
            return this;
        }
        DefaultNutsVersionFilter d = new DefaultNutsVersionFilter(getSession());
        d.intervals.addAll(intervals2);
        return d;
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
        return Objects.equals(this.intervals, other.intervals);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NutsVersionInterval interval : intervals) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(interval.toString());
        }
        return sb.toString();
    }

}
