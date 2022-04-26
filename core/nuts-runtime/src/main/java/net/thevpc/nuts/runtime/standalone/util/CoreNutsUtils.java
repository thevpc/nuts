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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {

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

    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OS_DIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.PLATFORM, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.DESKTOP, null);
    }

    public static String randomColorName() {
        return COLOR_NAMES[(int) (Math.random() * COLOR_NAMES.length)];
    }

    public static String repeat(char txt, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(txt);

        }
        return sb.toString();
    }

    public static NutsId findNutsIdBySimpleName(NutsId id, Collection<NutsId> all) {
        if (all != null) {
            for (NutsId nutsId : all) {
                if (nutsId != null) {
                    if (nutsId.equalsShortId(id)) {
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
        return (!NutsBlankable.isBlank(value) && !CoreStringUtils.containsVars(value));
    }

    public static boolean isEffectiveId(NutsId id) {
        return (isEffectiveValue(id.getGroupId()) && isEffectiveValue(id.getArtifactId()) && isEffectiveValue(id.getVersion().getValue()));
    }

    public static boolean containsVars(NutsId id) {
        return (CoreStringUtils.containsVars(id.getGroupId())
                && CoreStringUtils.containsVars(id.getArtifactId())
                && CoreStringUtils.containsVars(id.getVersion().getValue()));
    }

    public static List<String> applyStringPropertiesList(List<String> child, Function<String, String> properties) {
        return new ArrayList<>(
                Arrays.asList(
                        applyStringProperties(child.toArray(new String[0]),properties)
                )
        );
    }

    public static List<String> applyStringProperties(List<String> child, Function<String, String> properties) {
        return new ArrayList<>(
                Arrays.asList(
                        applyStringProperties(child.toArray(new String[0]),properties)
                )
        );
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
        if (NutsBlankable.isBlank(s)) {
            return NutsVersion.BLANK;
        }
        String s2 = applyStringProperties(s, properties);
        if (!NutsUtilStrings.trim(s2).equals(s)) {
            return NutsVersion.of(s2).orElse(NutsVersion.BLANK);
        }
        return child;
    }

    public static String applyStringProperties(String child, Function<String, String> properties) {
        if (NutsBlankable.isBlank(child)) {
            return null;
        }
//        return applyStringProperties(child, properties == null ? null : new StringConverterAdapter(properties));
        return StringPlaceHolderParser.replaceDollarPlaceHolders(child, properties);
    }

    //    public static String applyStringProperties(String child, Function<String,String> properties) {
//        if (CoreStringUtils.isEmpty(child)) {
//            return null;
//        }
//        return CoreStringUtils.replaceDollarPlaceHolders(child, properties);
//    }
    public static String applyStringInheritance(String child, String parent) {
        child = NutsUtilStrings.trimToNull(child);
        parent = NutsUtilStrings.trimToNull(parent);
        if (child == null) {
            return parent;
        }
        return child;
    }

    public static NutsId applyNutsIdInheritance(NutsId child, NutsId parent) {
        if (parent != null) {
            boolean modified = false;
            String repository = child.getRepository();
            String group = child.getGroupId();
            String name = child.getArtifactId();
            String version = child.getVersion().getValue();
            Map<String, String> props = child.getProperties();
            if (NutsBlankable.isBlank(repository)) {
                modified = true;
                repository = parent.getRepository();
            }
            if (NutsBlankable.isBlank(group)) {
                modified = true;
                group = parent.getGroupId();
            }
            if (NutsBlankable.isBlank(name)) {
                modified = true;
                name = parent.getArtifactId();
            }
            if (NutsBlankable.isBlank(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getProperties();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                props.putAll(parentFaceMap);
            }
            if (modified) {
                return NutsIdBuilder.of().setRepository(repository)
                        .setGroupId(group)
                        .setArtifactId(name)
                        .setVersion(version)
                        .setProperties(props).build();
            }
        }
        return child;
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

    public static Map<String, Object> traceJsonNutsDefinition(NutsSession session, NutsDefinition def) {
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("id", def.getId());
        if (def.getContent() != null) {
            if (def.getContent().getPath() != null) {
                x.put("path", def.getContent().getPath().toString());
            }
            x.put("cached", def.getContent().isCached());
            x.put("temporary", def.getContent().isTemporary());
        }
        if (def.getInstallInformation() != null) {
            if (def.getInstallInformation().getInstallFolder() != null) {
                x.put("install-folder", def.getInstallInformation().getInstallFolder());
            }
            x.put("install-status", def.getInstallInformation().getInstallStatus().toString());
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
            x.put("descriptor", def.getDescriptor().formatter(session).format());
            x.put("effective-descriptor", NutsDescriptorUtils.getEffectiveDescriptor(def,session)
                    .formatter(session).format());
        }
        return x;
    }

    //    public static NutsContentType readOptionOutputFormat(NutsCommandLine cmdLine) {
//        NutsArgument a = cmdLine.peek();
//        switch(a.getStringKey().orElse("")) {
//            case "--output-format": {
//                a = cmdLine.nextString();
//                return CoreEnumUtils.parseEnumString(a.getStringValue(), NutsContentType.class, false);
//            }
//            case "--json": {
//                a = cmdLine.nextString();
//                return (NutsContentType.JSON);
//            }
//            case "--props": {
//                a = cmdLine.nextString();
//                return (NutsContentType.PROPS);
//            }
//            case "--table": {
//                a = cmdLine.nextString();
//                return (NutsContentType.TABLE);
//            }
//            case "--tree": {
//                a = cmdLine.nextString();
//                return (NutsContentType.TREE);
//            }
//            case "--plain": {
//                a = cmdLine.nextString();
//                return (NutsContentType.PLAIN);
//            }
//            case "--xml": {
//                a = cmdLine.nextString();
//                return (NutsContentType.XML);
//            }
//            case "--tson": {
//                a = cmdLine.nextString();
//                return (NutsContentType.TSON);
//            }
//            case "--yaml": {
//                a = cmdLine.nextString();
//                return (NutsContentType.YAML);
//            }
//        }
//        return null;
//    }

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
            int qualifierIndex = s.indexOf('-');
            if(qualifierIndex>=0){
                s=s.substring(0,qualifierIndex);
            }
            int a = 0;
            for (String part : s.split("\\.")) {
                a = a * 100 + CoreNumberUtils.convertToInteger(part, 0);
            }
            return a;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static boolean isValidWorkspaceName(String workspace) {
        if (NutsBlankable.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NutsBlankable.isBlank(workspace)) {
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

    public static String idToPath(NutsId id) {
        return id.getGroupId().replace('.', '/') + "/"
                + id.getArtifactId() + "/" + id.getVersion();
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

    public static boolean isUnsupportedFetchModeException(Throwable ex) {
        String msg = null;
        if (ex instanceof NutsFetchModeNotSupportedException) {
            return true;
        }
        if (ex instanceof NutsNotFoundException) {
            if (ex.getCause() != null) {
                Throwable ex2 = ex.getCause();
                return ex2 instanceof NutsFetchModeNotSupportedException;
            }
        }
        return false;
    }


    public static NutsIdType detectIdType(NutsId depId, NutsSession session) {
        switch (depId.getShortName()) {
            case NutsConstants.Ids.NUTS_API: {
                return NutsIdType.API;
            }
            case NutsConstants.Ids.NUTS_RUNTIME: {
                return NutsIdType.RUNTIME;
            }
            default: {
                String rt = session.getWorkspace().getRuntimeId().getShortName();
                if (rt.equals(depId.getShortName())) {
                    return NutsIdType.RUNTIME;
                } else {
                    for (NutsClassLoaderNode n : session.boot().getBootExtensionClassLoaderNode()) {
                        if (NutsId.of(n.getId()).orElse(NutsId.BLANK).equalsShortId(depId)) {
                            return NutsIdType.EXTENSION;
                        }
                    }
                    return NutsIdType.REGULAR;
                }
            }
        }
    }

    public static List<NutsId> resolveNutsApiIds(NutsId id, NutsSession session) {
        List<NutsDependency> deps = session.fetch().setId(id).setDependencies(true).getResultDefinition().getDependencies().transitive().toList();
        return resolveNutsApiIds2(deps, session);
    }

    public static List<NutsId> resolveNutsApiIds(NutsDefinition def, NutsSession session) {
        return resolveNutsApiIds(def.getDependencies(), session);
    }

    public static List<NutsId> resolveNutsApiIds(NutsDependencies deps, NutsSession session) {
        return resolveNutsApiIds(deps, session);
    }

    public static List<NutsId> resolveNutsApiIds2(List<NutsDependency> deps, NutsSession session) {
        return deps.stream()
                .map(NutsDependency::toId)
                .filter(x -> x.getShortName().equals("net.thevpc.nuts:nuts"))
                .distinct().collect(Collectors.toList());
    }

    public static List<NutsId> resolveNutsApiIds(List<NutsId> deps, NutsSession session) {
        return deps.stream()
                .filter(x -> x.getShortName().equals("net.thevpc.nuts:nuts"))
                .distinct().collect(Collectors.toList());
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
            group = (s != null) ? s.getThreadGroup()
                    : Thread.currentThread().getThreadGroup();
            namePrefix = namePattern + "-"
                    + CoreStringUtils.indexToString(poolNumber.getAndIncrement())
                    + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(this.daemon);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    public static boolean isCustomTrue(String name,NutsSession session) {
        return session.boot().getCustomBootOption(name)
                .ifEmpty(NutsValue.of("true"))
                .flatMap(NutsValue::asBoolean)
                .orElse(false);
    }
    public static boolean isCustomFalse(String name,NutsSession session) {
        return session.boot().getCustomBootOption(name)
                .flatMap(NutsValue::asBoolean)
                .orElse(false);
    }

    public static boolean isShowCommand(NutsSession session) {
        return session.boot().getCustomBootOption("---show-command")
                .flatMap(NutsValue::asBoolean)
                .orElse(false);
    }


}
