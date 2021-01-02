/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.depracated.SimpleNutsDescriptorComparator;
import net.thevpc.nuts.runtime.standalone.main.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.common.TraceResult;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {
    public static final String REMOTE_MAVEN_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master";

    /**
     * vpc-public-nuts git repository. contains mainly nuts author non java
     * artifacts.
     */
    public static final String REMOTE_NUTS_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master";

    /**
     * vpc-public-nuts local repository at ${home.config}/.vpc-public-nuts
     */
    public static final String LOCAL_NUTS_FOLDER = "${home.config}/.vpc-public-nuts";

    public static final int DEFAULT_UUID_LENGTH = 25;
    public static final int DEFAULT_DATE_TIME_FORMATTER_LENGTH = 23;
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    public static final String[] COLOR_NAMES = new TreeSet<String>(Arrays.asList(
            "Maroon", "Brown", "Olive", "Teal", "Navy", "Black", "Red", "Orange", "Yellow", "Lime", "Green", "Cyan", "Blue", "Purple", "Magenta", "Grey", "Pink",
            "Apricot", "Beige", "Mint", "Lavender", "White", "Turquoise", "Aqua", "Aquamarine", "Gold", "Coral", "Tomato", "Firebrick",
            "Crimson", "Salmon", "Moccasin", "PeachPuff", "Khaki", "Cornsilk", "Bisque", "Wheat", "Tan", "Peru", "Chocolate", "Sienna", "Snow", "Azure", "Ivory", "Linen", "Silver", "Gray"
    )).toArray(new String[0]);
    public static final int LOCK_TIME = 3;
    public static final TimeUnit LOCK_TIME_UNIT = TimeUnit.SECONDS;
    public static final NutsDefaultThreadFactory nutsDefaultThreadFactory = new NutsDefaultThreadFactory("nuts-pool", true);
    private static final Map<String, String> _QUERY_EMPTY_ENV = new HashMap<>();
    public static final Map<String, String> QUERY_EMPTY_ENV = Collections.unmodifiableMap(_QUERY_EMPTY_ENV);
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
    //    public static NutsId SAMPLE_NUTS_ID = new DefaultNutsId("namespace", "group", "name", "version", "param='true'");
//    public static NutsDescriptor SAMPLE_NUTS_DESCRIPTOR
//            = new DefaultNutsDescriptorBuilder()
//            .setId(new DefaultNutsId(null, "group", "name", "version", (String) null))
////                    .setAlternative("suse")
//            .setName("Application Full Name")
//            .setDescription("Application Description")
//            .setExecutable(true)
//            .setPackaging("jar")
//            //                    .setExt("exe")
//            .setArch(new String[]{"64bit"})
//            .setOs(new String[]{"linux#4.6"})
//            .setOsdist(new String[]{"opensuse#42"})
//            .setPlatform(new String[]{"java#1.8"})
//            .setExecutor(new DefaultNutsArtifactCall(
//                    new DefaultNutsId(null, null, "java", "1.8", (String) null),
//                    new String[]{"-jar"}
//            ))
//            .setInstaller(new DefaultNutsArtifactCall(
//                    new DefaultNutsId(null, null, "java", "1.8", (String) null),
//                    new String[]{"-jar"}
//            ))
//            .setLocations(new NutsIdLocation[]{
//                    new DefaultNutsIdLocation("http://server/somelink", null, null)
//            })
//            .setDependencies(
//                    new NutsDependency[]{
//                            new DefaultNutsDependencyBuilder()
//                                    .setNamespace("namespace")
//                                    .setGroupId("group")
//                                    .setArtifactId("name")
//                                    .setVersion("version")
//                                    .setOptional("false").build()
//                    }
//            )
//            .build();

    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OSDIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.PLATFORM, null);
    }

    public static String randomColorName() {
        return COLOR_NAMES[(int) (Math.random() * COLOR_NAMES.length)];
    }

    public static NutsId findNutsIdBySimpleName(NutsId id, Collection<NutsId> all) {
        if (all != null) {
            for (NutsId nutsId : all) {
                if (nutsId != null) {
                    if (nutsId.equalsShortName(id)) {
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
        return (isEffectiveValue(id.getGroupId()) && isEffectiveValue(id.getArtifactId()) && isEffectiveValue(id.getVersion().getValue()));
    }

    public static boolean containsVars(NutsId id) {
        return (CoreStringUtils.containsVars(id.getGroupId()) && CoreStringUtils.containsVars(id.getArtifactId()) && CoreStringUtils.containsVars(id.getVersion().getValue()));
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

    public static <T> T simplify(T any) {
        if (any == null) {
            return null;
        }
        if (any instanceof Simplifiable) {
            return ((Simplifiable<T>) any).simplify();
        }
        return any;
    }

    public static <T extends NutsFilter> T simplifyFilterOr(NutsWorkspace ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return (T) ws.filters().always(cls);
        }
        if (all.length == 1) {
            return (T) all[0].simplify();
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        boolean someFalse = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return (T) ws.filters().always(cls);
                    }
                    case FALSE: {
                        someFalse = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            if (someFalse) {
                return (T) ws.filters().never(cls);
            }
            return (T) ws.filters().always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return (T) ws.filters().any(cls,all2.toArray((T[]) Array.newInstance(cls,0)));
    }

    public static <T extends NutsFilter> T simplifyFilterAnd(NutsWorkspace ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return (T) ws.filters().always(cls);
        }
        if (all.length == 1) {
            return (T) all[0].simplify();
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case FALSE: {
                        return (T) ws.filters().never(cls);
                    }
                    case TRUE: {
                        updates = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            return (T) ws.filters().always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return (T) ws.filters().all(cls,all2.toArray((T[]) Array.newInstance(cls,0)));
    }

    public static <T extends NutsFilter> T simplifyFilterNone(NutsWorkspace ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return (T) ws.filters().always(cls);
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return (T) ws.filters().never(cls);
                    }
                    case FALSE: {
                        updates = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            return (T) ws.filters().always(cls);
        }
        if (!updates) {
            return base;
        }
        return (T) ws.filters().none(cls,all2.toArray((T[]) Array.newInstance(cls,0)));
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

    public static <T extends NutsFilter> T[] simplifyAndShrinkFilters(Class<T> cls, Predicate<T> onRemove, T... any) {
        List<T> all = new ArrayList<>();
        boolean updates = false;
        for (T t : any) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                if (onRemove != null && onRemove.test(t2)) {
                    updates = true;
                } else {
                    if (t2 != t) {
                        updates = true;
                    }
                    all.add(t2);
                }
            } else {
                updates = true;
            }
        }
        if (!updates) {
            return null;
        }
        return all.toArray((T[]) Array.newInstance(cls, 0));
    }

    public static NutsId applyNutsIdInheritance(NutsId child, NutsId parent,NutsWorkspace ws) {
        if (parent != null) {
            boolean modified = false;
            String namespace = child.getNamespace();
            String group = child.getGroupId();
            String name = child.getArtifactId();
            String version = child.getVersion().getValue();
            Map<String, String> props = child.getProperties();
            if (CoreStringUtils.isBlank(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (CoreStringUtils.isBlank(group)) {
                modified = true;
                group = parent.getGroupId();
            }
            if (CoreStringUtils.isBlank(name)) {
                modified = true;
                name = parent.getArtifactId();
            }
            if (CoreStringUtils.isBlank(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getProperties();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                props.putAll(parentFaceMap);
            }
            if (modified) {
                return ws.id().builder().setNamespace(namespace)
                        .setGroupId(group)
                        .setArtifactId(name)
                        .setVersion(version)
                        .setProperties(props).build()
                ;
            }
        }
        return child;
    }

    public static boolean isDefaultOptional(String s1) {
        s1 = CoreStringUtils.trim(s1);
        return s1.isEmpty() || s1.equals("false");
    }

//    public static boolean isDefaultAlternative(String s1) {
//        s1 = CoreStringUtils.trim(s1);
//        return s1.isEmpty() || s1.equals(NutsConstants.IdProperties.ALTERNATIVE_DEFAULT_VALUE);
//    }

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

    public static NutsRepositoryRef optionsToRef(NutsAddRepositoryOptions options) {
        return new NutsRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployOrder(options.getDeployOrder());
    }

    public static NutsAddRepositoryOptions refToOptions(NutsRepositoryRef ref) {
        return new NutsAddRepositoryOptions()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployOrder(ref.getDeployOrder())
                .setTemporary(false);
    }

    public static String resolveRepositoryEffectiveUrlFromUrl(String location) {
        Matcher matcher = Pattern.compile("(?<p>[a-zA-Z+])://(?<r>.*>)").matcher(location);
        if (matcher.find()) {
            String protocol = matcher.group("p");
            String rest = matcher.group("r");
            boolean http = false;
            boolean https = false;
            boolean nuts = false;
            boolean nutsrv = false;
            boolean file = false;
            boolean mvn = false;
            for (String s : protocol.split("[+]")) {
                switch (s) {
                    case "http": {
                        http = true;
                        break;
                    }
                    case "https": {
                        https = true;
                        break;
                    }
                    case "nuts": {
                        nuts = true;
                        break;
                    }
                    case "nutsrv": {
                        nutsrv = true;
                        break;
                    }
                    case "file": {
                        file = true;
                        break;
                    }
                    case "mvn": {
                        mvn = true;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unsupported protocol " + s + " in " + location);
                    }
                }
            }
            if (((nuts ? 1 : 0) + (mvn ? 1 : 0) > 0) || ((nutsrv ? 1 : 0) + (mvn ? 1 : 0) > 0)) {
                throw new IllegalArgumentException("Unsupported protocol " + protocol + " in " + location);
            }
            if (https) {
                return "https://" + rest;
            }
            if (http) {
                return "http://" + rest;
            }
            if (file) {
                return "file:" + rest;
            }
            return location;//"file:"+rest;
        }
//        try {
        return location;//new File(location).toURI().toURL().toString();
//        } catch (MalformedURLException e) {
//            throw new IllegalArgumentException(e);
//        }
    }

    public static String resolveRepositoryTypeFromUrl(String location) {
        Matcher matcher = Pattern.compile("(?<p>[a-zA-Z+])://(?<r>.*>)").matcher(location);
        if (matcher.find()) {
            String protocol = matcher.group("p");
            String rest = matcher.group("r");
            boolean http = false;
            boolean https = false;
            boolean nuts = false;
            boolean nutsrv = false;
            boolean file = false;
            boolean mvn = false;
            for (String s : protocol.split("[+]")) {
                switch (s) {
                    case "http": {
                        http = true;
                        break;
                    }
                    case "https": {
                        https = true;
                        break;
                    }
                    case "nuts": {
                        nuts = true;
                        break;
                    }
                    case "nutsrv": {
                        nutsrv = true;
                        break;
                    }
                    case "file": {
                        file = true;
                        break;
                    }
                    case "mvn": {
                        mvn = true;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unsupported protocol " + s + " in " + location);
                    }
                }
            }
            if (((nuts ? 1 : 0) + (mvn ? 1 : 0) > 0) || ((nutsrv ? 1 : 0) + (mvn ? 1 : 0) > 0)) {
                throw new IllegalArgumentException("Unsupported protocol " + protocol + " in " + location);
            }
            if (nutsrv) {
                return NutsConstants.RepoTypes.NUTS_SERVER;
            }
            if (nuts) {
                return NutsConstants.RepoTypes.NUTS;
            }
            if (mvn) {
                return NutsConstants.RepoTypes.MAVEN;
            }
        }
        return NutsConstants.RepoTypes.MAVEN;
    }

    public static NutsAddRepositoryOptions defToOptions(NutsRepositoryDefinition def) {
        NutsAddRepositoryOptions o = new NutsAddRepositoryOptions();
        String type = def.getType();
        String location = def.getLocation();
        if (location != null) {
            if (CoreStringUtils.isBlank(type)) {
                type = resolveRepositoryTypeFromUrl(location);
            }
            location = resolveRepositoryEffectiveUrlFromUrl(location);
        }
        o.setName(def.getName());
        o.setCreate(def.isCreate());
        o.setFailSafe(def.isFailSafe());
        o.setProxy(def.isProxy());
        o.setTemporary(def.isTemporary());
        o.setDeployOrder(def.getDeployOrder());
        o.setEnabled(true); ///????
        o.setSession(def.getSession()); ///????
        if (def.isReference()) {
            o.setLocation(location);
        } else {
            o.setLocation(def.getName());
            o.setConfig(new NutsRepositoryConfig()
                    .setName(def.getName())
                    .setType(type)
                    .setLocation(location)
                    .setStoreLocationStrategy(def.getStoreLocationStrategy())
            );
        }
        return o;
    }

    public static NutsSession silent(NutsSession session) {
        return session.isTrace() ? session.copy().setTrace(false) : session;
    }

    public static String tracePlainNutsDefinition(NutsWorkspace ws, NutsDefinition id) {
        NutsIdFormat idFormat = ws.id().formatter();
        return idFormat.value(id.getId()).format();
    }

//    public static void wconfigToBconfig(NutsWorkspaceConfig wconfig, NutsBootConfig bconfig) {
//        bconfig.setStoreLocations(new NutsStoreLocationsMap(wconfig.getStoreLocations()).toMap());
//        bconfig.setHomeLocations(new NutsHomeLocationsMap(wconfig.getHomeLocations()).toMap());
//    }

//    public static void optionsToWconfig(NutsWorkspaceOptions options, NutsWorkspaceConfig wconfig) {
//        wconfig.setStoreLocations(new NutsStoreLocationsMap(wconfig.getStoreLocations()).toMapOrNull());
//        wconfig.setHomeLocations(new NutsHomeLocationsMap(wconfig.getHomeLocations()).toMapOrNull());
//    }

    public static Object tracePropsNutsDefinition(NutsWorkspace ws, NutsDefinition id) {
        NutsIdFormat idFormat = ws.id().formatter();
        return idFormat.value(id.getId()).toString();
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
        if (def.getInstallInformation() != null) {
            if (def.getInstallInformation().getInstallFolder() != null) {
                x.put("install-folder", def.getInstallInformation().getInstallFolder().toString());
            }
            x.put("install-status", def.getInstallInformation().getInstallStatus().stream().map(NutsInstallStatus::id).collect(Collectors.joining(", "))
            );
            x.put("was-installed", def.getInstallInformation().isWasInstalled());
            x.put("was-required", def.getInstallInformation().isWasRequired());
        }
        if (def.getRepositoryName() != null) {
            x.put("repository-name", def.getRepositoryName());
        }
        if (def.getRepositoryUuid() != null) {
            x.put("repository-uuid", def.getRepositoryUuid());
        }
        if (def.getDescriptor() != null) {
            x.put("descriptor", ws.descriptor().formatter().value(def.getDescriptor()).format());
            x.put("effective-descriptor", ws.descriptor().formatter(
                    NutsWorkspaceUtils.of(ws).getEffectiveDescriptor(def)
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

    public static NutsIterableOutput getValidOutputFormat(NutsSession session) {
        NutsIterableOutput f = session.getIterableOutput();
        if (f == null) {
            return session.getWorkspace().formats().iter().setSession(session);
        }
        return f;
    }

    public static void traceMessage(NutsLogger log, Level lvl, String name, NutsSession session, NutsFetchMode fetchMode, NutsId id, TraceResult tracePhase, String title, long startTime, String extraMsg) {
        if (!log.isLoggable(lvl)) {
            return;
        }
        if (extraMsg == null) {
            extraMsg = "";
        } else {
            extraMsg = " : " + extraMsg;
        }
        long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
        String modeString = CoreStringUtils.alignLeft(fetchMode.id(), 7);
        log.with().session(session).level(lvl).verb(tracePhase.name()).time(time).formatted()
                .log("[{0}] {1} {2} {3} {4}",
                        modeString,
                        CoreStringUtils.alignLeft(name, 20),
                        CoreStringUtils.alignLeft(title, 18),
                        (id == null ? "" : id),
                        extraMsg);
    }

    public static NutsContentType readOptionOutputFormat(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        switch (a.getStringKey()) {
            case "--output-format": {
                a = cmdLine.nextString();
                return CoreCommonUtils.parseEnumString(a.getStringValue(), NutsContentType.class, false);
            }
            case "--json": {
                a = cmdLine.nextString();
                return (NutsContentType.JSON);
            }
            case "--props": {
                a = cmdLine.nextString();
                return (NutsContentType.PROPS);
            }
            case "--table": {
                a = cmdLine.nextString();
                return (NutsContentType.TABLE);
            }
            case "--tree": {
                a = cmdLine.nextString();
                return (NutsContentType.TREE);
            }
            case "--plain": {
                a = cmdLine.nextString();
                return (NutsContentType.PLAIN);
            }
        }
        return null;
    }

    public static boolean matchesSimpleNameStaticVersion(NutsId id, NutsId pattern) {
        if (pattern == null) {
            return id == null;
        }
        if (id == null) {
            return false;
        }
        if (pattern.getVersion().isBlank()) {
            return pattern.getShortName().equals(id.getShortName());
        }
        return pattern.getLongName().equals(id.getLongName());
    }

    public static String[] nullArray_Locations(String[] a) {
        return nullArray(a, NutsStoreLocation.values().length);
    }

//    public static String trimToNullAlternative(String s) {
//        if (s == null) {
//            return null;
//        }
//        s = s.trim();
//        return (s.isEmpty() || NutsConstants.IdProperties.ALTERNATIVE_DEFAULT_VALUE.equalsIgnoreCase(s)) ? null : s;
//    }

    public static String[] nullArray_LocationsAndOses(String[] a) {
        return nullArray(a, NutsStoreLocation.values().length * NutsOsFamily.values().length);
    }

    public static String[] nullArray(String[] a, int size) {
        if (a == null) {
            return null;
        }
        boolean ok = false;
        for (String string : a) {
            if (string != null) {
                ok = true;
                break;
            }
        }
        if (ok) {
            if (a.length == size) {
                return a;
            }
            String[] aa = new String[size];
            System.arraycopy(a, 0, aa, 0, size);
            return aa;
        }
        return null;
    }

    public static String getArrItem(String[] a, int index) {
        return (a == null || a.length <= index) ? null : a[index];
    }

    public static String[] nonNullArray_Locations(String[] a) {
        return nonNullArray(a, NutsStoreLocation.values().length);
    }

    public static String[] nonNullArray_LocationsAndOses(String[] a) {
        return nonNullArray(a, NutsStoreLocation.values().length * NutsOsFamily.values().length);
    }

    public static String[] nonNullArray(String[] a, int size) {
        if (a == null) {
            return new String[size];
        }
        if (a.length == size) {
            return a;
        }
        String[] aa = new String[size];
        System.arraycopy(a, 0, aa, 0, size);
        return aa;
    }

    public static int getApiVersionOrdinalNumber(String s) {
        try {
            int a = 0;
            for (String part : s.split("\\.")) {
                a = a * 100 + Integer.parseInt(part);
            }
            return a;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static void checkId_GN(NutsId id) {
        if (id == null) {
            throw new NutsElementNotFoundException(null, "missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsElementNotFoundException(null, "missing group for " + id);
        }
    }

    public static void checkId_GNV(NutsId id) {
        if (id == null) {
            throw new NutsElementNotFoundException(null, "missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsElementNotFoundException(null, "missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getArtifactId())) {
            throw new NutsElementNotFoundException(null, "missing name for " + id.toString());
        }
    }

    public static boolean isValidWorkspaceName(String workspace) {
        if (CoreStringUtils.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return true;
        } else {
            return false;
        }
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (CoreStringUtils.isBlank(workspace)) {
            return NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return workspaceName;
        } else {
            String p = null;
            try {
                p = new File(workspaceName).getCanonicalFile().getName();
            } catch (IOException ex) {
                p = new File(workspaceName).getAbsoluteFile().getName();
            }
            if (p.isEmpty() || p.equals(".") || p.equals("..")) {
                return "unknown";
            }
            return p;
        }
    }

    public static NutsUpdateOptions validate(NutsUpdateOptions o, NutsWorkspace ws) {
        if (o == null) {
            o = new NutsUpdateOptions();
        }
        if (o.getSession() == null) {
            o.setSession(ws.createSession());
        } else {
            NutsWorkspaceUtils.of(ws).validateSession(o.getSession());
        }
        return o;
    }

    public static NutsAddOptions validate(NutsAddOptions o, NutsWorkspace ws) {
        if (o == null) {
            o = new NutsAddOptions();
        }
        if (o.getSession() == null) {
            o.setSession(ws.createSession());
        } else {
            NutsWorkspaceUtils.of(ws).validateSession(o.getSession());
        }
        return o;
    }

    public static NutsRemoveOptions validate(NutsRemoveOptions o, NutsWorkspace ws) {
        if (o == null) {
            o = new NutsRemoveOptions();
        }
        if (o.getSession() == null) {
            o.setSession(ws.createSession());
        } else {
            NutsWorkspaceUtils.of(ws).validateSession(o.getSession());
        }
        return o;
    }

    public static NutsAddOptions toAddOptions(NutsUpdateOptions o) {
        return new NutsAddOptions().setSession(o.getSession());
    }

    public static NutsRemoveOptions toRemoveOptions(NutsUpdateOptions o) {
        return new NutsRemoveOptions().setSession(o.getSession());
    }

    public static NutsUpdateOptions toUpdateOptions(NutsAddOptions o) {
        return new NutsUpdateOptions().setSession(o.getSession());
    }

    public static NutsUpdateOptions toUpdateOptions(NutsRemoveOptions o) {
        return new NutsUpdateOptions().setSession(o.getSession());
    }

    public static NutsRemoveOptions toRemoveOptions(NutsAddOptions o) {
        return new NutsRemoveOptions().setSession(o.getSession());
    }

    public static NutsRemoveOptions toRemoveOptions(NutsRemoveOptions o) {
        return new NutsRemoveOptions().setSession(o.getSession());
    }

    public static String idToPath(NutsId id) {
        return id.getGroupId().replace('.', '/') + "/" +
                id.getArtifactId() + "/" + id.getVersion();
    }

    public static Properties copyOfNonNull(Properties p) {
        if (p == null) {
            return new Properties();
        }
        Properties p2 = new Properties();
        p2.putAll(p);
        return p2;
    }

    public static Properties copyOfOrNull(Properties p) {
        if (p == null) {
            return null;
        }
        Properties p2 = new Properties();
        p2.putAll(p);
        return p2;
    }

    public static boolean acceptClassifier(NutsIdLocation location, String classifier) {
        if (location == null) {
            return false;
        }
        String c0 = CoreStringUtils.trim(classifier);
        String c1 = CoreStringUtils.trim(location.getClassifier());
        return c0.equals(c1);
    }

    public static String formatLogValue(Object unresolved, Object resolved) {
        String a = desc(unresolved);
        String b = desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static String desc(Object s) {
        if (s == null) {
            return "<EMPTY>";
        }
        String ss
                = (s instanceof Enum) ? ((Enum) s).name().toLowerCase().replace('_', '-')
                : s.toString().trim();
        return ss.isEmpty() ? "<EMPTY>" : ss;
    }

    public static boolean isUnsupportedFetchModeException(Throwable ex) {
        String msg = null;
        if (ex instanceof NutsFetchModeNotSupportedException) {
            return true;
        }
        if (ex instanceof NutsNotFoundException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof NutsFetchModeNotSupportedException) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean acceptMonitoring(NutsSession session) {
        // DefaultNutsStreamProgressMonitor is enable only if plain output
        // so it is disable in json, xml, table, ...
        if (!session.isPlainOut()) {
            return false;
        }
        if (NutsWorkspaceUtils.parseProgressOptions(session).contains("false")) {
            return false;
        }
        Object o = session.getProperty("monitor-allowed");
        NutsWorkspace ws = session.getWorkspace();
        if (o != null) {
            o = ws.commandLine().create(String.valueOf(o)).next().getBoolean();
        }
        boolean monitorable = true;
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (!CoreCommonUtils.getSysBoolNutsProperty("monitor.enabled", true)) {
            monitorable = false;
        }
        if (ws instanceof DefaultNutsWorkspace) {
            if (!((DefaultNutsWorkspace) ws).LOG.isLoggable(Level.INFO)) {
                monitorable = false;
            }
        }
        return monitorable;
    }

    public static NutsSession checkSession(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("missing Session");
        }
        return session;
    }

    public static int getSupportSpeedLevel(NutsRepository repository, NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive, NutsSession session) {
        if (repository instanceof NutsInstalledRepository) {
            return 0;
        }
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        double result = 0;
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            int r = repository.config().getSpeed();
            if (r > 0) {
                result += 1.0 / r;
            }
        }
        if (transitive) {
            for (NutsRepository remote : repository.config().getMirrors(session)) {
                int r = getSupportSpeedLevel(remote, supportedAction, id, mode, transitive, session);
                if (r > 0) {
                    result += 1.0 / r;
                }
            }
        }
        int intResult = 0;
        if (result != 0) {
            intResult = (int) (1.0 / result);
            if (intResult < 0) {
                intResult = Integer.MAX_VALUE;
            }
        }
        return intResult;
    }

    public static int getSupportDeployLevel(NutsRepository repository, NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive, NutsSession session) {
        if (repository instanceof NutsInstalledRepository) {
            return 0;
        }
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        int result = 0;
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            int r = repository.config().getDeployOrder();
            if (r > 0) {
                result += r;
            }
        }
        if (transitive) {
            for (NutsRepository remote : repository.config().getMirrors(session)) {
                int r = getSupportSpeedLevel(remote, supportedAction, id, mode, transitive, session);
                if (r > 0) {
                    result += r;
                }
            }
        }
        return result;
    }

    public static NutsRepositoryDefinition repositoryStringToDefinition(String s) {
        switch (s) {
            case NutsConstants.Names.DEFAULT_REPOSITORY_NAME: {
                return new NutsRepositoryDefinition()
                        .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setLocation(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setDeployOrder(10)
                        .setFailSafe(false)
                        .setCreate(true)
                        .setType(NutsConstants.RepoTypes.NUTS)
                        .setReference(false);
            }
            case ".m2":
            case "m2":
            case "maven-local": {
                return new NutsRepositoryDefinition().setName("maven-local")
                        .setLocation(System.getProperty("user.home") + CoreIOUtils.syspath("/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN)
                        .setProxy(CoreCommonUtils.getSysBoolNutsProperty("cache.cache-local-files", false))
                        .setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL);
            }
            case "maven-central": {
                return new NutsRepositoryDefinition().setName("maven-central")
                        .setLocation(NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL).setType(NutsConstants.RepoTypes.MAVEN)
                        .setProxy(CoreCommonUtils.getSysBoolNutsProperty("cache.cache-local-files", false))
                        .setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL);
            }
            case "vpc-public-maven": {
                return new NutsRepositoryDefinition().setName("vpc-public-maven")
                        .setLocation(REMOTE_MAVEN_GIT).setType(NutsConstants.RepoTypes.MAVEN)
                        .setProxy(CoreCommonUtils.getSysBoolNutsProperty("cache.cache-local-files", false))
                        .setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL);
            }
            case "vpc-public-nuts": {
                return new NutsRepositoryDefinition().setName("vpc-public-nuts")
                        .setLocation(REMOTE_NUTS_GIT).setType(NutsConstants.RepoTypes.NUTS)
                        .setProxy(CoreCommonUtils.getSysBoolNutsProperty("cache.cache-local-files", false))
                        .setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL);
            }
//                case "nuts-git": {
//                    repos.add(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT);
//                    break;
//                }
            default: {
                String[] kv = extractKeyEqValue(s);
                if (kv == null) {
                    throw new IllegalArgumentException("Unsupported boot repository. Missing name: " + s);
                }
                return new NutsRepositoryDefinition().setName(kv[0])
                        .setLocation(kv[1]).setType(resolveRepositoryTypeFromUrl(kv[1]))
                        .setProxy(CoreCommonUtils.getSysBoolNutsProperty("cache.cache-local-files", false))
                        .setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL);
            }
        }
    }

    private static String[] extractKeyEqValue(String s) {
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.*)").matcher(s);
        if (matcher.find()) {
            return new String[]{matcher.group("key"), matcher.group("value")};
        } else {
            return null;
        }
    }

    public String tracePlainNutsId(NutsWorkspace ws, NutsId id) {
        NutsIdFormat idFormat = ws.id().formatter();
        return idFormat.value(id).format();
    }

    public static class NutsDefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final boolean daemon;

        NutsDefaultThreadFactory(String namePattern, boolean daemon) {
            this.daemon = daemon;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = namePattern + "-" +
                    CoreCommonUtils.indexToString(poolNumber.getAndIncrement()) +
                    "-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(this.daemon);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public static String[] parseCommandLineArray(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        final int IN_DBQUOTED_WORD = 3;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ': {
                            //ignore
                            break;
                        }
                        case '\'': {
                            status = IN_QUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '"': {
                            status = IN_DBQUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '\\': {
                            status = IN_WORD;
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '\'': {
                            throw new NutsParseException(null, "illegal char " + c);
                        }
                        case '"': {
                            throw new NutsParseException(null, "illegal char " + c);
                        }
                        case '\\': {
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_QUOTED_WORD: {
                    switch (c) {
                        case '\'': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscapedArgument(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                    break;
                }
                case IN_DBQUOTED_WORD: {
                    switch (c) {
                        case '"': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscapedArgument(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_QUOTED_WORD: {
                throw new NutsParseException(null, "expected '");
            }
        }
        return args.toArray(new String[0]);
    }
    public static int readEscapedArgument(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case 'n': {
                sb.append('\n');
                break;
            }
            case 't': {
                sb.append('\t');
                break;
            }
            case 'r': {
                sb.append('\r');
                break;
            }
            case 'f': {
                sb.append('\f');
                break;
            }
            default: {
                sb.append(c);
            }
        }
        return i;
    }

}
