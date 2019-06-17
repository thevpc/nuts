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
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.filters.dependency.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {

    private static final Logger LOG = Logger.getLogger(CoreNutsUtils.class.getName());
//    public static final IntegerParserConfig INTEGER_LENIENT_NULL = IntegerParserConfig.LENIENT_F.setInvalidValue(null);
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}*-]+)://)?([a-zA-Z0-9_.${}*-]+)(:([a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    public static final NutsDependencyFilter OPTIONAL = new OptionalNutsDependencyFilter(true);
    public static final NutsDependencyFilter NON_OPTIONAL = new OptionalNutsDependencyFilter(false);
    public static final NutsDependencyFilter SCOPE_RUN = CoreFilterUtils.And(new ScopeNutsDependencyFilter("compile,system,runtime"), NON_OPTIONAL);
    public static final NutsDependencyFilter SCOPE_TEST = CoreFilterUtils.And(new ScopeNutsDependencyFilter("compile,system,runtime,test"), NON_OPTIONAL);

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
                x += weight(parseNutsDependency(null, s));
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
    public static NutsDescriptor SAMPLE_NUTS_DESCRIPTOR
            = new DefaultNutsDescriptorBuilder()
                    .setId(new DefaultNutsId(null, "group", "name", "version", (String) null))
                    .setAlternative("suse")
                    .setName("Application Full Name")
                    .setDescription("Application Description")
                    .setExecutable(true)
                    .setPackaging("jar")
                    //                    .setExt("exe")
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

    private static final Map<String, String> _QUERY_EMPTY_ENV = new HashMap<>();
    public static final Map<String, String> QUERY_EMPTY_ENV = Collections.unmodifiableMap(_QUERY_EMPTY_ENV);

    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.QueryKeys.ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QueryKeys.OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QueryKeys.OSDIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QueryKeys.PLATFORM, null);
    }

    public static NutsId findNutsIdBySimpleName(NutsId id, Collection<NutsId> all) {
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

    public static boolean isEffectiveValue(String value) {
        return (!CoreStringUtils.isBlank(value) && !CoreStringUtils.containsVars(value));
    }

    public static boolean isEffectiveId(NutsId id) {
        return (isEffectiveValue(id.getGroup()) && isEffectiveValue(id.getName()) && isEffectiveValue(id.getVersion().getValue()));
    }

    public static boolean containsVars(NutsId id) {
        return (CoreStringUtils.containsVars(id.getGroup()) && CoreStringUtils.containsVars(id.getName()) && CoreStringUtils.containsVars(id.getVersion().getValue()));
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

    public static String[] applyStringProperties(String[] child, Function<String, String> properties) {
        String[] vals = new String[child.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = applyStringProperties(child[i], properties);
        }
        return vals;
    }

    public static Map<String, String> applyMapProperties(Map<String, String> child, Function<String, String> properties) {
        Map<String, String> m2 = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : child.entrySet()) {
            m2.put(applyStringProperties(entry.getKey(), properties), applyStringProperties(entry.getValue(), properties));
        }
        return m2;
    }

    public static NutsVersion applyStringProperties(NutsVersion child, Function<String, String> properties) {
        if (child == null) {
            return child;
        }
        String s = child.getValue();
        if (CoreStringUtils.isBlank(s)) {
            return DefaultNutsVersion.EMPTY;
        }
        String s2 = applyStringProperties(s, properties);
        if (!CoreStringUtils.trim(s2).equals(s)) {
            return DefaultNutsVersion.valueOf(s2);
        }
        return child;
    }

    public static String applyStringProperties(String child, Function<String, String> properties) {
        if (CoreStringUtils.isBlank(child)) {
            return null;
        }
//        return applyStringProperties(child, properties == null ? null : new StringConverterAdapter(properties));
        return CoreStringUtils.replaceDollarPlaceHolders(child, properties);
    }

//    public static String applyStringProperties(String child, Function<String,String> properties) {
//        if (CoreStringUtils.isEmpty(child)) {
//            return null;
//        }
//        return CoreStringUtils.replaceDollarPlaceHolders(child, properties);
//    }
    public static String applyStringInheritance(String child, String parent) {
        child = CoreStringUtils.trimToNull(child);
        parent = CoreStringUtils.trimToNull(parent);
        if (child == null) {
            return parent;
        }
        return child;
    }

    public static NutsDependency parseNutsDependency(NutsWorkspace ws, String nutFormat) {
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
            Map<String, String> queryMap = CoreStringUtils.parseMap(face, "&");
            for (String s : queryMap.keySet()) {
                if (!DEPENDENCY_SUPPORTED_PARAMS.contains(s)) {
                    throw new NutsIllegalArgumentException(ws, "Unsupported parameter " + CoreStringUtils.simpleQuote(s, false, "") + " in " + nutFormat);
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

    public static NutsId applyNutsIdInheritance(NutsId child, NutsId parent) {
        if (parent != null) {
            boolean modified = false;
            String namespace = child.getNamespace();
            String group = child.getGroup();
            String name = child.getName();
            String version = child.getVersion().getValue();
            Map<String, String> face = child.getQueryMap();
            if (CoreStringUtils.isBlank(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (CoreStringUtils.isBlank(group)) {
                modified = true;
                group = parent.getGroup();
            }
            if (CoreStringUtils.isBlank(name)) {
                modified = true;
                name = parent.getName();
            }
            if (CoreStringUtils.isBlank(version)) {
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
        s1 = CoreStringUtils.trim(s1);
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

//    public static String combineScopes(String s1, String s2) {
//        s1 = normalizeScope(s1);
//        s2 = normalizeScope(s2);
//        switch (s1) {
//            case "compile": {
//                switch (s2) {
//                    case "compile":
//                        return "compile";
//                    case "runtime":
//                        return "runtime";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "system";
//                    case "test":
//                        return "test";
//                    default:
//                        return s2;
//                }
//            }
//            case "runtime": {
//                switch (s2) {
//                    case "compile":
//                        return "runtime";
//                    case "runtime":
//                        return "runtime";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "system";
//                    case "test":
//                        return "test";
//                    default:
//                        return "runtime";
//                }
//            }
//            case "provided": {
//                switch (s2) {
//                    case "compile":
//                        return "provided";
//                    case "runtime":
//                        return "provided";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "provided";
//                    case "test":
//                        return "provided";
//                    default:
//                        return "provided";
//                }
//            }
//            case "system": {
//                switch (s2) {
//                    case "compile":
//                        return "system";
//                    case "runtime":
//                        return "system";
//                    case "provided":
//                        return "system";
//                    case "system":
//                        return "system";
//                    case "test":
//                        return "system";
//                    default:
//                        return "system";
//                }
//            }
//            case "test": {
//                switch (s2) {
//                    case "compile":
//                        return "test";
//                    case "runtime":
//                        return "test";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "test";
//                    case "test":
//                        return "test";
//                    default:
//                        return "test";
//                }
//            }
//            default: {
//                return s1;
//            }
//        }
//    }
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

    public static boolean isValidIdentifier(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        char[] c = s.toCharArray();
        if (!Character.isJavaIdentifierStart(c[0])) {
            return false;
        }
        for (int i = 1; i < c.length; i++) {
            if (!Character.isJavaIdentifierPart(c[i]) && c[i] != '-') {
                return false;
            }
        }

        return true;
    }

    public static NutsRepositoryRef optionsToRef(NutsCreateRepositoryOptions options) {
        return new NutsRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployPriority(options.getDeployOrder());
    }

    public static NutsCreateRepositoryOptions refToOptions(NutsRepositoryRef ref) {
        return new NutsCreateRepositoryOptions()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployOrder(ref.getDeployOrder())
                .setTemporary(false);
    }

    public static NutsCreateRepositoryOptions defToOptions(NutsRepositoryDefinition def) {
        NutsCreateRepositoryOptions o = new NutsCreateRepositoryOptions();
        o.setName(def.getName());
        o.setCreate(def.isCreate());
        o.setFailSafe(def.isFailSafe());
        o.setProxy(def.isProxy());
        o.setTemporary(def.isTemporary());
        o.setDeployOrder(def.getDeployOrder());
        if (def.isReference()) {
            o.setLocation(def.getLocation());
        } else {
            o.setLocation(def.getName());
            o.setConfig(new NutsRepositoryConfig()
                    .setName(def.getName())
                    .setType(def.getType())
                    .setLocation(def.getLocation())
                    .setStoreLocationStrategy(def.getStoreLocationStrategy())
            );
        }
        return o;
    }

    public static void wconfigToBconfig(NutsWorkspaceConfig wconfig, NutsBootConfig bconfig) {
        bconfig.setStoreLocation(NutsStoreLocation.PROGRAMS, wconfig.getProgramsStoreLocation());
        bconfig.setStoreLocation(NutsStoreLocation.CONFIG, wconfig.getConfigStoreLocation());
        bconfig.setStoreLocation(NutsStoreLocation.VAR, wconfig.getVarStoreLocation());
        bconfig.setStoreLocation(NutsStoreLocation.LOG, wconfig.getLogStoreLocation());
        bconfig.setStoreLocation(NutsStoreLocation.TEMP, wconfig.getTempStoreLocation());
        bconfig.setStoreLocation(NutsStoreLocation.CACHE, wconfig.getCacheStoreLocation());
        bconfig.setStoreLocation(NutsStoreLocation.LIB, wconfig.getLibStoreLocation());

        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.PROGRAMS, wconfig.getProgramsSystemHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.CONFIG, wconfig.getConfigSystemHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.VAR, wconfig.getVarSystemHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.LOG, wconfig.getLogSystemHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.TEMP, wconfig.getTempSystemHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.CACHE, wconfig.getCacheSystemHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.LIB, wconfig.getLibSystemHome());

        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.PROGRAMS, wconfig.getProgramsWindowsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.CONFIG, wconfig.getConfigWindowsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.VAR, wconfig.getVarWindowsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.LOG, wconfig.getLogWindowsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.TEMP, wconfig.getTempWindowsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.CACHE, wconfig.getCacheWindowsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.LIB, wconfig.getLibWindowsHome());

        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.PROGRAMS, wconfig.getProgramsMacOsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.CONFIG, wconfig.getConfigMacOsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.VAR, wconfig.getVarMacOsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.LOG, wconfig.getLogMacOsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.TEMP, wconfig.getTempMacOsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.CACHE, wconfig.getCacheMacOsHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.LIB, wconfig.getLibMacOsHome());

        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.PROGRAMS, wconfig.getProgramsLinuxHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.CONFIG, wconfig.getConfigLinuxHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.VAR, wconfig.getVarLinuxHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.LOG, wconfig.getLogLinuxHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.TEMP, wconfig.getTempLinuxHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.CACHE, wconfig.getCacheLinuxHome());
        bconfig.setHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.LIB, wconfig.getLibLinuxHome());
    }

    public static void optionsToWconfig(NutsWorkspaceOptions options, NutsWorkspaceConfig wconfig) {
        wconfig.setProgramsStoreLocation(options.getStoreLocation(NutsStoreLocation.PROGRAMS));
        wconfig.setConfigStoreLocation(options.getStoreLocation(NutsStoreLocation.CONFIG));
        wconfig.setVarStoreLocation(options.getStoreLocation(NutsStoreLocation.VAR));
        wconfig.setLogStoreLocation(options.getStoreLocation(NutsStoreLocation.LOG));
        wconfig.setTempStoreLocation(options.getStoreLocation(NutsStoreLocation.TEMP));
        wconfig.setCacheStoreLocation(options.getStoreLocation(NutsStoreLocation.CACHE));
        wconfig.setLibStoreLocation(options.getStoreLocation(NutsStoreLocation.LIB));

        wconfig.setProgramsSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.PROGRAMS));
        wconfig.setConfigSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.CONFIG));
        wconfig.setVarSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.VAR));
        wconfig.setLogSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.LOG));
        wconfig.setTempSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.TEMP));
        wconfig.setCacheSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.CACHE));
        wconfig.setLibSystemHome(options.getHomeLocation(NutsStoreLocationLayout.SYSTEM, NutsStoreLocation.LIB));

        wconfig.setProgramsWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.PROGRAMS));
        wconfig.setConfigWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.CONFIG));
        wconfig.setVarWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.VAR));
        wconfig.setLogWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.LOG));
        wconfig.setTempWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.TEMP));
        wconfig.setCacheWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.CACHE));
        wconfig.setLibWindowsHome(options.getHomeLocation(NutsStoreLocationLayout.WINDOWS, NutsStoreLocation.LIB));

        wconfig.setProgramsMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.PROGRAMS));
        wconfig.setConfigMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.CONFIG));
        wconfig.setVarMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.VAR));
        wconfig.setLogMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.LOG));
        wconfig.setTempMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.TEMP));
        wconfig.setCacheMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.CACHE));
        wconfig.setLibMacOsHome(options.getHomeLocation(NutsStoreLocationLayout.MACOS, NutsStoreLocation.LIB));

        wconfig.setProgramsLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.PROGRAMS));
        wconfig.setConfigLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.CONFIG));
        wconfig.setVarLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.VAR));
        wconfig.setLogLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.LOG));
        wconfig.setTempLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.TEMP));
        wconfig.setCacheLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.CACHE));
        wconfig.setLibLinuxHome(options.getHomeLocation(NutsStoreLocationLayout.LINUX, NutsStoreLocation.LIB));

    }

    public static void traceMessage(NutsFetchStrategy fetchMode, NutsId id, TraceResult tracePhase, String message, long startTime) {
        String timeMessage = "";
        if (startTime != 0) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                timeMessage = " (" + time + "ms)";
            }
        }
        String tracePhaseString = "";
        switch (tracePhase) {
            case ERROR: {
                tracePhaseString = "[ERROR  ] ";
                break;
            }
            case SUCCESS: {
                tracePhaseString = "[SUCCESS] ";
                break;
            }
            case START: {
                tracePhaseString = "[START  ] ";
                break;
            }
        }
        String fetchString = fetchString = "[" + CoreStringUtils.alignLeft(fetchMode.name(), 7) + "] ";
        LOG.log(Level.FINEST, tracePhaseString + fetchString
                + CoreStringUtils.alignLeft(message, 18) + " " + id + timeMessage);
    }

    public String tracePlainNutsId(NutsWorkspace ws, NutsId id) {
        NutsIdFormat idFormat = ws.format().id();
        return idFormat.set(id).format();
    }

    public static String tracePlainNutsDefinition(NutsWorkspace ws, NutsDefinition id) {
        NutsIdFormat idFormat = ws.format().id();
        return idFormat.set(id.getId()).format();
    }

    public static Object tracePropsNutsDefinition(NutsWorkspace ws, NutsDefinition id) {
        NutsIdFormat idFormat = ws.format().id();
        return idFormat.set(id.getId()).toString();
    }

    public static Map<String, Object> traceJsonNutsDefinition(NutsWorkspace ws, NutsDefinition def) {
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("id", tracePlainNutsDefinition(ws, def));
        if (def.getContent() != null) {
            if (def.getContent().getPath() != null) {
                x.put("path", def.getContent().getPath().toString());
            }
            x.put("cached", def.getContent().isCached());
            x.put("temporary", def.getContent().isTemporary());
        }
        if (def.getInstallation() != null) {
            if (def.getInstallation().getInstallFolder() != null) {
                x.put("install-folder", def.getInstallation().getInstallFolder().toString());
            }
            x.put("installed", def.getInstallation().isInstalled());
            x.put("just-installed", def.getInstallation().isJustInstalled());
        }
        if (def.getRepositoryName() != null) {
            x.put("repository-name", def.getRepositoryName());
        }
        if (def.getRepositoryUuid() != null) {
            x.put("repository-uuid", def.getRepositoryUuid());
        }
        if (def.getDescriptor() != null) {
            x.put("descriptor", ws.format().descriptor().set(def.getDescriptor()).format());
            x.put("effective-descriptor", ws.format().descriptor().set(
                    NutsWorkspaceUtils.getEffectiveDescriptor(ws, def)
            ).format());
        }
        return x;
    }

    public static boolean isIncludesHelpOption(String[] cmd) {
        if (cmd != null) {
            for (String c : cmd) {
                if (!c.startsWith("-")) {
                    break;
                }
                if ("--help".equals(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static NutsIncrementalFormat getValidOutputFormat(NutsWorkspace ws, NutsSession session) {
        NutsIncrementalFormat f = session.getIncrementalOutput();
        if (f == null) {
            return ws.format().iter().session(session);
        }
        return f;
    }

    public static void traceMessage(Logger log, String name, NutsRepositorySession session, NutsId id, TraceResult tracePhase, String title, long startTime) {
        String timeMessage = "";
        if (startTime != 0) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                timeMessage = " (" + time + "ms)";
            }
        }
        String tracePhaseString = "";
        switch (tracePhase) {
            case ERROR: {
                tracePhaseString = "[ERROR  ] ";
                break;
            }
            case SUCCESS: {
                tracePhaseString = "[SUCCESS] ";
                break;
            }
            case START: {
                tracePhaseString = "[START  ] ";
                break;
            }
        }
        String fetchString = fetchString = "[" + CoreStringUtils.alignLeft(session.getFetchMode().name(), 7) + "] ";
        log.log(Level.FINEST, "{0}{1}{2} {3} {4}{5}", new Object[]{tracePhaseString, fetchString, CoreStringUtils.alignLeft(title, 18), CoreStringUtils.alignLeft(name, 20), id == null ? "" : id.toString(), timeMessage});
    }

    public static NutsOutputFormat readOptionOutputFormat(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        switch (a.getStringKey()) {
            case "--output-format": {
                a = cmdLine.nextString();
                return CoreCommonUtils.parseEnumString(a.getStringValue(), NutsOutputFormat.class, false);
            }
            case "--json": {
                return (NutsOutputFormat.JSON);
            }
            case "--props": {
                return (NutsOutputFormat.PROPS);
            }
            case "--table": {
                return (NutsOutputFormat.TABLE);
            }
            case "--tree": {
                return (NutsOutputFormat.TREE);
            }
            case "--plain": {
                return (NutsOutputFormat.PLAIN);
            }
        }
        return null;
    }

    public static String trimToNullAlternative(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        return (s.isEmpty() || NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE.equalsIgnoreCase(s)) ? null : s;
    }

    public static boolean matchesSimpleNameStaticVersion(NutsId id, NutsId pattern) {
        if (pattern == null) {
            return id == null;
        }
        if (id == null) {
            return false;
        }
        if (pattern.getVersion().isBlank()) {
            return pattern.getSimpleName().equals(id.getSimpleName());
        }
        return pattern.getLongName().equals(id.getLongName());
    }
}
