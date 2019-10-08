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
package net.vpc.app.nuts.runtime.filters.version;

import java.io.IOException;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.vpc.app.nuts.runtime.DefaultNutsVersionInterval;
import net.vpc.app.nuts.runtime.filters.id.NutsScriptAwareIdFilter;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

/**
 * Examples [2.6,], ]2.6,] . Created by vpc on 1/20/17.
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
     * comma separated intervals such as :
     *   [ version1 , version2 ], [ version1 , version2 ]
     * </pre>
     */
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
        if (DefaultNutsVersion.isBlank(version) || "*".equals(version)) {
            return AllNutsVersionFilter.INSTANCE;
        }
        ParseHelper pp = new ParseHelper();
        return pp.parse(version);
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

    public void add(NutsVersionInterval interval) {
        intervals.add(interval);
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        return "id.version.matches('" + CoreStringUtils.escapeCoteStrings(toString()) + "')";
    }

    @Override
    public NutsVersionFilter simplify() {
        List<NutsVersionInterval> intervals2 = new ArrayList<>();
        boolean updates = false;
        for (NutsVersionInterval interval : intervals) {
            NutsVersionInterval _interval = CoreNutsUtils.simplify(interval);
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
        DefaultNutsVersionFilter d = new DefaultNutsVersionFilter();
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
        if (!Objects.equals(this.intervals, other.intervals)) {
            return false;
        }
        return true;
    }

    private static class ParseHelper {

        int t;
        final int START = 0;
        final int NEXT = 1;
        final int NEXT_COMMA = 2;
        final int EXPECT_V1 = 3;
        final int EXPECT_V_COMMA = 4;
        final int EXPECT_V2 = 5;
        final int EXPECT_CLOSE = 6;
        int state = NEXT;
        int open = -1;
        int close = -1;
        String v1 = null;
        String v2 = null;
        DefaultNutsVersionFilter dd = new DefaultNutsVersionFilter();

        void reset() {
            open = -1;
            close = -1;
            v1 = null;
            v2 = null;
        }

        void addNextValue(String sval) {
            if (sval.endsWith("*")) {
                String min = sval.substring(0, sval.length() - 1);
                if(min.equals("")){
                    dd.add(new DefaultNutsVersionInterval(false, false, min, null));
                }else {
                    String max = DefaultNutsVersion.valueOf(min).inc(-1).getValue();
                    dd.add(new DefaultNutsVersionInterval(true, false, min, max));
                }
            } else {
                dd.add(new DefaultNutsVersionInterval(true, true, sval, sval));
            }
        }

        void addNextInterval() {
            boolean inclusiveLowerBoundary = open == '[' && (v1 != null);
            boolean inclusiveUpperBoundary = close == ']' && (v2 != null);
            dd.add(new DefaultNutsVersionInterval(inclusiveLowerBoundary, inclusiveUpperBoundary, v1, v2));
            reset();
        }

        NutsVersionFilter parse(String version) {
            StreamTokenizer st = new StreamTokenizer(new StringReader(version));
            st.resetSyntax();
            st.whitespaceChars(0, 32);
            for (int i = 33; i < 256; i++) {
                switch ((char) i) {
                    case '(':
                    case ')':
                    case ',':
                    case '[':
                    case ']': {
                        break;
                    }
                    default: {
                        st.wordChars(i, i);
                    }
                }
            }
            try {
                while ((t = st.nextToken()) != StreamTokenizer.TT_EOF) {
                    switch (state) {
                        case START:
                        case NEXT: {
                            switch (t) {
                                case StreamTokenizer.TT_WORD: {
                                    addNextValue(st.sval);
                                    state = NEXT_COMMA;
                                    break;
                                }
                                case '[':
                                case ']':
                                case '(': {
                                    open = t;
                                    state = EXPECT_V1;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Unexpected  " + ((char) t));
                                }
                            }
                            break;
                        }
                        case NEXT_COMMA: {
                            switch (t) {
                                case ',': {
                                    state = NEXT;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Expected ',' found " + ((char) t));
                                }
                            }
                            break;
                        }
                        case EXPECT_V1: {
                            switch (t) {
                                case StreamTokenizer.TT_WORD: {
                                    v1 = st.sval;
                                    state = EXPECT_V_COMMA;
                                    break;
                                }
                                case ',': {
                                    state = EXPECT_V2;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Unexpected  " + ((char) t));
                                }
                            }
                            break;

                        }
                        case EXPECT_V_COMMA: {
                            switch (t) {
                                case ',': {
                                    state = EXPECT_V2;
                                    break;
                                }
                                case ']': {
                                    close = t;
                                    v2 = v1;
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                case '[':
                                case ')': {
                                    close = t;
                                    v2 = v1; //the same?
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Unexpected  " + ((char) t));
                                }
                            }
                            break;
                        }
                        case EXPECT_V2: {
                            switch (t) {
                                case StreamTokenizer.TT_WORD: {
                                    v2 = st.sval;
                                    state = EXPECT_CLOSE;
                                    break;
                                }
                                case '[':
                                case ']':
                                case ')': {
                                    close = t;
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Unexpected  " + ((char) t));
                                }
                            }
                            break;
                        }
                        case EXPECT_CLOSE: {
                            switch (t) {
                                case '[':
                                case ']':
                                case ')': {
                                    close = t;
                                    addNextInterval();
                                    state = NEXT_COMMA;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Unexpected  " + ((char) t));
                                }
                            }
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("Unsupported state " + state);
                        }
                    }
                }
                if (state != NEXT_COMMA && state != START) {
                    throw new IllegalArgumentException("Invalid state" + state);
                }
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
            return dd;
        }
    }
}
