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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import java.io.File;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.mapper.*;
import net.thevpc.nuts.runtime.standalone.format.xml.NutsElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.standalone.format.xml.NutsElementFactoryXmlElement;
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

import net.thevpc.nuts.runtime.standalone.util.collections.ClassMap;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectRepository;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositoryLocation;

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
    private static final NutsElementMapper F_NUTS_ENUM = new NutsElementMapperNutsEnum();
    private static final NutsElementMapper F_NUTS_REPO_LOCATION = new NutsElementMapperNutsRepositoryLocation();

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
        addDefaultMapper(Boolean.class, F_BOOLEANS);
        addDefaultMapper(boolean.class, F_BOOLEANS);
        addDefaultMapper(byte.class, F_NUMBERS);
        addDefaultMapper(short.class, F_NUMBERS);
        addDefaultMapper(int.class, F_NUMBERS);
        addDefaultMapper(long.class, F_NUMBERS);
        addDefaultMapper(float.class, F_NUMBERS);
        addDefaultMapper(double.class, F_NUMBERS);
        addDefaultMapper(Number.class, F_NUMBERS);

        addDefaultMapper(char.class, F_CHAR);
        addDefaultMapper(Character.class, F_CHAR);

        addDefaultMapper(Object.class, F_OBJ);
        addDefaultMapper(String.class, F_STRINGS);

        addDefaultMapper(StringBuilder.class, F_STRINGS);
        addDefaultMapper(StringBuffer.class, F_STRINGS);

        addDefaultMapper(Path.class, F_PATH);
        addDefaultMapper(File.class, F_FILE);
        addDefaultMapper(java.util.Date.class, F_DATE);
        addDefaultMapper(java.time.Instant.class, F_INSTANT);
        addDefaultMapper(Enum.class, F_ENUMS);
        addDefaultMapper(Collection.class, F_COLLECTION);
        addDefaultMapper(Iterator.class, F_ITERATOR);
        addDefaultMapper(Map.class, F_MAP);
        addDefaultMapper(Map.Entry.class, F_MAPENTRY);
        addDefaultMapper(org.w3c.dom.Element.class, F_XML_ELEMENT);
        addDefaultMapper(org.w3c.dom.Document.class, F_XML_DOCUMENT);
        addDefaultMapper(boolean[].class, new NutsElementMapperPrimitiveBooleanArray());
        addDefaultMapper(byte[].class, new NutsElementMapperPrimitiveByteArray());
        addDefaultMapper(short[].class, new NutsElementMapperPrimitiveShortArray());
        addDefaultMapper(char[].class, new NutsElementMapperPrimitiveCharArray());
        addDefaultMapper(int[].class, new NutsElementMapperPrimitiveIntArray());
        addDefaultMapper(long[].class, new NutsElementMapperPrimitiveLongArray());
        addDefaultMapper(float[].class, new NutsElementMapperFloatArray());
        addDefaultMapper(double[].class, new NutsElementMapperPrimitiveDoubleArray());
        addDefaultMapper(Object[].class, new NutsElementMapperObjectArray());
        addDefaultMapper(NutsPrimitiveElement.class, new NutsElementMapperNutsPrimitiveElement());
        addDefaultMapper(NutsArrayElement.class, new NutsElementMapperNutsArrayElement());
        addDefaultMapper(NutsObjectElement.class, new NutsElementMapperNutsObjectElement());
        addDefaultMapper(NutsArrayElementBuilder.class, new NutsElementMapperNutsElementBuilder());
        addDefaultMapper(NutsObjectElementBuilder.class, new NutsElementMapperNutsElementBuilder());
        addDefaultMapper(NutsElement.class, new NutsElementMapperNutsElement());
        addDefaultMapper(NutsElementEntry.class, F_NAMED_ELEM);
        addDefaultMapper(NutsCommandLine.class, new NutsElementMapperCommandLine());
        addDefaultMapper(NutsString.class, new NutsElementMapperNutsString());
        addDefaultMapper(NutsText.class, new NutsElementMapperNutsText());
        addDefaultMapper(NutsPath.class, new NutsElementMapperNutsPath());
        addDefaultMapper(NutsFilter.class, new NutsElementMapperNutsFilter());

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
        setCoreMapper(NutsEnum.class, F_NUTS_ENUM);
        setCoreMapper(NutsRepositoryLocation.class, F_NUTS_REPO_LOCATION);
        setCoreMapper(NutsValue.class, new NutsElementMapperNutsValue());
        this.ws = ws;
        this.session = session;
    }

    public final void addDefaultMapper(Class cls, NutsElementMapper instance) {
        defaultMappers.put(cls, instance);
        setCoreMapper(cls,instance);
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
        if(NutsSession.class.isAssignableFrom(cls)){
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle(
                    "%s is not serializable", type
            ));
        }
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
        if (context.getIndestructibleObjects() != null) {
            if (context.getIndestructibleObjects().test(o.getClass())) {
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
            return context.elem().ofNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (context.getIndestructibleObjects() != null) {
            if (context.getIndestructibleObjects().test(o.getClass())) {
                return context.elem().ofCustom(o);
            }
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
            return Arrays.stream((Object[]) array).map(x -> context.destruct(x, null)).collect(Collectors.toList());
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
