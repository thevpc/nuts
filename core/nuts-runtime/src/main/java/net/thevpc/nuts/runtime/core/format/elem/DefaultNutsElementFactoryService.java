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
package net.thevpc.nuts.runtime.core.format.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.xml.NutsElementFactoryXmlDocument;
import net.thevpc.nuts.runtime.core.format.xml.NutsElementFactoryXmlElement;
import com.google.gson.JsonElement;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.bundles.datastr.ClassMap;

/**
 *
 * @author thevpc
 */
public class DefaultNutsElementFactoryService implements NutsElementFactoryService {

    private static final NutsElementFactory F_STRINGS = new NutsElementFactoryString();
    private static final NutsElementFactory F_NUMBERS = new NutsElementFactoryNumber();
    private static final NutsElementFactory F_BOOLEANS = new NutsElementFactoryBoolean();
    private static final NutsElementFactory F_ENUMS = new NutsElementFactoryEnum();
    private static final NutsElementFactory F_DATE = new NutsElementFactoryDate();
    private static final NutsElementFactory F_INSTANT = new NutsElementFactoryInstant();
    private static final NutsElementFactory F_COLLECTION = new NutsElementFactoryCollection();
    private static final NutsElementFactory F_ITERATOR = new NutsElementFactoryIterator();
    private static final NutsElementFactory F_MAP = new NutsElementFactoryMap();
    private static final NutsElementFactory F_NAMED_ELEM = new NutsElementFactoryNamedElement();
    private static final NutsElementFactory F_MAPENTRY = new NutsElementFactoryMapEntry();
    private static final NutsElementFactory F_XML_ELEMENT = new NutsElementFactoryXmlElement();
    private static final NutsElementFactory F_XML_DOCUMENT = new NutsElementFactoryXmlDocument();
    private static final NutsElementFactory F_NUTS_DEF = new NutsElementFactoryNutsDefinition();
    private static final NutsElementFactory F_NUTS_ID = new NutsElementFactoryNutsId();
//    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private final ClassMap<NutsElementFactory> factories = new ClassMap<>(null, NutsElementFactory.class);
    private final NutsWorkspace ws;

    public DefaultNutsElementFactoryService(NutsWorkspace ws) {
        addHierarchyFactory(String.class, F_STRINGS);
        addHierarchyFactory(Boolean.class, F_BOOLEANS);
        addHierarchyFactory(StringBuilder.class, F_STRINGS);
        addHierarchyFactory(StringBuffer.class, F_STRINGS);
        addHierarchyFactory(Number.class, F_NUMBERS);
        addHierarchyFactory(java.util.Date.class, F_DATE);
        addHierarchyFactory(java.time.Instant.class, F_INSTANT);
        addHierarchyFactory(Enum.class, F_ENUMS);

//        addHierarchyFactory(JsonElement.class, F_JSONELEMENT);
        addHierarchyFactory(org.w3c.dom.Element.class, F_XML_ELEMENT);
        addHierarchyFactory(org.w3c.dom.Document.class, F_XML_DOCUMENT);
        addHierarchyFactory(NutsDefinition.class, F_NUTS_DEF);
        addHierarchyFactory(NutsId.class, F_NUTS_ID);
        addHierarchyFactory(NutsNamedElement.class, F_NAMED_ELEM);

        addHierarchyFactory(Collection.class, F_COLLECTION);
        addHierarchyFactory(Iterator.class, F_ITERATOR);
        addHierarchyFactory(Map.class, F_MAP);
        addHierarchyFactory(Map.Entry.class, F_MAPENTRY);
        this.ws = ws;
    }

    public final void addHierarchyFactory(Class cls, NutsElementFactory instance) {
        factories.put(cls, instance);
    }

