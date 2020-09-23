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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.fileversion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.common.mvn.Pom;
import net.vpc.common.mvn.PomXmlParser;

/**
 *
 * @author vpc
 */
public class MavenFolderPathVersionResolver implements PathVersionResolver {

    public Set<VersionDescriptor> resolve(String filePath, NutsApplicationContext context) {
        if (Files.isRegularFile(Paths.get(filePath).resolve("pom.xml"))) {
            Properties properties = new Properties();
            Set<VersionDescriptor> all = new HashSet<>();
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath).resolve("pom.xml"))) {
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
            return all;
        } else {
            return null;
        }
    }
}
