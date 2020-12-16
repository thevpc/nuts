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
package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.NutsElementBuilder;
import java.util.HashMap;
import java.util.Map;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author thevpc
 */
public class DefaultNutsElementFactoryContext implements NutsElementFactoryContext {

    private NutsElementFactoryService factory;
    private NutsElementFactory fallback;
    private final Map<String, Object> properties = new HashMap<String, Object>();
    private final NutsWorkspace workspace;
    private final NutsElementBuilder builder;

    public DefaultNutsElementFactoryContext(NutsWorkspace workspace) {
        this.workspace = workspace;
        builder = new DefaultNutsElementBuilder();
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsElementBuilder builder() {
        return builder;
    }

    public NutsElementFactoryService getFactory() {
        return factory;
    }

    public void setFactory(NutsElementFactoryService factory) {
        this.factory = factory;
    }

    @Override
    public NutsElementFactory getFallback() {
        return fallback;
    }

    @Override
    public void setFallback(NutsElementFactory fallback) {
        this.fallback = fallback;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public NutsElement toElement(Object o) {
        return factory.create(o, this);
    }

}
