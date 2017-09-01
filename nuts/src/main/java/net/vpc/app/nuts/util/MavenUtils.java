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
package net.vpc.app.nuts.util;

import net.vpc.app.nuts.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {

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

//    public static void main(String[] args) {
//        NutsDescriptor d= null;
//        try {
//            d = parsePomXml(new FileInputStream("/home/vpc/.m2/repository/org/jline/jline/3.1.3/jline-3.1.3.pom"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        System.out.println(d);
//    }

    public static NutsDescriptor parsePomXml(InputStream stream) {
        StringBuilder p_groupId = new StringBuilder();
        StringBuilder p_artifactId = new StringBuilder();
        StringBuilder p_version = new StringBuilder();

        StringBuilder d_groupId = new StringBuilder();
        StringBuilder d_artifactId = new StringBuilder();
        StringBuilder d_version = new StringBuilder();
        StringBuilder d_scope = new StringBuilder();
        StringBuilder d_optional = new StringBuilder();

        StringBuilder groupId = new StringBuilder();
        StringBuilder artifactId = new StringBuilder();
        StringBuilder version = new StringBuilder();
        StringBuilder packaging = new StringBuilder();
        StringBuilder name = new StringBuilder();
        StringBuilder description = new StringBuilder();
        StringBuilder propertyValue = new StringBuilder();
        Map<String, String> props = new HashMap<>();
        List<NutsDependency> deps = new ArrayList<>();
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
                        if (isStackPath(nodePath, "project", "dependencies", "dependency")) {
                            deps.add(
                                    new NutsDependency(
                                            null,
                                            d_groupId.toString().trim(),
                                            d_artifactId.toString().trim(),
                                            mavenVersionToNutsVersion(d_version.toString().trim()),
                                            d_scope.toString().trim(),
                                            d_optional.toString().trim()
                                    )
                            );
                        }
                        nodePath.pop();
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        boolean executable = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
        return new DefaultNutsDescriptor(
                new NutsId(
                        null, groupId.toString().trim(), artifactId.toString().trim(),
                        version.toString().trim(),
                        ""
                ), null,
                p_groupId.length() == 0 ? new NutsId[0] : new NutsId[]{
                        new NutsId(
                                null, p_groupId.toString().trim(), p_artifactId.toString().trim(),
                                p_version.toString().trim(),
                                ""
                        )
                },
                packaging.toString(),
                executable,
                "jar",
                null, null, name.toString(), description.toString(),
                new String[]{},
                new String[]{},
                new String[]{},
                //TODO should i check what version of java ?
                new String[]{"java"},
                deps.toArray(new NutsDependency[deps.size()]), props
        );
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
                        }else {
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
        for (Iterator<String> iterator = versions.iterator(); iterator.hasNext(); ) {
            String version = iterator.next();
            if (latest.length() > 0 && VersionUtils.compareVersions(version, latest.toString()) > 0) {
                //do not add
            } else {
                info.versions.add(version);
            }
        }
        return info;
    }

    public static class MavenMetadataInfo {

        String latest;
        String release;
        List<String> versions = new ArrayList<>();

        public List<String> getVersions() {
            return versions;
        }
    }

    public static NutsDescriptor parsePomXml(InputStream stream, NutsWorkspace ws, NutsSession session) throws IOException {
        NutsDescriptor nutsDescriptor = null;
        try {
//            bytes = IOUtils.readStreamAsBytes(stream, true);
            nutsDescriptor = MavenUtils.parsePomXml(stream);
            HashMap<String, String> properties = new HashMap<>();
            NutsSession transitiveSession = session.copy().setTransitive(true);
            NutsId parentId = null;
            for (NutsId nutsId : nutsDescriptor.getParents()) {
                parentId = nutsId;
            }
            NutsDescriptor parentDescriptor = null;
            if (parentId != null) {
                if (!NutsUtils.isEffectiveId(parentId)) {
                    try {
                        parentDescriptor = ws.fetchDescriptor(parentId.toString(), true, transitiveSession);
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
                nutsDescriptor = nutsDescriptor.setProperties(properties, true).applyProperties(properties);
            }
            NutsId thisId = nutsDescriptor.getId();
            if (!NutsUtils.isEffectiveId(thisId)) {
                if (parentId != null) {
                    if (StringUtils.isEmpty(thisId.getGroup())) {
                        thisId = thisId.setGroup(parentId.getGroup());
                    }
                    if (StringUtils.isEmpty(thisId.getVersion().getValue())) {
                        thisId = thisId.setVersion(parentId.getVersion().getValue());
                    }
                }
                HashMap<NutsId,NutsDescriptor> cache=new HashMap<>();
                Set<String> done=new HashSet<>();
                Stack<NutsId> todo=new Stack<>();
                todo.push(nutsDescriptor.getId());
                cache.put(nutsDescriptor.getId(),nutsDescriptor);
                while(todo.isEmpty()) {
                    NutsId pid=todo.pop();
                    NutsDescriptor d=cache.get(pid);
                    if(d==null){
                        try {
                            d = ws.fetchDescriptor(pid.toString(), true, transitiveSession);
                        } catch (Exception ex) {
                            throw new NutsNotFoundException(nutsDescriptor.getId().toString(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + pid, ex);
                        }
                    }
                    done.add(pid.getFullName());
                    if (NutsUtils.containsVars(thisId)) {
                        thisId.apply(new MapStringMapper(d.getProperties()));
                    }else{
                        break;
                    }
                    for (NutsId nutsId : d.getParents()) {
                        if(!done.contains(nutsId.getFullName())){
                            todo.push(nutsId);
                        }
                    }
                }
                if (NutsUtils.containsVars(thisId)) {
                    throw new NutsNotFoundException(nutsDescriptor.getId().toString(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, null);
                }
                nutsDescriptor = nutsDescriptor.setId(thisId);
            }
            String nutsPackaging = nutsDescriptor.getProperties().get("nuts-packaging");
            if (!StringUtils.isEmpty(nutsPackaging)) {
                nutsDescriptor = nutsDescriptor.setPackaging(nutsPackaging);
            }
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
        return nutsDescriptor;
    }
}
