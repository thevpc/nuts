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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vpc
 */
class PrivateNutsRepositorySelector {

    private static final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();

    static {
        defaultRepositoriesByName.put("system", PrivateNutsUtilIO.syspath(
                NutsUtilPlatforms.getPlatformHomeFolder(null,
                        NutsStoreLocation.CONFIG, null,
                        true,
                        NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                        + "/" + NutsConstants.Folders.REPOSITORIES
                        + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
        ));
        //
        defaultRepositoriesByName.put("maven-local", System.getProperty("user.home") + PrivateNutsUtilIO.syspath("/.m2/repository"));
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

    private final String name;
    private final String url;
    private PrivateNutsRepositorySelectorOp op = PrivateNutsRepositorySelectorOp.INCLUDE;

    public PrivateNutsRepositorySelector(PrivateNutsRepositorySelectorOp op, String name, String url) {
        this.op = op;
        this.name = name;
        this.url = url;
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

    public static PrivateNutsRepositorySelectorList parse(String[] textes) {
        if (textes == null) {
            return new PrivateNutsRepositorySelectorList();
        }
        PrivateNutsRepositorySelectorList all = new PrivateNutsRepositorySelectorList();
        for (String t : textes) {
            all = all.join(parseList(t));
        }
        return all;
    }

    public static PrivateNutsRepositorySelection parseSelection(String s) {
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
            return new PrivateNutsRepositorySelection(name, url);
        }
        return null;
    }

    public static PrivateNutsRepositorySelectorList parseList(String text) {
        if (text == null || NutsUtilStrings.isBlank(text)) {
            return new PrivateNutsRepositorySelectorList();
        }
        PrivateNutsRepositorySelectorOp op = PrivateNutsRepositorySelectorOp.INCLUDE;
        List<PrivateNutsRepositorySelector> all = new ArrayList<>();
        for (String s : text.split("[,;]")) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("+")) {
                    op = PrivateNutsRepositorySelectorOp.INCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("-")) {
                    op = PrivateNutsRepositorySelectorOp.EXCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("=")) {
                    op = PrivateNutsRepositorySelectorOp.EXACT;
                    s = s.substring(1);
                }
                PrivateNutsRepositorySelection z = parseSelection(s);
                if (z != null) {
                    all.add(new PrivateNutsRepositorySelector(op, z.getName(), z.getUrl()));
                }
            }
        }
        return new PrivateNutsRepositorySelectorList(all.toArray(new PrivateNutsRepositorySelector[0]));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (op == PrivateNutsRepositorySelectorOp.EXACT) {
            sb.append(":=");
        } else if (op == PrivateNutsRepositorySelectorOp.EXCLUDE) {
            sb.append(":-");
        } else if (op == PrivateNutsRepositorySelectorOp.INCLUDE) {
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

    public PrivateNutsRepositorySelectorOp getOp() {
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
        return _url.length() > 0 && _url.equals(otherURL);
    }

    public static PrivateNutsRepositorySelection validateSelection(PrivateNutsRepositorySelection s) {
        if(s==null){
            return null;
        }
        String n=NutsUtilStrings.trim(s.getName());
        String u=NutsUtilStrings.trim(s.getUrl());
        if(n.isEmpty() && u.isEmpty()){
            return null;
        }
        if(n.isEmpty()){
            //url only
            n = getRepositoryNameByURL(u);
            if(n!=null){
                return new PrivateNutsRepositorySelection(n,u);
            }
            String u2 = getRepositoryURLByName(u);
            if(u2!=null){
                return new PrivateNutsRepositorySelection(u,u2);
            }
            return new PrivateNutsRepositorySelection("",u);
        }else if(u.isEmpty()){
            //url only
            u = getRepositoryURLByName(n);
            if(u!=null){
                return new PrivateNutsRepositorySelection(n,u);
            }
            String n2 = getRepositoryNameByURL(n);
            if(n2!=null){
                return new PrivateNutsRepositorySelection(n2,n);
            }
            return new PrivateNutsRepositorySelection(n,n);
        }else{
            return new PrivateNutsRepositorySelection(n,u);
        }
    }

}
