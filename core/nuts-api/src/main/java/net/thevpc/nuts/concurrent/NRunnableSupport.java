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
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.spi.NScorable;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.text.NMsg;

import java.util.function.Supplier;

public interface NRunnableSupport extends NScorable {
    static NRunnableSupport of(int score, Runnable supplier) {
        return of(score, supplier, null);
    }

    static NRunnableSupport of(int score, Runnable supplier, Supplier<NMsg> emptyMessage) {
        return (score <= 0 || supplier == null) ? ofInvalid(emptyMessage)
                : new DefaultNRunnableSupport(supplier, score, emptyMessage)
                ;
    }

    @SuppressWarnings("unchecked")
    static NRunnableSupport ofInvalid(Supplier<NMsg> emptyMessage) {
        return new DefaultNRunnableSupport(null, UNSUPPORTED_SCORE, emptyMessage);
    }

    static boolean isValid(NRunnableSupport s, NScorableContext scorableContext) {
        return s != null && s.isValid(scorableContext);
    }

    void run();

    default boolean isValid(NScorableContext scorableContext) {
        return getScore(scorableContext) > 0;
    }

    int getScore(NScorableContext scorableContext);
}
