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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
class PrivateNutsRepositorySelector {

    private boolean include;
    private boolean exclude;
    private boolean exact;
    private String name;
    private String url;

    public static SelectorList parse(String[] textes) {
        if (textes == null) {
            return new SelectorList();
        }
        SelectorList all = new SelectorList();
        for (String t : textes) {
            all = all.join(parse(t));
        }
        return all;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exact) {
            sb.append("=");
        } else if (exclude) {
            sb.append("-");
        }
        if (name != null && name.length() > 0) {
            sb.append(name);
            sb.append("=");
        }
        if (url != null && url.length() > 0) {
            sb.append(url);
        }
        return sb.toString();
    }

    public static PrivateNutsRepositorySelector parseOne(String text) {
        SelectorList r = PrivateNutsRepositorySelector.parse(text);
        PrivateNutsRepositorySelector[] i = r.resolveSelectors(null);
        if (i.length != 1) {
            throw new IllegalArgumentException("invalid repository " + text);
        }
        return i[0];
    }

    public static SelectorList parse(String text) {
        if (text == null) {
            return new SelectorList();
        }
        boolean include = false;
        boolean exclude = false;
        boolean exact = false;
        List<PrivateNutsRepositorySelector> all = new ArrayList<>();
        for (String s : text.split(",; ")) {
            String name = null;
            String url = null;
            if (s.startsWith("+")) {
                include = true;
                exclude = false;
                exact = false;
                s = s.substring(1);
            } else if (s.startsWith("-")) {
                include = false;
                exclude = true;
                exact = false;
                s = s.substring(1);
            } else if (s.startsWith("=")) {
                include = false;
                exclude = true;
                exact = false;
                s = s.substring(1);
            } else {
                include = true;
                exclude = false;
                exact = false;
            }
            Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(s);
            if (matcher.find()) {
                name = matcher.group("name");
                url = matcher.group("value");
            } else {
                url = s;
                if(s.matches("[a-zA-Z-_]+")){
                    name=s;
                }
            }
            if (url.length() > 0) {
                all.add(new PrivateNutsRepositorySelector(include, exclude, exact, name, url));
            }
        }
        return new SelectorList(all.toArray(new PrivateNutsRepositorySelector[0]));
    }

    public PrivateNutsRepositorySelector(boolean include, boolean exclude, boolean exact, String name, String url) {
        this.include = include;
        this.exclude = exclude;
        this.exact = exact;
        this.name = name;
        this.url = url;
    }

    public boolean isInclude() {
        return include;
    }

    public boolean isExclude() {
        return exclude;
    }

    public boolean isExact() {
        return exact;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
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

        public PrivateNutsRepositorySelector[] resolveSelectors(Map<String, String> existing) {
            Map<String, String> existing2 = new HashMap<>();
            if (existing != null) {
                for (Map.Entry<String, String> entry : existing.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (!PrivateNutsUtils.isBlank(k)) {
                        if (PrivateNutsUtils.isBlank(v)) {
                            v = k;
                        }
                    }
                    existing2.put(k, v);
                }
            }

            List<PrivateNutsRepositorySelector> all2 = new ArrayList<>();
            HashSet<String> visitedNames = new HashSet<>();
            HashSet<String> visitedUrls = new HashSet<>();
            for (PrivateNutsRepositorySelector r : all) {
                if (r.include || r.exact) {
                    boolean accept = true;
                    if (r.name != null) {
                        String u2 = r.getUrl();
                        if (visitedNames.contains(r.name)) {
                            accept = false;
                        } else {
                            visitedNames.add(r.name);
                        }
                        if (existing2.containsKey(r.name)) {
                            String ss = existing2.remove(r.name);
                            if (ss != null && u2 == null) {
                                u2 = ss;
                            }
                        }
                        r = new PrivateNutsRepositorySelector(true, false, false, r.name, u2);
                    } else if (r.exact) {
                        r = new PrivateNutsRepositorySelector(true, false, false, r.name, r.getUrl());
                    }
                    all2.add(r);
                }
            }
            for (Map.Entry<String, String> e : existing2.entrySet()) {
                if (acceptExisting(e.getKey(), e.getValue())) {
                    all2.add(new PrivateNutsRepositorySelector(true, false, false, e.getKey(), e.getValue()));
                }
            }
            return all2.toArray(new PrivateNutsRepositorySelector[0]);
        }

        public boolean acceptExisting(String n, String url) {
            boolean includeOthers = true;
            for (PrivateNutsRepositorySelector s : all) {
                if (s.name != null && s.name.equals(n)) {
                    if (s.include) {
                        return true;
                    }
                    if (s.exclude) {
                        return true;
                    }
                }
                if (s.url != null && s.url.equals(n)) {
                    if (s.include) {
                        return true;
                    }
                    if (s.exclude) {
                        return true;
                    }
                }
                if (s.exact) {
                    includeOthers = false;
                }
            }
            return includeOthers;
        }
    }

    public static String resolvePath(String path) {
        switch (path) {
            case ".m2":
            case "m2":
            case "maven-local": {
                return System.getProperty("user.home") + PrivateNutsUtils.syspath("/.m2/repository");
            }
            case "maven":
            case "central":
            case "maven-central": {
                return "https://repo.maven.apache.org/maven2";
            }
            case "jcenter": {
                return "https://jcenter.bintray.com";
            }
            case "jboss": {
                return "https://repository.jboss.org/nexus/content/repositories/releases";
            }
            case "clojars": {
                return "https://repo.clojars.org";
            }
            case "atlassian": {
                return "https://packages.atlassian.com/maven/public";
            }
            case "atlassian-snapshot": {
                return "https://packages.atlassian.com/maven/public-snapshot";
            }
            case "oracle": {
                return "https://maven.oracle.com";
            }
            case "google": {
                return "https://maven.google.com";
            }
            case "spring":
            case "spring-framework": {
                return "https://repo.spring.io/release";
            }
            case "maven-thevpc-git":
            case "vpc-public-maven": {
                return "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master";
            }
            case "nuts-thevpc-git":
            case "vpc-public-nuts": {
                return "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master";
            }
            case "thevpc": {
                return "http://thevpc.net";
            }
            case "tahabensalah": {
                return "http://tahabensalah.net";
            }
        }
        return path;
    }

}
