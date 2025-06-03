/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.format.elem.parser.DefaultNElementParser;
import net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.util.NMsg;

/**
 * @author thevpc
 */
public class DefaultNElementFactoryContext implements NElementFactoryContext {

    private final Map<String, Object> properties = new HashMap<>();
    private final Set<RefItem> visited = new LinkedHashSet<>();
    private final NReflectRepository repository;
    private boolean ntf;
    private UserElementMapperStore userElementMapperStore;
    private Predicate<Class<?>> indestructibleObjects;

    public DefaultNElementFactoryContext(DefaultNElementParser base, NReflectRepository repository, UserElementMapperStore userElementMapperStore,Predicate<Class<?>> indestructibleObjects) {
        this.indestructibleObjects = indestructibleObjects;
        this.repository = repository;
        this.ntf = base.isNtf();
        this.userElementMapperStore = userElementMapperStore;
    }

    public NElementMapper getMapper(Type type, boolean defaultOnly){
        return userElementMapperStore.getMapper(type, defaultOnly);
    }

    @Override
    public Predicate<Class<?>> getIndestructibleObjects() {
        return indestructibleObjects;
    }


    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    private String stacktrace() {
        StringBuilder sb = new StringBuilder(

        );
        boolean nl = false;
        for (RefItem refItem : visited) {
            if (nl) {
                sb.append("\n");
            } else {
                nl = true;
            }
            sb.append(refItem.step).append(": ").append(refItem.o.getClass().getName());
        }
        return sb.toString();
    }

    @Override
    public NElement defaultObjectToElement(Object o, Type expectedType) {
        if (o != null) {
            RefItem ro = new RefItem(o, "defaultObjectToElement");
            if (visited.contains(ro)) {
                throw new NIllegalArgumentException(NMsg.ofC("unable to serialize object of type %s because of cyclic references: %s", o.getClass().getName(), stacktrace()));
            }
            visited.add(ro);
            try {
                return base.getElementFactoryService().defaultCreateElement(o, expectedType, this);
            } finally {
                visited.remove(ro);
            }
        }
        return base.getElementFactoryService().defaultCreateElement(o, expectedType, this);
    }

    @Override
    public Object defaultDestruct(Object o, Type expectedType) {
        if (o != null) {
            RefItem ro = new RefItem(o, "defaultDestruct");
            if (visited.contains(ro)) {
                throw new NIllegalArgumentException(NMsg.ofC("unable to destruct object of type %s because of cyclic references: %s", o.getClass().getName(), stacktrace()));
            }
            visited.add(ro);
            try {
                return base.getElementFactoryService().defaultDestruct(o, expectedType, this);
            } finally {
                visited.remove(ro);
            }
        }
        return base.getElementFactoryService().defaultDestruct(o, expectedType, this);
    }

    @Override
    public NElement objectToElement(Object o, Type expectedType) {
        if (o != null) {
            RefItem ro = new RefItem(o, "objectToElement");
            if (visited.contains(ro)) {
                throw new NIllegalArgumentException(NMsg.ofC("unable to serialize object of type %s because of cyclic references: %s", o.getClass().getName(), stacktrace()));
            }
            visited.add(ro);
            try {
                return base.getElementFactoryService().createElement(o, expectedType, this);
            } finally {
                visited.remove(ro);
            }
        } else {
            return base.getElementFactoryService().createElement(o, expectedType, this);
        }
    }

    @Override
    public Object destruct(Object o, Type expectedType) {
        if (o != null) {
            RefItem ro = new RefItem(o, "destruct");
            if (visited.contains(ro)) {
                throw new NIllegalArgumentException(NMsg.ofC("unable to destruct object of type %s because of cyclic references.", o.getClass().getName()));
            }
            visited.add(ro);
            try {
                return base.getElementFactoryService().destruct(o, expectedType, this);
            } finally {
                visited.remove(ro);
            }
        }
        return base.getElementFactoryService().destruct(o, expectedType, this);
    }

    @Override
    public <T> T elementToObject(NElement o, Class<T> type) {
        return (T) elementToObject(o, (Type) type);
    }

    @Override
    public Object elementToObject(NElement o, Type type) {
        return base.getElementFactoryService().createObject(o, type, this);
    }

    @Override
    public <T> T defaultElementToObject(NElement o, Class<T> type) {
        return (T) defaultElementToObject(o, (Type) type);
    }

    @Override
    public Object defaultElementToObject(NElement o, Type type) {
        return base.getElementFactoryService().defaultCreateObject(o, type, this);
    }

    @Override
    public boolean isNtf() {
        return ntf;
    }

    public DefaultNElementFactoryContext setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    private static class RefItem {
        private final Object o;
        private final String step;

        public RefItem(Object o, String step) {
            this.o = o;
            this.step = step;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(o) * 31 + step.hashCode();
        }

        @Override
        public boolean equals(Object o1) {
            if (this == o1) return true;
            if (o1 == null || getClass() != o1.getClass()) return false;
            RefItem refItem = (RefItem) o1;
            return o == refItem.o && step.equals(refItem.step);
        }

        @Override
        public String toString() {
            return step + "(" + o + ')';
        }
    }

    @Override
    public NReflectRepository getTypesRepository() {
        return repository;
    }
}
