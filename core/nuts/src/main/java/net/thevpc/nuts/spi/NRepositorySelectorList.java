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

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NRepositorySelectorList {

    private final List<NRepositorySelector> selectors = new ArrayList<>();

    public NRepositorySelectorList() {
    }

    public NRepositorySelectorList(NRepositorySelector[] a) {
        for (NRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                selectors.add(repoDefString);
            }
        }
    }

    public static NOptional<NRepositorySelectorList> of(List<String> expressions, NSession session) {
        return of(expressions, session == null ? NRepositoryDB.ofDefault() : NRepositoryDB.of(session), session);
    }

    public static NOptional<NRepositorySelectorList> of(List<String> expressions, NRepositoryDB db, NSession session) {
        if (expressions == null) {
            return NOptional.of(new NRepositorySelectorList());
        }
        NRepositorySelectorList result = new NRepositorySelectorList();
        for (String t : expressions) {
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    NOptional<NRepositorySelectorList> r = of(t, db, session);
                    if (r.isNotPresent()) {
                        String finalT = t;
                        return NOptional.ofError(x -> NMsg.ofC("invalid selector list : %s", finalT));
                    }
                    result = result.merge(r.get());
                }
            }
        }
        return NOptional.of(result);
    }

    public static NOptional<NRepositorySelectorList> of(String expression, NRepositoryDB db, NSession session) {
        if (NBlankable.isBlank(expression)) {
            return NOptional.of(new NRepositorySelectorList());
        }
        NSelectorOp op = NSelectorOp.INCLUDE;
        List<NRepositorySelector> all = new ArrayList<>();
        for (String s : NStringUtils.split(expression, ",;", true, true)) {
            s = s.trim();
            if (s.length() > 0) {
                NOptional<NRepositorySelector> oe = NRepositorySelector.of(op, s, db, session);
                if (oe.isNotPresent()) {
                    return NOptional.ofError(x -> NMsg.ofC("invalid selector list : %s", expression));
                }
                NRepositorySelector e = oe.get();
                op = e.getOp();
                all.add(e);
            }
        }
        return NOptional.of(new NRepositorySelectorList(all.toArray(new NRepositorySelector[0])));
    }

    public NRepositorySelectorList merge(NRepositorySelectorList other) {
        if (other == null || other.selectors.isEmpty()) {
            return this;
        }
        List<NRepositorySelector> result = new ArrayList<>();
        result.addAll(selectors);
        result.addAll(other.selectors);
        return new NRepositorySelectorList(result.toArray(new NRepositorySelector[0]));
    }

    public NRepositoryLocation[] resolve(NRepositoryLocation[] available, NRepositoryDB db) {
        NRepositoryURLList current = new NRepositoryURLList();
        if (available != null) {
            for (NRepositoryLocation entry : available) {
                String k = entry.getName();
                String v = entry.getFullLocation();
                if (NBlankable.isBlank(v) && !NBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryLocationByName(k);
                    if (u2 != null) {
                        v = u2;
                    } else {
                        v = k;
                    }
                } else if (!NBlankable.isBlank(v) && NBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryNameByLocation(k);
                    if (u2 != null) {
                        k = u2;
                    }
                }
                current.add(NRepositoryLocation.of(k, v));
            }
        }
        List<NRepositoryLocation> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        List<NRepositorySelector> selectorsExclude = new ArrayList<>();
        List<NRepositorySelector> selectorsInclude = new ArrayList<>();
        boolean exact = false;
        for (NRepositorySelector selector : selectors) {
            switch (selector.getOp()) {
                case EXACT: {
                    exact = true;
                    selectorsInclude.add(selector);
                    break;
                }
                case INCLUDE: {
                    selectorsInclude.add(selector);
                    break;
                }
                case EXCLUDE: {
                    selectorsExclude.add(selector);
                    break;
                }
            }
        }
        if (exact) {
            current.clear();
        }

        //now remove all excluded
        for (NRepositorySelector r : selectorsExclude) {
            Set<String> allNames = getAllNames(r,db);
            int i = current.indexOfNames(allNames.toArray(new String[0]), 0);
            if (i >= 0) {
                current.removeAt(i);
            }
        }
        //finally add included in the defined order


        for (NRepositorySelector r : selectorsInclude) {
            Set<String> allNames = getAllNames(r,db);
            if (!isVisitedFlag(allNames, visited)) {
                visited.addAll(allNames);
                result.add(NRepositoryLocation.of(r.getName(), r.getUrl()));
            }
        }
        for (NRepositoryLocation e : current.toArray()) {
            if (acceptExisting(e)) {
                Set<String> allNames = db.getAllNames(e.getName());
                if (!isVisitedFlag(allNames, visited)) {
                    visited.addAll(allNames);
                    result.add(e);
                }
            }
        }
        return result.toArray(new NRepositoryLocation[0]);
    }
    private Set<String> getAllNames(NRepositorySelector r,NRepositoryDB db){
        if (!NBlankable.isBlank(r.getName())) {
            return db.getAllNames(r.getName());
        }else{
            String name = db.getRepositoryNameByLocation(r.getUrl());
            return db.getAllNames(name);
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

    public boolean acceptExisting(NRepositoryLocation location) {
        boolean includeOthers = true;
        for (NRepositorySelector s : selectors) {
            if (s.matches(location)) {
                switch (s.getOp()) {
                    case EXACT:
                    case INCLUDE:
                        return true;
                    case EXCLUDE:
                        return false;
                }
            }
            if (s.getOp() == NSelectorOp.EXACT) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NRepositorySelector[] toArray() {
        return selectors.toArray(new NRepositorySelector[0]);
    }


    @Override
    public String toString() {
        return selectors.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","));
    }
}
