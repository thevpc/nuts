package net.vpc.app.nuts.toolbox.fileversion;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.format.PropertiesFormatter;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.InputStreamVisitor;
import net.vpc.common.io.PathFilter;
import net.vpc.common.io.ZipUtils;
import net.vpc.common.mvn.Pom;
import net.vpc.common.mvn.PomXmlParser;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.xfile.XFile;
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

import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class FileVersionMain extends NutsApplication {
    public static void main(String[] args) {
        new FileVersionMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
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
        CommandLine commandLine = new CommandLine(context);
        Argument a;
        int processed = 0;
        while (commandLine.hasNext()) {
            if (context.configure(commandLine)) {
                //
            } else if ((a = commandLine.readBooleanOption("--maven")) != null) {
                maven = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--win-pe")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--exe")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--dll")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--long")) != null) {
                longFormat = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--name")) != null) {
                nameFormat = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--sort")) != null) {
                sort = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--id")) != null) {
                idFormat = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--all")) != null) {
                all = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--table")) != null) {
                table = a.getBooleanValue();
            } else if ((a = commandLine.readBooleanOption("--error")) != null) {
                error = a.getBooleanValue();
            } else {
                a = commandLine.read();
                String arg = a.getStringExpression();
                if (maven || arg.endsWith(".jar") || arg.endsWith(".war") || arg.endsWith(".ear")) {
                    if (commandLine.isExecMode()) {
                        Set<VersionDescriptor> value = null;
                        try {
                            processed++;
                            value = detectJarWarEarVersions(context.getWorkspace().io().expandPath(arg), context, ws);
                        } catch (IOException e) {
                            throw new NutsExecutionException(e, 2);
                        }
                        if (!value.isEmpty()) {
                            results.put(arg, value);
                        }
                    }
                } else if (winPE || arg.endsWith(".exe") || arg.endsWith(".dll")) {
                    if (commandLine.isExecMode()) {
                        Set<VersionDescriptor> value = null;
                        try {
                            processed++;
                            value = detectExeVersions(context.getWorkspace().io().expandPath(arg), context, ws);
                        } catch (IOException e) {
                            throw new NutsExecutionException(e, 2);
                        }
                        if (!value.isEmpty()) {
                            results.put(arg, value);
                        }
                    }
                } else {
                    unsupportedFileTypes.add(arg);
                }
            }
        }
        if (commandLine.isExecMode()) {
            if (processed == 0) {
                throw new NutsExecutionException("file-version: Missing file", 2);
            }
            if (table && all) {
                throw new NutsExecutionException("file-version: Options conflict --table --all", 1);
            }
            if (table && longFormat) {
                throw new NutsExecutionException("file-version: Options conflict --table --long", 1);
            }

            PrintStream out = context.out();
            PrintStream err = context.out();

            if (table) {
                PropertiesFormatter tt = new PropertiesFormatter().setSort(sort).setTable(true);
                Properties pp = new Properties();
                for (Map.Entry<String, Set<VersionDescriptor>> entry : results.entrySet()) {
                    VersionDescriptor o = entry.getValue().toArray(new VersionDescriptor[0])[0];
                    if (nameFormat) {
                        pp.setProperty(entry.getKey(), o.getId().getSimpleName());
                    } else if (idFormat) {
                        pp.setProperty(entry.getKey(), o.getId().toString());
                    } else if (longFormat) {
                        //should never happen
                    } else {
                        pp.setProperty(entry.getKey(), o.getId().toString());
                    }
                }
                if (error) {
                    for (String t : unsupportedFileTypes) {
                        File f = new File(context.getWorkspace().io().expandPath(t));
                        if (f.isFile()) {
                            pp.setProperty(t, "<<ERROR>> Unsupported File type");
                        } else if (f.isDirectory()) {
                            pp.setProperty(t, "<<ERROR>> Ignored Folder");
                        } else {
                            pp.setProperty(t, "<<ERROR>> File not found");
                        }
                    }
                }
                tt.format(pp, out);
            } else {
                Set<String> keys = sort ? new TreeSet<>(results.keySet()) : new LinkedHashSet<>(results.keySet());
                for (String k : keys) {
                    if (results.size() > 1) {
                        if (longFormat || all) {
                            out.printf("==%s==:%n", k);
                        } else {
                            out.printf("==%s==: ", k);
                        }
                    }
                    Set<VersionDescriptor> v = results.get(k);
                    for (VersionDescriptor descriptor : v) {
                        if (nameFormat) {
                            out.printf("[[%s]]%n", descriptor.getId().getSimpleName());
                        } else if (idFormat) {
                            out.printf("[[%s]]%n", descriptor.getId());
                        } else if (longFormat) {
                            out.printf("[[%s]]%n", descriptor.getId());
                            PropertiesFormatter f = new PropertiesFormatter()
                                    .setTable(true)
                                    .setSort(true);
                            f.format(descriptor.getProperties(), out);
                        } else {
                            out.printf("[[%s]]%n", descriptor.getId().getVersion());
                        }
                        if (!all) {
                            break;
                        }
                    }
                }
                if (error) {
                    if (!unsupportedFileTypes.isEmpty()) {
                        for (String t : unsupportedFileTypes) {
                            File f = new File(context.getWorkspace().io().expandPath(t));
                            if (f.isFile()) {
                                err.printf("%s : Unsupported File type%n", t);
                            } else if (f.isDirectory()) {
                                err.printf("%s : Ignored Folder%n", t);
                            } else {
                                err.printf("%s : File not found%n", t);
                            }
                        }
                    }
                }
            }
            if (!unsupportedFileTypes.isEmpty()) {
                throw new NutsExecutionException("file-version: Unsupported File types " + unsupportedFileTypes, 3);
            }
        }
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

    private Set<VersionDescriptor> detectExeVersions(String filePath, NutsApplicationContext context, NutsWorkspace ws) throws IOException {
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

    private Set<VersionDescriptor> detectJarWarEarVersions(String filePath, NutsApplicationContext context, NutsWorkspace ws) throws IOException {
        Set<VersionDescriptor> all = new HashSet<>();
        try (InputStream is = XFile.of(context.getWorkspace().io().expandPath("filePath")).getInputStream()) {
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
                            NutsDescriptor d = ws.parser().parseDescriptor(inputStream);
                            inputStream.close();
                            Properties properties = new Properties();
                            properties.setProperty("parents", StringUtils.join(",", d.getParents(), Object::toString));
                            properties.setProperty("name", d.getId().getName());
                            properties.setProperty("face", d.getId().getFace());
                            properties.setProperty("group", d.getId().getGroup());
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

    public static XFile xfileOf(String expression, String cwd) {
        if (expression.startsWith("file:") || expression.contains("://")) {
            return XFile.of(expression);
        }
        return XFile.of(FileUtils.getAbsoluteFile2(expression, cwd));
    }
}
