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
package net.thevpc.nuts.runtime.core.format.json;

import com.google.gson.JsonParseException;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import net.thevpc.nuts.NutsArtifactCall;
import net.thevpc.nuts.NutsArtifactCallBuilder;
import net.thevpc.nuts.NutsClassifierMapping;
import net.thevpc.nuts.NutsClassifierMappingBuilder;
import net.thevpc.nuts.NutsDependency;
import net.thevpc.nuts.NutsDependencyTreeNode;
import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdLocation;
import net.thevpc.nuts.NutsIdLocationBuilder;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.xml.NutsXmlUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCallBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsClassifierMappingBuilder;
import net.thevpc.nuts.runtime.standalone.MutableNutsDependencyTreeNode;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author vpc
 */
public class AdapterHelpers {

    public static class NutsIdJsonAdapter implements NutsElemenSerializationAdapter<NutsId> {

        @Override
        public NutsId deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            String s = context.getAs(json, String.class);
            if (s == null) {
                return null;
            }
            return context.workspace().id().parser().setLenient(true).parse(s);
        }

        @Override
        public NutsElement serialize(NutsId src, Type typeOfSrc, NutsElementSerializationContext context) {
            return src == null ? context.elements().forNull() : context.elements().forString(src.toString());
        }
    }

    public static class NutsVersionJsonAdapter implements
            NutsElemenSerializationAdapter<NutsVersion> {

        @Override
        public NutsVersion deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            String s = context.getAs(json, String.class);
            if (s == null) {
                return null;
            }
            return context.workspace().version().parser().parse(s);
        }

        @Override
        public NutsElement serialize(NutsVersion src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    public static class NutsDescriptorJsonAdapter implements
            NutsElemenSerializationAdapter<NutsDescriptor> {

        @Override
        public NutsDescriptor deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            DefaultNutsDescriptorBuilder b = context.getAs(json, DefaultNutsDescriptorBuilder.class);
            return context.workspace().descriptor().descriptorBuilder().set(b).build();
        }

        @Override
        public NutsElement serialize(NutsDescriptor src, Type typeOfSrc, NutsElementSerializationContext context) {
            if (src != null) {
                return context.serialize(context.workspace().descriptor().descriptorBuilder().set(src));
            }
            return context.serialize(src);
        }
    }

    public static class NutsDependencyTreeNodeJsonAdapter implements
            NutsElemenSerializationAdapter<NutsDependencyTreeNode> {

        private NutsWorkspace ws;

        public NutsDependencyTreeNodeJsonAdapter(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsDependencyTreeNode deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            return context.getAs(json, MutableNutsDependencyTreeNode.class);
        }

        @Override
        public NutsElement serialize(NutsDependencyTreeNode src, Type typeOfSrc, NutsElementSerializationContext context) {
            if (src != null) {
                return context.serialize(new MutableNutsDependencyTreeNode(src));
            }
            return context.serialize(src);
        }
    }

    public static class NutsDependencyJsonAdapter implements
            NutsElemenSerializationAdapter<NutsDependency> {

        @Override
        public NutsDependency deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            String b = context.getAs(json, String.class);
            return context.workspace().dependency().parser().parseDependency(b);
        }

        @Override
        public NutsElement serialize(NutsDependency src, Type typeOfSrc, NutsElementSerializationContext context) {
            if (src != null) {
                return context.serialize(src.toString());
            }
            return context.serialize(src);
        }
    }

    public static class NutsIdLocationJsonAdapter implements
            NutsElemenSerializationAdapter<NutsIdLocation> {

        @Override
        public NutsIdLocation deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            NutsIdLocationBuilder b = context.getAs(json, DefaultNutsIdLocationBuilder.class);
            return b.build();
        }

        @Override
        public NutsElement serialize(NutsIdLocation src, Type typeOfSrc, NutsElementSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsIdLocationBuilder(src));
            }
            return context.serialize(src);
        }
    }

    public static class NutsClassifierMappingJsonAdapter implements
            NutsElemenSerializationAdapter<NutsClassifierMapping> {

        @Override
        public NutsClassifierMapping deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            NutsClassifierMappingBuilder b = context.getAs(json, DefaultNutsClassifierMappingBuilder.class);
            return b.build();
        }

        @Override
        public NutsElement serialize(NutsClassifierMapping src, Type typeOfSrc, NutsElementSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsClassifierMappingBuilder().set(src));
            }
            return context.serialize(src);
        }
    }

    public static class NutsArtifactCallElementAdapter implements
            NutsElemenSerializationAdapter<NutsArtifactCall> {

        @Override
        public NutsArtifactCall deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            NutsArtifactCallBuilder b = context.getAs(json, DefaultNutsArtifactCallBuilder.class);
            return b.build();
        }

        @Override
        public NutsElement serialize(NutsArtifactCall src, Type typeOfSrc, NutsElementSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsArtifactCallBuilder(src));
            }
            return context.elements().forNull();
        }
    }

    public static class PathJsonAdapter implements
            NutsElemenSerializationAdapter<Path> {

        @Override
        public Path deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            String s = context.getAs(json, String.class);
            return s==null?null:Paths.get(s);
        }

        @Override
        public NutsElement serialize(Path src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.elements().forString(src.toString());
        }
    }

    public static class FileJsonAdapter implements
            NutsElemenSerializationAdapter<File> {

        @Override
        public File deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            String s = context.getAs(json, String.class);
            return s==null?null:new File(s);
        }

        @Override
        public NutsElement serialize(File src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.elements().forString(src.getPath());
        }
    }

    public static class DateJsonAdapter implements NutsElemenSerializationAdapter<Date> {

        @Override
        public NutsElement serialize(Date src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.elements().forString(src.toInstant().toString());
        }

        @Override
        public Date deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            return new Date(context.elements().forDate(
                    context.getAs(json, String.class)
            ).primitive().getDate().toEpochMilli());
        }
    }

    public static class InstantJsonAdapter implements NutsElemenSerializationAdapter<Instant> {

        public InstantJsonAdapter() {
        }

        @Override
        public Instant deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            String s = context.getAs(json, String.class);
            return context.elements().forDate(s).primitive().getDate();
        }

        @Override
        public NutsElement serialize(Instant src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.elements().forDate(src);
        }
    }

    public static class NutsElementElementAdapter implements
            NutsElemenSerializationAdapter<NutsElement> {

        @Override
        public NutsElement deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            return context.getAs(json, NutsElement.class);
        }

        @Override
        public NutsElement serialize(NutsElement src, Type typeOfSrc, NutsElementSerializationContext context) {
            return src;
        }
    }

    public static class XmlElementJsonAdapter implements
            NutsElemenSerializationAdapter<org.w3c.dom.Element> {

        @Override
        public org.w3c.dom.Element deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            NutsElement nelem = context.getAs(json, NutsElement.class);
            return context.workspace().formats().element().toXmlElement(nelem, null);
        }

        @Override
        public NutsElement serialize(org.w3c.dom.Element src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.workspace().formats().element().convert(src, NutsElement.class);
        }
    }

    public static class XmlDocumentJsonAdapter implements
            NutsElemenSerializationAdapter<org.w3c.dom.Document> {

        @Override
        public org.w3c.dom.Document deserialize(Object json, Type typeOfT, NutsElementDeserializationContext context) {
            Document doc;
            try {
                doc = NutsXmlUtils.createDocument(context.session());
            } catch (ParserConfigurationException ex) {
                throw new JsonParseException(CoreStringUtils.exceptionToString(ex), ex);
            }
            NutsElement nelem = context.getAs(json, NutsElement.class);
            Element ee = context.workspace().formats().element().toXmlElement(nelem, doc);
            ee = (Element) doc.importNode(ee, true);
            doc.appendChild(ee);
            return doc;
        }

        @Override
        public NutsElement serialize(org.w3c.dom.Document src, Type typeOfSrc, NutsElementSerializationContext context) {
            return context.workspace().formats().element().convert(src.getDocumentElement(), NutsElement.class);
        }
    }

}
