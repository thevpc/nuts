/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.format.elem;

import net.vpc.app.nuts.runtime.format.xml.NutsElementFactoryXmlElement;
import net.vpc.app.nuts.runtime.format.json.DefaultNutsJsonFormat;
import com.google.gson.JsonElement;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.runtime.util.common.ClassMap;
import org.w3c.dom.Node;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsNamedElement;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.format.json.NutsElementFactoryJsonElement;
import net.vpc.app.nuts.runtime.format.xml.NutsElementFactoryXmlDocument;
import net.vpc.app.nuts.NutsInstallInformation;

/**
 *
 * @author vpc
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
    public static final NutsElementFactory F_JSONELEMENT = new NutsElementFactoryJsonElement();

    private final ClassMap<NutsElementFactory> factories = new ClassMap<>(null, NutsElementFactory.class);
    private final NutsWorkspace ws;

    public DefaultNutsElementFactoryService(NutsWorkspace ws) {
        addHirarchyFactory(String.class, F_STRINGS);
        addHirarchyFactory(Boolean.class, F_BOOLEANS);
        addHirarchyFactory(StringBuilder.class, F_STRINGS);
        addHirarchyFactory(StringBuffer.class, F_STRINGS);
        addHirarchyFactory(Number.class, F_NUMBERS);
        addHirarchyFactory(java.util.Date.class, F_DATE);
        addHirarchyFactory(java.time.Instant.class, F_INSTANT);
        addHirarchyFactory(Enum.class, F_ENUMS);

        addHirarchyFactory(JsonElement.class, F_JSONELEMENT);
        addHirarchyFactory(org.w3c.dom.Element.class, F_XML_ELEMENT);
        addHirarchyFactory(org.w3c.dom.Document.class, F_XML_DOCUMENT);
        addHirarchyFactory(NutsDefinition.class, F_NUTS_DEF);
        addHirarchyFactory(NutsId.class, F_NUTS_ID);
        addHirarchyFactory(NutsNamedElement.class, F_NAMED_ELEM);

        addHirarchyFactory(Collection.class, F_COLLECTION);
        addHirarchyFactory(Iterator.class, F_ITERATOR);
        addHirarchyFactory(Map.class, F_MAP);
        addHirarchyFactory(Map.Entry.class, F_MAPENTRY);
        this.ws = ws;
    }

    public final void addHirarchyFactory(Class cls, NutsElementFactory instance) {
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
        DefaultNutsJsonFormat json = (DefaultNutsJsonFormat) ws.json();
        return create(json.convert(o, JsonElement.class), context);
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
            Node je = (Node) o;
            if (je instanceof NutsDefinition) {
                NutsDefinition def = (NutsDefinition) je;
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
                    x.put("installed", installation.isInstalled());
                    x.put("just-installed", installation.isJustInstalled());
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
            throw new IllegalArgumentException("Unsupported");
        }
    }

    private static class NutsElementFactoryNutsId implements NutsElementFactory {

        @Override
        public NutsElement create(Object o, NutsElementFactoryContext context) {
            Node je = (Node) o;
            if (je instanceof NutsId) {
                NutsId def = (NutsId) je;
                return context.toElement(def.toString());
            }
            throw new IllegalArgumentException("Unsupported");
        }
    }

}
