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
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.*;
import net.vpc.app.nuts.extensions.filters.dependency.*;
import net.vpc.app.nuts.extensions.filters.descriptor.*;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.repository.ExprNutsRepositoryFilter;
import net.vpc.app.nuts.extensions.filters.repository.NutsRepositoryFilterAnd;
import net.vpc.app.nuts.extensions.filters.version.NutsVersionFilterAnd;
import net.vpc.app.nuts.extensions.filters.version.NutsVersionFilterOr;
import net.vpc.common.io.*;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Filter;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {
    private static final Logger log = Logger.getLogger(CoreNutsUtils.class.getName());
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
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
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
            return o1.toString().compareTo(o2.toString());
        }
    };
    public static Comparator<NutsDefinition> NUTS_FILE_COMPARATOR = new Comparator<NutsDefinition>() {
        @Override
        public int compare(NutsDefinition o1, NutsDefinition o2) {
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


//    public static NutsId SAMPLE_NUTS_ID = new DefaultNutsId("namespace", "group", "name", "version", "param='true'");

    public static NutsDescriptor SAMPLE_NUTS_DESCRIPTOR =
            new DefaultNutsDescriptorBuilder()
                    .setId(new DefaultNutsId(null, "group", "name", "version", (String) null))
                    .setAlternative("suse")
                    .setName("Application Full Name")
                    .setDescription("Application Description")
                    .setExecutable(true)
                    .setPackaging("jar")
                    .setExt("exe")
                    .setArch(new String[]{"64bit"})
                    .setOs(new String[]{"linux#4.6"})
                    .setOsdist(new String[]{"opensuse#42"})
                    .setPlatform(new String[]{"java#8"})
                    .setExecutor(new NutsExecutorDescriptor(
                            new DefaultNutsId(null, null, "java", "8", (String) null),
                            new String[]{"-jar"}
                    ))
                    .setInstaller(new NutsExecutorDescriptor(
                            new DefaultNutsId(null, null, "java", "8", (String) null),
                            new String[]{"-jar"}
                    ))
                    .setLocations(new String[]{
                            "http://server/somelink"
                    })
                    .setDependencies(
                            new NutsDependency[]{
                                    new DefaultNutsDependency(
                                            "namespace", "group", "name", null, DefaultNutsVersion.valueOf("version"), "compile",
                                            "false", new NutsId[0]
                                    )
                            }
                    )
                    .build();

    public static NutsId finNutsIdBySimpleName(NutsId id, Collection<NutsId> all) {
        if (all != null) {
            for (NutsId nutsId : all) {
                if (nutsId != null) {
                    if (nutsId.equalsSimpleName(id)) {
                        return nutsId;
                    }
                }
            }
        }
        return null;
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

    public static File getNutsFolder(NutsId id, File root) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        File groupFolder = new File(root, id.getGroup().replaceAll("\\.", File.separator));
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
        }
        File versionFolder = new File(artifactFolder, id.getVersion().getValue());
        String face = id.getFace();
        if (StringUtils.isEmpty(face)) {
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

    public static NutsDescriptor createNutsDescriptor() {
        return new DefaultNutsDescriptorBuilder().setId(parseNutsId("my-group:my-id#1.0")).build();
    }

    /**
     * examples : script://groupId:artifactId/version?query
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @return
     */
//    public static NutsId parseId(String nutFormat) {
//        return parseId(nutFormat);
//    }
//    public static NutsId parseRequiredId(String nutFormat) {
//        return parseRequiredId(nutFormat);
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
    public static NutsId findNutsIdBySimpleNameInStrings(NutsId id, Collection<String> all) {
        if (all != null) {
            for (String nutsId : all) {
                if (nutsId != null) {
                    NutsId nutsId2 = parseRequiredNutsId(nutsId);
                    if (nutsId2.equalsSimpleName(id)) {
                        return nutsId2;
                    }
                }
            }
        }
        return null;
    }

    public static NutsId findNutsIdBySimpleNameInIds(NutsId id, Collection<NutsId> all) {
        if (all != null) {
            for (NutsId nutsId : all) {
                if (nutsId != null) {
                    if (nutsId.equalsSimpleName(id)) {
                        return nutsId;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isEffectiveValue(String value) {
        return (!StringUtils.isEmpty(value) && !CoreStringUtils.containsVars(value));
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
            String query = m.group(9);
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            return new DefaultNutsId(
                    protocol,
                    group,
                    artifact,
                    version,
                    query
            );
        }
        return null;
    }

    public static NutsId parseRequiredNutsId(String nutFormat) {
        NutsId id = parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static NutsId parseNullableOrErrorNutsId(String nutFormat) {
        if (StringUtils.isEmpty(nutFormat)) {
            return null;
        }
        NutsId id = parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static String getNutsFileName(NutsId id, String ext) {
        String classifier = id.getClassifier();
        if (StringUtils.isEmpty(ext)) {
            ext = "jar";
        }
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        String classifierNamePart = (".json".equals(ext) || ".nuts".equals(ext) || ".pom".equals(ext)) ? "" :
                (StringUtils.isEmpty(classifier) ? "" : (("-") + classifier));

        return id.getName() + "-" + id.getVersion() + classifierNamePart + ext;
    }

    public static String[] applyStringProperties(String[] child, ObjectConverter<String, String> properties) {
        String[] vals = new String[child.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = applyStringProperties(child[i], properties);
        }
        return vals;
    }

    public static Map<String, String> applyMapProperties(Map<String, String> child, ObjectConverter<String, String> properties) {
        Map<String, String> m2 = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : child.entrySet()) {
            m2.put(applyStringProperties(entry.getKey(), properties), applyStringProperties(entry.getValue(), properties));
        }
        return m2;
    }

    public static NutsVersion applyStringProperties(NutsVersion child, ObjectConverter<String, String> properties) {
        if (child == null) {
            return child;
        }
        String s = child.getValue();
        if (StringUtils.isEmpty(s)) {
            return DefaultNutsVersion.EMPTY;
        }
        String s2 = applyStringProperties(s, properties);
        if (!StringUtils.trim(s2).equals(s)) {
            return DefaultNutsVersion.valueOf(s2);
        }
        return child;
    }

    public static String applyStringProperties(String child, ObjectConverter<String, String> properties) {
        if (StringUtils.isEmpty(child)) {
            return null;
        }
        return CoreStringUtils.replaceVars(child, properties);
    }

    public static String applyStringInheritance(String child, String parent) {
        child = StringUtils.trimToNull(child);
        parent = StringUtils.trimToNull(parent);
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
            String face = StringUtils.trim(m.group(9));
            Map<String, String> queryMap = StringUtils.parseMap(face, "&");
            for (String s : queryMap.keySet()) {
                if (!DEPENDENCY_SUPPORTED_PARAMS.contains(s)) {
                    throw new NutsIllegalArgumentException("Unsupported parameter " + CoreStringUtils.simpleQuote(s, false, "") + " in " + nutFormat);
                }
            }
            if (name == null) {
                name = group;
                group = null;
            }
            return new DefaultNutsDependency(
                    protocol,
                    group,
                    name,
                    queryMap.get("classifier"),
                    DefaultNutsVersion.valueOf(version),
                    queryMap.get("scope"),
                    queryMap.get("optional"),
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
        if (object.getType().equals(String.class)) {
            String s = (String) object.getValue();
            return new ExprNutsRepositoryFilter(s);
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

    public static NutsRepositoryFilter And(NutsRepositoryFilter... all) {
        return new NutsRepositoryFilterAnd(all);
    }

    public static NutsVersionFilter Or(NutsVersionFilter... all) {
        return new NutsVersionFilterOr(all);
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osdist, String platform) {
        return simplify(
                And(
                        new NutsDescriptorFilterArch(arch),
                        new NutsDescriptorFilterOs(os),
                        new NutsDescriptorFilterOsdist(osdist),
                        new NutsDescriptorFilterPlatform(platform)
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
        return all.toArray((T[]) Array.newInstance(cls, 0));
    }

    //    public static String getResourceString(String resource, NutsWorkspace ws, Class cls) {
//        String help = null;
//        try {
//            InputStream s = null;
//            try {
//                s = cls.getResourceAsStream(resource);
//                if (s != null) {
//                    help = IOUtils.loadString(s, true);
//                }
//            } finally {
//                if (s != null) {
//                    s.close();
//                }
//            }
//        } catch (IOException e) {
//            Logger.getLogger(Nuts.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
//        }
//        if (help == null) {
//            help = "no help found";
//        }
//        HashMap<String, String> props = new HashMap<>((Map) System.getProperties());
//        props.putAll(ws.getConfigManager().getUserProperties());
//        help = CoreStringUtils.replaceVars(help, new MapStringMapper(props));
//        return help;
//    }
//
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

    public static List<NutsId> filterNutsIdByLatestVersion(List<NutsId> base) {
        LinkedHashMap<String, NutsId> valid = new LinkedHashMap<>();
        for (NutsId n : base) {
            NutsId old = valid.get(n.getSimpleName());
            if (old == null || old.getVersion().compareTo(n.getVersion()) < 0) {
                valid.put(n.getSimpleName(), n);
            }
        }
        return new ArrayList<>(valid.values());
    }

    public static List<NutsExtensionInfo> filterNutsExtensionInfoByLatestVersion(List<NutsExtensionInfo> base) {
        LinkedHashMap<String, NutsExtensionInfo> valid = new LinkedHashMap<>();
        for (NutsExtensionInfo n : base) {
            NutsExtensionInfo old = valid.get(n.getId().getSimpleName());
            if (old == null || old.getId().getVersion().compareTo(n.getId().getVersion()) < 0) {
                valid.put(n.getId().getSimpleName(), n);
            }
        }
        return new ArrayList<>(valid.values());
    }

    public static String expandPath(String path, NutsWorkspace ws) {
        return expandPath(path, ws.getConfigManager().getHomeLocation());
    }

    public static String expandPath(String path, String nutsHome) {
        String defaultNutsHome = Nuts.getDefaultNutsHome();
        if (StringUtils.isEmpty(nutsHome)) {
            nutsHome = defaultNutsHome;
        }
        if (path.startsWith(defaultNutsHome + "/")) {
            path = nutsHome + "/" + path.substring(defaultNutsHome.length() + 1);
        }
        if (path.startsWith("~/")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static <T> Filter<T> createFilter(ObjectFilter<T> t) {
        if (t == null) {
            return null;
        }
        return new Filter<T>() {
            @Override
            public boolean accept(T value) {
                return t.accept(value);
            }
        };
    }

    public static CharacterizedFile characterize(NutsWorkspace ws, InputStreamSource contentFile, NutsSession session) {
        session = validateSession(session, ws);
        CharacterizedFile c = new CharacterizedFile();
        c.contentFile = contentFile;
        if (c.contentFile.getSource() instanceof File) {
            //okkay
        } else {
            File temp = CoreIOUtils.createTempFile(contentFile.getName(), false, null);
            CoreNutsUtils.copy(contentFile.open(), temp, true, true);
            c.contentFile = IOUtils.toInputStreamSource(temp, null, null, new File(ws.getConfigManager().getCwd()));
            c.addTemp(temp);
            return characterize(ws, IOUtils.toInputStreamSource(temp, null, null, new File(ws.getConfigManager().getCwd())), session);
        }
        File fileSource = (File) c.contentFile.getSource();
        if ((!fileSource.exists())) {
            throw new NutsIllegalArgumentException("File does not exists " + fileSource);
        }
        if (fileSource.isDirectory()) {
            File ext = new File(fileSource, NutsConstants.NUTS_DESC_FILE_NAME);
            if (ext.exists()) {
                c.descriptor = ws.getParseManager().parseDescriptor(ext);
            } else {
                c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, session);
            }
            if (c.descriptor != null) {
                if ("zip".equals(c.descriptor.getExt())) {
                    File zipFilePath = new File(ws.getIOManager().resolvePath(fileSource.getPath() + ".zip"));
                    ZipUtils.zip(fileSource.getPath(), new ZipOptions(), zipFilePath.getPath());
                    c.contentFile = IOUtils.toInputStreamSource(zipFilePath, null, null, new File(ws.getConfigManager().getCwd()));
                    c.addTemp(zipFilePath);
                } else {
                    throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                }
            }
        } else if (fileSource.isFile()) {
            File ext = new File(ws.getIOManager().resolvePath(fileSource.getPath() + "." + NutsConstants.NUTS_DESC_FILE_NAME));
            if (ext.exists()) {
                c.descriptor = ws.getParseManager().parseDescriptor(ext);
            } else {
                c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, session);
            }
        } else {
            throw new NutsIllegalArgumentException("Path does not denote a valid file or folder " + c.contentFile);
        }

        return c;
    }

    public static NutsSession validateSession(NutsSession session, NutsWorkspace ws) {
        if (session == null) {
            session = ws.createSession();
        }
        return session;
    }

    public static NutsDescriptor resolveNutsDescriptorFromFileContent(NutsWorkspace ws, InputStreamSource localPath, NutsSession session) {
        session = validateSession(session, ws);
        if (localPath != null) {
            List<NutsDescriptorContentParserComponent> allParsers = ws.getExtensionManager().createAllSupported(NutsDescriptorContentParserComponent.class, ws);
            if (allParsers.size() > 0) {
                String fileExtension = FileUtils.getFileExtension(localPath.getName());
                NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(ws, session, localPath, fileExtension, null, null);
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (desc != null) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    public static NutsId applyNutsIdInheritance(NutsId child, NutsId parent) {
        if (parent != null) {
            boolean modified = false;
            String namespace = child.getNamespace();
            String group = child.getGroup();
            String name = child.getName();
            String version = child.getVersion().getValue();
            Map<String, String> face = child.getQueryMap();
            if (StringUtils.isEmpty(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (StringUtils.isEmpty(group)) {
                modified = true;
                group = parent.getGroup();
            }
            if (StringUtils.isEmpty(name)) {
                modified = true;
                name = parent.getName();
            }
            if (StringUtils.isEmpty(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getQueryMap();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                face.putAll(parentFaceMap);
            }
            if (modified) {
                return new DefaultNutsId(
                        namespace,
                        group,
                        name,
                        version,
                        face
                );
            }
        }
        return child;
    }

    public static boolean isDefaultScope(String s1) {
        return normalizeScope(s1).equals("compile");
    }

    public static boolean isDefaultOptional(String s1) {
        s1 = StringUtils.trim(s1);
        return s1.isEmpty() || s1.equals("false");
    }

    public static String normalizeScope(String s1) {
        if (s1 == null) {
            s1 = "";
        }
        s1 = s1.toLowerCase().trim();
        if (s1.isEmpty()) {
            s1 = "compile";
        }
        return s1;
    }

    public static String combineScopes(String s1, String s2) {
        s1 = normalizeScope(s1);
        s2 = normalizeScope(s2);
        switch (s1) {
            case "compile": {
                switch (s2) {
                    case "compile":
                        return "compile";
                    case "runtime":
                        return "runtime";
                    case "provided":
                        return "provided";
                    case "system":
                        return "system";
                    case "test":
                        return "test";
                    default:
                        return s2;
                }
            }
            case "runtime": {
                switch (s2) {
                    case "compile":
                        return "runtime";
                    case "runtime":
                        return "runtime";
                    case "provided":
                        return "provided";
                    case "system":
                        return "system";
                    case "test":
                        return "test";
                    default:
                        return "runtime";
                }
            }
            case "provided": {
                switch (s2) {
                    case "compile":
                        return "provided";
                    case "runtime":
                        return "provided";
                    case "provided":
                        return "provided";
                    case "system":
                        return "provided";
                    case "test":
                        return "provided";
                    default:
                        return "provided";
                }
            }
            case "system": {
                switch (s2) {
                    case "compile":
                        return "system";
                    case "runtime":
                        return "system";
                    case "provided":
                        return "system";
                    case "system":
                        return "system";
                    case "test":
                        return "system";
                    default:
                        return "system";
                }
            }
            case "test": {
                switch (s2) {
                    case "compile":
                        return "test";
                    case "runtime":
                        return "test";
                    case "provided":
                        return "provided";
                    case "system":
                        return "test";
                    case "test":
                        return "test";
                    default:
                        return "test";
                }
            }
            default: {
                return s1;
            }
        }
    }

    public static int getScopesPriority(String s1) {
        switch (normalizeScope(s1)) {
            case "compile":
                return 5;
            case "runtime":
                return 4;
            case "provided":
                return 3;
            case "system":
                return 2;
            case "test":
                return 1;
            default:
                return -1;
        }
    }

    public static int compareScopes(String s1, String s2) {
        int x = getScopesPriority(s1);
        int y = getScopesPriority(s2);
        int c = Integer.compare(x, y);
        if (c != 0) {
            return x;
        }
        if (x == -1) {
            return normalizeScope(s1).compareTo(normalizeScope(s2));
        }
        return 0;
    }

    public static void checkReadOnly(NutsWorkspace ws) {
        if (ws.getConfigManager().isReadOnly()) {
            throw new NutsReadOnlyException(ws.getConfigManager().getWorkspaceLocation());
        }
    }

    public static void copy(File from, File to, boolean mkdirs) throws RuntimeIOException {
        try {
            IOUtils.copy(from, to, mkdirs);
        } catch (RuntimeIOException ex) {
            throw new NutsIOException(ex.getCause());
        }
    }

    public static void copy(String from, File to, boolean mkdirs) throws NutsIOException {
        try {
            IOUtils.copy(from, to, mkdirs);
        } catch (RuntimeIOException ex) {
            throw new NutsIOException(ex.getCause());
        }
    }

    public static void copy(InputStream from, File to, boolean mkdirs, boolean closeInput) throws RuntimeIOException {
        try {
            IOUtils.copy(from, to, mkdirs, closeInput);
        } catch (RuntimeIOException ex) {
            throw new NutsIOException(ex.getCause());
        }
    }
}
