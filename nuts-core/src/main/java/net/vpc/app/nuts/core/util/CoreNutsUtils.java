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

import java.io.File;
import java.io.IOException;

import net.vpc.app.nuts.core.log.NutsLogVerb;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.Simplifiable;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.filters.dependency.*;

import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {

    public static final int DEFAULT_UUID_LENGTH = 25;
    public static final int DEFAULT_DATE_TIME_FORMATTER_LENGTH = 23;
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone( ZoneId.systemDefault() )
            ;
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}*-]+)://)?([a-zA-Z0-9_.${}*-]+)(:([a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    public static final NutsDependencyFilter OPTIONAL = NutsDependencyOptionFilter.OPTIONAL;
    public static final NutsDependencyFilter NON_OPTIONAL = NutsDependencyOptionFilter.NON_OPTIONAL;
    private static final Map<String, String> _QUERY_EMPTY_ENV = new HashMap<>();
    public static final Map<String, String> QUERY_EMPTY_ENV = Collections.unmodifiableMap(_QUERY_EMPTY_ENV);
    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OSDIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.PLATFORM, null);
    }

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

    private static Set<String> DEPENDENCY_SUPPORTED_PARAMS = new HashSet<>(Arrays.asList(NutsConstants.IdProperties.SCOPE, NutsConstants.IdProperties.OPTIONAL));
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
            for (NutsVersionInterval s : desc.intervals()) {
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
//                    .setAlternative("suse")
                    .setName("Application Full Name")
                    .setDescription("Application Description")
                    .setExecutable(true)
                    .setPackaging("jar")
                    //                    .setExt("exe")
                    .setArch(new String[]{"64bit"})
                    .setOs(new String[]{"linux#4.6"})
                    .setOsdist(new String[]{"opensuse#42"})
                    .setPlatform(new String[]{"java#1.8"})
                    .setExecutor(new DefaultNutsArtifactCall(
                            new DefaultNutsId(null, null, "java", "1.8", (String) null),
                            new String[]{"-jar"}
                    ))
                    .setInstaller(new DefaultNutsArtifactCall(
                            new DefaultNutsId(null, null, "java", "1.8", (String) null),
                            new String[]{"-jar"}
                    ))
                    .setLocations(new NutsIdLocation[]{
                new DefaultNutsIdLocation("http://server/somelink",null,null)
            })
                    .setDependencies(
                            new NutsDependency[]{
                                 new DefaultNutsDependencyBuilder()
                                    .namespace("namespace")
                                    .groupId("group")
                                    .artifactId("name")
                                    .version("version")
                                    .optional("false").build()
                            }
                    )
                    .build();



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
            return new DefaultNutsDependencyBuilder()
                    .setNamespace(protocol)
                    .setGroupId(group)
                    .setArtifactId(name)
                    .setVersion(version)
                    .setProperties(queryMap)
                    .build();
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
            String group = child.getGroupId();
            String name = child.getArtifactId();
            String version = child.getVersion().getValue();
            Map<String, String> face = child.getProperties();
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

//    public static boolean isDefaultAlternative(String s1) {
//        s1 = CoreStringUtils.trim(s1);
//        return s1.isEmpty() || s1.equals(NutsConstants.IdProperties.ALTERNATIVE_DEFAULT_VALUE);
//    }

    public static boolean isDefaultOptional(String s1) {
        s1 = CoreStringUtils.trim(s1);
        return s1.isEmpty() || s1.equals("false");
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
                .setDeployOrder(options.getDeployOrder());
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

//    public static void wconfigToBconfig(NutsWorkspaceConfig wconfig, NutsBootConfig bconfig) {
//        bconfig.setStoreLocations(new NutsStoreLocationsMap(wconfig.getStoreLocations()).toMap());
//        bconfig.setHomeLocations(new NutsHomeLocationsMap(wconfig.getHomeLocations()).toMap());
//    }

//    public static void optionsToWconfig(NutsWorkspaceOptions options, NutsWorkspaceConfig wconfig) {
//        wconfig.setStoreLocations(new NutsStoreLocationsMap(wconfig.getStoreLocations()).toMapOrNull());
//        wconfig.setHomeLocations(new NutsHomeLocationsMap(wconfig.getHomeLocations()).toMapOrNull());
//    }

    public String tracePlainNutsId(NutsWorkspace ws, NutsId id) {
        NutsIdFormat idFormat = ws.id();
        return idFormat.value(id).format();
    }

    public static String tracePlainNutsDefinition(NutsWorkspace ws, NutsDefinition id) {
        NutsIdFormat idFormat = ws.id();
        return idFormat.value(id.getId()).format();
    }

    public static Object tracePropsNutsDefinition(NutsWorkspace ws, NutsDefinition id) {
        NutsIdFormat idFormat = ws.id();
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
            x.put("installed", def.getInstallInformation().isInstalled());
            x.put("just-installed", def.getInstallInformation().isJustInstalled());
        }
        if (def.getRepositoryName() != null) {
            x.put("repository-name", def.getRepositoryName());
        }
        if (def.getRepositoryUuid() != null) {
            x.put("repository-uuid", def.getRepositoryUuid());
        }
        if (def.getDescriptor() != null) {
            x.put("descriptor", ws.descriptor().value(def.getDescriptor()).format());
            x.put("effective-descriptor", ws.descriptor().value(
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
            return session.getWorkspace().iter().session(session);
        }
        return f;
    }

    public static void traceMessage(NutsLogger log, Level lvl,String name, NutsRepositorySession session, NutsId id, TraceResult tracePhase, String title, long startTime,String extraMsg) {
        if(!log.isLoggable(lvl)){
            return;
        }
        if(extraMsg==null){
            extraMsg="";
        }else{
            extraMsg=" : "+extraMsg;
        }
        String timeMessage = "";
        if (startTime != 0) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                timeMessage = " (" + time + "ms)";
            }
        }
        String fetchString = CoreStringUtils.alignLeft(session.getFetchMode().id(), 7);
        log.log(lvl, tracePhase.name(), "[{0}] {1} {2} {3}{4}{5}", new Object[]{fetchString, CoreStringUtils.alignLeft(name, 20), CoreStringUtils.alignLeft(title, 18), id == null ? "" : id.toString(), timeMessage,extraMsg});
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

//    public static String trimToNullAlternative(String s) {
//        if (s == null) {
//            return null;
//        }
//        s = s.trim();
//        return (s.isEmpty() || NutsConstants.IdProperties.ALTERNATIVE_DEFAULT_VALUE.equalsIgnoreCase(s)) ? null : s;
//    }

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
            throw new NutsElementNotFoundException(null, "Missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsElementNotFoundException(null, "Missing group for " + id);
        }
    }

    public static void checkId_GNV(NutsId id) {
        if (id == null) {
            throw new NutsElementNotFoundException(null, "Missing id");
        }
        if (CoreStringUtils.isBlank(id.getGroupId())) {
            throw new NutsElementNotFoundException(null, "Missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getArtifactId())) {
            throw new NutsElementNotFoundException(null, "Missing name for " + id.toString());
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

    public static NutsUpdateOptions validate(NutsUpdateOptions o,NutsWorkspace ws){
        if(o==null){
            o=new NutsUpdateOptions();
        }
        if(o.getSession()==null){
            o.session(ws.createSession());
        }
        return o;
    }
    
    public static NutsAddOptions validate(NutsAddOptions o,NutsWorkspace ws){
        if(o==null){
            o=new NutsAddOptions();
        }
        if(o.getSession()==null){
            o.session(ws.createSession());
        }
        return o;
    }

    public static NutsRemoveOptions validate(NutsRemoveOptions o,NutsWorkspace ws){
        if(o==null){
            o=new NutsRemoveOptions();
        }
        if(o.getSession()==null){
            o.session(ws.createSession());
        }
        return o;
    }

    public static NutsAddOptions toAddOptions(NutsUpdateOptions o){
        return new NutsAddOptions().session(o.getSession());
    }
    public static NutsRemoveOptions toRemoveOptions(NutsUpdateOptions o){
        return new NutsRemoveOptions().session(o.getSession());
    }

    public static NutsUpdateOptions toUpdateOptions(NutsAddOptions o){
        return new NutsUpdateOptions().session(o.getSession());
    }
    public static NutsUpdateOptions toUpdateOptions(NutsRemoveOptions o){
        return new NutsUpdateOptions().session(o.getSession());
    }

    public static NutsRemoveOptions toRemoveOptions(NutsAddOptions o){
        return new NutsRemoveOptions().session(o.getSession());
    }
    public static NutsRemoveOptions toRemoveOptions(NutsRemoveOptions o){
        return new NutsRemoveOptions().session(o.getSession());
    }

    public static String idToPath(NutsId id) {
        return id.getGroupId().replace('.','/')+"/"+
                id.getArtifactId()+"/"+id.getVersion();
    }

    public static Properties copyOfNonNull(Properties p){
        if(p==null){
            return new Properties();
        }
        Properties p2=new Properties();
        p2.putAll(p);
        return p2;
    }

    public static Properties copyOfOrNull(Properties p){
        if(p==null){
            return null;
        }
        Properties p2=new Properties();
        p2.putAll(p);
        return p2;
    }

    public static boolean acceptClassifier(NutsIdLocation location,String classifier){
        if(location==null){
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
    public static String resolveMessageToTraceOrNullIfNutsNotFoundException(Exception ex){
        String msg=null;
        if(ex instanceof NutsNotFoundException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                if (ex2 instanceof UncheckedIOException) {
                    ex2 = ex.getCause();
                }
                msg = ex2.getMessage();
            }
        }else{
            msg=ex.getMessage();
        }
        return msg;
    }
}
