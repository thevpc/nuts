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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.FilePath;
import net.vpc.common.commandline.format.PropertiesFormatter;
import net.vpc.common.io.InputStreamVisitor;
import net.vpc.common.io.PathFilter;
import net.vpc.common.io.ZipUtils;
import net.vpc.common.mvn.Pom;
import net.vpc.common.mvn.PomXmlParser;
import net.vpc.common.strings.StringUtils;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by vpc on 1/7/17.
 */
public class FileVersionCommand extends AbstractNutsCommand {

    public FileVersionCommand() {
        super("file-version", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        PrintStream out = context.getFormattedOut();
        PrintStream err = context.getFormattedErr();
        NutsWorkspace ws = context.getWorkspace();
        Set<String> unsupportedFileTypes = new HashSet<>();
        Map<String, Set<VersionDescriptor>> results = new HashMap<>();
        boolean maven = false;
        boolean winPE = false;
        boolean all = false;
        boolean longFormat = false;
        boolean nameFormat = false;
        boolean idFormat = false;
        boolean sort = false;
        boolean table = false;
        boolean error = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--maven")) {
                maven = true;
            } else if (args[i].equals("--win-pe")) {
                winPE = true;
            } else if (args[i].equals("--exe")) {
                winPE = true;
            } else if (args[i].equals("--dll")) {
                winPE = true;
            } else if (args[i].equals("--long")) {
                longFormat = true;
            } else if (args[i].equals("--name")) {
                nameFormat = true;
            } else if (args[i].equals("--sort")) {
                sort = true;
            } else if (args[i].equals("--id")) {
                idFormat = true;
            } else if (args[i].equals("--all")) {
                all = true;
            } else if (args[i].equals("--table")) {
                table = true;
            } else if (args[i].equals("--error")) {
                error = true;
            } else {
                if (maven || args[i].endsWith(".jar") || args[i].endsWith(".war") || args[i].endsWith(".ear")) {
                    Set<VersionDescriptor> value = detectJarWarEarVersions(context.getAbsolutePath(args[i]), context, ws);
                    if (!value.isEmpty()) {
                        results.put(args[i], value);
                    }
                } else if (winPE || args[i].endsWith(".exe") || args[i].endsWith(".dll")) {
                    Set<VersionDescriptor> value = detectExeVersions(context.getAbsolutePath(args[i]), context, ws);
                    if (!value.isEmpty()) {
                        results.put(args[i], value);
                    }
                } else {
                    unsupportedFileTypes.add(args[i]);
                }
            }
        }
        if (table && all) {
            throw new IllegalArgumentException("Options conflict --table --all");
        }
        if (table && longFormat) {
            throw new IllegalArgumentException("Options conflict --table --long");
        }

