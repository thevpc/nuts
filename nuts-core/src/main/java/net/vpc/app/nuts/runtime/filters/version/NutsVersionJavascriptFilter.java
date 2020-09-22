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
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.filters.version;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.common.JavascriptHelper;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Objects;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsVersionJavascriptFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, JsNutsVersionFilter {

    private String code;
    private JavascriptHelper engineHelper;

    public static NutsVersionJavascriptFilter valueOf(String value, NutsSession session) {
        if (CoreStringUtils.isBlank(value)) {
            return null;
        }
        return new NutsVersionJavascriptFilter(value);
    }

    public NutsVersionJavascriptFilter(String code) {
        this.code = code;
        //check if valid
//        accept(SAMPLE_DependencyNUTS_DESCRIPTOR);
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean accept(NutsVersion d, NutsSession session) {
        JavascriptHelper engineHelper = new JavascriptHelper(code, "var dependency=x; var id=x.getId(); var version=id.getVersion();", null, null, session);
        return engineHelper.accept(d);
    }

    @Override
    public NutsVersionFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsVersionFilterExpr() {
        return "util.matches(version,'" + CoreStringUtils.escapeCoteStrings(code) + "')";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.code);
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
        final NutsVersionJavascriptFilter other = (NutsVersionJavascriptFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsVersionJavascriptFilter{" + code + '}';
    }

}