    @Override
    public NutsElement create(Object o, NutsElementFactoryContext context) {
        if (o == null) {
            return context.builder().forNull();
        }
        if (o instanceof NutsElement) {
            return (NutsElement) o;
        }
        Class<?> cls = o.getClass();
        NutsElementFactory a = factories.get(cls);
        if (a != null) {
            return a.create(o, context);
        }
        if (o.getClass().isArray()) {
            return new NutsArrayElementMapper(o, context);
        }
        if (context != null && context.getFallback() != null) {
            NutsElement f = context.getFallback().create(o, context);
            if (f != null) {
                return f;
            }
        }
        DefaultNutsElementFormat json = (DefaultNutsElementFormat) ws.formats().element().setContentType(NutsContentType.JSON);
//        return create(json.convert(o, JsonElement.class), context);
        return json.convert(o, NutsElement.class);
        // new DefaultNutsPrimitiveElement(NutsElementType.UNKNWON, o)
    }

    public NutsElement fromJsonElement(JsonElement o) {
        return create(o, null);
    }

    private static class NutsElementFactoryNamedElement implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            NutsNamedElement je = (NutsNamedElement) o;
            Map<String, Object> m = new HashMap<>();
            m.put("name", je.getName());
            m.put("value", je.getValue());
            return new NutsObjectElementMap1(m, context);
        }
    }

    private static class NutsElementFactoryMap implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            Map je = (Map) o;
            boolean type1 = true;
            for (Object object : je.keySet()) {
                if (!(object instanceof String)) {
                    type1 = false;
                    break;
                }
            }
            if (type1) {
                return new NutsObjectElementMap1(je, context);
            }
            return new NutsObjectElementMap2(je, context);
        }
    }

    private static class NutsElementFactoryMapEntry implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            Map.Entry je = (Map.Entry) o;
            Map<String,Object> m = new HashMap<>();
            m.put("key", je.getKey());
            m.put("value", je.getValue());
            return context.toElement(m);
        }
    }

    private static class NutsElementFactoryCollection implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return new NutsArrayElementMapper(o, context);
        }
    }

    private static class NutsElementFactoryIterator implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return new NutsArrayElementMapper(o, context);
        }
    }

    private static class NutsElementFactoryDate implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return context.builder().forDate((Date) o);
        }
    }
    private static class NutsElementFactoryInstant implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return context.builder().forDate((Date) o);
        }
    }

    private static class NutsElementFactoryNumber implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return context.builder().forNumber((Number) o);
        }
    }

    private static class NutsElementFactoryBoolean implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return context.builder().forBoolean((Boolean) o);
        }
    }

    private static class NutsElementFactoryEnum implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return context.builder().forString(String.valueOf(o));
        }
    }

    private static class NutsElementFactoryString implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            return context.builder().forString(String.valueOf(o));
        }
    }

    private static class NutsElementFactoryNutsDefinition implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            if (o instanceof NutsDefinition) {
                NutsDefinition def = (NutsDefinition) o;
                Map<String, Object> x = new LinkedHashMap<>();
                x.put("id", def.getId());
                NutsContent content = def.getContent();
                if (content != null) {
                    if (content.getPath() != null) {
                        x.put("path", content.getPath().toString());
                    }
                    x.put("cached", content.isCached());
                    x.put("temporary", content.isTemporary());
                }
                NutsInstallInformation installation = def.getInstallInformation();
                if (installation != null) {
                    if (installation.getInstallFolder() != null) {
                        x.put("install-folder", installation.getInstallFolder().toString());
                    }
                    x.put("install-status", installation.getInstallStatus().toString());
                    x.put("was-installed", installation.isWasInstalled());
                    x.put("was-required", installation.isWasRequired());
                }
                if (def.getRepositoryName() != null) {
                    x.put("repository-name", def.getRepositoryName());
                }
                if (def.getRepositoryUuid() != null) {
                    x.put("repository-uuid", def.getRepositoryUuid());
                }
                if (def.getDescriptor() != null) {
                    x.put("descriptor", def.getDescriptor());
//                x.put("effective-descriptor", toCanonical(ws,def.getEffectiveDescriptor()));
                }
                return context.toElement(x);
            }
            throw new NutsIllegalArgumentException(context.getWorkspace(),"unsupported");
        }
    }

    private static class NutsElementFactoryNutsId implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            if (o instanceof NutsId) {
                NutsId def = (NutsId) o;
                return context.toElement(def.toString());
            }
            throw new NutsIllegalArgumentException(context.getWorkspace(),"unsupported");
        }
    }

}
