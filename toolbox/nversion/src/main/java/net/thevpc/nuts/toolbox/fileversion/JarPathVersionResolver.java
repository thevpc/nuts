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
package net.thevpc.nuts.toolbox.fileversion;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.common.io.InputStreamVisitor;
import net.thevpc.common.io.PathFilter;
import net.thevpc.common.io.ZipUtils;
import net.thevpc.common.mvn.Pom;
import net.thevpc.common.mvn.PomXmlParser;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.common.xfile.XFile;
import net.thevpc.nuts.NutsContentType;

/**
 *
 * @author thevpc
 */
public class JarPathVersionResolver implements PathVersionResolver{
    public Set<VersionDescriptor> resolve(String filePath, NutsApplicationContext context){
        if (filePath.endsWith(".jar") || filePath.endsWith(".war") || filePath.endsWith(".ear")) {
            
        }else{
            return null;
        }
        Set<VersionDescriptor> all = new HashSet<>();
        try (InputStream is = XFile.of(context.getWorkspace().io().expandPath(filePath)).getInputStream()) {
            ZipUtils.visitZipStream(is, new PathFilter() {
                @Override
                public boolean accept(String path) {
                    if ("META-INF/MANIFEST.MF".equals(path)) {
                        return true;
                    }
                    if (("META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME).equals(path)) {
                        return true;
                    }
                    if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {
                        return true;
                    }
                    if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.properties")) {
                        return true;
                    }
                    return false;
                }
            }, new InputStreamVisitor() {
                @Override
                public boolean visit(String path, InputStream inputStream) throws IOException {
                    if ("META-INF/MANIFEST.MF".equals(path)) {
                        Manifest manifest = new Manifest(inputStream);
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
                        if (!StringUtils.isBlank(Bundle_SymbolicName)
                                && !StringUtils.isBlank(Bundle_Name)
                                && !StringUtils.isBlank(Bundle_Version)) {
                            all.add(new VersionDescriptor(
                                    context.getWorkspace().id().builder().setGroupId(Bundle_SymbolicName).setArtifactId(Bundle_Name).setVersion(Bundle_Version).build(),
                                    properties
                            ));
                        }

                    } else if (("META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME).equals(path)) {
                        try {
                            NutsDescriptor d = context.getWorkspace().descriptor().parser().parse(inputStream);
                            inputStream.close();
                            Properties properties = new Properties();
                            properties.setProperty("parents", StringUtils.join(",", d.getParents(), Object::toString));
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
                            properties.setProperty("locations", context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(d.getLocations()).format());
                            properties.setProperty("platform", StringUtils.join(";", d.getPlatform()));
                            properties.setProperty("os", StringUtils.join(";", d.getOs()));
                            properties.setProperty("arch", StringUtils.join(";", d.getArch()));
                            properties.setProperty("osdist", StringUtils.join(";", d.getOsdist()));
                            properties.setProperty("nuts.version-provider", NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                            if (d.getProperties() != null) {
                                for (Map.Entry<String, String> e : d.getProperties().entrySet()) {
                                    properties.put("property." + e.getKey(), e.getValue());
                                }
                            }
                            all.add(new VersionDescriptor(d.getId(), properties));
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }

                    } else if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {

                        Properties properties = new Properties();
                        try {
                            Pom d = new PomXmlParser().parse(inputStream);
                            properties.put("groupId", d.getGroupId());
                            properties.put("artifactId", d.getArtifactId());
                            properties.put("version", d.getVersion());
                            properties.put("name", d.getName());
                            properties.setProperty("nuts.version-provider", "maven");
                            if (d.getProperties() != null) {
                                for (Map.Entry<String, String> e : d.getProperties().entrySet()) {
                                    properties.put("property." + e.getKey(), e.getValue());
                                }
                            }
                            all.add(new VersionDescriptor(
                                    context.getWorkspace().id().builder().setGroupId(d.getPomId().getGroupId())
                                            .setNamespace(d.getPomId().getArtifactId())
                                            .setVersion(d.getPomId().getVersion())
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
                                        context.getWorkspace().id().builder()
                                                .setGroupId(groupId).setArtifactId(artifactId).setVersion(version)
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
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }
        return all;
    }
}
