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
package net.vpc.app.nuts.core.filters.id;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsPatternIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsJsAwareIdFilter {

    private boolean valid = false;
    private Pattern idPattern;

    private String[] ids = new String[0];

    public NutsPatternIdFilter() {

    }

    public NutsPatternIdFilter(String[] id) {
        List<String> all = new ArrayList<>();
        if (id != null) {
            for (String i : id) {
                if (i != null) {
                    i = i.trim();
                    if (i.length() > 0 && !i.equals("*")) {
                        all.add(i);
                    }
                }
            }
        }
        this.ids = all.toArray(new String[0]);
    }

    public static Pattern compile(String[] arr, boolean addStars) {
        if (arr == null || arr.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String patternString : arr) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("(");
            String str = CoreStringUtils.simpexpToRegexp(patternString, true);
            if (addStars) {
                str = ".*" + str + ".*";
            }
            sb.append(str);
            sb.append(")");
        }
        return Pattern.compile(sb.toString());
    }

    private void rebuild() {
        if (!valid) {
            this.idPattern = null;
            this.idPattern = compile(ids, false);
            valid = true;
        }
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
        valid = false;
    }

    public boolean accept(NutsId id) {
        rebuild();
        if (idPattern != null) {
            if (!idPattern.matcher(id.toString()).matches()
                    && !idPattern.matcher(id.getSimpleName()).matches()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        StringBuilder sb = new StringBuilder();
        if (ids.length == 0) {
            return "true";
        }
        if (ids.length > 1) {
            sb.append("(");
        }
        for (String id : ids) {
            if (sb.length() > 0) {
                sb.append(" || ");
            }
            sb.append("id.matches('").append(CoreStringUtils.escapeCoteStrings(id)).append("')");
        }
        if (ids.length > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public NutsIdFilter simplify() {
        if (ids.length == 0) {
            return null;
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.deepHashCode(this.ids);
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
        final NutsPatternIdFilter other = (NutsPatternIdFilter) obj;
        if (!Arrays.deepEquals(this.ids, other.ids)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsIdPatternFilter" + Arrays.toString(ids);
    }
    
    public static boolean containsWildcad(String id){
        return 
                id.indexOf('*')>=0
               // ||id.indexOf('|')>=0
                ;
    }

}
