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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

import java.io.File;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.elem.mapper.*;
import net.thevpc.nuts.runtime.standalone.format.xml.NElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.standalone.format.xml.NElementFactoryXmlElement;
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

import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author thevpc
 */
public class DefaultNElementFactoryService implements NElementFactoryService {

    private static final NElementMapper F_NULL = new NElementMapperNull();
    private static final NElementMapper F_NUTS_ARR = new NElementMapperArray();
    private static final NElementMapper F_STRINGS = new NElementMapperString();
    private static final NElementMapper F_CHAR = new NElementMapperChar();
    private static final NElementMapper F_NUMBERS = new NElementMapperNumber();
    private static final NElementMapper F_BOOLEANS = new NElementMapperBoolean();
    private static final NElementMapper F_ENUMS = new NElementMapperEnum();
    private static final NElementMapper F_INSTANT = new NElementMapperInstant();
    private static final NElementMapper F_DATE = new NElementMapperUtilDate();
    private static final NElementMapper F_PATH = new NElementMapperPath();
    private static final NElementMapper F_FILE = new NElementMapperFile();
    private static final NElementMapper F_ITERATOR = new NElementMapperIterator();
    private static final NElementMapper F_NAMED_ELEM = new NElementMapperNamedElement();
    private static final NElementMapper F_MAPENTRY = new NElementMapperMapEntry();
    private static final NElementMapper F_XML_ELEMENT = new NElementFactoryXmlElement();
    private static final NElementMapper F_XML_DOCUMENT = new NElementFactoryXmlDocument();
    private static final NElementMapper F_NUTS_DEF = new NElementMapperNDefinition();
    private static final NElementMapper F_NUTS_ID = new NElementMapperNId();
    private static final NElementMapper F_NUTS_VERSION = new NElementMapperNVersion();
    private static final NElementMapper F_NUTS_DESCRIPTOR = new NElementMapperNDescriptor();
    private static final NElementMapper F_NUTS_ENV_CONDITION = new NElementMapperNEnvCondition();
    private static final NElementMapper F_NUTS_ENV_CONDITION_BUILDER = new NElementMapperNEnvConditionBuilder();
    private static final NElementMapper F_NUTS_DEPENDENCY = new NElementMapperNDependency();
    private static final NElementMapper F_NUTS_SDK_LOCATION = new NElementMapperNPlatformLocation();
    private static final NElementMapper F_NUTS_ID_LOCATION = new NElementMapperNIdLocation();
    private static final NElementMapper F_ARTIFACT_CALL = new NElementMapperNArtifactCall();
    private static final NElementMapper F_DESCRIPTOR_PROPERTY = new NElementMapperNDescriptorProperty();
    private static final NElementMapper F_DESCRIPTOR_CONTRIBUTOR = new NElementMapperNDescriptorContributor();
    private static final NElementMapper F_DESCRIPTOR_LICENSE = new NElementMapperNDescriptorLicense();
    private static final NElementMapper F_DESCRIPTOR_ORGANIZATION = new NElementMapperNDescriptorOrganization();
    private static final NElementMapper F_DESCRIPTOR_PROPERTY_BUILDER = new NElementMapperNDescriptorPropertyBuilder();
    private static final NElementMapper F_NUTS_ENUM = new NElementMapperNEnum();
    private static final NElementMapper F_NUTS_REPO_LOCATION = new NElementMapperNRepositoryLocation();

//    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private final NClassMap<NElementMapper> defaultMappers = new NClassMap<>(null, NElementMapper.class);
    private final Map<Class, NElementMapper> coreMappers = new HashMap<>();
    private final NClassMap<NElementMapper> customMappers = new NClassMap<>(null, NElementMapper.class);
    private NReflectRepository typesRepository;
    private final NWorkspace workspace;
    private final NElementMapper F_OBJ = new NElementMapperObjReflect(this);

    private final NElementMapper F_COLLECTION = new NElementMapperCollection(this);
    private final NElementMapper F_MAP = new NElementMapperMap(this);

