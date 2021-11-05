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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.format.elem;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import net.thevpc.nuts.NutsElements;

/**
 *
 * @author vpc
 */
public class DefaultNutsElementFactoryContext implements NutsElementFactoryContext {

    private DefaultNutsElements base;
    private final Map<String, Object> properties = new HashMap<>();
    private boolean ntf;

    public DefaultNutsElementFactoryContext(DefaultNutsElements base) {
        this.base = base;
        this.ntf = base.isNtf();
    }

    @Override
    public boolean isNtf() {
        return ntf;
    }

    public DefaultNutsElementFactoryContext setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return base.getSession();
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return base.getWorkspace();
    }

    @Override
    public NutsElements elem() {
        return base;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Predicate<Type> getDestructTypeFilter() {
        return base.getDestructTypeFilter();
    }
    

    @Override
    public Object defaultDestruct(Object o, Type expectedType) {
        return base.getElementFactoryService().defaultDestruct(o, expectedType, this);
    }

    @Override
    public Object destruct(Object o, Type expectedType) {
        return base.getElementFactoryService().destruct(o, expectedType, this);
    }
    

    @Override
    public NutsElement objectToElement(Object o, Type expectedType) {
        return base.getElementFactoryService().createElement(o, expectedType, this);
    }

    @Override
    public <T> T elementToObject(NutsElement o, Class<T> type) {
        return (T)elementToObject(o, (Type)type);
    }

    @Override
    public <T> T defaultElementToObject(NutsElement o, Class<T> type) {
        return (T)defaultElementToObject(o, (Type)type);
    }

    
    @Override
    public Object elementToObject(NutsElement o, Type type) {
        return base.getElementFactoryService().createObject(o, type, this);
    }

    @Override
    public NutsElement defaultObjectToElement(Object o, Type expectedType) {
        return base.getElementFactoryService().defaultCreateElement(o, expectedType, this);
    }

    @Override
    public Object defaultElementToObject(NutsElement o, Type type) {
        return base.getElementFactoryService().defaultCreateObject(o, type, this);
    }

}
