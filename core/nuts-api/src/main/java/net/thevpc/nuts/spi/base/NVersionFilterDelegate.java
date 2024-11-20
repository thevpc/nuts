/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 *
 * @author thevpc
 */
public abstract class NVersionFilterDelegate extends AbstractVersionFilter {
    public abstract NVersionFilter baseVersionFilter();

    public NVersionFilterDelegate(NWorkspace workspace) {
        super(workspace, NFilterOp.CUSTOM);
    }

    @Override
    public boolean acceptVersion(NVersion version) {
        return baseVersionFilter().acceptVersion(version);
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        return baseVersionFilter().withDesc(description);
    }

    @Override
    public NVersionFilter simplify() {
        return (NVersionFilter) baseVersionFilter().simplify();
    }

    @Override
    public boolean acceptSearchId(NSearchId sid) {
        return baseVersionFilter().acceptSearchId(sid);
    }

    @Override
    public NFilterOp getFilterOp() {
        return baseVersionFilter().getFilterOp();
    }

    @Override
    public List<NFilter> getSubFilters() {
        return baseVersionFilter().getSubFilters();
    }

    @Override
    public NOptional<List<NVersionInterval>> intervals() {
        return baseVersionFilter().intervals();
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return baseVersionFilter().simplify(type);
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return baseVersionFilter().getFilterType();
    }

    @Override
    public NElement describe() {
        return baseVersionFilter().describe();
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return baseVersionFilter().to(type);
    }


}
