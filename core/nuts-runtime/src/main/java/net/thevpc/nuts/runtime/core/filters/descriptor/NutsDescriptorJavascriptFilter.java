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
package net.thevpc.nuts.runtime.core.filters.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.JavascriptHelper;

import java.util.Objects;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsDescriptorJavascriptFilter extends AbstractDescriptorFilter implements JsNutsDescriptorFilter {

    private String code;

    public static NutsDescriptorFilter valueOf(String value, NutsSession ws) {
        if (NutsUtilStrings.isBlank(value)) {
            return ws.getWorkspace().descriptor().filter().always();
        }
        return new NutsDescriptorJavascriptFilter(ws,value);
    }

    public NutsDescriptorJavascriptFilter(NutsSession ws,String code) {
        super(ws, NutsFilterOp.CUSTOM);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor d, NutsSession session) {
        JavascriptHelper engineHelper = new JavascriptHelper(code, "var descriptor=x; var id=x.getId(); var version=id.getVersion();", null, null,session);
        return engineHelper.accept(d);
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        return getCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.code);
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
        final NutsDescriptorJavascriptFilter other = (NutsDescriptorJavascriptFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsDescriptorJavascriptFilter{" + code + '}';
    }

}