    public DefaultNElementFactoryService(NWorkspace workspace) {
        typesRepository = NWorkspaceUtils.of(workspace).getReflectRepository();
        this.workspace=workspace;
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
        addDefaultMapper(boolean[].class, new NElementMapperPrimitiveBooleanArray());
        addDefaultMapper(byte[].class, new NElementMapperPrimitiveByteArray());
        addDefaultMapper(short[].class, new NElementMapperPrimitiveShortArray());
        addDefaultMapper(char[].class, new NElementMapperPrimitiveCharArray());
        addDefaultMapper(int[].class, new NElementMapperPrimitiveIntArray());
        addDefaultMapper(long[].class, new NElementMapperPrimitiveLongArray());
        addDefaultMapper(float[].class, new NElementMapperFloatArray());
        addDefaultMapper(double[].class, new NElementMapperPrimitiveDoubleArray());
        addDefaultMapper(Object[].class, new NElementMapperObjectArray());
        addDefaultMapper(NPrimitiveElement.class, new NElementMapperNPrimitiveElement());
        addDefaultMapper(NArrayElement.class, new NElementMapperNArrayElement());
        addDefaultMapper(NObjectElement.class, new NElementMapperNObjectElement());
        addDefaultMapper(NArrayElementBuilder.class, new NElementMapperNElementBuilder());
        addDefaultMapper(NObjectElementBuilder.class, new NElementMapperNElementBuilder());
        addDefaultMapper(NElement.class, new NElementMapperNElement());
        addDefaultMapper(NElementEntry.class, F_NAMED_ELEM);
        addDefaultMapper(NCmdLine.class, new NElementMapperCmdLine());
//        addDefaultMapper(NText.class, new NElementMapperNString());
        addDefaultMapper(NText.class, new NElementMapperNText());
        addDefaultMapper(NPath.class, new NElementMapperNPath());
        addDefaultMapper(NFilter.class, new NElementMapperNFilter());

//        addHierarchyFactory(JsonElement.class, F_JSONELEMENT);
        setCoreMapper(NDefinition.class, F_NUTS_DEF);
        setCoreMapper(NId.class, F_NUTS_ID);
        setCoreMapper(NVersion.class, F_NUTS_VERSION);
        setCoreMapper(NDescriptor.class, F_NUTS_DESCRIPTOR);
        setCoreMapper(NDependency.class, F_NUTS_DEPENDENCY);
        setCoreMapper(NIdLocation.class, F_NUTS_ID_LOCATION);
        setCoreMapper(NArtifactCall.class, F_ARTIFACT_CALL);
        setCoreMapper(NPlatformLocation.class, F_NUTS_SDK_LOCATION);
        setCoreMapper(NEnvCondition.class, F_NUTS_ENV_CONDITION);
        setCoreMapper(NEnvConditionBuilder.class, F_NUTS_ENV_CONDITION_BUILDER);
        setCoreMapper(NDescriptorProperty.class, F_DESCRIPTOR_PROPERTY);
        setCoreMapper(NDescriptorPropertyBuilder.class, F_DESCRIPTOR_PROPERTY_BUILDER);
        setCoreMapper(NDescriptorContributor.class, F_DESCRIPTOR_CONTRIBUTOR);
        setCoreMapper(NDescriptorContributorBuilder.class, F_DESCRIPTOR_CONTRIBUTOR);
        setCoreMapper(NDescriptorLicense.class, F_DESCRIPTOR_LICENSE);
        setCoreMapper(NDescriptorLicenseBuilder.class, F_DESCRIPTOR_LICENSE);
        setCoreMapper(NDescriptorOrganization.class, F_DESCRIPTOR_ORGANIZATION);
        setCoreMapper(NDescriptorOrganizationBuilder.class, F_DESCRIPTOR_ORGANIZATION);
        setCoreMapper(NEnum.class, F_NUTS_ENUM);
        setCoreMapper(NRepositoryLocation.class, F_NUTS_REPO_LOCATION);
        setCoreMapper(NLiteral.class, new NElementMapperNLiteral());
    }

    public final void addDefaultMapper(Class cls, NElementMapper instance) {
        defaultMappers.put(cls, instance);
        setCoreMapper(cls,instance);
    }

    public final void setCoreMapper(Class cls, NElementMapper instance) {
        coreMappers.put(cls, instance);
        customMappers.put(cls, instance);
    }

    public final void setMapper(Class cls, NElementMapper instance) {
        if (instance == null) {
            NElementMapper cc = coreMappers.get(cls);
            if (cc != null) {
                customMappers.put(cls, cc);
            } else {
                customMappers.remove(cls);
            }
        } else {
            customMappers.put(cls, instance);
        }
    }

    public NElementMapper getMapper(Type type, boolean defaultOnly) {
        if (type == null) {
            return F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type);
        NSession session = workspace.currentSession();
        if(NSession.class.isAssignableFrom(cls)){
            throw new NIllegalArgumentException(NMsg.ofC(
                    "%s is not serializable", type
            ));
        }
        if (cls.isArray()) {
            NElementMapper f = defaultMappers.getExact(cls);
            if (f != null) {
                return f;
            }
            return F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NElementMapper f = customMappers.get(cls);
            if (f != null) {
                return f;
            }
        }
        final NElementMapper r = defaultMappers.get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find serialization factory for %s", type
        ));
    }

    protected Object createObject(NElement o, Type to, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null || o.type() == NElementType.NULL) {
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
                    throw new NUnsupportedEnumException(o.type());
                }
            }
        }
        NElementMapper f = getMapper(to, defaultOnly);
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
        return getMapper(expectedType, defaultOnly).destruct(o, expectedType, context);
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
                preloaded.add(context.objectToElement(Array.get(array, i), null));
            }
            return new DefaultNArrayElement(preloaded,context.getWorkspace());
        } else {
            return new DefaultNArrayElement(
                    Arrays.stream((Object[]) array).map(x -> context.objectToElement(x, null)).collect(Collectors.toList())
                    ,context.getWorkspace()
            );
        }
    }

}
