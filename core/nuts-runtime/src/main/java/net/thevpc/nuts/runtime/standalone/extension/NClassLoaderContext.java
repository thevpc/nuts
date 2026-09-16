/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Tracks the composite currently asking a leaf to load a class or resource. A leaf is
 * shared by the registry and therefore cannot keep a workspace composite as
 * its parent, but it still needs to resolve classes and resources from sibling leaves while
 * it is being used by one.
 */
final class NClassLoaderContext {
    private NClassLoaderContext() {
    }

    private static final ThreadLocal<Deque<NClassLoaderPeer>> ACTIVE =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Boolean> SIBLING_LOOKUP =
            ThreadLocal.withInitial(() -> false);

    /**
     * Snapshot of the active classloader context across threads.
     */
    public static final class Snapshot implements AutoCloseable {
        private final List<NClassLoaderPeer> peers;
        private final boolean siblingLookup;

        Snapshot(List<NClassLoaderPeer> peers, boolean siblingLookup) {
            this.peers = peers;
            this.siblingLookup = siblingLookup;
        }

        public void restore() {
            ACTIVE.set(new ArrayDeque<>(peers));
            SIBLING_LOOKUP.set(siblingLookup);
        }

        @Override
        public void close() {
            ACTIVE.remove();
            SIBLING_LOOKUP.remove();
        }
    }

    static Snapshot snapshot() {
        Deque<NClassLoaderPeer> stack = ACTIVE.get();
        return new Snapshot(new ArrayList<>(stack), SIBLING_LOOKUP.get());
    }

    static Snapshot restore(Snapshot snapshot) {
        Snapshot prev = snapshot();
        if (snapshot != null) {
            snapshot.restore();
        } else {
            ACTIVE.remove();
            SIBLING_LOOKUP.remove();
        }
        return prev;
    }

    static void enter(NClassLoaderPeer peer) {
        ACTIVE.get().push(peer);
    }

    static void exit(NClassLoaderPeer peer) {
        Deque<NClassLoaderPeer> stack = ACTIVE.get();
        if (!stack.isEmpty() && stack.peek() == peer) {
            stack.pop();
        } else {
            stack.removeFirstOccurrence(peer);
        }
        if (stack.isEmpty()) {
            ACTIVE.remove();
        }
    }

    static NClassLoaderPeer current() {
        Deque<NClassLoaderPeer> stack = ACTIVE.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    static boolean isSiblingLookup() {
        return SIBLING_LOOKUP.get();
    }

    static void beginSiblingLookup() {
        SIBLING_LOOKUP.set(true);
    }

    static void endSiblingLookup() {
        SIBLING_LOOKUP.remove();
    }
}

interface NClassLoaderPeer {
    Class<?> loadClassFromChildren(ClassLoader requester, String name)
            throws ClassNotFoundException;

    List<URL> loadResourcesFromChildren(ClassLoader requester, String name)
            throws IOException;

    default boolean isShortNameVersionAllowed(NId candidateId) {
        return true;
    }
}
