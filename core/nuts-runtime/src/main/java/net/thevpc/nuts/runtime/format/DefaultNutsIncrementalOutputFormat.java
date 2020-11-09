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
package net.thevpc.nuts.runtime.format;

import net.thevpc.nuts.NutsOutputFormat;
import net.thevpc.nuts.NutsUnsupportedArgumentException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.json.DefaultSearchFormatJson;
import net.thevpc.nuts.runtime.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.runtime.format.table.DefaultSearchFormatTable;
import net.thevpc.nuts.runtime.format.tree.DefaultSearchFormatTree;
import net.thevpc.nuts.runtime.format.xml.DefaultSearchFormatXml;
import net.thevpc.nuts.runtime.format.props.DefaultSearchFormatProps;
import net.thevpc.nuts.NutsIterableFormat;

/**
 *
 * @author vpc
 */
public class DefaultNutsIncrementalOutputFormat extends NutsIncrementalOutputFormatBase {

    public DefaultNutsIncrementalOutputFormat(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsIterableFormat getEffectiveFormat() {
        NutsOutputFormat outputFormat = getOutputFormat();
        NutsIterableFormat old = super.getFormat();
        if (old == null || old.getOutputFormat() != outputFormat) {
            switch (outputFormat) {
                case JSON: {
                    return setFormat(new DefaultSearchFormatJson(getValidSession(), getValidOut(),getDisplayOptions()));
                }
                case XML: {
                    return setFormat(new DefaultSearchFormatXml(getValidSession(), getValidOut(),getDisplayOptions()));
                }
                case PLAIN: {
                    return setFormat(new DefaultSearchFormatPlain(getValidSession(), getValidOut(),getDisplayOptions()));
                }
                case PROPS: {
                    return setFormat(new DefaultSearchFormatProps(getValidSession(), getValidOut(),getDisplayOptions()));
                }
                case TABLE: {
                    return setFormat(new DefaultSearchFormatTable(getValidSession(), getValidOut(),getDisplayOptions()));
                }
                case TREE: {
                    return setFormat(new DefaultSearchFormatTree(getValidSession(), getValidOut(),getDisplayOptions()));
                }
                default: {
                    throw new NutsUnsupportedArgumentException(getWorkspace(), "Unsupproted " + outputFormat);
                }
            }
        }
        return old;
    }
}
