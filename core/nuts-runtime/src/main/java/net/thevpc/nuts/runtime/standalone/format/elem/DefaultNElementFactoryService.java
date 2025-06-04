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

import net.thevpc.nuts.elem.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.elem.item.DefaultNArrayElement;
import net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore.DefaultElementMapperStore;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNElementFactoryService implements NElementFactoryService {


//    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private NReflectRepository typesRepository;
    private final NWorkspace workspace;

    public DefaultNElementFactoryService(NWorkspace workspace) {
        typesRepository = NWorkspaceUtils.of(workspace).getReflectRepository();
        this.workspace = workspace;

    }







    protected Object createObject(NElement o, Type to, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null || o.type() == NElementType.NULL) {
            return DefaultElementMapperStore.F_NULL.createObject(o, to, context);
        }
        if (to == null) {
            NElementMapper f = context.getMapper(o, defaultOnly);
            if(f==null){
                throw new NUnsupportedEnumException(o.type());
            }
        }
        NElementMapper f = context.getMapper(to, defaultOnly);
        return f.createObject(o, to, context);
    }

    @Override
    public Object createObject(NElement o, Type to, NElementFactoryContext context) {
        return createObject(o, to, context, false);
    }

    @Override
    public Object defaultCreateObject(NElement o, Type to, NElementFactoryContext context) {
        return createObject(o, to, context, true);
    }

    protected Object destruct(Object o, Type expectedType, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null) {
            return null;
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (context.getIndestructibleObjects() != null) {
            if (context.getIndestructibleObjects().test(o.getClass())) {
                return o;
            }
        }
        return context.getMapper(expectedType, defaultOnly).destruct(o, expectedType, context);
    }

    @Override
    public Object destruct(Object o, Type expectedType, NElementFactoryContext context) {
        return destruct(o, expectedType, context, false);
    }

    @Override
    public Object defaultDestruct(Object o, Type expectedType, NElementFactoryContext context) {
        return destruct(o, expectedType, context, true);
    }

    protected NElement createElement(Object o, Type expectedType, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null) {
            return NElement.ofNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (context.getIndestructibleObjects() != null) {
            if (context.getIndestructibleObjects().test(o.getClass())) {
                return NElement.ofCustom(o);
            }
        }
        NElementMapper mapper = context.getMapper(expectedType, defaultOnly);
        return mapper.createElement(o, expectedType, context);
    }

    @Override
    public NElement createElement(Object o, Type expectedType, NElementFactoryContext context) {
        return createElement(o, expectedType, context, false);
    }

    @Override
    public NElement defaultCreateElement(Object o, Type expectedType, NElementFactoryContext context) {
        return createElement(o, expectedType, context, true);
    }

    public NReflectRepository getTypesRepository() {
        return typesRepository;
    }

    public static List<Object> _destructArray1(Object array, NElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<Object> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.destruct(Array.get(array, i), null));
            }
            return preloaded;
        } else {
            return Arrays.stream((Object[]) array).map(x -> context.destruct(x, null)).collect(Collectors.toList());
        }
    }

    public static NArrayElement _createArray1(Object array, NElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<NElement> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.createElement(Array.get(array, i)));
            }
            return new DefaultNArrayElement(null,null,preloaded, new NElementAnnotation[0],null);
        } else {
            return new DefaultNArrayElement(null,null,
                    Arrays.stream((Object[]) array).map(x -> context.createElement(x)).collect(Collectors.toList())
                    ,new NElementAnnotation[0],null
            );
        }
    }

}
