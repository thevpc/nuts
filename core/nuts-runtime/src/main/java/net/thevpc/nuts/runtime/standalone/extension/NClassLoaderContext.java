package net.thevpc.nuts.runtime.standalone.extension;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks the composite currently asking a leaf to load a class. A leaf is
 * shared by the registry and therefore cannot keep a workspace composite as
 * its parent, but it still needs to resolve classes from sibling leaves while
 * it is being used by one.
 */
final class NClassLoaderContext {
    private NClassLoaderContext() {
    }

    private static final ThreadLocal<Deque<NClassLoaderPeer>> ACTIVE =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Boolean> SIBLING_LOOKUP =
            ThreadLocal.withInitial(() -> false);

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
}
