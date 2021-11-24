/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsSession;

import java.util.ArrayList;
import java.util.List;

public class NutsRepositorySelectorList {

    private final List<NutsRepositorySelector> all = new ArrayList<>();

    public NutsRepositorySelectorList() {
    }

    public NutsRepositorySelectorList(NutsRepositorySelector[] a) {
        for (NutsRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                all.add(repoDefString);
            }
        }
    }
    public static NutsRepositorySelectorList ofAll(String[] expressions, NutsRepositoryDB db, NutsSession session) {
        if (expressions == null) {
            return new NutsRepositorySelectorList();
        }
        NutsRepositorySelectorList all = new NutsRepositorySelectorList();
        for (String t : expressions) {
            all = all.join(of(t,db,session));
        }
        return all;
    }

    public static NutsRepositorySelectorList of(String expression, NutsRepositoryDB db, NutsSession session) {

        if (expression == null || NutsBlankable.isBlank(expression)) {
            return new NutsRepositorySelectorList();
        }
        NutsSelectorOp op = NutsSelectorOp.INCLUDE;
        List<NutsRepositorySelector> all = new ArrayList<>();
        for (String s : expression.split("[,;]")) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("+")) {
                    op = NutsSelectorOp.INCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("-")) {
                    op = NutsSelectorOp.EXCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("=")) {
                    op = NutsSelectorOp.EXACT;
                    s = s.substring(1);
                }
                NutsRepositoryURL z = NutsRepositoryURL.of(s,db,session);
                if (z != null) {
                    all.add(new NutsRepositorySelector(op, z.getName(), z.getLocation()));
                }
            }
        }
        return new NutsRepositorySelectorList(all.toArray(new NutsRepositorySelector[0]));
    }

    public NutsRepositorySelectorList join(NutsRepositorySelectorList other) {
        if (other == null || other.all.isEmpty()) {
            return this;
        }
        List<NutsRepositorySelector> all2 = new ArrayList<>();
        all2.addAll(all);
        all2.addAll(other.all);
        return new NutsRepositorySelectorList(all2.toArray(new NutsRepositorySelector[0]));
    }

    public NutsRepositoryURL[] resolveSelectors(NutsRepositoryURL[] existing, NutsRepositoryDB db) {
        NutsRepositoryURLList existing2 = new NutsRepositoryURLList();
        if (existing != null) {
            for (NutsRepositoryURL entry : existing) {
                String k = entry.getName();
                String v = entry.getLocation();
                if (NutsBlankable.isBlank(v) && !NutsBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryURLByName(k);
                    if (u2 != null) {
                        v = u2;
                    } else {
                        v = k;
                    }
                } else if (!NutsBlankable.isBlank(v) && NutsBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryNameByURL(k);
                    if (u2 != null) {
                        k = u2;
                    }
                }
                existing2.add(NutsRepositoryURL.of(k, v));
            }
        }
        List<NutsRepositoryURL> all2 = new ArrayList<>();
        for (NutsRepositorySelector r : all) {
            if (r.getOp() != NutsSelectorOp.EXCLUDE) {
//                    boolean accept = true;
                if (!NutsBlankable.isBlank(r.getName())) {
                    String u2 = r.getUrl();
                    int i = existing2.indexOfName(r.getName(), 0);
                    if (i >= 0) {
                        NutsRepositoryURL ss = existing2.removeAt(i);
                        if (ss != null && u2 == null) {
                            u2 = ss.getLocation();
                        }
                    }
                    all2.add(NutsRepositoryURL.of(r.getName(), u2));
                } else if (r.getOp() == NutsSelectorOp.EXACT) {
                    all2.add(NutsRepositoryURL.of(r.getName(), r.getUrl()));
                }
            }
        }
        for (NutsRepositoryURL e : existing2.toArray()) {
            if (acceptExisting(e)) {
                all2.add(e);
            }
        }
        return all2.toArray(new NutsRepositoryURL[0]);
    }

    public boolean acceptExisting(NutsRepositoryURL ss) {
        String n = ss.getName();
        String url = ss.getLocation();
        boolean includeOthers = true;
        for (NutsRepositorySelector s : all) {
            if (s.matches(n, url)) {
                switch (s.getOp()) {
                    case EXACT:
                        return true;
                    case INCLUDE:
                        return true;
                    case EXCLUDE:
                        return false;
                }
            }
            if (s.getOp() == NutsSelectorOp.EXACT) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NutsRepositorySelector[] toArray() {
        return all.toArray(new NutsRepositorySelector[0]);
    }


}
