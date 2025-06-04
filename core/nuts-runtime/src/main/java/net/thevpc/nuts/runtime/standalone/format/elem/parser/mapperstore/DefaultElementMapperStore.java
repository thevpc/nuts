package net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore;

import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.format.elem.mapper.*;
import net.thevpc.nuts.runtime.standalone.format.xml.NElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.standalone.format.xml.NElementFactoryXmlElement;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NFilter;

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
    public final NElementMapper F_OBJ = new NElementMapperObjReflect();

    public final NElementMapper F_COLLECTION = new NElementMapperCollection();
    public final NElementMapper F_MAP = new NElementMapperMap();

    private final NClassMap<NElementMapper> defaultMappers = new NClassMap<>(null, NElementMapper.class);

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
//        addDefaultMapper(NPairElement.class, F_NPAIR_ELEM);
        addDefaultMapper(NCmdLine.class, new NElementMapperCmdLine());
//        addDefaultMapper(NText.class, new NElementMapperNString());
        addDefaultMapper(NText.class, new NElementMapperNText());
        addDefaultMapper(NPath.class, new NElementMapperNPath());
        addDefaultMapper(NFilter.class, new NElementMapperNFilter());
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
            case INTEGER: {
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
            case BIG_INTEGER: {
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
