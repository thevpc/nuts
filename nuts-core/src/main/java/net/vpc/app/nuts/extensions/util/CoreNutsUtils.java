/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.DefaultHttpTransportComponent;
import net.vpc.app.nuts.extensions.core.DefaultNutsDescriptor;
import net.vpc.app.nuts.extensions.core.NutsDependencyImpl;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;
import net.vpc.app.nuts.StringMapper;

import javax.json.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {
    //    private static final Logger log = Logger.getLogger(NutsUtils.class.getName());
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    public static final String DEFAULT_PASSPHRASE = CoreSecurityUtils.bytesToHex("It's completely nuts!!".getBytes());
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    private static Set<String> DEPENDENCY_SUPPORTED_PARAMS = new HashSet<>(Arrays.asList("scope", "optional"));
    public static NutsDependencyFilter EXEC_DEPENDENCIES_FILTER =
            new NutsDependencyFilter() {
                @Override
                public boolean accept(NutsDependency d) {
                    return !d.isOptional() &&
                            (CoreStringUtils.isEmpty(d.getScope())
                                    || "compile".equals(d.getScope())
                                    || "runtime".equals(d.getScope())
                                    || "provided ".equals(d.getScope())
                            );
                }
            };
    public static Comparator<NutsDescriptor> NUTS_DESC_ENV_SPEC_COMPARATOR = new Comparator<NutsDescriptor>() {
        @Override
        public int compare(NutsDescriptor o1, NutsDescriptor o2) {
            //most specific first
            return Integer.compare(weight(o2), weight(o1));
        }

        private int weight(NutsDescriptor desc) {
            int x = 1;
            x *= weight(desc.getArch());
            x *= weight(desc.getOs());
            x *= weight(desc.getOsdist());
            x *= weight(desc.getPlatform());
            return x;
        }

        private int weight(String[] desc) {
            int x = 1;
            for (String s : desc) {
                x += weight(parseNutsDependency(s));
            }
            return x;
        }

        private int weight(NutsDependency desc) {
            return weight(desc.getVersion());
        }

        private int weight(NutsVersion desc) {
            int x = 1;
            for (NutsVersionInterval s : desc.toIntervals()) {
                x *= weight(s);
            }
            return x;
        }

        private int weight(NutsVersionInterval desc) {
            return desc.isFixedValue() ? 2 : 3;
        }
    };

    public static NutsId finNutsIdByFullName(NutsId id, Collection<NutsId> all) {
        if (all != null) {
            for (NutsId nutsId : all) {
                if (nutsId != null) {
                    if (nutsId.isSameFullName(id)) {
                        return nutsId;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isSameGroupAndName(NutsId a, NutsId b) {
        return a.setVersion("").unsetQuery().setNamespace("")
                .equals(b.setVersion("").unsetQuery().setNamespace(""));
    }

    public static void validateNutName(String name) {
        if (!name.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new IllegalArgumentException("Invalid nuts name " + name);
        }
    }

    public static String formatImport(List<String> imports) throws IOException {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();
        for (String s : imports) {
            s = s.trim();
            if (s.length() > 0) {
                if (!all.contains(s)) {
                    all.add(s);
                    if (sb.length() > 0) {
                        sb.append(":");
                    }
                    sb.append(s);
                }
            }
        }
        return sb.toString();
    }

    public static List<String> parseImport(String imports) throws IOException {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        if (imports != null) {
            String[] groupsArr = imports.split(":");
            for (String grp : groupsArr) {
                String grp2 = CoreStringUtils.trim(grp);
                if (grp2.length() > 0) {
                    if (!all.contains(grp2)) {
                        all.add(grp2);
                    }
                }
            }
        }
        return new ArrayList<>(all);
    }

    public static File getNutsFolder(NutsId id, File root) {
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new NutsIdInvalidFormatException("Missing group for " + id);
        }
        File groupFolder = new File(root, id.getGroup().replaceAll("\\.", File.separator));
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new NutsIdInvalidFormatException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsIdInvalidFormatException("Missing version for " + id.toString());
        }
        File versionFolder = new File(artifactFolder, id.getVersion().getValue());
        String face = id.getFace();
        if (CoreStringUtils.isEmpty(face)) {
            face = NutsConstants.QUERY_FACE_DEFAULT_VALUE;
        }
        return new File(versionFolder, face);
    }

    public static String[] splitNameAndValue(String arg) {
        int i = arg.indexOf('=');
        if(i>=0){
            return new String[]{
                    i==0?"":arg.substring(0,i),
                    i==arg.length()-1?"":arg.substring(i+1),
            };
        }
        return null;
    }

    public static String[][] splitEnvAndAppArgs(String[] args) {
        List<String> env = new ArrayList<>();
        List<String> app = new ArrayList<>();
        boolean expectEnv = true;
        for (String s : args) {
            if (expectEnv) {
                if (s.startsWith("--nuts-")) {
                    if (s.startsWith("--nuts-arg-")) {
                        app.add("--nuts-" + s.substring(0, "--nuts-arg-".length()));
                    } else {
                        env.add(s.substring("--nuts".length()));
                    }
                } else {
                    app.add(s);
                    expectEnv = false;
                }
            } else {
                app.add(s);
            }
        }
        return new String[][]{
                env.toArray(new String[env.size()]),
                app.toArray(new String[app.size()]),
        };
    }

    public static NutsDescriptor createNutsDescriptor() throws IOException {
        return createNutsDescriptor(
                parseNutsId("my-group:my-id#1.0"),
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static NutsDescriptor createNutsDescriptor(NutsId id, String face, NutsId[] parents, String packaging, boolean executable, String ext, NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description, String[] arch, String[] os, String[] osdist, String[] platform, NutsDependency[] dependencies, Map<String, String> properties) {
        return new DefaultNutsDescriptor(
                id, face, parents, packaging, executable, ext, executor, installer, name, description, arch, os, osdist, platform, dependencies, properties
        );
    }
    public static NutsDescriptor createNutsDescriptor(NutsDescriptor other) {
        return createNutsDescriptor(
                other.getId(), other.getFace(), other.getParents(), other.getPackaging(), other.isExecutable(),
                other.getExt(), other.getExecutor(), other.getInstaller(), other.getName(), other.getDescription(),
                other.getArch(), other.getOs(), other.getOsdist(), other.getPlatform(), other.getDependencies(), other.getProperties()
        );
    }

    /**
     * examples : script://groupId:artifactId/version?query
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @return
     */
//    public static NutsId parseNutsId(String nutFormat) {
//        return parseNutsId(nutFormat);
//    }

//    public static NutsId parseOrErrorNutsId(String nutFormat) {
//        return parseOrErrorNutsId(nutFormat);
//    }

//    public static NutsId parseNullableOrErrorNutsId(String nutFormat) {
//        return parseNullableOrErrorNutsId(nutFormat);
//    }

//    public static NutsDescriptor parseOrNullNutsDescriptor(File file) {
//        return parseOrNullNutsDescriptor(file);
//    }
//
//    public static NutsDescriptor parseNutsDescriptor(File file) throws IOException {
//        return parseNutsDescriptor(file);
//    }
//
//    public static NutsDescriptor parseNutsDescriptor(String str) throws IOException {
//        return parseNutsDescriptor(str);
//    }
//
//    public static NutsDescriptor parseNutsDescriptor(InputStream in) throws IOException {
//        return parseNutsDescriptor(in);
//    }

    public static NutsId finNutsIdByFullNameInStrings(NutsId id, Collection<String> all) {
        if (all != null) {
            for (String nutsId : all) {
                if (nutsId != null) {
                    NutsId nutsId2 = CoreNutsUtils.parseOrErrorNutsId(nutsId);
                    if (nutsId2.isSameFullName(id)) {
                        return nutsId2;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isEffectiveValue(String value) {
        return (!CoreStringUtils.isEmpty(value) && !containsVars(value));
    }

    public static boolean containsVars(String value) {
        return value != null && value.contains("${");
    }

    public static boolean isEffectiveId(NutsId id) {
        return (isEffectiveValue(id.getGroup()) && isEffectiveValue(id.getName()) && isEffectiveValue(id.getVersion().getValue()));
    }

    public static boolean containsVars(NutsId id) {
        return (containsVars(id.getGroup()) && containsVars(id.getName()) && containsVars(id.getVersion().getValue()));
    }

    public static void validateRepositoryId(String repositoryId) {
        if (!repositoryId.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new IllegalArgumentException("Invalid repository id " + repositoryId);
        }
    }

    public static HttpConnectionFacade getHttpClientFacade(NutsWorkspace ws, String url) throws IOException {
//        System.out.println("getHttpClientFacade "+url);
        NutsTransportComponent best = ws.getFactory().createSupported(NutsTransportComponent.class, url);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        return best.open(url);
    }

    public static NutsDescriptor parseOrNullNutsDescriptor(File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            try (FileInputStream os = new FileInputStream(file)) {
                try {
                    return parseNutsDescriptor(os);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            //ignore
        }
        return null;
    }

    public static NutsDescriptor parseNutsDescriptor(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream os = new FileInputStream(file)) {
            try {
                return parseNutsDescriptor(os);
            } catch (RuntimeException ex) {
                throw new IOException("Unable to parse file " + file, ex);
            }
        }

    }

    public static NutsDescriptor parseNutsDescriptor(String str) throws IOException {
        if (CoreStringUtils.isEmpty(str)) {
            return null;
        }
        return parseNutsDescriptor(new ByteArrayInputStream(str.getBytes()));
    }

    public static NutsDescriptor parseNutsDescriptor(InputStream in) throws IOException {
        JsonObject jsonObject = Json.createReader(in).readObject();
        String nutsVersion = jsonObject.getString("nuts-version");
        if ("1.0".equals(nutsVersion)) {
            NutsId id = CoreNutsUtils.parseOrErrorNutsId(jsonObject.getString("id"));
            String alt = CoreJsonUtils.get().deserialize(jsonObject.get(NutsConstants.QUERY_FACE), String.class);
            String packaging = CoreJsonUtils.get().deserialize(jsonObject.get("packaging"), String.class);
            String ext = CoreJsonUtils.get().deserialize(jsonObject.get("ext"), String.class);
            String[] parentStrings = CoreJsonUtils.get().deserialize(jsonObject.get("parents"), String[].class);
            Map<String, String> props = CoreJsonUtils.get().deserializeStringsMap(jsonObject.get("properties"), new HashMap());
            NutsId[] parents = new NutsId[parentStrings == null ? 0 : parentStrings.length];
            for (int i = 0; i < parents.length; i++) {
                parents[i] = CoreNutsUtils.parseOrErrorNutsId(parentStrings[i]);
            }
            String name = CoreJsonUtils.get().deserialize(jsonObject.get("name"), String.class);
            String description = CoreJsonUtils.get().deserialize(jsonObject.get("description"), String.class);
            boolean executable = false;
            if (jsonObject.get("executable") != null) {
                executable = (jsonObject.getBoolean("executable"));
            }
            NutsExecutorDescriptor executor = null;
            NutsExecutorDescriptor installer = null;
            if (!CoreJsonUtils.get().isNull(jsonObject.get("executor"))) {
                JsonObject runObj = (JsonObject) jsonObject.get("executor");
                NutsId rid = runObj.get("id") == null ? null : CoreNutsUtils.parseOrErrorNutsId(runObj.getString("id"));
                String[] rargs = CoreJsonUtils.get().deserialize(runObj.getJsonArray("args"), String[].class);
                Properties rprops = CoreJsonUtils.get().deserialize(runObj.getJsonObject("properties"), Properties.class);
                executor = new NutsExecutorDescriptor(rid, rargs, rprops);
            }
            if (!CoreJsonUtils.get().isNull(jsonObject.get("installer"))) {
                JsonObject runObj = (JsonObject) jsonObject.get("installer");
                NutsId rid = runObj.get("id") == null ? null : CoreNutsUtils.parseOrErrorNutsId(runObj.getString("id"));
                String[] rargs = CoreJsonUtils.get().deserialize(runObj.getJsonArray("args"), String[].class);
                Properties rprops = CoreJsonUtils.get().deserialize(runObj.getJsonObject("properties"), Properties.class);
                installer = new NutsExecutorDescriptor(rid, rargs, rprops);
            }
            List<NutsDependency> deps = new ArrayList<>();

            JsonArray dependencies = CoreJsonUtils.get().isNull(jsonObject.get("dependencies")) ? null : jsonObject.getJsonArray("dependencies");
            if (dependencies != null) {
                for (JsonValue dependency : dependencies) {
                    NutsDependency depp = parseNutsDependency(((JsonString) dependency).getString());
                    if (depp == null) {
                        throw new IOException("Invalid dependency " + dependency);
                    }
                    deps.add(depp);
                }
            }
            String[] os = CoreJsonUtils.get().getJsonObjectStringArray(jsonObject, "os");
            String[] osdist = CoreJsonUtils.get().getJsonObjectStringArray(jsonObject, "osdist");
            String[] arch = CoreJsonUtils.get().getJsonObjectStringArray(jsonObject, "arch");
            String[] platform = CoreJsonUtils.get().getJsonObjectStringArray(jsonObject, "platform");

            return new DefaultNutsDescriptor(
                    id,
                    alt,
                    parents,
                    packaging,
                    executable,
                    ext,
                    executor,
                    installer,
                    name,
                    description,
                    arch,
                    os,
                    osdist,
                    platform,
                    deps.toArray(new NutsDependency[deps.size()]),
                    props
            );
        }
        throw new IOException("Unsupported version " + nutsVersion);
    }

    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutFormat
     * @return
     */
    public static NutsId parseNutsId(String nutFormat) {
        if (nutFormat == null) {
            return null;
        }
        Matcher m = NUTS_ID_PATTERN.matcher(nutFormat);
        if (m.find()) {
            String protocol = m.group(2);
            String group = m.group(3);
            String artifact = m.group(5);
            String version = m.group(7);
            String face = m.group(9);
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            return new NutsIdImpl(
                    protocol,
                    group,
                    artifact,
                    version,
                    face
            );
        }
        return null;
    }

    public static NutsId parseOrErrorNutsId(String nutFormat) {
        NutsId id = parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsIdInvalidFormatException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static NutsId parseNullableOrErrorNutsId(String nutFormat) {
        if (CoreStringUtils.isEmpty(nutFormat)) {
            return null;
        }
        NutsId id = parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsIdInvalidFormatException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static String getNutsFileName(NutsId id, String ext) {
        if (CoreStringUtils.isEmpty(ext)) {
            ext = "jar";
        }
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        return id.getName() + "-" + id.getVersion() + ext;
    }

    public static String[] applyStringProperties(String[] child, StringMapper properties) {
        String[] vals = new String[child.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = applyStringProperties(child[i], properties);
        }
        return vals;
    }

    public static Map<String, String> applyMapProperties(Map<String, String> child, StringMapper properties) {
        Map<String, String> m2 = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : child.entrySet()) {
            m2.put(applyStringProperties(entry.getKey(), properties), applyStringProperties(entry.getValue(), properties));
        }
        return m2;
    }

    public static String applyStringProperties(String child, StringMapper properties) {
        if (CoreStringUtils.isEmpty(child)) {
            return null;
        }
        return CoreStringUtils.replaceVars(child, properties);
    }

    public static String applyStringInheritance(String child, String parent) {
        child = CoreStringUtils.trimToNull(child);
        parent = CoreStringUtils.trimToNull(parent);
        if (child == null) {
            return parent;
        }
        return child;
    }

    public static NutsDependency parseOrErrorNutsDependency(String nutFormat) {
        NutsDependency id = parseNutsDependency(nutFormat);
        if (id == null) {
            throw new NutsIdInvalidFormatException("Invalid Dependency format : " + nutFormat);
        }
        return id;
    }

    public static NutsDependency parseNutsDependency(String nutFormat) {
        if (nutFormat == null) {
            return null;
        }
        Matcher m = DEPENDENCY_NUTS_DESCRIPTOR_PATTERN.matcher(nutFormat);
        if (m.find()) {
            String protocol = m.group(2);
            String group = m.group(3);
            String name = m.group(5);
            String version = m.group(7);
            String face = CoreStringUtils.trim(m.group(9));
            Map<String, String> scope = CoreStringUtils.parseMap(face, "&");
            for (String s : scope.keySet()) {
                if (!DEPENDENCY_SUPPORTED_PARAMS.contains(s)) {
                    throw new IllegalArgumentException("Unsupported parameter " + CoreStringUtils.simpleQuote(s, false, "") + " in " + nutFormat);
                }
            }
            if (name == null) {
                name = group;
                group = null;
            }
            return new NutsDependencyImpl(
                    protocol,
                    group,
                    name,
                    version,
                    scope.get("scope"),
                    scope.get("optional")
            );
        }
        return null;
    }
}
