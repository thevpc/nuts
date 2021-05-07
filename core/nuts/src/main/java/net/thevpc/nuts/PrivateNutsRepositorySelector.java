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
package net.thevpc.nuts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
class PrivateNutsRepositorySelector {

    private static final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();

    static {
        defaultRepositoriesByName.put("system", PrivateNutsUtils.syspath(
                Nuts.getPlatformHomeFolder(null,
                        NutsStoreLocation.CONFIG, null,
                        true,
                        NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                + "/" + NutsConstants.Folders.REPOSITORIES
                + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
        ));
        //
        defaultRepositoriesByName.put("maven-local", System.getProperty("user.home") + PrivateNutsUtils.syspath("/.m2/repository"));
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

    }

    public static String getRepositoryNameByURL(String url) {
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            if (entry.getValue().equals(url)) {
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
                || s.indexOf(",") >= 0
                || s.indexOf(";") >= 0) {
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
        List<PrivateNutsRepositorySelector> all = new ArrayList<>();
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
                    all.add(new PrivateNutsRepositorySelector(op, z.getName(), z.getUrl()));
                }
            }
        }
        return new SelectorList(all.toArray(new PrivateNutsRepositorySelector[0]));
    }

    public PrivateNutsRepositorySelector(Op op, String name, String url) {
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

    }

    public static class SelectorList {

        private List<PrivateNutsRepositorySelector> all = new ArrayList<>();

        public SelectorList() {
        }

        public SelectorList(PrivateNutsRepositorySelector[] a) {
            for (PrivateNutsRepositorySelector repoDefString : a) {
                if (repoDefString != null) {
                    all.add(repoDefString);
                }
            }
        }

        public SelectorList join(SelectorList other) {
            if (other == null || other.all.isEmpty()) {
                return this;
            }
            List<PrivateNutsRepositorySelector> all2 = new ArrayList<>();
            all2.addAll(all);
            all2.addAll(other.all);
            return new SelectorList(all2.toArray(new PrivateNutsRepositorySelector[0]));
        }

        public Selection[] resolveSelectors(Map<String, String> existing) {
            Map<String, String> existing2 = new HashMap<>();
            if (existing != null) {
                for (Map.Entry<String, String> entry : existing.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (isBlank(v) && !isBlank(k)) {
                        String u2 = getRepositoryURLByName(k);
                        if (u2 != null) {
                            v = u2;
                        }else{
                            v = k;
                        }
                    } else if (!isBlank(v) && isBlank(k)) {
                        String u2 = getRepositoryNameByURL(k);
                        if (u2 != null) {
                            k=u2;
                        }
                    }
                    existing2.put(k, v);
                }
            }
            List<Selection> all2 = new ArrayList<>();
//            HashSet<String> visitedNames = new HashSet<>();
//            HashSet<String> visitedUrls = new HashSet<>();
            for (PrivateNutsRepositorySelector r : all) {
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
            for (PrivateNutsRepositorySelector s : all) {
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
    private static boolean isBlank(String s){
        return PrivateNutsUtils.isBlank(s);
    }
}
