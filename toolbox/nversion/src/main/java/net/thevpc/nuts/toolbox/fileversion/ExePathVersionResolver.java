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
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.thevpc.nuts.NutsApplicationContext;
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
 * @author thevpc
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
                    if (!_StringUtils.isBlank(artifactId) && !_StringUtils.isBlank(artifactVersion)) {
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
