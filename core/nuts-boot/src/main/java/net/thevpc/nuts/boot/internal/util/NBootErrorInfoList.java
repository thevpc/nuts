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
package net.thevpc.nuts.boot.internal.util;

import net.thevpc.nuts.boot.NBootId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NBootErrorInfoList {
    private final List<NReservedErrorInfo> all = new ArrayList<>();

    public void removeErrorsFor(NBootId nutsId) {
        all.removeIf(x -> x.getNutsId().equals(nutsId));
    }

    public void addFirst(NBootMsg[] messages, Throwable t) {
        NReservedErrorInfo[] ret = new NReservedErrorInfo[messages.length];
        for (int i = 0; i < ret.length; i++) {
            if (t != null && i == ret.length - 1) {
                ret[i] = NReservedErrorInfo.of(messages[i], t);
            } else {
                ret[i] = NReservedErrorInfo.of(messages[i]);
            }
        }
    }

    public void addFirst(NBootMsg e) {
        addFirst(NReservedErrorInfo.of(e));
    }

    public void addFirst(NBootMsg e, Throwable t) {
        addFirst(NReservedErrorInfo.of(e, t));
    }

    public void addFirst(NReservedErrorInfo e) {
        all.add(0, e);
    }

    public void addFirst(NReservedErrorInfo... e) {
        all.addAll(0, Arrays.asList(e));
    }

    public void insert(int pos, NReservedErrorInfo e) {
        all.add(pos, e);
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
