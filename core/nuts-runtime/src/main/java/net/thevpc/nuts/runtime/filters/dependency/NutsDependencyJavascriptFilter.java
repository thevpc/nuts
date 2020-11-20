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
package net.thevpc.nuts.runtime.filters.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.DefaultNutsId;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.common.JavascriptHelper;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

import java.util.Objects;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsDependencyJavascriptFilter extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter>, JsNutsDependencyFilter {

    private static NutsId SAMPLE_NUTS_ID = new DefaultNutsId("sample", "sample", "sample", "sample", "sample");

    private String code;

    public static NutsDependencyJavascriptFilter valueOf(String value, NutsWorkspace ws) {
        if (CoreStringUtils.isBlank(value)) {
            return null;
        }
        return new NutsDependencyJavascriptFilter(ws,value);
    }

    public NutsDependencyJavascriptFilter(NutsWorkspace ws,String code) {
        super(ws, NutsFilterOp.CUSTOM);
        this.code = code;
        //check if valid
//        accept(SAMPLE_DependencyNUTS_DESCRIPTOR);
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency d, NutsSession session) {
        JavascriptHelper engineHelper = new JavascriptHelper(code, "var dependency=x; var id=x.getId(); var version=id.getVersion();", null, null, session);
        return engineHelper.accept(d);
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsDependencyFilterExpr() {
//        return "util.matches(dependency,'" + CoreStringUtils.escapeCoteStrings(code) + "')";
        return getCode();
    }

    @Override
    public String toString() {
        return "NutsDependencyJavascriptFilter{" + code + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.code);
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
        final NutsDependencyJavascriptFilter other = (NutsDependencyJavascriptFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

}