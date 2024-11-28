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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.NReservedBootRepositoryDB;
import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NRepositorySelectorListBoot {

    private final List<NRepositorySelectorBoot> selectors = new ArrayList<>();

    public NRepositorySelectorListBoot() {
    }

    public NRepositorySelectorListBoot(NRepositorySelectorBoot[] a) {
        for (NRepositorySelectorBoot repoDefString : a) {
            if (repoDefString != null) {
                selectors.add(repoDefString);
            }
        }
    }

    public static NRepositorySelectorListBoot of(List<String> expressions) {
        return of(expressions, NReservedBootRepositoryDB.of());
    }

    public static NRepositorySelectorListBoot of(List<String> expressions, NReservedBootRepositoryDB db) {
        if (expressions == null) {
            return new NRepositorySelectorListBoot();
        }
        NRepositorySelectorListBoot result = new NRepositorySelectorListBoot();
        for (String t : expressions) {
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    NRepositorySelectorListBoot r = of(t, db);
                    if (r==null) {
                        return null;
                    }
                    result = result.merge(r);
                }
            }
        }
        return result;
    }

    public static NRepositorySelectorListBoot of(String expression, NReservedBootRepositoryDB db) {
        if (NStringUtilsBoot.isBlank(expression)) {
            return new NRepositorySelectorListBoot();
        }
        String op = "INCLUDE";
        List<NRepositorySelectorBoot> all = new ArrayList<>();
        for (String s : NStringUtilsBoot.split(expression, ",;", true, true)) {
            s = s.trim();
            if (s.length() > 0) {
                NRepositorySelectorBoot oe = NRepositorySelectorBoot.of(op, s, db);
                if (oe==null) {
                    return null;
//                    return NOptional.ofError(() -> NMsgBoot.ofC("invalid selector list : %s", expression));
                }
                NRepositorySelectorBoot e = oe;
                op = e.getOp();
                all.add(e);
            }
        }
        return new NRepositorySelectorListBoot(all.toArray(new NRepositorySelectorBoot[0]));
    }

    public NRepositorySelectorListBoot merge(NRepositorySelectorListBoot other) {
        if (other == null || other.selectors.isEmpty()) {
            return this;
        }
        List<NRepositorySelectorBoot> result = new ArrayList<>();
        result.addAll(selectors);
        result.addAll(other.selectors);
        return new NRepositorySelectorListBoot(result.toArray(new NRepositorySelectorBoot[0]));
    }

    public NRepositoryLocationBoot[] resolve(NRepositoryLocationBoot[] available, NReservedBootRepositoryDB db) {
        NRepositoryLocationListBoot current = new NRepositoryLocationListBoot();
        if (available != null) {
            for (NRepositoryLocationBoot entry : available) {
                if(entry!=null) {
                    String k = entry.getName();
                    String v = entry.getFullLocation();
                    if (NStringUtilsBoot.isBlank(v) && !NStringUtilsBoot.isBlank(k)) {
                        NAddRepositoryOptionsBoot ro = db.getRepositoryOptionsByName(k);
                        String u = ro==null?null:ro.getConfig().getLocation().getFullLocation();
                        if (u != null) {
                            v = u;
                        } else {
                            v = k;
                        }
                    } else if (!NStringUtilsBoot.isBlank(v) && NStringUtilsBoot.isBlank(k)) {
                        NAddRepositoryOptionsBoot ro = db.getRepositoryOptionsByLocation(k);
                        String n = ro==null?null:ro.getName();
                        if (n != null) {
                            k = n;
                        }
                    }
                    current.add(NRepositoryLocationBoot.of(k, v));
                }
            }
        }
        List<NRepositoryLocationBoot> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        List<NRepositorySelectorBoot> selectorsExclude = new ArrayList<>();
        List<NRepositorySelectorBoot> selectorsInclude = new ArrayList<>();
        boolean exact = false;
        for (NRepositorySelectorBoot selector : selectors) {
            switch (selector.getOp()) {
                case "EXACT": {
                    exact = true;
                    selectorsInclude.add(selector);
                    break;
                }
                case "INCLUDE": {
                    selectorsInclude.add(selector);
                    break;
                }
                case "EXCLUDE": {
                    selectorsExclude.add(selector);
                    break;
                }
            }
        }
        if (exact) {
            current.clear();
        }

        //now remove all excluded
        for (NRepositorySelectorBoot r : selectorsExclude) {
            Set<String> allNames = getAllNames(r,db);
            int i = current.indexOfNames(allNames.toArray(new String[0]), 0);
            if (i >= 0) {
                current.removeAt(i);
            }
        }
        //finally add included in the defined order


        for (NRepositorySelectorBoot r : selectorsInclude) {
            Set<String> allNames = getAllNames(r,db);
            if (!isVisitedFlag(allNames, visited)) {
                visited.addAll(allNames);
                result.add(NRepositoryLocationBoot.of(r.getName(), r.getUrl()));
            }
        }
        for (NRepositoryLocationBoot e : current.toArray()) {
            if (acceptExisting(e)) {
                Set<String> allNames = db.findAllNamesByName(e.getName());
                if (!isVisitedFlag(allNames, visited)) {
                    visited.addAll(allNames);
                    result.add(e);
                }
            }
        }
        return result.toArray(new NRepositoryLocationBoot[0]);
    }
    private Set<String> getAllNames(NRepositorySelectorBoot r,NReservedBootRepositoryDB db){
        if (!NStringUtilsBoot.isBlank(r.getName())) {
            return db.findAllNamesByName(r.getName());
        }else{
            NAddRepositoryOptionsBoot lo = db.getRepositoryOptionsByLocation(r.getUrl());
            String name = lo==null?null:lo.getName();
            return db.findAllNamesByName(name);
        }
    }
    private boolean isVisitedFlag(Set<String> allNames, Set<String> visited) {
        boolean visitedFlag = false;
        for (String allName : allNames) {
            if (visited.contains(allName)) {
                visitedFlag = true;
                break;
            }
        }
        return visitedFlag;
    }

    public boolean acceptExisting(NRepositoryLocationBoot location) {
        boolean includeOthers = true;
        for (NRepositorySelectorBoot s : selectors) {
            if (s.matches(location)) {
                switch (s.getOp()) {
                    case "EXACT":
                    case "INCLUDE":
                        return true;
                    case "EXCLUDE":
                        return false;
                }
            }
            if (s.getOp().equals("EXACT")) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NRepositorySelectorBoot[] toArray() {
        return selectors.toArray(new NRepositorySelectorBoot[0]);
    }


    @Override
    public String toString() {
        return selectors.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","));
    }
}
