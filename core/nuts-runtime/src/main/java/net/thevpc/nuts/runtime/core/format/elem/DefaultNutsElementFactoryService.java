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

import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import java.io.File;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.elem.mappers.*;
import net.thevpc.nuts.runtime.core.format.xml.NutsElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.core.format.xml.NutsElementFactoryXmlElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.bundles.collections.ClassMap;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectRepository;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsElementFactoryService implements NutsElementFactoryService {

    private static final NutsElementMapper F_NULL = new NutsElementMapperNull();
    private static final NutsElementMapper F_NUTS_ARR = new NutsElementMapperArray();
    private static final NutsElementMapper F_STRINGS = new NutsElementMapperString();
    private static final NutsElementMapper F_CHAR = new NutsElementMapperChar();
    private static final NutsElementMapper F_NUMBERS = new NutsElementMapperNumber();
    private static final NutsElementMapper F_BOOLEANS = new NutsElementMapperBoolean();
    private static final NutsElementMapper F_ENUMS = new NutsElementMapperEnum();
    private static final NutsElementMapper F_INSTANT = new NutsElementMapperInstant();
    private static final NutsElementMapper F_DATE = new NutsElementMapperUtilDate();
    private static final NutsElementMapper F_PATH = new NutsElementMapperPath();
    private static final NutsElementMapper F_FILE = new NutsElementMapperFile();
    private static final NutsElementMapper F_ITERATOR = new NutsElementMapperIterator();
    private static final NutsElementMapper F_NAMED_ELEM = new NutsElementMapperNamedElement();
    private static final NutsElementMapper F_MAPENTRY = new NutsElementMapperMapEntry();
    private static final NutsElementMapper F_XML_ELEMENT = new NutsElementFactoryXmlElement();
    private static final NutsElementMapper F_XML_DOCUMENT = new NutsElementFactoryXmlDocument();
    private static final NutsElementMapper F_NUTS_DEF = new NutsElementMapperNutsDefinition();
    private static final NutsElementMapper F_NUTS_ID = new NutsElementMapperNutsId();
    private static final NutsElementMapper F_NUTS_VERSION = new NutsElementMapperNutsVersion();
    private static final NutsElementMapper F_NUTS_DESCRIPTOR = new NutsElementMapperNutsDescriptor();
    private static final NutsElementMapper F_NUTS_ENV_CONDITION = new NutsElementMapperNutsEnvCondition();
    private static final NutsElementMapper F_NUTS_ENV_CONDITION_BUILDER = new NutsElementMapperNutsEnvConditionBuilder();
    private static final NutsElementMapper F_NUTS_DEPENDENCY = new NutsElementMapperNutsDependency();
    private static final NutsElementMapper F_NUTS_SDK_LOCATION = new NutsElementMapperNutsPlatformLocation();
    private static final NutsElementMapper F_NUTS_ID_LOCATION = new NutsElementMapperNutsIdLocation();
    private static final NutsElementMapper F_ARTIFACT_CALL = new NutsElementMapperNutsArtifactCall();
    private static final NutsElementMapper F_DESCRIPTOR_PROPERTY = new NutsElementMapperNutsDescriptorProperty();
    private static final NutsElementMapper F_DESCRIPTOR_PROPERTY_BUILDER = new NutsElementMapperNutsDescriptorPropertyBuilder();

//    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private final ClassMap<NutsElementMapper> defaultMappers = new ClassMap<>(null, NutsElementMapper.class);
    private final Map<Class, NutsElementMapper> coreMappers = new HashMap<>();
    private final ClassMap<NutsElementMapper> customMappers = new ClassMap<>(null, NutsElementMapper.class);
    private ReflectRepository typesRepository;
    private final NutsWorkspace ws;
    private final NutsSession session;
    private final NutsElementMapper F_OBJ = new NutsElementMapperObjReflect(this);

    private final NutsElementMapper F_COLLECTION = new NutsElementMapperCollection(this);
    private final NutsElementMapper F_MAP = new NutsElementMapperMap(this);

    public DefaultNutsElementFactoryService(NutsWorkspace ws, NutsSession session) {
        typesRepository = NutsWorkspaceUtils.of(session).getReflectRepository();
        addDefaultFactory(Boolean.class, F_BOOLEANS);
        addDefaultFactory(boolean.class, F_BOOLEANS);
        addDefaultFactory(byte.class, F_NUMBERS);
        addDefaultFactory(short.class, F_NUMBERS);
        addDefaultFactory(int.class, F_NUMBERS);
        addDefaultFactory(long.class, F_NUMBERS);
        addDefaultFactory(float.class, F_NUMBERS);
        addDefaultFactory(double.class, F_NUMBERS);
        addDefaultFactory(Number.class, F_NUMBERS);

        addDefaultFactory(char.class, F_CHAR);
        addDefaultFactory(Character.class, F_CHAR);

        addDefaultFactory(Object.class, F_OBJ);
        addDefaultFactory(String.class, F_STRINGS);

        addDefaultFactory(StringBuilder.class, F_STRINGS);
        addDefaultFactory(StringBuffer.class, F_STRINGS);

        addDefaultFactory(Path.class, F_PATH);
        addDefaultFactory(File.class, F_FILE);
        addDefaultFactory(java.util.Date.class, F_DATE);
        addDefaultFactory(java.time.Instant.class, F_INSTANT);
        addDefaultFactory(Enum.class, F_ENUMS);
        addDefaultFactory(Collection.class, F_COLLECTION);
        addDefaultFactory(Iterator.class, F_ITERATOR);
        addDefaultFactory(Map.class, F_MAP);
        addDefaultFactory(Map.Entry.class, F_MAPENTRY);
        addDefaultFactory(org.w3c.dom.Element.class, F_XML_ELEMENT);
        addDefaultFactory(org.w3c.dom.Document.class, F_XML_DOCUMENT);
        addDefaultFactory(boolean[].class, new NutsElementMapperPrimitiveBooleanArray());
        addDefaultFactory(byte[].class, new NutsElementMapperPrimitiveByteArray());
        addDefaultFactory(short[].class, new NutsElementMapperPrimitiveShortArray());
        addDefaultFactory(char[].class, new NutsElementMapperPrimitiveCharArray());
        addDefaultFactory(int[].class, new NutsElementMapperPrimitiveIntArray());
        addDefaultFactory(long[].class, new NutsElementMapperPrimitiveLongArray());
        addDefaultFactory(float[].class, new NutsElementMapperFloatArray());
        addDefaultFactory(double[].class, new NutsElementMapperPrimitiveDoubleArray());
        addDefaultFactory(Object[].class, new NutsElementMapperObjectArray());
        addDefaultFactory(NutsPrimitiveElement.class, new NutsElementMapperNutsPrimitiveElement());
        addDefaultFactory(NutsArrayElement.class, new NutsElementMapperNutsArrayElement());
        addDefaultFactory(NutsObjectElement.class, new NutsElementMapperNutsObjectElement());
        addDefaultFactory(NutsElement.class, new NutsElementMapperNutsElement());
        addDefaultFactory(NutsElementEntry.class, F_NAMED_ELEM);
        addDefaultFactory(NutsCommandLine.class, new NutsElementMapperCommandLine());
        addDefaultFactory(NutsString.class, new NutsElementMapperNutsString());
        addDefaultFactory(NutsText.class, new NutsElementMapperNutsText());
        addDefaultFactory(NutsPath.class, new NutsElementMapperNutsPath());

//        addHierarchyFactory(JsonElement.class, F_JSONELEMENT);
        setCoreMapper(NutsDefinition.class, F_NUTS_DEF);
        setCoreMapper(NutsId.class, F_NUTS_ID);
        setCoreMapper(NutsVersion.class, F_NUTS_VERSION);
        setCoreMapper(NutsDescriptor.class, F_NUTS_DESCRIPTOR);
        setCoreMapper(NutsDependency.class, F_NUTS_DEPENDENCY);
        setCoreMapper(NutsIdLocation.class, F_NUTS_ID_LOCATION);
        setCoreMapper(NutsArtifactCall.class, F_ARTIFACT_CALL);
        setCoreMapper(NutsPlatformLocation.class, F_NUTS_SDK_LOCATION);
        setCoreMapper(NutsEnvCondition.class, F_NUTS_ENV_CONDITION);
        setCoreMapper(NutsEnvConditionBuilder.class, F_NUTS_ENV_CONDITION_BUILDER);
        setCoreMapper(NutsDescriptorProperty.class, F_DESCRIPTOR_PROPERTY);
        setCoreMapper(NutsDescriptorPropertyBuilder.class, F_DESCRIPTOR_PROPERTY_BUILDER);
        this.ws = ws;
        this.session = session;
    }

    public final void addDefaultFactory(Class cls, NutsElementMapper instance) {
        defaultMappers.put(cls, instance);
    }

    public final void setCoreMapper(Class cls, NutsElementMapper instance) {
        coreMappers.put(cls, instance);
        customMappers.put(cls, instance);
    }

    public final void setMapper(Class cls, NutsElementMapper instance) {
        if (instance == null) {
            NutsElementMapper cc = coreMappers.get(cls);
            if (cc != null) {
                customMappers.put(cls, cc);
            } else {
                customMappers.remove(cls);
            }
        } else {
            customMappers.put(cls, instance);
        }
    }

    public NutsElementMapper getMapper(Type type, boolean defaultOnly) {
        if (type == null) {
            return F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type);
        if (cls.isArray()) {
            NutsElementMapper f = defaultMappers.getExact(cls);
            if (f != null) {
                return f;
            }
            return F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NutsElementMapper f = customMappers.get(cls);
            if (f != null) {
                return f;
            }
        }
        final NutsElementMapper r = defaultMappers.get(cls);
        if (r != null) {
            return r;
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle(
                "unable to find serialization factory for %s", type
        ));
    }

    protected Object createObject(NutsElement o, Type to, NutsElementFactoryContext context, boolean defaultOnly) {
        if (o == null || o.type() == NutsElementType.NULL) {
            return F_NULL.createObject(o, to, context);
        }
        if(to==null){
            switch (o.type()){
                case OBJECT:{
                    to=Map.class;
                    break;
                }
                case ARRAY:{
                    to=List.class;
                    break;
                }
                case STRING:{
                    to=String.class;
                    break;
                }
                case INTEGER:{
                    to=Integer.class;
                    break;
                }
                case FLOAT:{
                    to=Float.class;
                    break;
                }
                case DOUBLE:{
                    to=Double.class;
                    break;
                }
                case BOOLEAN:{
                    to=Boolean.class;
                    break;
                }
                case INSTANT:{
                    to=Instant.class;
                    break;
                }
                case BIG_DECIMAL:{
                    to=BigDecimal.class;
                    break;
                }
                case BIG_INTEGER:{
                    to=BigInteger.class;
                    break;
                }
                case LONG:{
                    to=Long.class;
                    break;
                }
                case BYTE:{
                    to=Byte.class;
                    break;
                }
                case SHORT:{
                    to=Short.class;
                    break;
                }
                case NULL:{
                    return null;
                }
                default:{
                    throw new NutsUnsupportedEnumException(context.getSession(), o.type());
                }
            }
        }
        NutsElementMapper f = getMapper(to, defaultOnly);
        return f.createObject(o, to, context);
    }

    @Override
    public Object createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        return createObject(o, to, context, false);
    }

    @Override
    public Object defaultCreateObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        return createObject(o, to, context, true);
    }

    protected Object destruct(Object o, Type expectedType, NutsElementFactoryContext context, boolean defaultOnly) {
        if (o == null) {
            return null;
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (context.getDestructTypeFilter() != null) {
            if (!context.getDestructTypeFilter().test(o.getClass())) {
                return o;
            }
        }
        return getMapper(expectedType, defaultOnly).destruct(o, expectedType, context);
    }

    @Override
    public Object destruct(Object o, Type expectedType, NutsElementFactoryContext context) {
        return destruct(o, expectedType, context, false);
    }

    @Override
    public Object defaultDestruct(Object o, Type expectedType, NutsElementFactoryContext context) {
        return destruct(o, expectedType, context, true);
    }

    protected NutsElement createElement(Object o, Type expectedType, NutsElementFactoryContext context, boolean defaultOnly) {
        if (o == null) {
            return context.element().forNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        return getMapper(expectedType, defaultOnly).createElement(o, expectedType, context);
    }

    @Override
    public NutsElement createElement(Object o, Type expectedType, NutsElementFactoryContext context) {
        return createElement(o, expectedType, context, false);
    }

    @Override
    public NutsElement defaultCreateElement(Object o, Type expectedType, NutsElementFactoryContext context) {
        return createElement(o, expectedType, context, true);
    }

    public ReflectRepository getTypesRepository() {
        return typesRepository;
    }

    public static List<Object> _destructArray1(Object array, NutsElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<Object> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.destruct(Array.get(array, i), null));
            }
            return preloaded;
        } else {
            return Arrays.stream((Object[]) array).map(x -> context.objectToElement(x, null)).collect(Collectors.toList());
        }
    }

    public static NutsArrayElement _createArray1(Object array, NutsElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<NutsElement> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.objectToElement(Array.get(array, i), null));
            }
            return new DefaultNutsArrayElement(preloaded,context.getSession());
        } else {
            return new DefaultNutsArrayElement(
                    Arrays.stream((Object[]) array).map(x -> context.objectToElement(x, null)).collect(Collectors.toList())
                    ,context.getSession()
            );
        }
    }

}
