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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.spi.base.AbstractVersionFilter;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Examples [2.6,], ]2.6,] . Created by vpc on 1/20/17.
 */
public class DefaultNVersionFilter extends AbstractVersionFilter implements NExprIdFilter, Serializable {

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
    private final List<NVersionInterval> intervals = new ArrayList<>();

    public DefaultNVersionFilter(NWorkspace workspace) {
        super(workspace, NFilterOp.CUSTOM);
    }

    public static NOptional<NVersionFilter> parse(String version, NWorkspace workspace) {
        if (NBlankable.isBlank(version) || "*".equals(version)) {
            return NOptional.of(NVersionFilters.of().always());
        }

        NOptional<List<NVersionInterval>> r = NVersionInterval.ofList(version);
        return r.map(
                x -> {
                    DefaultNVersionFilter dd = new DefaultNVersionFilter(workspace);
                    for (NVersionInterval i : x) {
                        dd.add(i);
                    }
                    return dd;
                }
        );
    }

    @Override
    public boolean acceptVersion(NVersion version) {
        if (intervals.isEmpty()) {
            return true;
        }
        for (NVersionInterval value : intervals) {
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
    public NOptional<List<NVersionInterval>> intervals() {
        return NOptional.of(intervals);
    }

    public void add(NVersionInterval interval) {
        intervals.add(interval);
    }

    public String toExpr() {
        return "id.version.matches("
                //this will escape `"' if It's present
                + NStringUtils.formatStringLiteral(
                //this will create '...' value
                NStringUtils.formatStringLiteral(toString(), NQuoteType.SIMPLE),
                NQuoteType.DOUBLE, NSupportMode.NEVER
        )
                + ")";
    }

    @Override
    public NVersionFilter simplify() {
        List<NVersionInterval> intervals2 = new ArrayList<>();
        boolean updates = false;
        for (NVersionInterval interval : intervals) {
            NVersionInterval _interval = CoreFilterUtils.simplify(interval);
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
        DefaultNVersionFilter d = new DefaultNVersionFilter(workspace);
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
        final DefaultNVersionFilter other = (DefaultNVersionFilter) obj;
        return Objects.equals(this.intervals, other.intervals);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NVersionInterval interval : intervals) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(interval.toString());
        }
        return sb.toString();
    }

}
