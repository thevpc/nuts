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
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.core.expr.StringMapParser;
import net.thevpc.nuts.runtime.core.expr.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.model.DefaultNutsEnvCondition;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

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

/**
 * Created by vpc on 5/16/17.
 */
public class CoreNutsUtils {

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

    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.OS_DIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.IdProperties.PLATFORM, null);
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

    public static NutsVersion applyStringProperties(NutsVersion child, Function<String, String> properties, NutsSession ws) {
        if (child == null) {
            return child;
        }
        String s = child.getValue();
        if (NutsBlankable.isBlank(s)) {
            return NutsVersion.of("", ws);
        }
        String s2 = applyStringProperties(s, properties);
        if (!NutsUtilStrings.trim(s2).equals(s)) {
            return NutsVersion.of(s2, ws);
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

    public static <T> T simplify(T any) {
        if (any == null) {
            return null;
        }
        if (any instanceof Simplifiable) {
            return ((Simplifiable<T>) any).simplify();
        }
        return any;
    }

    public static <T extends NutsFilter> T simplifyFilterOr(NutsSession ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return NutsFilters.of(ws).always(cls);
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
                        return NutsFilters.of(ws).always(cls);
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
        if (all2.isEmpty()) {
            if (someFalse) {
                return NutsFilters.of(ws).never(cls);
            }
            return NutsFilters.of(ws).always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return NutsFilters.of(ws).any(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T extends NutsFilter> T simplifyFilterAnd(NutsSession ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return NutsFilters.of(ws).always(cls);
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
                        return NutsFilters.of(ws).never(cls);
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
            return NutsFilters.of(ws).always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return NutsFilters.of(ws).all(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T extends NutsFilter> T simplifyFilterNone(NutsSession ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return NutsFilters.of(ws).always(cls);
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return NutsFilters.of(ws).never(cls);
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
            return NutsFilters.of(ws).always(cls);
        }
        if (!updates) {
            return base;
        }
        return NutsFilters.of(ws).none(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
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

    public static NutsId applyNutsIdInheritance(NutsId child, NutsId parent, NutsSession ws) {
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
                return NutsIdBuilder.of(ws).setRepository(repository)
                        .setGroupId(group)
                        .setArtifactId(name)
                        .setVersion(version)
                        .setProperties(props).build();
            }
        }
        return child;
    }

    public static boolean isDefaultOptional(String s1) {
        s1 = NutsUtilStrings.trim(s1);
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

    public static NutsRepositoryRef optionsToRef(NutsAddRepositoryOptions options) {
        return new NutsRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployWeight(options.getDeployWeight());
    }

    public static NutsAddRepositoryOptions refToOptions(NutsRepositoryRef ref) {
        return new NutsAddRepositoryOptions()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployWeight(ref.getDeployWeight())
                .setTemporary(false);
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
            x.put("descriptor", def.getDescriptor().formatter().setSession(session).format());
            x.put("effective-descriptor", NutsWorkspaceUtils.of(session).getEffectiveDescriptor(def)
                    .formatter().setSession(session).format());
        }
        return x;
    }

    public static boolean processHelpOptions(String[] args, NutsSession session) {
        if(isIncludesHelpOption(args)) {
            NutsCommandLine cmdLine = NutsCommandLine.of(args, session);
            while (cmdLine.hasNext()) {
                NutsArgument a = cmdLine.peek();
                if (a.isOption()) {
                    switch (a.getKey().getString()) {
                        case "--help": {
                            cmdLine.skip();
                            break;
                        }
                        default: {
                            session.configureLast(cmdLine);
                        }
                    }
                } else {
                    cmdLine.skip();
                    cmdLine.skipAll();
                }
            }
            return true;
        }
        return false;
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

    public static void traceMessage(NutsLogger log, Level lvl, String name, NutsSession session, NutsFetchMode fetchMode, NutsId id, NutsLogVerb tracePhase, String title, long startTime, NutsMessage extraMsg) {
        if (!log.isLoggable(lvl)) {
            return;
        }
        String sep;
        if (extraMsg == null) {
            sep = "";
            extraMsg = NutsMessage.formatted("");
        } else {
            sep = " : ";
        }
        long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
        String modeString = CoreStringUtils.alignLeft(fetchMode.id(), 7);
        log.with().session(session).level(lvl).verb(tracePhase).time(time)
                .log(NutsMessage.jstyle("[{0}] {1} {2} {3} {4}",
                        modeString,
                        CoreStringUtils.alignLeft(name, 20),
                        CoreStringUtils.alignLeft(title, 18),
                        (id == null ? "" : id),
                        extraMsg));
    }

//    public static NutsContentType readOptionOutputFormat(NutsCommandLine cmdLine) {
//        NutsArgument a = cmdLine.peek();
//        switch (a.getKey().getString()) {
//            case "--output-format": {
//                a = cmdLine.nextString();
//                return CoreEnumUtils.parseEnumString(a.getValue().getString(), NutsContentType.class, false);
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

    public static boolean acceptClassifier(NutsIdLocation location, String classifier) {
        if (location == null) {
            return false;
        }
        String c0 = NutsUtilStrings.trim(classifier);
        String c1 = NutsUtilStrings.trim(location.getClassifier());
        return c0.equals(c1);
    }

    public static NutsString formatLogValue(NutsTexts text, Object unresolved, Object resolved) {
        NutsString a = desc(unresolved, text);
        NutsString b = desc(resolved, text);
        if (a.equals(b)) {
            return a;
        } else {
            return
                    text.builder()
                            .append(a)
                            .append(" => ")
                            .append(b)
                    ;
        }
    }

    public static NutsString desc(Object s, NutsTexts text) {
        if (s == null || (s instanceof String && ((String) s).isEmpty())) {
            return text.ofStyled("<EMPTY>", NutsTextStyle.option());
        }
        return text.toText(s);
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

    public static ProgressOptions parseProgressOptions(NutsSession session) {
        ProgressOptions o = new ProgressOptions();
        boolean enabledVisited = false;
        StringMapParser p = new StringMapParser("=", ",; ");
        Map<String, String> m = p.parseMap(session.getProgressOptions(), session);
        for (Map.Entry<String, String> e : m.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (!enabledVisited) {
                if (v == null) {
                    Boolean a = NutsUtilStrings.parseBoolean(k, null, null);
                    if (a != null) {
                        o.enabled = a;
                        enabledVisited = true;
                    } else {
                        o.vals.put(k, NutsVal.of(v, session));
                    }
                }
            } else {
                o.vals.put(k, NutsVal.of(v, session));
            }
        }
        return o;
    }

    public static boolean acceptProgress(NutsSession session) {
        if (!session.isPlainOut()) {
            return false;
        }
        return !session.isBot() && parseProgressOptions(session).isEnabled();
    }

    public static boolean acceptMonitoring(NutsSession session) {
        // DefaultNutsStreamProgressMonitor is enable only if plain output
        // so it is disable in json, xml, table, ...
        if (!session.isPlainOut()) {
            return false;
        }
        if (!acceptProgress(session)) {
            return false;
        }
        Object o = session.getProperty("monitor-allowed");
        NutsWorkspace ws = session.getWorkspace();
        if (o != null) {
            o = NutsCommandLine.of(new String[]{String.valueOf(o)}, session).next().getAll().getBoolean();
        }
        boolean monitorable = true;
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (!session.boot().getCustomBootOption("monitor.enabled").getBoolean(true)) {
            monitorable = false;
        }
        if (ws instanceof DefaultNutsWorkspace) {
            if (!((DefaultNutsWorkspace) ws).LOG.isLoggable(Level.INFO)) {
                monitorable = false;
            }
        }
        return monitorable;
    }

    public static Iterator<NutsDependency> itIdToDep(Iterator<NutsId> id) {
        return IteratorBuilder.of(id).map(IteratorUtils.namedFunction(NutsId::toDependency, "IdToDependency")).build();
    }

    public static Iterator<NutsDependency> itIdToDep(Iterator<NutsId> id, NutsDependency copyFrom) {
        String _optional = copyFrom.getOptional();
        String _scope = copyFrom.getScope();
        return IteratorBuilder.of(id).map(IteratorUtils.namedFunction(
                x -> x.toDependency().builder().setOptional(_optional).setScope(_scope).build(), "IdToDependency")).build();
    }

//    private static boolean acceptCondition(NutsId[] curr, String[] expected, NutsSession session) {
//        if (expected.length > 0) {
//            boolean accept = false;
//            for (NutsId v : curr) {
//                if (acceptCondition(v, expected, session)) {
//                    accept = true;
//                    break;
//                }
//            }
//            if (!accept) {
//                return false;
//            }
//        }
//        return true;
//    }

//    private static boolean acceptCondition(NutsId curr, String[] expected, NutsSession session) {
//        if (expected.length != 0) {
//            boolean accept = false;
//            NutsId a = curr;
//            for (String v : expected) {
//                if (NutsId.of(v, session).filter().acceptId(a, session)) {
//                    accept = true;
//                    break;
//                }
//            }
//            if (!accept) {
//                return false;
//            }
//        }
//        return true;
//    }

//    public static boolean acceptCondition(NutsEnvCondition condition, boolean allInstalledPlatforms, NutsSession session) {
//        return CoreFilterUtils.acceptCondition(condition,allInstalledPlatforms, session);
////        if (condition == null || condition.isBlank()) {
////            return true;
////        }
////        NutsWorkspaceEnvManager env = session.env();
////        List<NutsId> pfs = new ArrayList<>();
////        if (allInstalledPlatforms) {
////            for (String s : Arrays.stream(condition.getPlatform()).collect(Collectors.toSet())) {
////                pfs.addAll(Arrays.stream(session.env().platforms().findPlatforms(
////                        NutsPlatformType.parseLenient(s, NutsPlatformType.UNKNOWN, NutsPlatformType.UNKNOWN)
////                )).map(NutsPlatformLocation::getId).collect(Collectors.toList()));
////            }
////        } else {
////            pfs.add(env.getPlatform());
////        }
////        return
////                acceptCondition(env.getArch(), condition.getArch(), session)
////                        && acceptCondition(env.getOs(), condition.getOs(), session)
////                        && acceptCondition(env.getOsDist(), condition.getOsDist(), session)
////                        && acceptCondition(env.getDesktopEnvironment(), condition.getDesktopEnvironment(), session)
////                        && acceptCondition(
////                        pfs.toArray(new NutsId[0])
////                        , condition.getDesktopEnvironment(), session);
//    }

    public static NutsEnvCondition blankCondition(NutsSession session) {
        return new DefaultNutsEnvCondition(null, null, null, null, null, session);
    }

    public static NutsEnvCondition trimToNull(NutsEnvCondition c, NutsSession session) {
        if (c == null || c.isBlank()) {
            return null;
        }
        return c;
    }

    public static NutsEnvCondition trimToBlank(NutsEnvCondition c, NutsSession session) {
        if (c == null) {
            return blankCondition(session);
        }
        return c;
    }

    public static Map<String, String> getPropertiesMap(NutsDescriptorProperty[] list, NutsSession session) {
        Map<String, String> m = new LinkedHashMap<>();
        if (list != null) {
            for (NutsDescriptorProperty property : list) {
                if (property.getCondition() == null || property.getCondition().isBlank()) {
                    m.put(property.getName(), property.getValue());
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.plain("unexpected properties with conditions. probably a bug"));
                }
            }
        }
        return m;
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
                        if (NutsId.of(n.getId(), session).equalsShortId(depId)) {
                            return NutsIdType.EXTENSION;
                        }
                    }
                    return NutsIdType.REGULAR;
                }
            }
        }
    }

    public static class ProgressOptions {
        private final Map<String, NutsVal> vals = new LinkedHashMap<>();
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isArmedNewline() {
            return isArmed("newline") || isArmed("%n");
        }

        public boolean isArmed(String k) {
            NutsVal q = vals.get(k);
            if (q == null) {
                return false;
            }
            return q.getBoolean(true);
        }
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
}
