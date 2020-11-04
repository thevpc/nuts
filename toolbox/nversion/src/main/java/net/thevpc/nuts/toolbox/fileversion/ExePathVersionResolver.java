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
package net.thevpc.nuts.toolbox.fileversion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.common.strings.StringUtils;
import org.boris.pecoff4j.PE;
import org.boris.pecoff4j.ResourceDirectory;
import org.boris.pecoff4j.ResourceEntry;
import org.boris.pecoff4j.constant.ResourceType;
import org.boris.pecoff4j.io.PEParser;
import org.boris.pecoff4j.io.ResourceParser;
import org.boris.pecoff4j.resources.StringFileInfo;
import org.boris.pecoff4j.resources.StringTable;
import org.boris.pecoff4j.resources.VersionInfo;
import org.boris.pecoff4j.util.ResourceHelper;

/**
 *
 * @author vpc
 */
public class ExePathVersionResolver implements PathVersionResolver{
    public Set<VersionDescriptor>  resolve(String filePath, NutsApplicationContext context){
        try {
            if(!filePath.endsWith(".exe") && !filePath.endsWith(".dll")){
                return null;
            }
            PE pe = PEParser.parse(filePath);
            ResourceDirectory rd = pe.getImageData().getResourceTable();
            Set<VersionDescriptor> d = new HashSet<>();
            ResourceEntry[] entries = ResourceHelper.findResources(rd, ResourceType.VERSION_INFO);
            for (ResourceEntry entry : entries) {
                byte[] data = entry.getData();
                VersionInfo version = ResourceParser.readVersionInfo(data);
                
                StringFileInfo strings = version.getStringFileInfo();
                StringTable table = strings.getTable(0);
                Properties p = new Properties();
                for (int j = 0; j < table.getCount(); j++) {
                    String key = table.getString(j).getKey();
                    String value = table.getString(j).getValue();
                    p.setProperty(key, value);
                }
                String artifactId = p.getProperty("AssemblyId");
                if (artifactId == null) {
                    artifactId = p.getProperty("AssemblyName");
                    if (artifactId == null) {
                        artifactId = p.getProperty("ProductName");
                        if (artifactId == null) {
                            artifactId = p.getProperty("InternalName");
                            if (artifactId == null) {
                                artifactId = p.getProperty("OriginalFileName");
                            }
                        }
                    }
                }
                if (artifactId != null) {
                    if (artifactId.toLowerCase().endsWith(".dll")) {
                        artifactId = artifactId.substring(0, artifactId.length() - ".dll".length());
                    } else if (artifactId.toLowerCase().endsWith(".exe")) {
                        artifactId = artifactId.substring(0, artifactId.length() - ".exe".length());
                    }
                    String artifactVersion = p.getProperty("AssemblyVersion");
                    if (artifactVersion == null) {
                        artifactVersion = p.getProperty("Assembly Version");
                        if (artifactVersion == null) {
                            artifactVersion = p.getProperty("ProductVersion");
                            if (artifactVersion == null) {
                                artifactVersion = p.getProperty("FileVersion");
                            }
                        }
                    }
                    p.setProperty("nuts.version-provider", "win-pe");
                    if (!StringUtils.isBlank(artifactId) && !StringUtils.isBlank(artifactVersion)) {
                        d.add(new VersionDescriptor(
                                context.getWorkspace().id().builder().setArtifactId(artifactId).setVersion(artifactVersion).build(),
                                p
                        ));
                    }
                }
            }
            return d;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
