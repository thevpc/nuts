/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.toolbox.nversion;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NUncompressVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;

/**
 * @author thevpc
 */
public class JarPathVersionResolver implements PathVersionResolver {
    public Set<VersionDescriptor> resolve(String filePath, NApplicationContext context) {
        if (filePath.endsWith(".jar") || filePath.endsWith(".war") || filePath.endsWith(".ear")) {

        } else {
            return null;
        }
        Set<VersionDescriptor> all = new HashSet<>();
        NSession session = context.getSession();
        try (InputStream is = (NPath.of(filePath, session).toAbsolute()).getInputStream()) {
            NUncompress.of(session)
                    .from(is)
                    .visit(new NUncompressVisitor() {
                        @Override
                        public boolean visitFolder(String path) {
                            return true;
                        }

                        @Override
                        public boolean visitFile(String path, InputStream inputStream) {
                            if ("META-INF/MANIFEST.MF".equals(path)) {
                                Manifest manifest = null;
                                try {
                                    manifest = new Manifest(inputStream);
                                } catch (IOException e) {
                                    throw new NIOException(session, e);
                                }
                                Attributes attrs = manifest.getMainAttributes();
                                String Bundle_SymbolicName = null;
                                String Bundle_Name = null;
                                String Bundle_Version = null;
                                Properties properties = new Properties();
                                for (Object o : attrs.keySet()) {
                                    Attributes.Name attrName = (Attributes.Name) o;
                                    String key = attrName.toString();
                                    String value = attrs.getValue(attrName);
                                    properties.setProperty(key, value);
                                    if ("Bundle-Version".equals(key)) {
                                        Bundle_Version = value;
                                    }
                                    if ("Bundle-SymbolicName".equals(key)) {
                                        Bundle_SymbolicName = value;
                                    }
                                    if ("Bundle-Name".equals(key)) {
                                        Bundle_Name = value;
                                    }
                                }
                                properties.setProperty("nuts.version-provider", "OSGI");
                                //OSGI
                                if (!NBlankable.isBlank(Bundle_SymbolicName)
                                        && !NBlankable.isBlank(Bundle_Name)
                                        && !NBlankable.isBlank(Bundle_Version)) {
                                    all.add(new VersionDescriptor(
                                            NIdBuilder.of().setGroupId(Bundle_SymbolicName).setArtifactId(Bundle_Name).setVersion(Bundle_Version).build(),
                                            properties
                                    ));
                                }

                            } else if (("META-INF/" + NConstants.Files.DESCRIPTOR_FILE_NAME).equals(path)) {
                                try {
                                    NDescriptor d = NDescriptorParser.of(session).parse(inputStream).get(session);
                                    inputStream.close();
                                    Properties properties = new Properties();
                                    properties.setProperty("parents", d.getParents().stream().map(Object::toString).collect(Collectors.joining(",")));
                                    properties.setProperty("name", d.getId().getArtifactId());
                                    properties.setProperty("face", d.getId().getFace());
                                    properties.setProperty("group", d.getId().getGroupId());
                                    properties.setProperty("version", d.getId().getVersion().toString());
//                            if (d.getExt() != null) {
//                                properties.setProperty("ext", d.getExt());
//                            }
                                    if (d.getPackaging() != null) {
                                        properties.setProperty("packaging", d.getPackaging());
                                    }
                                    if (d.getDescription() != null) {
                                        properties.setProperty("description", d.getDescription());
                                    }
                                    properties.setProperty("locations", NElements.of(session).json()
                                            .setValue(d.getLocations()).setNtf(false).format().filteredText()
                                    );
                                    properties.setProperty(NConstants.IdProperties.ARCH, String.join(";", d.getCondition().getArch()));
                                    properties.setProperty(NConstants.IdProperties.OS, String.join(";", d.getCondition().getOs()));
                                    properties.setProperty(NConstants.IdProperties.OS_DIST, String.join(";", d.getCondition().getOsDist()));
                                    properties.setProperty(NConstants.IdProperties.PLATFORM, String.join(";", d.getCondition().getPlatform()));
                                    properties.setProperty(NConstants.IdProperties.DESKTOP, String.join(";", d.getCondition().getDesktopEnvironment()));
                                    properties.setProperty(NConstants.IdProperties.PROFILE, String.join(";", d.getCondition().getProfile()));
                                    properties.setProperty("nuts.version-provider", NConstants.Files.DESCRIPTOR_FILE_NAME);
                                    if (d.getProperties() != null) {
                                        for (NDescriptorProperty e : d.getProperties()) {
                                            properties.put("property." + e.getName(), e.getValue());
                                        }
                                    }
                                    all.add(new VersionDescriptor(d.getId(), properties));
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }

                            } else if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {

                                Properties properties = new Properties();
                                try {
                                    NDescriptor d = NDescriptorParser.of(session)
                                            .setDescriptorStyle(NDescriptorStyle.MAVEN)
                                            .parse(inputStream).get(session);
                                    properties.put("groupId", d.getId().getGroupId());
                                    properties.put("artifactId", d.getId().getArtifactId());
                                    properties.put("version", d.getId().getVersion().toString());
                                    properties.put("name", d.getName());
                                    properties.setProperty("nuts.version-provider", "maven");
                                    if (d.getProperties() != null) {
                                        for (NDescriptorProperty e : d.getProperties()) {
                                            properties.put("property." + e.getName(), e.getValue());
                                        }
                                    }
                                    all.add(new VersionDescriptor(
                                            NIdBuilder.of().setGroupId(d.getId().getGroupId())
                                                    .setRepository(d.getId().getArtifactId())
                                                    .setVersion(d.getId().getVersion())
                                                    .build(),
                                            properties));
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }

                            } else if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.properties")) {
                                try {
                                    Properties prop = new Properties();
                                    try {
                                        prop.load(inputStream);
                                    } catch (IOException e) {
                                        //
                                    }
                                    String version = prop.getProperty("version");
                                    String groupId = prop.getProperty("groupId");
                                    String artifactId = prop.getProperty("artifactId");
                                    prop.setProperty("nuts.version-provider", "maven");
                                    if (version != null && version.trim().length() != 0) {
                                        all.add(new VersionDescriptor(
                                                NIdBuilder.of(groupId, artifactId).setVersion(version)
                                                        .build(),
                                                prop
                                        ));
                                    }
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }

                            }
                            return true;
                        }
                    });
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return all;
    }
}
