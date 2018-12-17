/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.extensions.core.NutsDependencyImpl;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreVersionUtils;
import net.vpc.app.nuts.extensions.util.MapStringMapper;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Chronometer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {
    private static final Logger log = Logger.getLogger(MavenUtils.class.getName());

    private static boolean isStackSubPath(Stack<String> stack, int offset, String... path) {
        for (int i = 0; i < path.length; i++) {
            int x = stack.size() - offset - path.length + i;
            if (x < 0) {
                return false;
            }
            if (!stack.get(x).equals(path[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStackPath(Stack<String> stack, String... path) {
        if (stack.size() != path.length) {
            return false;
        }
        for (int i = 0; i < path.length; i++) {
            if (!stack.get(stack.size() - path.length + i).equals(path[i])) {
                return false;
            }
        }
        return true;
    }

    public static NutsDescriptor parsePomXml(InputStream stream, String urlDesc) {
        try {
            byte[] bytes = IOUtils.loadByteArray(stream, -1, true);
            int skip = 0;
            while (skip < bytes.length && Character.isWhitespace(bytes[skip])) {
                skip++;
            }
            ByteArrayInputStream ok = new ByteArrayInputStream(bytes);
            ok.skip(skip);
            return parsePomXml0(ok, urlDesc);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    public static NutsDescriptor parsePomXml0(InputStream stream, String urlDesc) {
        long startTime = System.currentTimeMillis();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        List<NutsDependency> deps = new ArrayList<>();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            if (stream == null) {
                return null;
            }
            Document doc = dBuilder.parse(stream);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            NodeList properties = doc.getDocumentElement().getElementsByTagName("properties");
            NodeList rootChildList = doc.getDocumentElement().getChildNodes();
            String groupId = "";
            String artifactId = "";
            String description = "";
            String version = "";
            String p_groupId = "";
            String p_artifactId = "";
            String p_version = "";
            String packaging = "";
            Map<String,String> props=new LinkedHashMap<>();
            for (int i = 0; i < rootChildList.getLength(); i++) {
                Element elem1 = toElement(rootChildList.item(i));
                if(elem1!=null) {
                    switch (elem1.getTagName()) {
                        case "groupId": {
                            groupId = elemToStr(elem1);
                            break;
                        }
                        case "artifactId": {
                            artifactId = elemToStr(elem1);
                            break;
                        }
                        case "version": {
                            version = elemToStr(elem1);
                            break;
                        }
                        case "packaging": {
                            packaging = elemToStr(elem1);
                            break;
                        }
                        case "description": {
                            description = elemToStr(elem1);
                            break;
                        }
                        case "parent": {
                            NodeList parentChildList = elem1.getChildNodes();
                            for (int j = 0; j < parentChildList.getLength(); j++) {
                                Element parElem = toElement(parentChildList.item(j));
                                if (parElem != null) {
                                    switch (parElem.getTagName()) {
                                        case "groupId": {
                                            p_groupId = elemToStr(parElem);
                                            break;
                                        }
                                        case "artifactId": {
                                            p_artifactId = elemToStr(parElem);
                                            break;
                                        }
                                        case "version": {
                                            p_version = elemToStr(parElem);
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case "properties": {
                            NodeList propsChildList = elem1.getChildNodes();
                            for (int j = 0; j < propsChildList.getLength(); j++) {
                                Element parElem = toElement(propsChildList.item(j));
                                if (parElem != null) {
                                    props.put(parElem.getTagName(), elemToStr(parElem));
                                }
                            }
                            break;
                        }
                        case "dependencies": {
                            NodeList dependenciesChildList = elem1.getChildNodes();
                            for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                                Element dependency = toElement(dependenciesChildList.item(j), "dependency");
                                if (dependency != null) {
                                    NodeList dependencyChildList = dependency.getChildNodes();
                                    String d_groupId = "";
                                    String d_artifactId = "";
                                    String d_version = "";
                                    String d_scope = "";
                                    String d_optional = "";
                                    List<NutsId> d_exclusions = new ArrayList<>();
                                    for (int k = 0; k < dependencyChildList.getLength(); k++) {
                                        Element c = toElement(dependencyChildList.item(k));
                                        if (c != null) {
                                            switch (c.getTagName()) {
                                                case "groupId": {
                                                    d_groupId = elemToStr(c);
                                                    break;
                                                }
                                                case "artifactId": {
                                                    d_artifactId = elemToStr(c);
                                                    break;
                                                }
                                                case "version": {
                                                    d_version = elemToStr(c);
                                                    break;
                                                }
                                                case "scope": {
                                                    d_scope = elemToStr(c);
                                                    break;
                                                }
                                                case "optional": {
                                                    d_optional = elemToStr(c);
                                                    break;
                                                }
                                                case "exclusions": {
                                                    NodeList exclusionsList = c.getChildNodes();
                                                    String ex_groupId = "";
                                                    String ex_artifactId = "";
                                                    for (int l = 0; l < exclusionsList.getLength(); l++) {
                                                        Element ex = toElement(exclusionsList.item(l));
                                                        if (ex != null) {
                                                            switch (ex.getTagName()) {
                                                                case "groupId": {
                                                                    ex_groupId = elemToStr(ex);
                                                                    break;
                                                                }
                                                                case "artifactId": {
                                                                    ex_artifactId = elemToStr(ex);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (!ex_groupId.isEmpty()) {
                                                        d_exclusions.add(new NutsIdImpl(ex_groupId, ex_artifactId, null));
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (d_scope.isEmpty()) {
                                        d_scope = "compile";
                                    }
                                    deps.add(new NutsDependencyImpl(
                                            null, d_groupId, d_artifactId, d_version, d_scope, d_optional, d_exclusions.toArray(new NutsId[0])
                                    ));
                                }
                            }

                            break;
                        }
                    }
                }
            }
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom.xml file from  {0} (time {1})", new Object[]{urlDesc, Chronometer.formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom.xml file from  {0}", new Object[]{urlDesc});
            }

            boolean executable = true;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            if (packaging.isEmpty()) {
                packaging="jar";
            }
            return new DefaultNutsDescriptorBuilder()
                    .setId(new NutsIdImpl(
                            null, groupId, artifactId,
                            version,
                            ""
                    ))
                    .setParents(p_groupId.length() == 0 ? new NutsId[0] : new NutsId[]{
                            new NutsIdImpl(
                                    null, p_groupId, p_artifactId,
                                    p_version.toString().trim(),
                                    ""
                            )
                    })
                    .setPackaging(packaging)
                    .setExecutable(executable)
                    .setExt("war".equals(packaging) ? "war" : "jar")
                    .setName(artifactId)
                    .setDescription(description.toString())
                    .setPlatform(new String[]{"java"})
                    .setDependencies(deps.toArray(new NutsDependency[0]))
                    .setProperties(props)
                    .build()
                    ;
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[ERROR  ] Loading pom.xml file from  {0} (time {1})", new Object[]{urlDesc, Chronometer.formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Loading pom.xml file from  {0}", new Object[]{urlDesc});
            }
            throw new NutsParseException("Error Parsing " + urlDesc, e);
        }
    }

    private static String elemToStr(Element ex) {
        return ex.getTextContent() == null ? "" : ex.getTextContent().trim();
    }

    public static NutsDescriptor parsePomXml0Old(InputStream stream, String urlDesc) {
        StringBuilder p_groupId = new StringBuilder();
        StringBuilder p_artifactId = new StringBuilder();
        StringBuilder p_version = new StringBuilder();

        StringBuilder d_groupId = new StringBuilder();
        StringBuilder d_artifactId = new StringBuilder();
        StringBuilder d_version = new StringBuilder();
        StringBuilder d_scope = new StringBuilder();
        StringBuilder d_optional = new StringBuilder();

        StringBuilder e_groupId = new StringBuilder();
        StringBuilder e_artifactId = new StringBuilder();
        StringBuilder e_version = new StringBuilder();

        StringBuilder groupId = new StringBuilder();
        StringBuilder artifactId = new StringBuilder();
        StringBuilder version = new StringBuilder();
        StringBuilder packaging = new StringBuilder();
        StringBuilder name = new StringBuilder();
        StringBuilder description = new StringBuilder();
        StringBuilder propertyValue = new StringBuilder();
        Map<String, String> props = new HashMap<>();
        List<NutsDependency> deps = new ArrayList<>();
        List<NutsId> exclusions = new ArrayList<>();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            XMLEventReader eventReader
                    = factory.createXMLEventReader(new InputStreamReader(stream));
            Stack<String> nodePath = new Stack<>();
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        nodePath.push(qName);
                        if (isStackSubPath(nodePath, 1, "project", "properties")) {
                            StringUtils.clear(propertyValue);
                        }
                        if (isStackPath(nodePath, "project", "dependencies", "dependency")) {
                            StringUtils.clear(d_groupId);
                            StringUtils.clear(d_artifactId);
                            StringUtils.clear(d_scope);
                            StringUtils.clear(d_version);
                            StringUtils.clear(d_optional);
                            exclusions.clear();
                        }
                        if (isStackPath(nodePath, "project", "dependencies", "dependency", "exclusions", "exclusion")) {
                            StringUtils.clear(e_groupId);
                            StringUtils.clear(e_artifactId);
                            StringUtils.clear(e_version);
                        }
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        if (isStackPath(nodePath, "project", "groupId")) {
                            groupId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "artifactId")) {
                            artifactId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "version")) {
                            version.append(event.asCharacters().getData());

                        } else if (isStackPath(nodePath, "project", "parent", "groupId")) {
                            p_groupId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "parent", "artifactId")) {
                            p_artifactId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "parent", "version")) {
                            p_version.append(event.asCharacters().getData());

                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "groupId")) {
                            d_groupId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "artifactId")) {
                            d_artifactId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "version")) {
                            d_version.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "scope")) {
                            d_scope.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "optional")) {
                            d_optional.append(event.asCharacters().getData());

                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "exclusions", "exclusion", "groupId")) {
                            e_groupId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "exclusions", "exclusion", "artifactId")) {
                            e_artifactId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "dependencies", "dependency", "exclusions", "exclusion", "version")) {
                            e_version.append(event.asCharacters().getData());

                        } else if (isStackPath(nodePath, "project", "packaging")) {
                            packaging.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "name")) {
                            name.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "project", "description")) {
                            description.append(event.asCharacters().getData());
                        } else if (isStackSubPath(nodePath, 1, "project", "properties")) {
                            propertyValue.append(event.asCharacters().getData());
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        EndElement endElement = event.asEndElement();
                        String localPart = endElement.getName().getLocalPart();
                        if (isStackSubPath(nodePath, 1, "project", "properties")) {
                            props.put(localPart, propertyValue.toString().trim());
                        }
                        if (isStackPath(nodePath, "project", "dependencies", "dependency", "exclusions", "exclusion")) {
                            exclusions.add(new NutsIdImpl(null, e_groupId.toString(), e_artifactId.toString(), e_version.toString(), (String) null));
                        }
                        if (isStackPath(nodePath, "project", "dependencies", "dependency")) {
                            deps.add(
                                    new NutsDependencyImpl(
                                            null,
                                            d_groupId.toString().trim(),
                                            d_artifactId.toString().trim(),
                                            mavenVersionToNutsVersion(d_version.toString().trim()),
                                            d_scope.toString().trim(),
                                            d_optional.toString().trim(),
                                            exclusions.toArray(new NutsId[0])
                                    )
                            );
                        }
                        nodePath.pop();
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new NutsParseException(e);
        }
        boolean executable = true;// !"maven-archetype".equals(packaging.toString()); // default is true :)
        if (packaging.toString().trim().isEmpty()) {
            packaging.append("jar");
        }
        return new DefaultNutsDescriptorBuilder()
                .setId(new NutsIdImpl(
                        null, groupId.toString().trim(), artifactId.toString().trim(),
                        version.toString().trim(),
                        ""
                ))
                .setParents(p_groupId.length() == 0 ? new NutsId[0] : new NutsId[]{
                        new NutsIdImpl(
                                null, p_groupId.toString().trim(), p_artifactId.toString().trim(),
                                p_version.toString().trim(),
                                ""
                        )
                })
                .setPackaging(packaging.toString())
                .setExecutable(executable)
                .setExt("war".equals(packaging.toString()) ? "war" : "jar")
                .setName(name.toString())
                .setDescription(description.toString())
                .setPlatform(new String[]{"java"})
                .setDependencies(deps.toArray(new NutsDependency[0]))
                .setProperties(props)
                .build()
                ;
    }

    public static String mavenVersionToNutsVersion(String version) {
        return version.replace("(", "]").replace(")", "[");
//        Pattern pattern = Pattern.compile("(((?<VAL1>(?<L1>[\\[\\]()])(?<LV1>[^\\[\\],()]*),(?<RV1>[^\\[\\],()]*)(?<R1>[\\[\\]()]))|(?<VAL2>(?<L2>[\\[\\]()])(?<V2>[^\\[\\],()]*)(?<R2>[\\[\\]()]))|(?<VAL3>(?<V3>[^\\[\\], ()]+)))(\\s|,|\n)*)");
//        Matcher y = pattern.matcher(version);
//        StringBuilder sb = new StringBuilder();
//        while (y.find()){
//            if(y.group("VAL1")!=null) {
//                boolean inclusiveLowerBoundary = y.group("L1").equals("[");
//                boolean inclusiveUpperBoundary = y.group("R1").equals("]");
//                String min = y.group("LV1");
//                String max = y.group("RV1");
//
//                if(sb.length()>0){
//                    sb.append(",");
//                }
//                sb.append(inclusiveLowerBoundary?"[":"]");
//                sb.append(min);
//                sb.append(",");
//                sb.append(max);
//                sb.append(inclusiveUpperBoundary?"]":"[");
//
//            }else if(y.group("VAL2")!=null){
//                boolean inclusiveLowerBoundary = y.group("L2").equals("[");
//                boolean inclusiveUpperBoundary = y.group("R2").equals("]");
//                String val=y.group("V2");
//                //  [a]  or ]a[
//                if((inclusiveLowerBoundary && inclusiveUpperBoundary) || (!inclusiveLowerBoundary && !inclusiveUpperBoundary)) {
//                    sb.append(inclusiveLowerBoundary?"[":"]");
//                    sb.append(val);
//                    sb.append(",");
//                    sb.append(val);
//                    sb.append(inclusiveUpperBoundary?"]":"[");
//                    // ]a]    == ],a]
//                }else if(!inclusiveLowerBoundary){
//                    sb.append(inclusiveLowerBoundary?"[":"]");
//                    sb.append("");
//                    sb.append(",");
//                    sb.append(val);
//                    sb.append(inclusiveUpperBoundary?"]":"[");
//
//                    // [a[    == [a,[
//                }else if(!inclusiveLowerBoundary){
//                    d.add(new DefaultNutsVersionFilter.NutsVersionInterval(false, true, val,null));
//                }
//            }else {
//                if(sb.length()>0){
//                    sb.append(",");
//                }
//                sb.append(y.group("V3"));
//            }
//        }
//        return sb.toString();
//

    }

    public static MavenMetadataInfo parseMavenMetaData(InputStream stream) {
        MavenMetadataInfo info = new MavenMetadataInfo();
        StringBuilder ver = new StringBuilder();
        StringBuilder latest = new StringBuilder();
        StringBuilder release = new StringBuilder();
        List<String> versions = new ArrayList<>();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            XMLEventReader eventReader
                    = factory.createXMLEventReader(new InputStreamReader(stream));
            Stack<String> nodePath = new Stack<>();
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        nodePath.push(qName);
                        StringUtils.clear(ver);
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            ver.append(event.asCharacters().getData());
                        }
                        if (isStackPath(nodePath, "metadata", "versioning", "release")) {
                            release.append(event.asCharacters().getData());
                        }
                        if (isStackPath(nodePath, "metadata", "versioning", "latest")) {
                            latest.append(event.asCharacters().getData());
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            nodePath.pop();
                            if (ver.length() > 0) {
                                versions.add(ver.toString());
                            }
                        } else {
                            nodePath.pop();
                        }
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        info.latest = latest.toString();
        info.release = release.toString();
        for (String version : versions) {
            if (latest.length() > 0 && CoreVersionUtils.compareVersions(version, latest.toString()) > 0) {
                //do not add
            } else {
                info.versions.add(version);
            }
        }
        return info;
    }

    public static NutsDescriptor parsePomXml(InputStream stream, NutsWorkspace ws, NutsSession session, String urlDesc) throws IOException {
        NutsDescriptor nutsDescriptor = null;
        if (session == null) {
            session = ws.createSession();
        }
        try {
            try {
//            bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = MavenUtils.parsePomXml(stream,urlDesc);
                HashMap<String, String> properties = new HashMap<>();
                NutsSession transitiveSession = session.copy().setTransitive(true);
                NutsId parentId = null;
                for (NutsId nutsId : nutsDescriptor.getParents()) {
                    parentId = nutsId;
                }
                NutsDescriptor parentDescriptor = null;
                if (parentId != null) {
                    if (!CoreNutsUtils.isEffectiveId(parentId)) {
                        try {
                            parentDescriptor = ws.fetchDescriptor(parentId.toString(), true, transitiveSession);
                        } catch (NutsException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new NutsNotFoundException(nutsDescriptor.getId().toString(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, ex);
                        }
                        parentId = parentDescriptor.getId();
                    }
                }
                if (parentId != null) {
                    properties.put("parent.groupId", parentId.getGroup());
                    properties.put("parent.artifactId", parentId.getName());
                    properties.put("parent.version", parentId.getVersion().getValue());

                    properties.put("project.parent.groupId", parentId.getGroup());
                    properties.put("project.parent.artifactId", parentId.getName());
                    properties.put("project.parent.version", parentId.getVersion().getValue());
                    nutsDescriptor = nutsDescriptor.setProperties(properties, true).applyProperties(properties);
                }
                NutsId thisId = nutsDescriptor.getId();
                if (!CoreNutsUtils.isEffectiveId(thisId)) {
                    if (parentId != null) {
                        if (StringUtils.isEmpty(thisId.getGroup())) {
                            thisId = thisId.setGroup(parentId.getGroup());
                        }
                        if (StringUtils.isEmpty(thisId.getVersion().getValue())) {
                            thisId = thisId.setVersion(parentId.getVersion().getValue());
                        }
                    }
                    HashMap<NutsId, NutsDescriptor> cache = new HashMap<>();
                    Set<String> done = new HashSet<>();
                    Stack<NutsId> todo = new Stack<>();
                    todo.push(nutsDescriptor.getId());
                    cache.put(nutsDescriptor.getId(), nutsDescriptor);
                    while (todo.isEmpty()) {
                        NutsId pid = todo.pop();
                        NutsDescriptor d = cache.get(pid);
                        if (d == null) {
                            try {
                                d = ws.fetchDescriptor(pid.toString(), true, transitiveSession);
                            } catch (NutsException ex) {
                                throw ex;
                            } catch (Exception ex) {
                                throw new NutsNotFoundException(nutsDescriptor.getId().toString(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + pid, ex);
                            }
                        }
                        done.add(pid.getSimpleName());
                        if (CoreNutsUtils.containsVars(thisId)) {
                            thisId.apply(new MapStringMapper(d.getProperties()));
                        } else {
                            break;
                        }
                        for (NutsId nutsId : d.getParents()) {
                            if (!done.contains(nutsId.getSimpleName())) {
                                todo.push(nutsId);
                            }
                        }
                    }
                    if (CoreNutsUtils.containsVars(thisId)) {
                        throw new NutsNotFoundException(nutsDescriptor.getId().toString(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, null);
                    }
                    nutsDescriptor = nutsDescriptor.setId(thisId);
                }
                String nutsPackaging = nutsDescriptor.getProperties().get("nuts-packaging");
                if (!StringUtils.isEmpty(nutsPackaging)) {
                    nutsDescriptor = nutsDescriptor.setPackaging(nutsPackaging);
                }
                properties.put("pom.groupId", thisId.getGroup());
                properties.put("project.groupId", thisId.getGroup());
                properties.put("project.artifactId", thisId.getName());
                properties.put("project.version", thisId.getVersion().getValue());
                properties.put("version", thisId.getVersion().getValue());
                nutsDescriptor = nutsDescriptor.setProperties(properties, true).applyProperties(properties);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (Exception ex) {
            throw new NutsParseException("Error Parsing " + urlDesc, ex);
        }
        return nutsDescriptor;
    }

    public static class MavenMetadataInfo {

        String latest;
        String release;
        List<String> versions = new ArrayList<>();

        public List<String> getVersions() {
            return versions;
        }
    }

    private static Element toElement(Node n) {
        if (n instanceof Element) {
            return (Element) n;
        }
        return null;
    }

    private static Element toElement(Node n, String name) {
        if (n instanceof Element) {
            if (((Element) n).getTagName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }
}
