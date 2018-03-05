/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.extensions.filters.dependency.OptionalNutsDependencyFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyFilterAnd;
import net.vpc.app.nuts.extensions.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterAnd;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyFilterOr;
import net.vpc.app.nuts.extensions.filters.version.NutsVersionFilterAnd;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterOr;
import net.vpc.app.nuts.extensions.filters.version.NutsVersionFilterOr;
import net.vpc.app.nuts.extensions.filters.dependency.ScopeNutsDependencyFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.id.NutsIdPatternFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.DefaultNutsDescriptor;
import net.vpc.app.nuts.extensions.core.NutsDependencyImpl;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;

import javax.json.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterById;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterOs;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterOsdist;
import net.vpc.app.nuts.extensions.filters.descriptor.NutsDescriptorFilterPlatform;
import net.vpc.app.nuts.extensions.filters.id.NutsIdJavascriptFilter;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {
//        if (source instanceof NutsId) {
//            NutsId d = (NutsId) source;
//            if ("maven-metadata".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("archetype-catalog".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("descriptor".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("main-sha1".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("descriptor-sha1".equals(d.getFace())) {
//                monitorable = false;
//            }
//        }

    public static final String FACE_CATALOG = "catalog";
    public static final String FACE_DESC = "descriptor";
    public static final String FACE_PACKAGE = "package";
    public static final String FACE_DESC_HASH = "descriptor-hash";
    public static final String FACE_PACKAGE_HASH = "package-hash";

    //    private static final Logger log = Logger.getLogger(NutsUtils.class.getName());
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    public static final String DEFAULT_PASSPHRASE = CoreSecurityUtils.bytesToHex("It's completely nuts!!".getBytes());
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    public static final NutsDependencyFilter OPTIONAL = new OptionalNutsDependencyFilter(true);
    public static final NutsDependencyFilter NON_OPTIONAL = new OptionalNutsDependencyFilter(false);
    public static final NutsDependencyFilter SCOPE_RUN = And(new ScopeNutsDependencyFilter("compile,system,runtime"), NON_OPTIONAL);
    public static final NutsDependencyFilter SCOPE_TEST = And(new ScopeNutsDependencyFilter("compile,system,runtime,test"), NON_OPTIONAL);

    public static Comparator<NutsId> NUTS_ID_COMPARATOR = new Comparator<NutsId>() {
        @Override
        public int compare(NutsId o1, NutsId o2) {
            if (o1 == null || o2 == null) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                return 1;
            }
            int t = o1.toString().compareTo(o2.toString());
            return t;
        }
    };
    public static Comparator<NutsFile> NUTS_FILE_COMPARATOR = new Comparator<NutsFile>() {
        @Override
        public int compare(NutsFile o1, NutsFile o2) {
            if (o1 == null || o2 == null) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                return 1;
            }
            return NUTS_ID_COMPARATOR.compare(o1.getId(), o2.getId());
        }
    };

    private static Set<String> DEPENDENCY_SUPPORTED_PARAMS = new HashSet<>(Arrays.asList("scope", "optional"));
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
            throw new NutsIllegalArgumentException("Invalid nuts name " + name);
        }
    }

    public static String formatImport(List<String> imports) {
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
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        File groupFolder = new File(root, id.getGroup().replaceAll("\\.", File.separator));
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
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
        if (i >= 0) {
            return new String[]{
                i == 0 ? "" : arg.substring(0, i),
                i == arg.length() - 1 ? "" : arg.substring(i + 1),};
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
            app.toArray(new String[app.size()]),};
    }

    public static NutsDescriptor createNutsDescriptor() {
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
        return (!CoreStringUtils.isEmpty(value) && !CoreStringUtils.containsVars(value));
    }

    public static boolean isEffectiveId(NutsId id) {
        return (isEffectiveValue(id.getGroup()) && isEffectiveValue(id.getName()) && isEffectiveValue(id.getVersion().getValue()));
    }

    public static boolean containsVars(NutsId id) {
        return (CoreStringUtils.containsVars(id.getGroup()) && CoreStringUtils.containsVars(id.getName()) && CoreStringUtils.containsVars(id.getVersion().getValue()));
    }

    public static void validateRepositoryId(String repositoryId) {
        if (!repositoryId.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NutsIllegalArgumentException("Invalid repository id " + repositoryId);
        }
    }

    public static NutsDescriptor parseOrNullNutsDescriptor(File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            return parseNutsDescriptor(new FileInputStream(file), true);
        } catch (Exception ex) {
            //ignore
        }
        return null;
    }

    public static NutsDescriptor parseNutsDescriptor(byte[] bytes) {
        return parseNutsDescriptor(new ByteArrayInputStream(bytes), true);
    }

    public static NutsDescriptor parseNutsDescriptor(URL url) {
        try {
            try {
                return parseNutsDescriptor(url.openStream(), true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException("Unable to parse url " + url, ex);
            }
        } catch (IOException ex) {
            throw new NutsParseException("Unable to parse url " + url, ex);
        }
    }

    public static NutsDescriptor parseNutsDescriptor(File file) {
        if (!file.exists()) {
            throw new NutsNotFoundException("Unable to parse file " + file + ". Does not exist");
        }
        try {
            return parseNutsDescriptor(new FileInputStream(file), true);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsParseException("Unable to parse file " + file, ex);
        }
    }

    public static NutsDescriptor parseNutsDescriptor(String str) {
        if (CoreStringUtils.isEmpty(str)) {
            return null;
        }
        return parseNutsDescriptor(new ByteArrayInputStream(str.getBytes()), true);
    }

    public static NutsDescriptor parseNutsDescriptor(InputStream in, boolean closeStream) {
        try {
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
                            throw new NutsParseException("Invalid dependency " + dependency);
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
            throw new NutsParseException("Unsupported version " + nutsVersion);
        } finally {
            if (closeStream) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }
            }
        }
    }

    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutsId
     * @return
     */
    public static NutsId parseNutsId(String nutsId) {
        if (nutsId == null) {
            return null;
        }
        Matcher m = NUTS_ID_PATTERN.matcher(nutsId);
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
            throw new NutsParseException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static NutsId parseNullableOrErrorNutsId(String nutFormat) {
        if (CoreStringUtils.isEmpty(nutFormat)) {
            return null;
        }
        NutsId id = parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException("Invalid Id format : " + nutFormat);
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
            throw new NutsParseException("Invalid Dependency format : " + nutFormat);
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
                    throw new NutsIllegalArgumentException("Unsupported parameter " + CoreStringUtils.simpleQuote(s, false, "") + " in " + nutFormat);
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
                    scope.get("optional"),
                    null
            );
        }
        return null;
    }

    public static NutsRepositoryFilter createNutsRepositoryFilter(TypedObject object) {
        if (object == null) {
            return null;
        }
        if (object.getType().equals(NutsRepositoryFilter.class)) {
            return (NutsRepositoryFilter) object.getValue();
        }
        throw new NutsIllegalArgumentException("createNutsRepositoryFilter Not yet supported from type " + object.getType().getName());
    }

    public static NutsDependencyFilter createNutsDependencyFilter(TypedObject object) {
        if (object == null) {
            return null;
        }
        if (object.getType().equals(NutsDependencyFilter.class)) {
            return (NutsDependencyFilter) object.getValue();
        }
        throw new NutsIllegalArgumentException("createNutsDependencyFilter Not yet supported from type " + object.getType().getName());
    }

    public static NutsVersionFilter createNutsVersionFilter(TypedObject object) {
        if (object == null) {
            return null;
        }
        if (object.getType().equals(NutsVersionFilter.class)) {
            return (NutsVersionFilter) object.getValue();
        }
        throw new NutsIllegalArgumentException("createNutsVersionFilter Not yet supported from type " + object.getType().getName());
    }

    public static NutsDescriptorFilter And(NutsDescriptorFilter... all) {
        return new NutsDescriptorFilterAnd(all);
    }

    public static NutsDescriptorFilter Or(NutsDescriptorFilter... all) {
        return new NutsDescriptorFilterOr(all);
    }

    public static NutsIdFilter And(NutsIdFilter... all) {
        return new NutsIdFilterAnd(all);
    }

    public static NutsIdFilter Or(NutsIdFilter... all) {
        return new NutsIdFilterOr(all);
    }

    public static NutsVersionFilter And(NutsVersionFilter... all) {
        return new NutsVersionFilterAnd(all);
    }

    public static NutsVersionFilter Or(NutsVersionFilter... all) {
        return new NutsVersionFilterOr(all);
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osdist, String platform) {
        return simplify(
                And(
                        new NutsDescriptorFilterArch(arch),
                        new NutsDescriptorFilterOs(arch),
                        new NutsDescriptorFilterOsdist(arch),
                        new NutsDescriptorFilterPlatform(arch)
                )
        );
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap) {
        return createNutsDescriptorFilter(
                faceMap == null ? null : faceMap.get("arch"),
                faceMap == null ? null : faceMap.get("os"),
                faceMap == null ? null : faceMap.get("osdist"),
                faceMap == null ? null : faceMap.get("platform"));
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(NutsIdFilter id) {
        return new NutsDescriptorFilterById(id);
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(TypedObject object) {
        if (object == null) {
            return null;
        }
        if (object.getType().equals(NutsDescriptorFilter.class)) {
            return (NutsDescriptorFilter) object.getValue();
        }
        throw new NutsIllegalArgumentException("createNutsDescriptorFilter Not yet supported from type " + object.getType().getName());
    }

    public static NutsIdFilter createNutsIdFilter(TypedObject object) {
        if (object == null) {
            return null;
        }
        if (object.getType().equals(NutsIdFilter.class)) {
            return (NutsIdFilter) object.getValue();
        }
        throw new NutsIllegalArgumentException("createNutsIdFilter Not yet supported from type " + object.getType().getName());
    }

    public static NutsDependencyFilter And(NutsDependencyFilter... all) {
        return new NutsDependencyFilterAnd(all);
    }

    public static NutsDependencyFilter Or(NutsDependencyFilter... all) {
        return new NutsDependencyFilterOr(all);
    }

    public static <T> T simplify(T any) {
        if (any == null) {
            return null;
        }
        if (any instanceof Simplifiable) {
            return ((Simplifiable<T>) any).simplify();
        }
        return any;
    }

    public static <T> T[] simplifyAndShrink(Class<T> cls, T... any) {
        List<T> all = new ArrayList<>();
        boolean updates = false;
        for (T t : any) {
            T t2 = simplify(t);
            if (t2 != null) {
                if (t2 != t) {
                    updates = true;
                }
                all.add(t2);
            } else {
                updates = true;
            }
        }
        if (!updates) {
            return null;
        }
        return all.toArray((T[]) Array.newInstance(cls, all.size()));
    }

    public static NutsSearch createSearch(String[] js, String[] ids, String[] arch, String[] packagings, String[] repos) {
        NutsSearch search = new NutsSearch();

        NutsDescriptorFilter dFilter = null;
        NutsIdFilter idFilter = null;
        NutsDependencyFilter depFilter = null;
        if (js != null) {
            for (String j : js) {
                if (!CoreStringUtils.isEmpty(j)) {
                    if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                        dFilter = simplify(And(dFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                    } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                        depFilter = simplify(And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                    } else {
                        idFilter = simplify(And(idFilter, NutsIdJavascriptFilter.valueOf(j)));
                    }
                }
            }
        }
        if (ids != null) {
            idFilter = simplify(And(idFilter, new NutsIdPatternFilter(ids)));
        }
        NutsDescriptorFilter packs = null;
        for (String v : packagings) {
            packs = CoreNutsUtils.simplify(CoreNutsUtils.Or(packs, new NutsDescriptorFilterPackaging(v)));
        }
        NutsDescriptorFilter archs = null;
        for (String v : arch) {
            archs = CoreNutsUtils.simplify(CoreNutsUtils.Or(archs, new NutsDescriptorFilterArch(v)));
        }

        dFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(dFilter, packs, archs));

//        search.setDependencyFilter(null);
        search.setDescriptorFilter(dFilter);
        search.setIdFilter(idFilter);

        if (repos != null) {
            search.setRepositoryFilter(new DefaultNutsRepositoryFilter(new HashSet<>(Arrays.asList(repos))));
        }
        return search;
    }

    public static String getResourceString(String resource, NutsWorkspace ws, Class cls) {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = cls.getResourceAsStream(resource);
                if (s != null) {
                    help = CoreIOUtils.readStreamAsString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Nuts.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
        }
        if (help == null) {
            help = "no help found";
        }
        HashMap<String, String> props = new HashMap<>((Map) System.getProperties());
        props.putAll(ws.getConfigManager().getRuntimeProperties());
        help = CoreStringUtils.replaceVars(help, new MapStringMapper(props));
        return help;
    }

    public static String getPath(NutsId id, String ext, String sep) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getGroup().replaceAll("\\.", sep));
        sb.append(sep);
        sb.append(id.getName());
        sb.append(sep);
        sb.append(id.getVersion().toString());
        sb.append(sep);
        String name = id.getName() + "-" + id.getVersion().getValue();
        sb.append(name);
        sb.append(ext);
        return sb.toString();
    }

}
