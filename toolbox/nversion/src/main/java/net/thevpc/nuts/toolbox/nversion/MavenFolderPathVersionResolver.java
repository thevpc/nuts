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
package net.thevpc.nuts.toolbox.nversion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.thevpc.nuts.*;

/**
 *
 * @author thevpc
 */
public class MavenFolderPathVersionResolver implements PathVersionResolver {

    public Set<VersionDescriptor> resolve(String filePath, NutsApplicationContext context) {
        if (Files.isRegularFile(Paths.get(filePath).resolve("pom.xml"))) {
            Properties properties = new Properties();
            Set<VersionDescriptor> all = new HashSet<>();
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath).resolve("pom.xml"))) {
                NutsSession session = context.getSession();
                NutsDescriptor d = NutsDescriptorParser.of(session)
                        .setDescriptorStyle(NutsDescriptorStyle.MAVEN)
                        .parse(inputStream);

                properties.put("groupId", d.getId().getGroupId());
                properties.put("artifactId", d.getId().getArtifactId());
                properties.put("version", d.getId().getVersion());
                properties.put("name", d.getName());
                properties.setProperty("nuts.version-provider", "maven");
                if (d.getProperties() != null) {
                    for (NutsDescriptorProperty e : d.getProperties()) {
                        properties.put("property." + e.getName(), e.getValue());
                    }
                }
                all.add(new VersionDescriptor(
                        NutsIdBuilder.of(session).setGroupId(d.getId().getGroupId())
                                .setRepository(d.getId().getArtifactId())
                                .setVersion(d.getId().getVersion())
                                .build(),
                        properties));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return all;
        } else {
            return null;
        }
    }
}
