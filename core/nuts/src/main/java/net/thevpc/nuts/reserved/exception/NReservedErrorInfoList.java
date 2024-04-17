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
package net.thevpc.nuts.reserved.exception;

import net.thevpc.nuts.NId;

import java.util.ArrayList;
import java.util.List;

public class NReservedErrorInfoList {
    private final List<NReservedErrorInfo> all = new ArrayList<>();

    public void removeErrorsFor(NId nutsId) {
        all.removeIf(x -> x.getNutsId().equals(nutsId));
    }

    public void insert(int pos, NReservedErrorInfo e) {
        all.add(pos,e);
    }
    public void add(NReservedErrorInfo e) {
        all.add(e);
    }

    public List<NReservedErrorInfo> list() {
        return all;
    }

    @Override
    public String toString() {
        StringBuilder errors = new StringBuilder();
        for (NReservedErrorInfo errorInfo : list()) {
            errors.append(errorInfo.toString()).append("\n");
        }
        return errors.toString();
    }
}
