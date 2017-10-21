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
package net.vpc.app.nuts.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.boot.DefaultHttpTransportComponent;

import javax.json.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsUtils {
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    public static NutsDependencyFilter EXEC_DEPENDENCIES_FILTER =
            d -> !d.isOptional() &&
                    (StringUtils.isEmpty(d.getScope())
                            || "compile".equals(d.getScope())
                            || "runtime".equals(d.getScope())
                            || "provided ".equals(d.getScope())
                    );
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
                x += weight(NutsDependency.parse(s));
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


    public static NutsId finNutsIdByFullNameInStrings(NutsId id, Collection<String> all) {
        if (all != null) {
            for (String nutsId : all) {
                if (nutsId != null) {
                    NutsId nutsId2 = NutsId.parseOrError(nutsId);
                    if (nutsId2.isSameFullName(id)) {
                        return nutsId2;
                    }
                }
            }
        }
        return null;
    }


    public static boolean isEffectiveValue(String value) {
        return (!StringUtils.isEmpty(value) && !containsVars(value));
    }

    public static boolean containsVars(String value) {
        return value.contains("${");
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
            } catch (IOException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new IOException("Unable to parse file " + file, ex);
            }
        }

    }

    public static NutsDescriptor parseNutsDescriptor(String str) throws IOException {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return parseNutsDescriptor(new ByteArrayInputStream(str.getBytes()));
    }

    public static NutsDescriptor parseNutsDescriptor(InputStream in) throws IOException {
        JsonObject jsonObject = Json.createReader(in).readObject();
        String nutsVersion = jsonObject.getString("nuts-version");
        if ("1.0".equals(nutsVersion)) {
            NutsId id = NutsId.parseOrError(jsonObject.getString("id"));
            String alt = JsonUtils.deserialize(jsonObject.get(NutsConstants.QUERY_FACE), String.class);
            String packaging = JsonUtils.deserialize(jsonObject.get("packaging"), String.class);
            String ext = JsonUtils.deserialize(jsonObject.get("ext"), String.class);
            String[] parentStrings = JsonUtils.deserialize(jsonObject.get("parents"), String[].class);
            Map<String, String> props = JsonUtils.deserializeStringsMap(jsonObject.get("properties"), new HashMap<>());
            NutsId[] parents = new NutsId[parentStrings == null ? 0 : parentStrings.length];
            for (int i = 0; i < parents.length; i++) {
                parents[i] = NutsId.parseOrError(parentStrings[i]);
            }
            String name = JsonUtils.deserialize(jsonObject.get("name"), String.class);
            String description = JsonUtils.deserialize(jsonObject.get("description"), String.class);
            boolean executable = false;
            if (jsonObject.get("executable") != null) {
                executable = (jsonObject.getBoolean("executable"));
            }
            NutsExecutorDescriptor executor = null;
            NutsExecutorDescriptor installer = null;
            if (!JsonUtils.isNull(jsonObject.get("executor"))) {
                JsonObject runObj = (JsonObject) jsonObject.get("executor");
                NutsId rid = runObj.get("id") == null ? null : NutsId.parseOrError(runObj.getString("id"));
                String[] rargs = JsonUtils.deserialize(runObj.getJsonArray("args"), String[].class);
                Properties rprops = JsonUtils.deserialize(runObj.getJsonObject("properties"), Properties.class);
                executor = new NutsExecutorDescriptor(rid, rargs, rprops);
            }
            if (!JsonUtils.isNull(jsonObject.get("installer"))) {
                JsonObject runObj = (JsonObject) jsonObject.get("installer");
                NutsId rid = runObj.get("id") == null ? null : NutsId.parseOrError(runObj.getString("id"));
                String[] rargs = JsonUtils.deserialize(runObj.getJsonArray("args"), String[].class);
                Properties rprops = JsonUtils.deserialize(runObj.getJsonObject("properties"), Properties.class);
                installer = new NutsExecutorDescriptor(rid, rargs, rprops);
            }
            List<NutsDependency> deps = new ArrayList<>();

            JsonArray dependencies = JsonUtils.isNull(jsonObject.get("dependencies")) ? null : jsonObject.getJsonArray("dependencies");
            if (dependencies != null) {
                for (JsonValue dependency : dependencies) {
                    NutsDependency depp = NutsDependency.parse(((JsonString) dependency).getString());
                    if (depp == null) {
                        throw new IOException("Invalid dependency " + dependency);
                    }
                    deps.add(depp);
                }
            }
            String[] os = JsonUtils.getJsonObjectStringArray(jsonObject, "os");
            String[] osdist = JsonUtils.getJsonObjectStringArray(jsonObject, "osdist");
            String[] arch = JsonUtils.getJsonObjectStringArray(jsonObject, "arch");
            String[] platform = JsonUtils.getJsonObjectStringArray(jsonObject, "platform");

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
            return new NutsId(
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
        if (StringUtils.isEmpty(nutFormat)) {
            return null;
        }
        NutsId id = parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsIdInvalidFormatException("Invalid Id format : " + nutFormat);
        }
        return id;
    }

    public static String getNutsFileName(NutsId id, String ext) {
        if (StringUtils.isEmpty(ext)) {
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
        if (StringUtils.isEmpty(child)) {
            return null;
        }
        return StringUtils.replaceVars(child, properties);
    }

    public static String applyStringInheritance(String child, String parent) {
        child = StringUtils.trimToNull(child);
        parent = StringUtils.trimToNull(parent);
        if (child == null) {
            return parent;
        }
        return child;
    }
}
