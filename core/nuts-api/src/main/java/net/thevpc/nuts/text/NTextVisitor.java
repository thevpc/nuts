/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.text;

import java.util.function.Consumer;

/**
 * @app.category Format
 */
public interface NTextVisitor {
    static NTextVisitor ofEnter(Consumer<NText> consumer) {
        return new NTextVisitor() {
            @Override
            public void enter(NText node) {
                consumer.accept(node);
            }

            @Override
            public void exit(NText node) {
                //
            }
        };
    }
    static NTextVisitor ofExit(Consumer<NText> consumer) {
        return new NTextVisitor() {
            @Override
            public void enter(NText node) {
                //
            }

            @Override
            public void exit(NText node) {
                consumer.accept(node);
            }
        };
    }

    /**
     * Visit.
     *
     * @param node node
     */
    void enter(NText node);

    default void exit(NText node) {
    }
}
