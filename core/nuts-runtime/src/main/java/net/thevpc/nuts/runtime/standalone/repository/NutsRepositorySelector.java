/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;

/**
 *
 * @author thevpc
 */
public class NutsRepositorySelector {

    private static final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();

    static {
        defaultRepositoriesByName.put("system", CoreIOUtils.getNativePath(
                NutsUtilPlatforms.getPlatformHomeFolder(null,
                        NutsStoreLocation.CONFIG, null,
                        true,
                        NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                + "/" + NutsConstants.Folders.REPOSITORIES
                + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
        ));
        //
        defaultRepositoriesByName.put("maven-local", System.getProperty("user.home") + CoreIOUtils.syspath("/.m2/repository"));
        defaultRepositoriesByName.put(".m2", defaultRepositoriesByName.get("maven-local"));
        defaultRepositoriesByName.put("m2", defaultRepositoriesByName.get("maven-local"));
        //
        defaultRepositoriesByName.put("maven-central", "https://repo.maven.apache.org/maven2");
        defaultRepositoriesByName.put("m2", defaultRepositoriesByName.get("maven-central"));
        defaultRepositoriesByName.put("central", defaultRepositoriesByName.get("maven-central"));
        //
        defaultRepositoriesByName.put("jcenter", "https://jcenter.bintray.com");
        //
        defaultRepositoriesByName.put("jboss", "https://repository.jboss.org/nexus/content/repositories/releases");
        //
        defaultRepositoriesByName.put("clojars", "https://repo.clojars.org");
        //
        defaultRepositoriesByName.put("atlassian", "https://packages.atlassian.com/maven/public");
        //
        defaultRepositoriesByName.put("atlassian-snapshot", "https://packages.atlassian.com/maven/public-snapshot");
        //
        defaultRepositoriesByName.put("oracle", "https://maven.oracle.com");
        //
        defaultRepositoriesByName.put("google", "https://maven.google.com");
        //
        defaultRepositoriesByName.put("spring", "https://repo.spring.io/release");
        defaultRepositoriesByName.put("spring-framework", defaultRepositoriesByName.get("spring"));
        //
        defaultRepositoriesByName.put("maven-thevpc-git", "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master");
        defaultRepositoriesByName.put("vpc-public-maven", defaultRepositoriesByName.get("maven-thevpc-git"));
        //
        defaultRepositoriesByName.put("nuts-thevpc-git", "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
        defaultRepositoriesByName.put("vpc-public-nuts", defaultRepositoriesByName.get("nuts-thevpc-git"));
        //
        defaultRepositoriesByName.put("thevpc", "http://thevpc.net/maven");
        defaultRepositoriesByName.put("dev", defaultRepositoriesByName.get("thevpc"));
        defaultRepositoriesByName.put("local", "local");

    }

    public static String getRepositoryNameByURL(String url) {
        NutsRepositoryURL nru = new NutsRepositoryURL(url);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            String v = entry.getValue();
            if (v.equals(nru.getURLString()) || v.equals(nru.getLocation())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getRepositoryURLByName(String name) {
        return defaultRepositoriesByName.get(name);
    }

    public static enum Op {
        INCLUDE,
        EXCLUDE,
        EXACT,
    }
    private Op op = Op.INCLUDE;
    private String name;
    private String url;

    public static SelectorList parse(String[] textes) {
        if (textes == null) {
            return new SelectorList();
        }
        SelectorList all = new SelectorList();
        for (String t : textes) {
            all = all.join(parseList(t));
        }
        return all;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (op == Op.EXACT) {
            sb.append(":=");
        } else if (op == Op.EXCLUDE) {
            sb.append(":-");
        } else if (op == Op.INCLUDE) {
            sb.append(":+");
        }
        if (name != null && name.length() > 0) {
            sb.append(name);
        }
        if (url != null && url.length() > 0) {
            sb.append("(");
            sb.append(url);
            sb.append(")");
        }
        return sb.toString();
    }

    public static Selection parseSelection(String s) {
        String name = null;
        String url = null;
        if (s == null) {
            throw new IllegalArgumentException("invalid null selection");
        }
        s = s.trim();
        if (s.startsWith("-")
                || s.startsWith("+")
                || s.startsWith("=")
                || s.indexOf(',') >= 0
                || s.indexOf(';') >= 0) {
            throw new IllegalArgumentException("invalid selection syntax");
        }
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(s);
        if (matcher.find()) {
            name = matcher.group("name");
            url = matcher.group("value");
        } else {
            if (s.matches("[a-zA-Z-_]+")) {
                name = s;
                String u = getRepositoryURLByName(name);
                if (u == null) {
                    url = name;
                } else {
                    url = u;
                }
            } else {
                url = s;
                String n = getRepositoryNameByURL(url);
                if (n == null) {
                    name = url;
                } else {
                    name = n;
                }
            }
        }
        if (url.length() > 0) {
            return new Selection(name, url);
        }
        return null;
    }

    public static SelectorList parseList(String text) {
        if (text == null) {
            return new SelectorList();
        }
        Op op = Op.INCLUDE;
        List<NutsRepositorySelector> all = new ArrayList<>();
        for (String s : text.split("[,;]")) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("+")) {
                    op = Op.INCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("-")) {
                    op = Op.EXCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("=")) {
                    op = Op.EXACT;
                    s = s.substring(1);
                }
                Selection z = parseSelection(s);
                if (z != null) {
                    all.add(new NutsRepositorySelector(op, z.getName(), z.getUrl()));
                }
            }
        }
        return new SelectorList(all.toArray(new NutsRepositorySelector[0]));
    }

    public NutsRepositorySelector(Op op, String name, String url) {
        this.op = op;
        this.name = name;
        this.url = url;
    }

    public Op getOp() {
        return op;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean matches(String otherName, String otherURL) {
        otherName = otherName == null ? "" : otherName.trim();
        otherURL = otherURL == null ? "" : otherURL.trim();
        String _name = this.name == null ? "" : this.name.trim();
        String _url = this.url == null ? "" : this.url.trim();
        otherURL = otherURL == null ? otherURL : otherURL.trim();
        if (_name.length() > 0 && _name.equals(otherName)) {
            return true;
        }
        if (_url.length() > 0 && _url.equals(otherURL)) {
            return true;
        }
        return false;
    }

    public static class Selection {

        private String name;
        private String url;

        public Selection(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            if(name==null){
                return url;
            }
            if(url==null){
                return name;
            }
            if(name.equals(url)){
                return url;
            }
            return name+"="+url;
        }
    }

    public static class SelectorList {

        private List<NutsRepositorySelector> all = new ArrayList<>();

        public SelectorList() {
        }

        public SelectorList(NutsRepositorySelector[] a) {
            for (NutsRepositorySelector repoDefString : a) {
                if (repoDefString != null) {
                    all.add(repoDefString);
                }
            }
        }

        public SelectorList join(SelectorList other) {
            if (other == null || other.all.isEmpty()) {
                return this;
            }
            List<NutsRepositorySelector> all2 = new ArrayList<>();
            all2.addAll(all);
            all2.addAll(other.all);
            return new SelectorList(all2.toArray(new NutsRepositorySelector[0]));
        }

        public Selection[] resolveSelectors(Map<String, String> existing) {
            Map<String, String> existing2 = new HashMap<>();
            if (existing != null) {
                for (Map.Entry<String, String> entry : existing.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (NutsBlankable.isBlank(v) && !NutsBlankable.isBlank(k)) {
                        String u2 = getRepositoryURLByName(k);
                        if (u2 != null) {
                            v = u2;
                        } else {
                            v = k;
                        }
                    } else if (!NutsBlankable.isBlank(v) && NutsBlankable.isBlank(k)) {
                        String u2 = getRepositoryNameByURL(k);
                        if (u2 != null) {
                            k = u2;
                        }
                    }
                    existing2.put(k, v);
                }
            }
            List<Selection> all2 = new ArrayList<>();
//            HashSet<String> visitedNames = new HashSet<>();
//            HashSet<String> visitedUrls = new HashSet<>();
            for (NutsRepositorySelector r : all) {
                if (r.op != Op.EXCLUDE) {
//                    boolean accept = true;
                    if (r.name != null) {
                        String u2 = r.getUrl();
//                        if (visitedNames.contains(r.name)) {
//                            accept = false;
//                        } else {
//                            visitedNames.add(r.name);
//                        }
                        if (existing2.containsKey(r.name)) {
                            String ss = existing2.remove(r.name);
                            if (ss != null && u2 == null) {
                                u2 = ss;
                            }
                        }
                        all2.add(new Selection(r.name, u2));
                    } else if (r.op == Op.EXACT) {
                        all2.add(new Selection(r.name, r.getUrl()));
                    }
                }
            }
            for (Map.Entry<String, String> e : existing2.entrySet()) {
                if (acceptExisting(e.getKey(), e.getValue())) {
                    all2.add(new Selection(e.getKey(), e.getValue()));
                }
            }
            return all2.toArray(new Selection[0]);
        }

        public boolean acceptExisting(String n, String url) {
            boolean includeOthers = true;
            for (NutsRepositorySelector s : all) {
                if (s.matches(n, url)) {
                    switch (s.op) {
                        case EXACT:
                            return true;
                        case INCLUDE:
                            return true;
                        case EXCLUDE:
                            return false;
                    }
                }
                if (s.op == Op.EXACT) {
                    includeOthers = false;
                }
            }
            return includeOthers;
        }
    }

    public static NutsAddRepositoryOptions createRepositoryOptions(String s, boolean requireName, NutsSession session) {
        NutsRepositorySelector.SelectorList r = NutsRepositorySelector.parseList(s);
        NutsRepositorySelector.Selection[] all = r.resolveSelectors(null);
        if (all.length != 1) {
            throw new IllegalArgumentException("unexpected");
        }
        return createRepositoryOptions(all[0], requireName, session);
    }

    public static NutsAddRepositoryOptions createRepositoryOptions(NutsRepositorySelector.Selection s, boolean requireName, NutsSession session) {
        NutsRepositoryURL nru = new NutsRepositoryURL(s.getUrl()).changeName(s.getName());
        String defaultName = null;
        if (defaultRepositoriesByName.containsKey(nru.getName())) {
            defaultName = nru.getName();
        } else {
            String nn = getRepositoryNameByURL(nru.getLocation());
            if (nn != null) {
                defaultName = nn;
            }
        }
        if (defaultName != null) {
            NutsAddRepositoryOptions u = createDefaultRepositoryOptions(defaultName, session);
            if (u != null
                    && (nru.getLocation().isEmpty()
                    || nru.getLocation().equals(u.getConfig().getLocation())
                    || nru.getURLString().equals(u.getConfig().getLocation()))) {
                //this is acceptable!
                if (!u.getName().equals(s.getName())) {
                    u.setName(s.getName());
                }
            }
            if (u != null) {
                return u;
            }
        }
        return createCustomRepositoryOptions(nru.getName(), nru.getURLString(), requireName, session);
    }

    public static NutsAddRepositoryOptions createCustomRepositoryOptions(String name, String url, boolean requireName, NutsSession session) {
        if ((name == null || name.isEmpty()) && requireName) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository name (<name>=<url>) for %s",name));
        }
        if (name == null || name.isEmpty()) {
            name = url;
            if (name.startsWith("http://")) {
                name = name.substring("http://".length());
            } else if (name.startsWith("https://")) {
                name = name.substring("https://".length());
            }

            name = name.replaceAll("[/\\\\:?.]", "-");
            while (name.endsWith("-")) {
                name = name.substring(0, name.length() - 1);
            }
            while (name.startsWith("-")) {
                name = name.substring(1);
            }
        }
        if (name.isEmpty() || url.isEmpty()) {
            throw new IllegalArgumentException("missing repository name (<name>=<url>) for " + name);
        }
//                boolean localRepo = CoreIOUtils.isPathFile(ppath);
        return new NutsAddRepositoryOptions().setName(name)
                .setFailSafe(false).setCreate(true)
                .setOrder(CoreIOUtils.isPathFile(url)
                        ? NutsAddRepositoryOptions.ORDER_USER_LOCAL
                        : NutsAddRepositoryOptions.ORDER_USER_REMOTE
                )
                .setConfig(new NutsRepositoryConfig()
                        .setLocation(url)
                );
    }

    public static NutsAddRepositoryOptions createDefaultRepositoryOptions(String nameOrURL, NutsSession session) {
        switch (nameOrURL) {
            case "local": {
                return new NutsAddRepositoryOptions()
                        .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setDeployWeight(10)
                        .setFailSafe(false)
                        .setCreate(true)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                                        .setType(NutsConstants.RepoTypes.NUTS)
                        );
            }
            case "system": {
                return new NutsAddRepositoryOptions()
                        .setDeployWeight(100)
                        .setName("system")
                        //                        .setLocation(
                        //                                CoreIOUtils.getNativePath(
                        //                                        Nuts.getPlatformHomeFolder(null,
                        //                                                NutsStoreLocation.CONFIG, null,
                        //                                                true,
                        //                                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                        //                                        + "/" + NutsConstants.Folders.REPOSITORIES
                        //                                        + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
                        //                                ))
                        .setFailSafe(true).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_SYSTEM_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(
                                                CoreIOUtils.getNativePath(
                                                        NutsUtilPlatforms.getPlatformHomeFolder(null,
                                                                NutsStoreLocation.CONFIG, session.config().stored().getHomeLocations(),
                                                                true,
                                                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                                                        + "/" + NutsConstants.Folders.REPOSITORIES
                                                        + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
                                                )
                                        )
                                        .setType(NutsConstants.RepoTypes.NUTS)
                        );
            }
            case ".m2":
            case "m2":
            case "maven-local": {
                return new NutsAddRepositoryOptions().setName("maven-local")
                        .setFailSafe(false).setCreate(true).setOrder(NutsAddRepositoryOptions.ORDER_USER_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(System.getProperty("user.home") + CoreIOUtils.syspath("/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN)
                        );
            }
            case "maven":
            case "central":
            case "maven-central": {
                return new NutsAddRepositoryOptions().setName("maven-central")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.maven.apache.org/maven2")
                                        .setType("maven")
                        );
            }
            case "jcenter": {
                return new NutsAddRepositoryOptions().setName("jcenter")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://jcenter.bintray.com")
                                        .setType("maven")
                        );
            }
            case "jboss": {
                return new NutsAddRepositoryOptions().setName("jboss")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repository.jboss.org/nexus/content/repositories/releases")
                                        .setType("maven")
                        );
            }
            case "clojars": {
                return new NutsAddRepositoryOptions().setName("clojars")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.clojars.org")
                                        .setType("maven")
                        );
            }
            case "atlassian": {
                return new NutsAddRepositoryOptions().setName("atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("htmlfs:https://packages.atlassian.com/maven/public")
                                        .setType("maven")
                        );
            }
            case "atlassian-snapshot": {
                return new NutsAddRepositoryOptions().setName("atlassian-atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://packages.atlassian.com/maven/public-snapshot")
                                        .setType("maven")
                        );
            }
            case "oracle": {
                return new NutsAddRepositoryOptions().setName("oracle")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://maven.oracle.com")
                                        .setType("maven")
                        );
            }
            case "google": {
                return new NutsAddRepositoryOptions().setName("google")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://maven.google.com")
                                        .setType("maven")
                        );
            }
            case "spring":
            case "spring-framework": {
                return new NutsAddRepositoryOptions().setName("spring")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.spring.io/release")
                                        .setType("maven")
                        );
            }
            case "maven-thevpc-git":
            case "vpc-public-maven": {
                return new NutsAddRepositoryOptions().setName("vpc-public-maven")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-maven/master")
                                        .setType("maven")
                        );
            }
            case "nuts-thevpc-git":
            case "vpc-public-nuts": {
                return new NutsAddRepositoryOptions().setName("vpc-public-nuts")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master")
                                        .setType("nuts")
                        );
            }
            case "dev":
            case "thevpc": {
                return new NutsAddRepositoryOptions().setName("thevpc")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("htmlfs:http://thevpc.net/maven")
                                        .setType("maven")
                        );
            }
        }
        return null;
    }

    private static String[] extractKeyEqValue(String s) {
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(s);
        if (matcher.find()) {
            return new String[]{matcher.group("name"), matcher.group("value")};
        } else {
            return null;
        }
    }
}
