package net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.runtime.standalone.elem.mapper.*;
import net.thevpc.nuts.runtime.standalone.format.xml.NElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.standalone.format.xml.NElementFactoryXmlElement;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NLiteral;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultElementMapperStore {
    public static final NElementMapper F_NULL = new NElementMapperNull();
    public static final NElementMapper F_NUTS_ARR = new NElementMapperArray();
    public static final NElementMapper F_STRINGS = new NElementMapperString();
    public static final NElementMapper F_CHAR = new NElementMapperChar();
    public static final NElementMapper F_NUMBERS = new NElementMapperNumber();
    public static final NElementMapper F_BOOLEANS = new NElementMapperBoolean();
    public static final NElementMapper F_ENUMS = new NElementMapperEnum();
    public static final NElementMapper F_INSTANT = new NElementMapperInstant();
    public static final NElementMapper F_DATE = new NElementMapperUtilDate();
    public static final NElementMapper F_PATH = new NElementMapperPath();
    public static final NElementMapper F_FILE = new NElementMapperFile();
    public static final NElementMapper F_ITERATOR = new NElementMapperIterator();
    //    public static final NElementMapper F_NPAIR_ELEM = new NElementMapperPairElement();
    public static final NElementMapper F_MAPENTRY = new NElementMapperMapEntry();
    public static final NElementMapper F_XML_ELEMENT = new NElementFactoryXmlElement();
    public static final NElementMapper F_XML_DOCUMENT = new NElementFactoryXmlDocument();
    public static final NElementMapper F_NUTS_DEF = new NElementMapperNDefinition();
    public static final NElementMapper F_NUTS_ID = new NElementMapperNId();
    public static final NElementMapper F_NUTS_VERSION = new NElementMapperNVersion();
    public static final NElementMapper F_NUTS_DESCRIPTOR = new NElementMapperNDescriptor();
    public static final NElementMapper F_NUTS_ENV_CONDITION = new NElementMapperNEnvCondition();
    public static final NElementMapper F_NUTS_ENV_CONDITION_BUILDER = new NElementMapperNEnvConditionBuilder();
    public static final NElementMapper F_NUTS_DEPENDENCY = new NElementMapperNDependency();
    public static final NElementMapper F_NUTS_SDK_LOCATION = new NElementMapperNPlatformLocation();
    public static final NElementMapper F_NUTS_ID_LOCATION = new NElementMapperNIdLocation();
    public static final NElementMapper F_ARTIFACT_CALL = new NElementMapperNArtifactCall();
    public static final NElementMapper F_DESCRIPTOR_PROPERTY = new NElementMapperNDescriptorProperty();
    public static final NElementMapper F_DESCRIPTOR_CONTRIBUTOR = new NElementMapperNDescriptorContributor();
    public static final NElementMapper F_DESCRIPTOR_LICENSE = new NElementMapperNDescriptorLicense();
    public static final NElementMapper F_DESCRIPTOR_ORGANIZATION = new NElementMapperNDescriptorOrganization();
    public static final NElementMapper F_DESCRIPTOR_PROPERTY_BUILDER = new NElementMapperNDescriptorPropertyBuilder();
    public static final NElementMapper F_NUTS_ENUM = new NElementMapperNEnum();
    public static final NElementMapper F_NUTS_REPO_LOCATION = new NElementMapperNRepositoryLocation();
    public static final NElementMapper F_LITERAL = new NElementMapperNLiteral();
    public static final NElementMapperPrimitiveBooleanArray F_BOOLEAN_ARRAY = new NElementMapperPrimitiveBooleanArray();
    public static final NElementMapperPrimitiveByteArray B_BYTE_ARRAY = new NElementMapperPrimitiveByteArray();
    public static final NElementMapperPrimitiveShortArray F_SHORT_ARRAY = new NElementMapperPrimitiveShortArray();
    public static final NElementMapperPrimitiveCharArray F_CHAR_ARRAY = new NElementMapperPrimitiveCharArray();
    public static final NElementMapperPrimitiveIntArray F_INT_ARRAY = new NElementMapperPrimitiveIntArray();
    public static final NElementMapperPrimitiveLongArray F_LONG_ARRAY = new NElementMapperPrimitiveLongArray();
    public static final NElementMapperFloatArray F_FLOAT_ARRAY = new NElementMapperFloatArray();
    public static final NElementMapperPrimitiveDoubleArray F_DOUBLE_ARRAY = new NElementMapperPrimitiveDoubleArray();
    public static final NElementMapperObjectArray F_OBJECT_ARRAY = new NElementMapperObjectArray();
    public static final NElementMapperNPrimitiveElement F_PRIMITIVE_ELEMENT = new NElementMapperNPrimitiveElement();
    public static final NElementMapperNArrayElement F_ARRAY_ELEMENT = new NElementMapperNArrayElement();
    public static final NElementMapperNObjectElement F_OBJECT_ELEMENT = new NElementMapperNObjectElement();
    public static final NElementMapperNElementBuilder F_ARRAY_ELEMENT_BUILDER = new NElementMapperNElementBuilder();
    public static final NElementMapperNElementBuilder F_OBJECT_ELEMENT_BUILDER = new NElementMapperNElementBuilder();
    public static final NElementMapperNElement F_ELEMENT = new NElementMapperNElement();
    public static final NElementMapperCmdLine F_CMDLINE = new NElementMapperCmdLine();
    public static final NElementMapperNText F_NTEXT = new NElementMapperNText();
    public static final NElementMapperNPath F_NPATH = new NElementMapperNPath();
    public static final NElementMapperNFilter F_NFILTER = new NElementMapperNFilter();
    public final NElementMapper F_OBJ = new NElementMapperObjReflect();

    public final NElementMapper F_COLLECTION = new NElementMapperCollection();
    public final NElementMapper F_MAP = new NElementMapperMap();

    private final NClassMap<NElementMapper> defaultMappers = new NClassMap<>(null, NElementMapper.class);
    private final NClassMap<NElementMapper> coreMappers = new NClassMap<>(null, NElementMapper.class);

    public DefaultElementMapperStore() {
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
        addDefaultMapper(boolean[].class, F_BOOLEAN_ARRAY);
        addDefaultMapper(byte[].class, B_BYTE_ARRAY);
        addDefaultMapper(short[].class, F_SHORT_ARRAY);
        addDefaultMapper(char[].class, F_CHAR_ARRAY);
        addDefaultMapper(int[].class, F_INT_ARRAY);
        addDefaultMapper(long[].class, F_LONG_ARRAY);
        addDefaultMapper(float[].class, F_FLOAT_ARRAY);
        addDefaultMapper(double[].class, F_DOUBLE_ARRAY);
        addDefaultMapper(Object[].class, F_OBJECT_ARRAY);
        addDefaultMapper(NPrimitiveElement.class, F_PRIMITIVE_ELEMENT);
        addDefaultMapper(NArrayElement.class, F_ARRAY_ELEMENT);
        addDefaultMapper(NObjectElement.class, F_OBJECT_ELEMENT);
        addDefaultMapper(NArrayElementBuilder.class, F_ARRAY_ELEMENT_BUILDER);
        addDefaultMapper(NObjectElementBuilder.class, F_OBJECT_ELEMENT_BUILDER);
        addDefaultMapper(NElement.class, F_ELEMENT);
//        addDefaultMapper(NPairElement.class, F_NPAIR_ELEM);
        addDefaultMapper(NCmdLine.class, F_CMDLINE);
//        addDefaultMapper(NText.class, new NElementMapperNString());
        addDefaultMapper(NText.class, F_NTEXT);
        addDefaultMapper(NPath.class, F_NPATH);
        addDefaultMapper(NFilter.class, F_NFILTER);

        /// /////  Core

        addCoreMapper(NDefinition.class, F_NUTS_DEF);
        addCoreMapper(NId.class, F_NUTS_ID);
        addCoreMapper(NVersion.class, F_NUTS_VERSION);
        addCoreMapper(NDescriptor.class, F_NUTS_DESCRIPTOR);
        addCoreMapper(NDependency.class, F_NUTS_DEPENDENCY);
        addCoreMapper(NIdLocation.class, F_NUTS_ID_LOCATION);
        addCoreMapper(NArtifactCall.class, F_ARTIFACT_CALL);
        addCoreMapper(NPlatformLocation.class, F_NUTS_SDK_LOCATION);
        addCoreMapper(NEnvCondition.class, F_NUTS_ENV_CONDITION);
        addCoreMapper(NEnvConditionBuilder.class, F_NUTS_ENV_CONDITION_BUILDER);
        addCoreMapper(NDescriptorProperty.class, F_DESCRIPTOR_PROPERTY);
        addCoreMapper(NDescriptorPropertyBuilder.class, F_DESCRIPTOR_PROPERTY_BUILDER);
        addCoreMapper(NDescriptorContributor.class, F_DESCRIPTOR_CONTRIBUTOR);
        addCoreMapper(NDescriptorContributorBuilder.class, F_DESCRIPTOR_CONTRIBUTOR);
        addCoreMapper(NDescriptorLicense.class, F_DESCRIPTOR_LICENSE);
        addCoreMapper(NDescriptorLicenseBuilder.class, F_DESCRIPTOR_LICENSE);
        addCoreMapper(NDescriptorOrganization.class, F_DESCRIPTOR_ORGANIZATION);
        addCoreMapper(NDescriptorOrganizationBuilder.class, F_DESCRIPTOR_ORGANIZATION);
        addCoreMapper(NEnum.class, F_NUTS_ENUM);
        addCoreMapper(NRepositoryLocation.class, F_NUTS_REPO_LOCATION);
        addCoreMapper(NLiteral.class, F_LITERAL);

    }

    public final void addCoreMapper(Class cls, NElementMapper instance) {
        coreMappers.put(cls, instance);
    }

    public NClassMap<NElementMapper> getCoreMappers() {
        return coreMappers;
    }

    public final void addDefaultMapper(Class cls, NElementMapper instance) {
        defaultMappers.put(cls, instance);
    }

    public NClassMap<NElementMapper> getDefaultMappers() {
        return defaultMappers;
    }

    public <T> NElementMapper<T> getMapper(NElement element, NElementMapperStore store) {
        switch (element.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case PARAMETRIZED_OBJECT: {
                return store.getMapper(Map.class);
            }
            case ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case UPLET:
            case NAMED_UPLET: {
                return store.getMapper(List.class);
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            case REGEX:
            case NAME: {
                return store.getMapper(String.class);
            }
            case INT: {
                return store.getMapper(Integer.class);
            }
            case FLOAT: {
                return store.getMapper(Float.class);
            }
            case DOUBLE: {
                return store.getMapper(Double.class);
            }
            case BOOLEAN: {
                return store.getMapper(Boolean.class);
            }
            case INSTANT: {
                return store.getMapper(Instant.class);
            }
            case LOCAL_DATE: {
                return store.getMapper(LocalDate.class);
            }
            case LOCAL_DATETIME: {
                return store.getMapper(LocalDateTime.class);
            }
            case LOCAL_TIME: {
                return store.getMapper(LocalTime.class);
            }
            case BIG_DECIMAL: {
                return store.getMapper(BigDecimal.class);
            }
            case BIG_INT: {
                return store.getMapper(BigInteger.class);
            }
            case LONG: {
                return store.getMapper(Long.class);
            }
            case BYTE: {
                return store.getMapper(Byte.class);
            }
            case SHORT: {
                return store.getMapper(Short.class);
            }
            case CHAR: {
                return store.getMapper(Character.class);
            }
            case FLOAT_COMPLEX: {
                return store.getMapper(NFloatComplex.class);
            }
            case BIG_COMPLEX: {
                return store.getMapper(NBigComplex.class);
            }
            case DOUBLE_COMPLEX: {
                return store.getMapper(NDoubleComplex.class);
            }
            case MATRIX:
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
            case PARAMETRIZED_MATRIX: {
                return store.getMapper(NMatrixElement.class);
            }
            case NULL: {
                return F_NULL;
            }
            case PAIR: {
                return store.getMapper(Map.Entry.class);
            }
            case CHAR_STREAM: {
                return store.getMapper(char[].class);
            }
            case BINARY_STREAM: {
                return store.getMapper(byte[].class);
            }
        }
        return null;
    }
}