        if (table) {
            PropertiesFormatter tt = new PropertiesFormatter().setSort(sort).setTable(true);
            Properties pp=new Properties();
            for (Map.Entry<String, Set<VersionDescriptor>> entry : results.entrySet()) {
                VersionDescriptor o = entry.getValue().toArray(new VersionDescriptor[entry.getValue().size()])[0];
                if (nameFormat) {
                    pp.setProperty(entry.getKey(), o.getId().getFullName());
                } else if (idFormat) {
                    pp.setProperty(entry.getKey(), o.getId().toString());
                } else if (longFormat) {
                    //should never happen
                } else {
                    pp.setProperty(entry.getKey(), o.getId().toString());
                }
            }
            if(error) {
                for (String t : unsupportedFileTypes) {
                    File f = new File(context.getAbsolutePath(t));
                    if (f.isFile()) {
                        pp.setProperty(t, "<<ERROR>> Unsupported File type");
                    } else if (f.isDirectory()) {
                        pp.setProperty(t, "<<ERROR>> Ignored Folder");
                    } else {
                        pp.setProperty(t, "<<ERROR>> File not found");
                    }
                }
            }
            tt.format(pp,out);
        } else {
            Set<String> keys = sort ? new TreeSet<>(results.keySet()) : new LinkedHashSet<>(results.keySet());
            for (String k : keys) {
                if (results.size() > 1) {
                    if (longFormat || all) {
                        out.printf("==%s==:\n", k);
                    } else {
                        out.printf("==%s==: ", k);
                    }
                }
                Set<VersionDescriptor> v = results.get(k);
                for (VersionDescriptor descriptor : v) {
                    if (nameFormat) {
                        out.printf("[[%s]]\n", descriptor.getId().getFullName());
                    } else if (idFormat) {
                        out.printf("[[%s]]\n", descriptor.getId());
                    } else if (longFormat) {
                        out.printf("[[%s]]\n", descriptor.getId());
                        PropertiesFormatter f = new PropertiesFormatter()
                                .setTable(true)
                                .setSort(true);
                        f.format(descriptor.getProperties(), out);
                    } else {
                        out.printf("[[%s]]\n", descriptor.getId().getVersion());
                    }
                    if (!all) {
                        break;
                    }
                }
            }
            if(error) {
                if (!unsupportedFileTypes.isEmpty()) {
                    for (String t : unsupportedFileTypes) {
                        File f = new File(context.getAbsolutePath(t));
                        if (f.isFile()) {
                            err.printf("%s : Unsupported File type\n", t);
                        } else if (f.isDirectory()) {
                            err.printf("%s : Ignored Folder\n", t);
                        } else {
                            err.printf("%s : File not found\n", t);
                        }
                    }
                }
            }
        }
        if (!unsupportedFileTypes.isEmpty()) {
            throw new IllegalArgumentException("Unsupported File types " + unsupportedFileTypes);
        }
        return 0;
    }

    public static class VersionDescriptor {
        private NutsId id;
        private Properties properties;

        public VersionDescriptor(NutsId id, Properties properties) {
            this.id = id;
            this.properties = properties;
        }

        public NutsId getId() {
            return id;
        }

        public Properties getProperties() {
            return properties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionDescriptor that = (VersionDescriptor) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id, properties);
        }
    }

    private Set<VersionDescriptor> detectExeVersions(String filePath, NutsCommandContext context, NutsWorkspace ws) throws IOException {
        PE pe = PEParser.parse(filePath);
        ResourceDirectory rd = pe.getImageData().getResourceTable();
        Set<VersionDescriptor> d = new HashSet<>();
        ResourceEntry[] entries = ResourceHelper.findResources(rd, ResourceType.VERSION_INFO);
        for (int i = 0; i < entries.length; i++) {
            byte[] data = entries[i].getData();
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
                if (!StringUtils.isEmpty(artifactId) && !StringUtils.isEmpty(artifactVersion)) {
                    d.add(new VersionDescriptor(
                            ws.createIdBuilder().setName(artifactId).setVersion(artifactVersion).build(),
                            p
                    ));
                }
            }
        }
        return d;
    }

    private Set<VersionDescriptor> detectJarWarEarVersions(String filePath, NutsCommandContext context, NutsWorkspace ws) throws IOException {
        Set<VersionDescriptor> all = new HashSet<>();
        try (InputStream is = FilePath.of(filePath).getInputStream()) {
            ZipUtils.visitZipStream(is, new PathFilter() {
                @Override
                public boolean accept(String path) {
                    if ("META-INF/MANIFEST.MF".equals(path)) {
                        return true;
                    }
                    if ("META-INF/nuts.json".equals(path)) {
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
                        if (
                                !StringUtils.isEmpty(Bundle_SymbolicName)
                                        && !StringUtils.isEmpty(Bundle_Name)
                                        && !StringUtils.isEmpty(Bundle_Version)
                                ) {
                            all.add(new VersionDescriptor(
                                    ws.createIdBuilder().setGroup(Bundle_SymbolicName).setName(Bundle_Name).setVersion(Bundle_Version).build(),
                                    properties
                            ));
                        }

                    } else if ("META-INF/nuts.json".equals(path)) {
                        try {
                            NutsDescriptor d = ws.parseDescriptor(inputStream);
                            inputStream.close();
                            Properties properties = new Properties();
                            properties.setProperty("parents", StringUtils.join(",", d.getParents(), Object::toString));
                            properties.setProperty("name", d.getId().getName());
                            properties.setProperty("face", d.getId().getFace());
                            properties.setProperty("group", d.getId().getGroup());
                            properties.setProperty("version", d.getId().getVersion().toString());
                            properties.setProperty("ext", d.getExt());
                            properties.setProperty("packaging", d.getPackaging());
                            properties.setProperty("description", d.getDescription());
                            properties.setProperty("locations", StringUtils.join(";", d.getLocations()));
                            properties.setProperty("platform", StringUtils.join(";", d.getPlatform()));
                            properties.setProperty("os", StringUtils.join(";", d.getOs()));
                            properties.setProperty("arch", StringUtils.join(";", d.getArch()));
                            properties.setProperty("osdist", StringUtils.join(";", d.getOsdist()));
                            properties.setProperty("nuts.version-provider", "nuts.json");
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
                                    ws.createIdBuilder().setGroup(d.getPomId().getGroupId())
                                            .setNamespace(d.getPomId().getArtifactId())
                                            .setVersion(d.getPomId().getVersion())
                                            .build()
                                    , properties));
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
                                        ws.createIdBuilder()
                                        .setGroup(groupId).setName(artifactId).setVersion(version)
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
        }
        return all;
    }
}
